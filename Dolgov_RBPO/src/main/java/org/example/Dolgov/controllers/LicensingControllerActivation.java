package org.example.Dolgov.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.Dolgov.entity.*;
import org.example.Dolgov.storage.*;
import org.example.Dolgov.services.impl.LicenseHistoryService;
import org.example.Dolgov.JWTconfiguration.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/licensing") // Базовый маршрут для работы с лицензиями
@RequiredArgsConstructor // Автоматическая генерация конструктора с обязательными зависимостями
public class LicensingControllerActivation {

    private final JwtTokenProvider jwtTokenProvider; // Провайдер JWT токенов
    private final ApplicationUserRepository applicationUserRepository; // Работа с данными пользователей
    private final LicenseRepository licenseRepository; // Работа с лицензиями
    private final DeviceLicenseRepository deviceLicenseRepository; // Привязка устройств и лицензий
    private final LicenseHistoryService licenseHistoryService; // История изменений лицензий
    private final DeviceRepository deviceRepository; // Работа с устройствами

    private static final Logger logger = LoggerFactory.getLogger(LicensingControllerActivation.class); // Логгер

    // Константы для сообщений
    public static final String ERROR_LICENSE_NOT_FOUND = "Лицензия не найдена";
    public static final String ERROR_DEVICE_EXISTS = "Устройство уже существует";
    public static final String ERROR_LICENSE_ALREADY_ACTIVE = "Лицензия уже активирована на этом устройстве";
    public static final String ERROR_NO_AVAILABLE_SEATS = "Нет доступных мест для активации";
    public static final String ERROR_AUTHENTICATION = "Ошибка аутентификации";

    // Конвертация LocalDate в Date
    private Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // Вспомогательный метод для отправки ошибок
    private ResponseEntity<String> createErrorResponse(HttpStatus status, String message) {
        logger.error(message);
        return ResponseEntity.status(status).body(message);
    }

    // Вспомогательный метод для регистрации устройства
    private Device registerDevice(String macAddress, String deviceName, Long userId) {
        Device newDevice = new Device();
        newDevice.setMacAddress(macAddress);
        newDevice.setName(deviceName);
        newDevice.setUserId(userId);
        return deviceRepository.save(newDevice);
    }

    @PostMapping("/activation")
    public ResponseEntity<?> activateLicense(HttpServletRequest request, @RequestBody LicenseActivation activationRequest) {
        try {
            // Получение ролей из JWT токена
            Set<String> roles = jwtTokenProvider.getRolesFromRequest(request);
            logger.info("Из токена извлечены роли: {}", roles);

            if (roles.isEmpty()) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, ERROR_AUTHENTICATION);
            }

            // Проверяем существование лицензии по коду
            Optional<License> licenseOptional = licenseRepository.findByCode(activationRequest.getCode());
            if (licenseOptional.isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, ERROR_LICENSE_NOT_FOUND);
            }
            License license = licenseOptional.get();
            logger.info("Лицензия с кодом {} найдена", activationRequest.getCode());

            // Получаем email пользователя из токена и ищем пользователя
            String email = jwtTokenProvider.getEmailFromRequest(request);
            Optional<ApplicationUser> userOptional = applicationUserRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Пользователь не найден");
            }
            ApplicationUser user = userOptional.get();

            // Привязка лицензии к пользователю
            if (license.getUser() == null) {
                license.setUser(user);
            } else if (!license.getUser().getEmail().equals(email)) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка: пользователь не совпадает");
            }

            // Проверяем, существует ли устройство с указанным MAC-адресом и именем
            Optional<Device> existingDeviceOptional = deviceRepository.findByMacAddressAndName(activationRequest.getMacAddress(), activationRequest.getDeviceName());
            if (existingDeviceOptional.isPresent()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, ERROR_DEVICE_EXISTS);
            }

            // Регистрируем новое устройство или используем существующее
            Device device = existingDeviceOptional.orElseGet(() -> registerDevice(activationRequest.getMacAddress(), activationRequest.getDeviceName(), user.getId()));

            // Проверка доступных мест для активации
            if (license.getDeviceCount() <= 0) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, ERROR_NO_AVAILABLE_SEATS);
            }

            // Проверка активации лицензии на устройстве
            if (deviceLicenseRepository.findByDeviceIdAndLicenseId(device.getId(), license.getId()).isPresent()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, ERROR_LICENSE_ALREADY_ACTIVE);
            }
//TODO дата активации устанавливается один раз при активации и дата окончания
            // Активация лицензии

            if (license.getFirstActivationDate() == null) {
                license.setFirstActivationDate(new Date());
                Date licenseEndDate = new Date(license.getFirstActivationDate().getTime() +
                        (long) license.getDuration() * 24 * 60 * 60 * 1000);
                license.setEndingDate(licenseEndDate); // Устанавливаем дату окончания
            }

// Рассчитываем дату окончания лицензии

            DeviceLicense deviceLicense = new DeviceLicense();
            deviceLicense.setDeviceId(device.getId());
            deviceLicense.setLicenseId(license.getId());
            deviceLicense.setActivationDate(new Date()); // Устанавливаем время активации
            deviceLicenseRepository.save(deviceLicense);

            logger.info("Лицензия с кодом {} активирована на устройстве с ID {}", activationRequest.getCode(), device.getId());

            // Обновление информации о лицензии
            license.setDeviceCount(license.getDeviceCount() - 1);
            licenseRepository.save(license);
            logger.info("Оставшиеся места для лицензии с кодом {} уменьшены на 1", activationRequest.getCode());

            // Запись в историю изменений лицензии
            licenseHistoryService.recordLicenseChange(
                    license.getId(),
                    user.getId(),
                    "Activated",
                    new Date(),
                    "Лицензия активирована на устройстве"
            );
            logger.info("Изменения лицензии записаны в историю");

            // Генерация успешного тикета
            Ticket ticket = Ticket.createTicket(null, false, license.getEndingDate());
            logger.info("Создан тикет подтверждения активации: {}", ticket);

            return ResponseEntity.ok("Лицензия успешно активирована на устройстве");

        } catch (Exception e) {
            logger.error("Ошибка при активации лицензии: {}", e.getMessage(), e);
            Ticket ticket = Ticket.createTicket(null, true, null);
            logger.info("Создан тикет с ошибкой: {}", ticket);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при активации лицензии");
        }
    }
}