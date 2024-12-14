package org.example.kurs.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.kurs.model.*;
import org.example.kurs.repository.*;
import org.example.kurs.service.LicenseHistoryService;
import org.example.kurs.configuration.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@RestController
@RequestMapping("/licensing") // Устанавливаем маршрут для функциональности лицензирования
@RequiredArgsConstructor // Автоматически генерируется конструктор с зависимостями
public class LicensingControllerCreate {

    private final JwtTokenProvider jwtTokenProvider; // Провайдер для работы с JWT токенами
    private final ProductRepository productRepository; // Репозиторий для работы с продуктами
    private final ApplicationUserRepository applicationUserRepository; // Репозиторий для пользователей
    private final LicenseTypeRepository licenseTypeRepository; // Репозиторий для типов лицензий
    private final LicenseRepository licenseRepository; // Репозиторий для работы с лицензиями
    private final LicenseHistoryService licenseHistoryService; // Сервис для записи истории изменений лицензий

    // Метод для преобразования LocalDate в Date
    private Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @PostMapping("/create")
    public ResponseEntity<?> createLicense(HttpServletRequest request, @RequestBody LicenseCreateRequest requestData) {
        Logger logger = LoggerFactory.getLogger(getClass());

        try {
            // Извлекаем роли из токена, используя resolveToken и getRolesFromToken
            logger.info("Extracting role from token...");
            Set<String> roles = jwtTokenProvider.getRolesFromRequest(request);  // Используем новый метод
            logger.info("Role extracted from token: {}", roles);

            // Проверяем, что роль пользователя - ADMIN
            if (!roles.contains("ROLE_ADMIN")) {
                // Логируем предупреждение о попытке создания лицензии без прав
                logger.warn("Attempt to create a license without ADMIN rights. Role: {}", roles);

                // Возвращаем ответ с кодом FORBIDDEN и сообщением
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("No rights to create a license");
            }

            // Проверка существования продукта по ID
            logger.info("Checking if product exists with ID: {}", requestData.getProductId());
            Product product = productRepository.findById(requestData.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            logger.info("Product found: {}", product.getName());

            // Проверка, заблокирован ли продукт
            if (product.isBlocked()) {  // Используем поле isBlocked для проверки
                logger.warn("Product with ID: {} is blocked", requestData.getProductId());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Product is blocked, license cannot be created");
            }

            // Проверка существования пользователя по ID (владельца)
            logger.info("Checking if user exists with ID: {}", requestData.getOwnerId());
            ApplicationUser owner = applicationUserRepository.findById(requestData.getOwnerId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            logger.info("User found: {}", owner.getUsername());

            // Проверка существования типа лицензии по ID
            logger.info("Checking if license type exists with ID: {}", requestData.getLicenseTypeId());
            LicenseType licenseType = licenseTypeRepository.findById(requestData.getLicenseTypeId())
                    .orElseThrow(() -> new IllegalArgumentException("License type not found"));
            logger.info("License type found: {}", licenseType.getName());

            // Создание новой лицензии
            logger.info("Creating new license...");
            License newLicense = new License();
            newLicense.setCode(generateActivationCode());  // Генерация активационного кода
            logger.info("Activation code generated: {}", newLicense.getCode());

            // Устанавливаем пользователя (user_id)
            newLicense.setOwner(owner);  // Устанавливаем владельца лицензии (owner_id)newLicense.setProduct(product);  // Устанавливаем продукт (product_id)
            newLicense.setLicenseType(licenseType);  // Устанавливаем тип лицензии (type_id)

            // Преобразуем LocalDate в Date для firstActivationDate
            newLicense.setFirstActivationDate(convertLocalDateToDate(LocalDate.now()));

            // Проверка параметров и расчет даты окончания
            int duration = licenseType.getDefaultDuration(); // Срок действия по умолчанию для типа лицензии
            LocalDate endingLocalDate = LocalDate.now().plusDays(duration);
            newLicense.setEndingDate(convertLocalDateToDate(endingLocalDate));

            // Устанавливаем статус блокировки
            newLicense.setBlocked(false);  // По умолчанию лицензия не заблокирована

            // Устанавливаем количество устройств
            newLicense.setDeviceCount(requestData.getDeviceCount());
            newLicense.setDuration(duration);
            newLicense.setDescription(requestData.getDescription() != null ? requestData.getDescription() : "Enjoy using our products!");

            // Сохраняем лицензию в базе данных
            logger.info("Saving license to the database...");
            licenseRepository.save(newLicense);
            logger.info("License successfully saved to the database with ID: {}", newLicense.getId());

            // Запись в историю
            String description = "License created";
            Date changeDate = convertLocalDateToDate(LocalDate.now());
            licenseHistoryService.recordLicenseChange(newLicense.getId(), owner.getId(), "Created", changeDate, description);
            logger.info("License change recorded in history");

            return ResponseEntity.status(HttpStatus.CREATED).body("License successfully created");

        } catch (IllegalArgumentException e) {
            logger.error("Error while creating the license: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("An error occurred while creating the license: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while creating the license");
        }
    }

    // Метод для генерации активационного кода (можно улучшить по необходимости)
    private String generateActivationCode() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}