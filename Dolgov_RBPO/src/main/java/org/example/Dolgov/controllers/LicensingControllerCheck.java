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

    // Константы для сообщений
    public static final String ERROR_AUTHENTICATION = "Ошибка аутентификации";
    public static final String ERROR_DEVICE_NOT_FOUND = "Устройство не найдено";
    public static final String ERROR_LICENSE_NOT_FOUND = "Лицензия не найдена";
    public static final String ERROR_LICENSE_NOT_ACTIVE = "Нет активной лицензии для устройства";

    // Вспомогательный метод для обработки ошибок
    private ResponseEntity<String> createErrorResponse(HttpStatus status, String message) {
        logger.error(message);
        return ResponseEntity.status(status).body(message);
    }

    @PostMapping("/check")
    public ResponseEntity<?> checkLicense(HttpServletRequest request, @RequestBody LicenseCheck requestData) {
        try {
            // Извлекаем роли из токена
            Set<String> roles = jwtTokenProvider.getRolesFromRequest(request);
            logger.info("Роли, извлеченные из токена: {}", roles);

            // Проверка аутентификации пользователя
            if (roles.isEmpty()) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, ERROR_AUTHENTICATION);
            }

            // Поиск устройства по MAC-адресу и имени
            Optional<Device> deviceOptional = deviceRepository.findByMacAddressAndName(requestData.getMacAddress(), requestData.getDeviceName());
            if (deviceOptional.isEmpty()) {
                return createErrorResponse(HttpStatus.NOT_FOUND, String.format(ERROR_DEVICE_NOT_FOUND, requestData.getMacAddress(), requestData.getDeviceName()));
            }
            Device device = deviceOptional.get(); // Получаем устройство из Optional
            logger.info("Устройство найдено: {}", device);

            // Получение информации о лицензии устройства
            Optional<DeviceLicense> deviceLicenseOptional = deviceLicenseRepository.findByDeviceId(device.getId());
            if (deviceLicenseOptional.isEmpty()) {
                return createErrorResponse(HttpStatus.NOT_FOUND, String.format(ERROR_LICENSE_NOT_ACTIVE, device.getId()));
            }

            // Лицензия найдена, извлекаем license_id и находим соответствующую лицензию
            DeviceLicense deviceLicense = deviceLicenseOptional.get();
            Optional<License> licenseOptional = licenseRepository.findById(deviceLicense.getLicenseId());

            if (licenseOptional.isPresent()) {
                License license = licenseOptional.get(); // Получаем лицензию из Optional
                // Создаем тикет, подтверждающий активацию лицензии
                Ticket ticket = Ticket.createTicket(license.getUser().getId(), false, license.getEndingDate());
                ticket.setDeviceId(deviceLicense.getDeviceId()); // Ассоциируем устройство с тикетом

                logger.info("Тикет подтверждения активации лицензии: {}", ticket);

                // Возвращаем успешный ответ с информацией о тикете
                return ResponseEntity.ok("Лицензия активирована на устройстве. Тикет: " + ticket.getId());
            } else {
                // Лицензия не найдена
                return createErrorResponse(HttpStatus.OK, String.format("Лицензия с ID %d не найдена", deviceLicense.getLicenseId()));
            }

        } catch (Exception e) {
            // Обрабатываем исключения и создаем тикет с ошибкой
            logger.error("Ошибка при проверке лицензии: {}", e.getMessage());
            Ticket ticket = Ticket.createTicket(null, true, null); // Ошибка без данных лицензии
            logger.info("Создан тикет с ошибкой: {}", ticket);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при проверке лицензии.");
        }
    }
}