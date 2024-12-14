package org.example.kurs.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.kurs.model.*;
import org.example.kurs.repository.*;
import org.example.kurs.configuration.JwtTokenProvider;
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

//TODO: 1. Не сохранять тикеты
//TODO: 2. Добавить CRUD для Device, Product, LicenseType

@RestController
@RequestMapping("/licensing")
@RequiredArgsConstructor
public class LicensingControllerUpdate {

    private final JwtTokenProvider jwtTokenProvider;
    private final ApplicationUserRepository applicationUserRepository;
    private final LicenseRepository licenseRepository;
    private final DeviceLicenseRepository deviceLicenseRepository;

    // Метод для обновления лицензии
    @PostMapping("/update")
    public ResponseEntity<?> updateLicense(HttpServletRequest request, @RequestBody LicenseUpdateRequest requestData) {
        Logger logger = LoggerFactory.getLogger(getClass());

        try {
            // 1. Извлекаем роли из токена
            Set<String> roles = jwtTokenProvider.getRolesFromRequest(request);
            logger.info("Role extracted from token: {}", roles);

            // 2. Проверка аутентификации пользователя
            if (roles.isEmpty()) {
                logger.error("Authentication error: no roles found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication error");
            }

            // 3. Проверка действительности ключа лицензии
            License license = licenseRepository.findByCode(requestData.getCode())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid license key"));
            logger.info("License with code {} found", requestData.getCode());

            String email1 = jwtTokenProvider.getEmailFromRequest(request);
            Optional<ApplicationUser> userOptional1 = applicationUserRepository.findByEmail(email1);
            ApplicationUser user1 = userOptional1.get();

            // Проверка, является ли пользователь владельцем лицензии
            if (user1.getId().equals(license.getUser().getId())) {
                logger.error("Error: user is not the owner of the license");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: user is not the owner of the license");
            }

            // 4. Проверка возможности продления
            if (license.getBlocked()) {
                logger.warn("License with code {} is blocked", requestData.getCode());
                // Create a ticket for renewal denial
                Ticket ticket = Ticket.createTicket(license.getOwner().getId(),
                        true,  // Blocked license
                        null); // No end date, as renewal is impossible

                // Log ticket
                logger.info("Ticket: {}", ticket);

                // Send response with the ticket
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("License is blocked, renewal is impossible.");
            }

            if (license.getEndingDate().before(new Date())) {
                logger.warn("License with code {} has expired", requestData.getCode());
                // Create a ticket for renewal denial
                Ticket ticket = Ticket.createTicket(license.getOwner().getId(),
                        false,  // License not blocked, but expired
                        null);  // No end date, as renewal is impossible

                // Log ticket
                logger.info("Ticket: {}", ticket);

                // Send response with the ticket
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("License has expired, renewal is impossible.");
            }

            // 5. Convert newExpirationDate from String to Date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date newExpirationDate = sdf.parse(requestData.getNewExpirationDate()); // Parse string to Date

            logger.info("New expiration date: {}", newExpirationDate);

            // Check that the new expiration date is later than the current license expiration date
            if (newExpirationDate.compareTo(license.getEndingDate()) <= 0) {
                logger.warn("New expiration date {} cannot be less than or equal to current expiration date {}",
                        requestData.getNewExpirationDate(), license.getEndingDate());

                // Create a ticket for renewal denial
                Ticket ticket = Ticket.createTicket(
                        license.getOwner().getId(), // License owner ID
                        false,  // License not blocked, but error in expiration date
                        null   // No end date, as there was an error
                );
                // Log ticket
                logger.info("Ticket: {}", ticket);

                // Send response with the ticket
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("New expiration date cannot be less than or equal to the current expiration date. A denial ticket was created.");
            }

            int newDuration = calculateDaysBetween(newExpirationDate);
            // 6. Renew the license
            license.setEndingDate(newExpirationDate); // Set the new expiration date from the request
            license.setDuration(newDuration);

            // Save changes in the database
            licenseRepository.save(license);
            logger.info("License with code {} renewed until: {}", requestData.getCode(), newExpirationDate);

            // 7. Create a ticket for renewal confirmation
            Ticket ticket = Ticket.createTicket(license.getOwner().getId(),
                    false,  // License not blocked
                    newExpirationDate);  // Set new expiration date

            // Check device_license table entry
            Optional<DeviceLicense> deviceLicenseOpt = deviceLicenseRepository.findByLicenseId(license.getId());
            Date activationDate = null;
            Long deviceId = deviceLicenseOpt.get().getDeviceId();
            String deviceMessage = "License not activated on the device";

            if (deviceLicenseOpt.isPresent()) {
                DeviceLicense deviceLicense = deviceLicenseOpt.get();

                activationDate = deviceLicense.getActivationDate();  // Activation date
                deviceId = deviceLicense.getDeviceId();  // Device ID

                // Message for activation confirmation
                deviceMessage = "License activated on the device";
            }

            // Set ticket data
            ticket.setActivationDate(activationDate);
            ticket.setDeviceId(deviceId);
            ticket.setTicketLifetime(newDuration);
            // Log ticket
            logger.info("Ticket: {}", ticket);

            // Send response with the message (split into two lines)
            return ResponseEntity.status(HttpStatus.OK).body(deviceMessage + "\nLicense renewed until: " + newExpirationDate);

        } catch (ParseException e) {
            logger.error("Error parsing date: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid date format.");
        } catch (IllegalArgumentException e) {
            logger.error("Error: {}", e.getMessage());
            // Create ticket for invalid key error
            Ticket ticket = Ticket.createTicket(null,
                    false,  // No blocking, error in renewal
                    null);  // No expiration date, as the error occurred

            // Log ticket
            logger.info("Ticket: {}", ticket);

            // Send response with the ticket
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).
                    body("Invalid license key.");
        } catch (Exception e) {
            logger.error("An error occurred: {}", e.getMessage());
            // Create ticket for unknown error
            Ticket ticket = Ticket.createTicket(null,
                    false,  // No blocking, error in renewal
                    null);  // No expiration date, as the error occurred

            // Log ticket
            logger.info("Ticket: {}", ticket);

            // Send response with the ticket
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while renewing the license.");
        }
    }

    // Method to calculate the number of days between the current date and the provided expiration date
    public static int calculateDaysBetween(Date expirationDate) {
        // Convert Date to LocalDate for easier date manipulation
        LocalDateTime currentDate = LocalDateTime.now(); // Current date and time
        LocalDate expirationLocalDate = expirationDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate(); // Convert Date to LocalDate

        // Calculate the difference in days
        long daysBetween = ChronoUnit.DAYS.between(currentDate.toLocalDate(), expirationLocalDate);

        return (int) daysBetween; // Return the number of days as an integer
    }
}