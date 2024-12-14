package org.example.kurs.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.kurs.model.*;
import org.example.kurs.repository.*;
import org.example.kurs.configuration.JwtTokenProvider;
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

    // Метод для проверки лицензии
    @PostMapping("/check")
    public ResponseEntity<?> checkLicense(HttpServletRequest request, @RequestBody LicenseCheckRequest requestData) {
        Logger logger = LoggerFactory.getLogger(getClass()); // Логгер для записи действий

        try {
            // Извлекаем роли из токена
            Set<String> roles = jwtTokenProvider.getRolesFromRequest(request);
            logger.info("Role extracted from token: {}", roles);

            // Проверка аутентификации пользователя
            if (roles.isEmpty()) {
                logger.error("Authentication error: no roles present");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication error");
            }

            // Поиск устройства по MAC-адресу и имени
            Optional<Device> deviceOptional = deviceRepository.findByMacAddressAndName(requestData.getMacAddress(), requestData.getDeviceName());
            if (!deviceOptional.isPresent()) {
                logger.error("Error: device not found with MAC address {} and name {}", requestData.getMacAddress(), requestData.getDeviceName());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Device not found");
            }
            Device device = deviceOptional.get(); // Получаем устройство из Optional
            logger.info("Device found: {}", device);

            // Получение информации о лицензиях устройства
            Optional<DeviceLicense> deviceLicenseOptional = deviceLicenseRepository.findByDeviceId(device.getId());
            if (!deviceLicenseOptional.isPresent()) {
                logger.warn("License not found for device with ID {}", device.getId());
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No active license found for the device");
            }

            // Лицензия найдена, извлекаем license_id и находим соответствующую лицензию в таблице licenses
            DeviceLicense deviceLicense = deviceLicenseOptional.get();

            // Находим лицензию по license_id
            Optional<License> licenseOptional = licenseRepository.findById(deviceLicense.getLicenseId());
            if (licenseOptional.isPresent()) {
                License license = licenseOptional.get(); // Получаем лицензию из Optional
                // Создаем тикет, подтверждающий активацию лицензии
                Ticket ticket = Ticket.createTicket(license.getUser().getId(), false, license.getEndingDate());
                ticket.setDeviceId(deviceLicense.getDeviceId()); // Ассоциируем устройство с тикетом

                logger.info("Ticket confirming license: {}", ticket);

                // Возвращаем успешный ответ с информацией о тикете
                return ResponseEntity.status(HttpStatus.OK).body("License activated on the device. Ticket: " + ticket.getId());
            } else {
                // Лицензия не найдена, создаем тикет с ошибкой
                logger.error("License with ID {} not found", deviceLicense.getLicenseId());
                Ticket ticket = Ticket.createTicket(null, true, null); // Создаем тикет с ошибкой
                return ResponseEntity.status(HttpStatus.OK).body("License activated on the device. Ticket: " + ticket);
            }

        } catch (Exception e) {
            // Обрабатываем исключения и создаем тикет с ошибкой
            logger.error("An error occurred while checking the license: {}", e.getMessage());Ticket ticket = Ticket.createTicket(null, false, null); // Ошибка без данных лицензии
            logger.info("Error ticket: {}", ticket);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while checking the license.");
        }
    }

}