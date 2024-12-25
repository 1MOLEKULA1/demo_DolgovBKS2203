package org.example.Dolgov.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.Dolgov.entity.*;
import org.example.Dolgov.storage.*;
import org.example.Dolgov.JWTconfiguration.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/licensing")
@RequiredArgsConstructor
public class LicensingControllerUpdate {

    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationUserRepository applicationUserRepository;
    private final LicenseRepository licenseRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;

    private static final Logger logger = LoggerFactory.getLogger(LicensingControllerUpdate.class);

    private static final String SECRET_KEY = "your-secret-key"; // TODO: заменить на реальный способ получения ключа


    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @PostMapping("/update")
    public ResponseEntity<?> updateLicense(HttpServletRequest request, @RequestBody LicenseUpdate requestData) {
        try {
            Set<String> roles = extractRolesFromToken(request);
            if (roles.isEmpty()) {
                return unauthorizedResponse("Роли не найдены в токене.");
            }

            License license = validateLicense(requestData.getCode());
            ApplicationUser user = validateLicenseOwnership(request, license);

            if (!canRenewLicense(license)) {
                return handleBlockedOrExpiredLicense(license);
            }

            Date newExpirationDate = parseExpirationDate(requestData.getNewExpirationDate());
            if (!isValidNewExpirationDate(license, newExpirationDate)) {
                return invalidExpirationDateResponse(license, newExpirationDate);
            }

            int newDuration = calculateDaysBetween(newExpirationDate);
            updateLicenseExpiration(license, newExpirationDate, newDuration);

            return handleSuccessfulRenewal(license, newExpirationDate, newDuration);
        } catch (ParseException e) {
            return handleParseException(e);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return handleGeneralError(e);
        }
    }

    private Set<String> extractRolesFromToken(HttpServletRequest request) {
        Set<String> roles = jwtTokenProvider.getRolesFromRequest(request);
        logger.info("Роль извлечена из токена: {}", roles);
        return roles;
    }

    private ResponseEntity<?> unauthorizedResponse(String message) {
        logger.error("Ошибка аутентификации: {}", message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
    }

    private License validateLicense(String code) {
        return licenseRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ключ лицензии"));
    }

    private ApplicationUser validateLicenseOwnership(HttpServletRequest request, License license) {
        String email = jwtTokenProvider.getEmailFromRequest(request);
        ApplicationUser user = applicationUserRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        if (!user.getId().equals(license.getUser().getId())) {
            logger.error("Ошибка: пользователь не является владельцем лицензии");
            throw new IllegalArgumentException("Пользователь не является владельцем лицензии");
        }

        return user;
    }

    private boolean canRenewLicense(License license) {
        return !license.getBlocked() && license.getEndingDate().after(new Date());
    }

    private ResponseEntity<?> handleBlockedOrExpiredLicense(License license) {
        logger.warn("Лицензия с кодом {} заблокирована или просрочена", license.getCode());
        Ticket ticket = createTicketForBlockedOrExpiredLicense(license);
        logger.info("Тикет: {}", ticket);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Лицензия заблокирована или просрочена. Продление невозможно.");
    }

    private Ticket createTicketForBlockedOrExpiredLicense(License license) {
        Long userId = license.getOwner().getId();
        boolean isBlocked = license.getBlocked();
        Date expirationDate = null;
        Long deviceId = null;

        return Ticket.createTicket(userId, isBlocked, expirationDate, deviceId, SECRET_KEY);
    }

    private Date parseExpirationDate(String expirationDateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return sdf.parse(expirationDateString);
    }

    private boolean isValidNewExpirationDate(License license, Date newExpirationDate) {
        return newExpirationDate.after(license.getEndingDate());
    }

    private ResponseEntity<?> invalidExpirationDateResponse(License license, Date newExpirationDate) {
        logger.warn("Новая дата окончания {} не может быть меньше или равна текущей дате окончания {}", newExpirationDate, license.getEndingDate());
        Ticket ticket = Ticket.createTicket(license.getOwner().getId(), false, null, null, SECRET_KEY);
        logger.info("Тикет: {}", ticket);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Новая дата окончания не может быть меньше или равна текущей.");
    }

    private void updateLicenseExpiration(License license, Date newExpirationDate, int newDuration) {
        license.setEndingDate(newExpirationDate);
        license.setDuration(newDuration);
        licenseRepository.save(license);
        logger.info("Лицензия обновлена до: {}", newExpirationDate);
    }

    private ResponseEntity<?> handleSuccessfulRenewal(License license, Date newExpirationDate, int newDuration) {
        Ticket ticket = createTicketForRenewalConfirmation(license, newExpirationDate, newDuration);
        logger.info("Тикет: {}", ticket);

        String deviceMessage = getDeviceActivationStatus(license);

        return ResponseEntity.status(HttpStatus.OK).body(deviceMessage + "\nЛицензия продлена до: " + newExpirationDate);
    }

    private Ticket createTicketForRenewalConfirmation(License license, Date newExpirationDate, int newDuration) {
        Long userId = license.getOwner().getId();
        boolean isBlocked = false;
        Long deviceId = null;

        return Ticket.createTicket(userId, isBlocked, newExpirationDate, deviceId, SECRET_KEY);
    }

    private String getDeviceActivationStatus(License license) {
        Optional<DeviceLicense> deviceLicenseOpt = deviceLicenseRepository.findByLicenseId(license.getId());
        if (deviceLicenseOpt.isPresent()) {
            DeviceLicense deviceLicense = deviceLicenseOpt.get();
            return "Лицензия активирована на устройстве с ID " + deviceLicense.getDeviceId();
        } else {
            return "Лицензия не активирована на устройстве";
        }
    }

    private int calculateDaysBetween(Date expirationDate) {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDate expirationLocalDate = expirationDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(currentDate.toLocalDate(), expirationLocalDate);
        return (int) daysBetween;
    }

    private ResponseEntity<?> handleParseException(ParseException e) {
        logger.error("Ошибка при парсинге даты: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Неверный формат даты.");
    }

    private ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.error("Ошибка: {}", e.getMessage());
        Ticket ticket = Ticket.createTicket(null, false, null, null, SECRET_KEY);
        logger.info("Тикет: {}", ticket);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Неверный ключ лицензии.");
    }

    private ResponseEntity<?> handleGeneralError(Exception e) {
        logger.error("Произошла ошибка: {}", e.getMessage());
        Ticket ticket = Ticket.createTicket(null, false, null, null, SECRET_KEY);
        logger.info("Тикет: {}", ticket);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при продлении лицензии.");
    }
}
