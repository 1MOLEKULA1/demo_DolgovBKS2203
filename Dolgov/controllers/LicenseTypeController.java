package org.example.Dolgov.controllers;

import org.example.Dolgov.entity.LicenseType;
import org.example.Dolgov.services.impl.LicenseTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

//TODO: 1. Нет разграничения доступа (Александр)


/**
 * Контроллер для управления типами лицензий.
 * Предоставляет методы для выполнения CRUD-операций над объектами LicenseType.
 */
@RestController
@RequestMapping("/api/license-types") // Базовый маршрут для управления типами лицензий
public class LicenseTypeController {

    private final LicenseTypeService licenseTypeService; // Сервис для работы с типами лицензий

    @Autowired
    public LicenseTypeController(LicenseTypeService licenseTypeService) {
        this.licenseTypeService = licenseTypeService; // Инициализация сервиса через конструктор
    }

    /**
     * Создание нового типа лицензии или обновление существующего.
     * Доступ только для пользователей с ролью ROLE_ADMIN.
     *
     * @param licenseType Тип лицензии, который нужно создать или обновить.
     * @return Ответ с сохраненным типом лицензии и статусом CREATED.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LicenseType> createOrUpdateLicenseType(@RequestBody LicenseType licenseType) {
        LicenseType savedLicenseType = licenseTypeService.saveLicenseType(licenseType); // Сохраняем тип лицензии
        return ResponseEntity.status(HttpStatus.CREATED).body(savedLicenseType); // Возвращаем ответ с типом лицензии и статусом CREATED
    }

    /**
     * Получение типа лицензии по ID.
     * Доступ для пользователей с ролью ROLE_USER или ROLE_ADMIN.
     *
     * @param id Идентификатор типа лицензии.
     * @return Ответ с типом лицензии, если он найден, или статусом NOT_FOUND.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<LicenseType> getLicenseTypeById(@PathVariable Long id) {
        Optional<LicenseType> licenseType = licenseTypeService.getLicenseTypeById(id); // Ищем тип лицензии по ID
        return licenseType.map(ResponseEntity::ok) // Если тип найден, возвращаем 200 OK с данными
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build()); // Если не найден, возвращаем статус NOT_FOUND
    }

    /**
     * Получение всех типов лицензий.
     * Доступ для пользователей с ролью ROLE_USER или ROLE_ADMIN.
     *
     * @return Список всех типов лицензий.
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<LicenseType>> getAllLicenseTypes() {
        List<LicenseType> licenseTypes = licenseTypeService.getAllLicenseTypes(); // Получаем список всех типов лицензий
        return ResponseEntity.ok(licenseTypes); // Возвращаем список типов лицензий с статусом OK
    }

    /**
     * Удаление типа лицензии по ID.
     * Доступ только для пользователей с ролью ROLE_ADMIN.
     *
     * @param id Идентификатор типа лицензии, который нужно удалить.
     * @return Ответ с успешным удалением или статусом NOT_FOUND, если тип не найден.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLicenseType(@PathVariable Long id) {
        Optional<LicenseType> licenseType = licenseTypeService.getLicenseTypeById(id); // Ищем тип лицензии по ID
        if (licenseType.isPresent()) { // Если тип найден, удаляем
            licenseTypeService.deleteLicenseType(id); // Удаляем тип лицензии
            return ResponseEntity.noContent().build(); // Возвращаем статус 204 No Content
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // Возвращаем статус 404 Not Found, если тип не найден
        }
    }
}
