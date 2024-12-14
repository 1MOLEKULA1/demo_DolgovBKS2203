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
@RequestMapping("/licensing") // Маршрут для доступа к функционалу лицензирования
@RequiredArgsConstructor // Автоматически генерирует конструктор с обязательными зависимостями
public class LicensingControllerActivation {

    private final JwtTokenProvider jwtTokenProvider; // Провайдер для работы с JWT токенами
    private final ApplicationUserRepository applicationUserRepository; // Репозиторий для работы с пользователями
    private final LicenseRepository licenseRepository; // Репозиторий для работы с лицензиями
    private final DeviceLicenseRepository deviceLicenseRepository; // Репозиторий для работы с привязками устройств и лицензий
    private final LicenseHistoryService licenseHistoryService; // Сервис для записи изменений в истории лицензий
    private final DeviceRepository deviceRepository; // Репозиторий для работы с устройствами

    // Метод для преобразования LocalDate в Date
    private Date convertLocalDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // Метод активации лицензии
    @PostMapping("/activation")
    public ResponseEntity<?> activateLicense(HttpServletRequest request, @RequestBody LicenseActivationRequest activationRequest) {
        Logger logger = LoggerFactory.getLogger(getClass()); // Логгер для записи действий
        try {
            // Извлекаем роли из токена
            Set<String> roles = jwtTokenProvider.getRolesFromRequest(request);
            logger.info("The role is extracted from the token: {}", roles);
//Роль извлечена из токена: {}
            // Проверка аутентификации пользователя
            if (roles.isEmpty()) {
                logger.error("Authentication error: missing roles");
                //Ошибка аутентификации: отсутствуют роли"
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication error");
            }

            // Поиск лицензии по коду
            Optional<License> licenseOptional = licenseRepository.findByCode(activationRequest.getCode());
            License license = licenseOptional.get(); // Преобразование Optional в объект лицензии

            // Проверка лицензии по коду
            if (!licenseOptional.isPresent()) {
                logger.error("The license with the {} code was not found", activationRequest.getCode());
                //Лицензия с кодом {} не найдена
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The license was not found");
            }
            logger.info("License with code {} found", activationRequest.getCode());
            //Лицензия с кодом {} найдена

            // Получаем email из JWT токена и ищем пользователя
            String email = jwtTokenProvider.getEmailFromRequest(request);
            Optional<ApplicationUser> userOptional = applicationUserRepository.findByEmail(email);
            ApplicationUser user = userOptional.get(); // Преобразование Optional в объект пользователя

            // Если у лицензии уже есть пользователь, проверяем его соответствие с текущим
            if (license.getUser() != null) {
                if (!license.getUser().getEmail().equals(email)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mistake");
                }
            } else {
                license.setUser(user); // Если пользователя нет, присваиваем текущего
            }

            // Регистрация или обновление устройства
            Optional<Device> deviceOptional = deviceRepository.findByMacAddressAndName(activationRequest.getMacAddress(), activationRequest.getDeviceName());
            Device device;

            // Проверка, существует ли устройство с таким MAC-адресом и именем
            Optional<Device> existingDevice = deviceRepository.findByMacAddressAndName(activationRequest.getMacAddress(), activationRequest.getDeviceName());
            if (existingDevice.isPresent()) {
                logger.error("The device with the MAC address {} and the name {} already exists", activationRequest.getMacAddress(), activationRequest.getDeviceName());
                //Устройство с MAC-адресом {} и именем {} уже существует
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A device with the same MAC address and name already exists");
                //Устройство с таким MAC-адресом и именем уже существует
            }
            // Если устройство найдено, используем его, иначе регистрируем новое
            if (deviceOptional.isPresent()) {
                device = deviceOptional.get();
                logger.info("Device found: {}", device);
                //Устройство найдено: {}
            } else {
                // Регистрация нового устройства
                device = new Device();
                device.setMacAddress(activationRequest.getMacAddress());
                device.setName(activationRequest.getDeviceName());
                device.setUserId(license.getUser().getId());
                deviceRepository.save(device); // Сохраняем устройство в базе данных
                logger.info("The device with the MAC address {} and name {} is registered", activationRequest.getMacAddress(), activationRequest.getDeviceName());
                //Устройство с MAC-адресом {} и именем {} зарегистрировано
            }

            // Проверка доступных мест для активации устройства на лицензии
            if (license.getDeviceCount() <= 0) {
                logger.warn("There are no places available for activation for a license with the code {}", activationRequest.getCode());
                //Для лицензии с кодом {} нет доступных мест для активации
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("There are no available places to activate on this license");
                //Нет доступных мест для активации на этой лицензии
            }

            // Проверка активации лицензии
            Optional<DeviceLicense> existingDeviceLicenseOptional = deviceLicenseRepository.findByDeviceIdAndLicenseId(device.getId(), license.getId());

            // Если лицензия уже активирована на устройстве
            if (existingDeviceLicenseOptional.isPresent()) {
                logger.warn("The license with the code {} has already been activated on the device with the ID {}", activationRequest.getCode(), device.getId());
                //Лицензия с кодом {} уже активирована на устройстве с ID {}
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("The license has already been activated on this device");
                    //Лицензия уже активирована на данном устройстве
            }

            // Активация лицензии на устройстве
            DeviceLicense deviceLicense = new DeviceLicense();
            deviceLicense.setLicenseId(license.getId());
            deviceLicense.setDeviceId(device.getId());
            deviceLicense.setActivationDate(new Date()); // Дата активации
            deviceLicenseRepository.save(deviceLicense); // Сохраняем привязку устройства и лицензии
            logger.info("The license with the code {} is activated on the device with the ID {}", activationRequest.getCode(), device.getId());

            // Обновляем количество доступных мест на лицензии
            license.setDeviceCount(license.getDeviceCount() - 1);
            licenseRepository.save(license); // Сохраняем обновленную информацию о лицензии
            logger.info("The number of available places for activation on a license with the code {} has been reduced by 1", activationRequest.getCode());
//Количество доступных мест для активации на лицензии с кодом {} уменьшено на 1"
            // Запись в историю лицензий
            String description = "The license is activated on the device";
            Date changeDate = new Date();
            licenseHistoryService.recordLicenseChange(license.getId(), license.getUser().getId(), "Activated", changeDate, description); // Записываем изменение в историю
            logger.info("The recording of license changes in the history is completed");

            // Создаем тикет для успешной активации
            Ticket ticket = Ticket.createTicket(null, false, license.getEndingDate());
                logger.info("A ticket confirming the activation of the license has been created: {}", ticket);

            return ResponseEntity.status(HttpStatus.OK).body("Лицензия успешно активирована на устройстве");

        } catch (Exception e) {
            // Логируем ошибку и создаем тикет с ошибкой
            logger.error("An error occurred while activating the license:{}", e.getMessage(), e);
            Ticket ticket = Ticket.createTicket(null, true, null);
            logger.info("Ticket with an error: {}", ticket);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while activating the license");
        }
    }
}