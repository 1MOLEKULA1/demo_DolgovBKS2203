package org.example.Dolgov.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.Dolgov.entity.*;
import org.example.Dolgov.storage.*;
import org.example.Dolgov.JWTconfiguration.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/licensing") // Устанавливаем маршрут для функциональности лицензирования
@RequiredArgsConstructor // Автоматически генерируется конструктор с зависимостями
public class LicensingControllerCheck {

    private final JwtTokenProvider jwtTokenProvider; // Провайдер для работы с JWT токенами
    private final LicenseRepository licenseRepository; // Репозиторий для работы с лицензиями
    private final DeviceLicenseRepository deviceLicenseRepository; // Репозиторий для ассоциаций устройство-лизензия
    private final DeviceRepository deviceRepository; // Репозиторий для работы с устройствами

    private static final Logger logger = LoggerFactory.getLogger(LicensingControllerCheck.class); // Логгер для записи действий

    private static final String SECRET_KEY = "SuperSecretKey123"; // Секретный ключ для подписи тикетов

    public static final String ERROR_AUTHENTICATION = "Ошибка аутентификации";
    public static final String ERROR_DEVICE_NOT_FOUND = "Устройство не найдено";
    public static final String ERROR_LICENSE_NOT_FOUND = "Лицензия не найдена";
    public static final String ERROR_LICENSE_NOT_ACTIVE = "Нет активной лицензии для устройства";

    private ResponseEntity<String> createErrorResponse(HttpStatus status, String message) {
        logger.error(message);
        return ResponseEntity.status(status).body(message);
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkLicense(HttpServletRequest request, @RequestBody LicenseCheck requestData) {
        try {
            Set<String> roles = jwtTokenProvider.getRolesFromRequest(request);
            logger.info("Роли, извлеченные из токена: {}", roles);

            if (roles.isEmpty()) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, ERROR_AUTHENTICATION);
            }

            Optional<Device> deviceOptional = deviceRepository.findByMacAddressAndName(requestData.getMacAddress(), requestData.getDeviceName());
            if (deviceOptional.isEmpty()) {
                return createErrorResponse(HttpStatus.NOT_FOUND, ERROR_DEVICE_NOT_FOUND);
            }

            Device device = deviceOptional.get();
            logger.info("Устройство найдено: {}", device);

            Optional<DeviceLicense> deviceLicenseOptional = deviceLicenseRepository.findByDeviceId(device);
            if (deviceLicenseOptional.isEmpty()) {
                return createErrorResponse(HttpStatus.NOT_FOUND, ERROR_LICENSE_NOT_ACTIVE);
            }

            DeviceLicense deviceLicense = deviceLicenseOptional.get();
            Optional<License> licenseOptional = licenseRepository.findById(deviceLicense.getLicenseId().getId());

            if (licenseOptional.isPresent()) {
                License license = licenseOptional.get();

                Ticket ticket = Ticket.createTicket(
                        license.getUser().getId(),
                        false,
                        license.getEndingDate(),
                        deviceLicense.getDeviceId().getId(),
                        SECRET_KEY
                );

                logger.info("Тикет подтверждения активации лицензии: {}", ticket);

                return ResponseEntity.ok("Лицензия активирована на устройстве. Тикет: " + ticket.getId());
            } else {
                return createErrorResponse(HttpStatus.NOT_FOUND, String.format(ERROR_LICENSE_NOT_FOUND, deviceLicense.getLicenseId()));
            }

        } catch (Exception e) {
            logger.error("Ошибка при проверке лицензии: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при проверке лицензии.");
        }
    }
}
