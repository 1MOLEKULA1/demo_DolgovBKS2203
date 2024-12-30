package org.example.Dolgov.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.Dolgov.entity.*;
import org.example.Dolgov.storage.*;
import org.example.Dolgov.services.impl.LicenseHistoryService;
import org.example.Dolgov.JWTconfiguration.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//TODO: 1. createLicense - проверку прав доступа сделать при помощи security, а не использовать кривую проверку Данилина (Александр)
//TODO: 2. assembleLicense - даты первой активации и окончания должны устанавливаться только при первой активации лицензии (Александр)

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

    private static final Logger logger = LoggerFactory.getLogger(LicensingControllerCreate.class);

    // Константы для сообщений об ошибках
    private static final String ERROR_AUTHORIZATION = "No rights to create a license";
    private static final String ERROR_PRODUCT_BLOCKED = "Product is blocked, license cannot be created";
    private static final String ERROR_PRODUCT_NOT_FOUND = "Product not found";
    private static final String ERROR_USER_NOT_FOUND = "User not found";
    private static final String ERROR_LICENSE_TYPE_NOT_FOUND = "License type not found";

    // Метод для преобразования LocalDate в Date
    private Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // Метод для возврата стандартного ответа с ошибкой
    private ResponseEntity<String> createErrorResponse(HttpStatus status, String message) {
        logger.error(message);
        return ResponseEntity.status(status).body(message);
    }
//TODO Переделать проверку на аккаунт админа для создания лицензии
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> createLicense(HttpServletRequest request, @RequestBody LicenseCreate requestData) {
        try {
            // Извлекаем роли из токена
            logger.info("Extracting role from token...");
            Set<String> roles = jwtTokenProvider.getRolesFromRequest(request);  // Используем новый метод
            logger.info("Role extracted from token: {}", roles);


            // Проверка существования продукта
            logger.info("Checking if product exists with ID: {}", requestData.getProductId());
            Product product = productRepository.findById(requestData.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException(ERROR_PRODUCT_NOT_FOUND));
            logger.info("Product found: {}", product.getName());

            // Проверка, заблокирован ли продукт
            if (product.isBlocked()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, ERROR_PRODUCT_BLOCKED);
            }

            // Проверка существования пользователя
            logger.info("Checking if user exists with ID: {}", requestData.getOwnerId());
            ApplicationUser owner = applicationUserRepository.findById(requestData.getOwnerId())
                    .orElseThrow(() -> new IllegalArgumentException(ERROR_USER_NOT_FOUND));
            logger.info("User found: {}", owner.getUsername());

            // Проверка существования типа лицензии
            logger.info("Checking if license type exists with ID: {}", requestData.getLicenseTypeId());
            LicenseType licenseType = licenseTypeRepository.findById(requestData.getLicenseTypeId())
                    .orElseThrow(() -> new IllegalArgumentException(ERROR_LICENSE_TYPE_NOT_FOUND));
            logger.info("License type found: {}", licenseType.getName());

            // Создание новой лицензии
            License newLicense = createNewLicense(product, owner, licenseType, requestData);
            logger.info("Saving license to the database...");
            licenseRepository.save(newLicense);
            logger.info("License successfully saved to the database with ID: {}", newLicense.getId());

            // Запись в историю
            licenseHistoryService.recordLicenseChange(newLicense, owner, "Created", new Date(), "License created");
            logger.info("License change recorded in history");

            return ResponseEntity.status(HttpStatus.CREATED).body("License successfully created");

        } catch (IllegalArgumentException e) {
            logger.error("Error while creating the license: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            logger.error("An error occurred while creating the license: {}", e.getMessage(), e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while creating the license");
        }
    }

    // Метод для создания новой лицензии
    private License createNewLicense(Product product, ApplicationUser owner, LicenseType licenseType, LicenseCreate requestData) {
        License newLicense = new License();
        newLicense.setCode(generateActivationCode());
        newLicense.setOwner(owner);
        newLicense.setProduct(product);
        newLicense.setLicenseType(licenseType);
        newLicense.setBlocked(false);
        newLicense.setDeviceCount(requestData.getDeviceCount());
        newLicense.setDuration(licenseType.getDefaultDuration());
        newLicense.setDescription(requestData.getDescription() != null ? requestData.getDescription() : "Enjoy using our products!");
        return newLicense;
    }

    // Метод для генерации активационного кода
    private String generateActivationCode() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }
}
