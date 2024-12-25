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
@RequestMapping("/licensing")
@RequiredArgsConstructor
public class LicensingControllerActivation {

    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationUserRepository applicationUserRepository;
    private final LicenseRepository licenseRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;
    private final LicenseHistoryService licenseHistoryService;
    private final DeviceRepository deviceRepository;

    private static final Logger logger = LoggerFactory.getLogger(LicensingControllerActivation.class);

    public static final String ERROR_LICENSE_NOT_FOUND = "Лицензия не найдена";
    public static final String ERROR_DEVICE_EXISTS = "Устройство уже существует";
    public static final String ERROR_LICENSE_ALREADY_ACTIVE = "Лицензия уже активирована на этом устройстве";
    public static final String ERROR_NO_AVAILABLE_SEATS = "Нет доступных мест для активации";
    public static final String ERROR_AUTHENTICATION = "Ошибка аутентификации";

    private static final String SECRET_KEY = "SuperSecretKey123"; // Секретный ключ для подписи тикетов

    private Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private ResponseEntity<String> createErrorResponse(HttpStatus status, String message) {
        logger.error(message);
        return ResponseEntity.status(status).body(message);
    }

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
            Set<String> roles = jwtTokenProvider.getRolesFromRequest(request);
            logger.info("Из токена извлечены роли: {}", roles);

            if (roles.isEmpty()) {
                return createErrorResponse(HttpStatus.UNAUTHORIZED, ERROR_AUTHENTICATION);
            }

            Optional<License> licenseOptional = licenseRepository.findByCode(activationRequest.getCode());
            if (licenseOptional.isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, ERROR_LICENSE_NOT_FOUND);
            }
            License license = licenseOptional.get();
            logger.info("Лицензия с кодом {} найдена", activationRequest.getCode());

            String email = jwtTokenProvider.getEmailFromRequest(request);
            Optional<ApplicationUser> userOptional = applicationUserRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Пользователь не найден");
            }
            ApplicationUser user = userOptional.get();

            if (license.getUser() == null) {
                license.setUser(user);
            } else if (!license.getUser().getEmail().equals(email)) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "Ошибка: пользователь не совпадает");
            }

            Optional<Device> existingDeviceOptional = deviceRepository.findByMacAddressAndName(activationRequest.getMacAddress(), activationRequest.getDeviceName());
            if (existingDeviceOptional.isPresent()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, ERROR_DEVICE_EXISTS);
            }

            Device device = existingDeviceOptional.orElseGet(() -> registerDevice(activationRequest.getMacAddress(), activationRequest.getDeviceName(), user.getId()));

            if (license.getDeviceCount() <= 0) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, ERROR_NO_AVAILABLE_SEATS);
            }

            if (deviceLicenseRepository.findByDeviceIdAndLicenseId(device.getId(), license.getId()).isPresent()) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, ERROR_LICENSE_ALREADY_ACTIVE);
            }

            if (license.getFirstActivationDate() == null) {
                license.setFirstActivationDate(new Date());
                Date licenseEndDate = new Date(license.getFirstActivationDate().getTime() +
                        (long) license.getDuration() * 24 * 60 * 60 * 1000);
                license.setEndingDate(licenseEndDate);
            }

            DeviceLicense deviceLicense = new DeviceLicense();
            deviceLicense.setDeviceId(device.getId());
            deviceLicense.setLicenseId(license.getId());
            deviceLicense.setActivationDate(new Date());
            deviceLicenseRepository.save(deviceLicense);

            logger.info("Лицензия с кодом {} активирована на устройстве с ID {}", activationRequest.getCode(), device.getId());

            license.setDeviceCount(license.getDeviceCount() - 1);
            licenseRepository.save(license);
            logger.info("Оставшиеся места для лицензии с кодом {} уменьшены на 1", activationRequest.getCode());

            licenseHistoryService.recordLicenseChange(
                    license.getId(),
                    user.getId(),
                    "Activated",
                    new Date(),
                    "Лицензия активирована на устройстве"
            );
            logger.info("Изменения лицензии записаны в историю");

            Ticket ticket = Ticket.createTicket(
                    user.getId(),
                    false,
                    license.getEndingDate(),
                    device.getId(),
                    SECRET_KEY
            );
            logger.info("Создан тикет подтверждения активации: {}", ticket);

            return ResponseEntity.ok("Лицензия успешно активирована на устройстве. Тикет: " + ticket.getId());

        } catch (Exception e) {
            logger.error("Ошибка при активации лицензии: {}", e.getMessage(), e);
            Ticket ticket = Ticket.createTicket(null, true, null, null, SECRET_KEY);
            logger.info("Создан тикет с ошибкой: {}", ticket);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при активации лицензии");
        }
    }
}
