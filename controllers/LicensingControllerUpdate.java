package org.example.Dolgov.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.Dolgov.entity.*;
import org.example.Dolgov.storage.*;
import org.example.Dolgov.JWTconfiguration.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // Основной метод для обновления лицензии
    @PostMapping("/update")
    public ResponseEntity<?> updateLicense(HttpServletRequest request, @RequestBody LicenseUpdate requestData) {
        try {
            // Извлечение ролей пользователя из токена
            Set<String> roles = extractRolesFromToken(request);
            if (roles.isEmpty()) {
                return unauthorizedResponse("Роли не найдены в токене.");
            }

            // Валидация лицензии по коду
            License license = validateLicense(requestData.getCode());

            // Проверка, является ли текущий пользователь владельцем лицензии
            ApplicationUser user = validateLicenseOwnership(request, license);

            // Проверка, можно ли продлить лицензию
            if (!canRenewLicense(license)) {
                return handleBlockedOrExpiredLicense(license);
            }

            // Парсинг новой даты окончания лицензии
            Date newExpirationDate = parseExpirationDate(requestData.getNewExpirationDate());
            if (!isValidNewExpirationDate(license, newExpirationDate)) {
                return invalidExpirationDateResponse(license, newExpirationDate);
            }

            // Вычисление нового срока действия лицензии
            int newDuration = calculateDaysBetween(newExpirationDate);
            // Обновление даты окончания лицензии
            updateLicenseExpiration(license, newExpirationDate, newDuration);

            // Возвращение успешного ответа с информацией о продлении
            return handleSuccessfulRenewal(license, newExpirationDate, newDuration);
        } catch (ParseException e) {
            return handleParseException(e);
        } catch (IllegalArgumentException e) {
            return handleIllegalArgumentException(e);
        } catch (Exception e) {
            return handleGeneralError(e);
        }
    }

    // Извлечение ролей из токена
    private Set<String> extractRolesFromToken(HttpServletRequest request) {
        Set<String> roles = jwtTokenProvider.getRolesFromRequest(request);
        logger.info("Роль извлечена из токена: {}", roles);
        return roles;
    }

    // Ответ для случая, если роли не найдены
    private ResponseEntity<?> unauthorizedResponse(String message) {
        logger.error("Ошибка аутентификации: {}", message);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(message);
    }

    // Валидация лицензии по коду
    private License validateLicense(String code) {
        return licenseRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Неверный ключ лицензии"));
    }

    // Проверка, является ли пользователь владельцем лицензии
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

    // Проверка, можно ли продлить лицензию (если она не заблокирована и не истекла)
    private boolean canRenewLicense(License license) {
        return !license.getBlocked() && license.getEndingDate().after(new Date());
    }

    // Обработка случая заблокированной или просроченной лицензии
    private ResponseEntity<?> handleBlockedOrExpiredLicense(License license) {
        logger.warn("Лицензия с кодом {} заблокирована или просрочена", license.getCode());
        Ticket ticket = createTicketForBlockedOrExpiredLicense(license);
        logger.info("Тикет: {}", ticket);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Лицензия заблокирована или просрочена. Продление невозможно.");
    }

    // Создание тикета для заблокированной или просроченной лицензии
    private Ticket createTicketForBlockedOrExpiredLicense(License license) {
        return Ticket.createTicket(license.getOwner().getId(), license.getBlocked(), null);
    }

    // Парсинг строки в объект Date для новой даты окончания
    private Date parseExpirationDate(String expirationDateString) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return sdf.parse(expirationDateString);
    }

    // Проверка, является ли новая дата окончания позже текущей
    private boolean isValidNewExpirationDate(License license, Date newExpirationDate) {
        return newExpirationDate.after(license.getEndingDate());
    }

    // Ответ в случае недействительной даты окончания
    private ResponseEntity<?> invalidExpirationDateResponse(License license, Date newExpirationDate) {
        logger.warn("Новая дата окончания {} не может быть меньше или равна текущей дате окончания {}", newExpirationDate, license.getEndingDate());
        Ticket ticket = Ticket.createTicket(license.getOwner().getId(), false, null);
        logger.info("Тикет: {}", ticket);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Новая дата окончания не может быть меньше или равна текущей.");
    }

    // Обновление даты окончания лицензии и сохранение изменений
    private void updateLicenseExpiration(License license, Date newExpirationDate, int newDuration) {
        license.setEndingDate(newExpirationDate);
        license.setDuration(newDuration);
        licenseRepository.save(license);
        logger.info("Лицензия обновлена до: {}", newExpirationDate);
    }

    // Обработка успешного продления лицензии
    private ResponseEntity<?> handleSuccessfulRenewal(License license, Date newExpirationDate, int newDuration) {
        Ticket ticket = createTicketForRenewalConfirmation(license, newExpirationDate, newDuration);
        logger.info("Тикет: {}", ticket);

        String deviceMessage = getDeviceActivationStatus(license);

        return ResponseEntity.status(HttpStatus.OK).body(deviceMessage + "\nЛицензия продлена до: " + newExpirationDate);
    }

    // Создание тикета для подтверждения продления лицензии
    private Ticket createTicketForRenewalConfirmation(License license, Date newExpirationDate, int newDuration) {
        Ticket ticket = Ticket.createTicket(license.getOwner().getId(), false, newExpirationDate);
        ticket.setTicketLifetime(newDuration);
        return ticket;
    }

    // Проверка статуса активации лицензии на устройстве
    private String getDeviceActivationStatus(License license) {
        Optional<DeviceLicense> deviceLicenseOpt = deviceLicenseRepository.findByLicenseId(license.getId());
        if (deviceLicenseOpt.isPresent()) {
            DeviceLicense deviceLicense = deviceLicenseOpt.get();
            return "Лицензия активирована на устройстве с ID " + deviceLicense.getDeviceId();
        } else {
            return "Лицензия не активирована на устройстве";
        }
    }

    // Вычисление количества дней между текущей датой и новой датой окончания
    private int calculateDaysBetween(Date expirationDate) {
        LocalDateTime currentDate = LocalDateTime.now();
        LocalDate expirationLocalDate = expirationDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        long daysBetween = ChronoUnit.DAYS.between(currentDate.toLocalDate(), expirationLocalDate);
        return (int) daysBetween;
    }

    // Обработка исключения ParseException
    private ResponseEntity<?> handleParseException(ParseException e) {
        logger.error("Ошибка при парсинге даты: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Неверный формат даты.");
    }

    // Обработка исключения IllegalArgumentException
    private ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.error("Ошибка: {}", e.getMessage());
        Ticket ticket = Ticket.createTicket(null, false, null);
        logger.info("Тикет: {}", ticket);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Неверный ключ лицензии.");
    }

    // Обработка других ошибок
    private ResponseEntity<?> handleGeneralError(Exception e) {
        logger.error("Произошла ошибка: {}", e.getMessage());
        Ticket ticket = Ticket.createTicket(null, false, null);
        logger.info("Тикет: {}", ticket);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при продлении лицензии.");
    }
}
