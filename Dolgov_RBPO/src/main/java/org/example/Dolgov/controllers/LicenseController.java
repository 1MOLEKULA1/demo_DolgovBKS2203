package org.example.Dolgov.controllers;

import org.example.Dolgov.entity.License;
import org.example.Dolgov.storage.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

//TODO: 1. Нет разграничения доступа (Александр)


/**
 * Контроллер для управления лицензиями.
 * Предоставляет методы для выполнения CRUD-операций над объектами License.
 */
@RestController
@RequestMapping("/api/licenses") // Базовый маршрут для управления лицензиями
public class LicenseController {

    @Autowired
    private LicenseRepository licenseRepository; // Репозиторий для работы с лицензиями

    /**
     * Получение всех лицензий.
     * Доступ для пользователей с ролью ROLE_USER или ROLE_ADMIN.
     *
     * @return Список всех лицензий.
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public List<License> getAllLicenses() {
        return licenseRepository.findAll(); // Получаем список всех лицензий
    }

    /**
     * Получение лицензии по ID.
     * Доступ для пользователей с ролью ROLE_USER или ROLE_ADMIN.
     *
     * @param id Идентификатор лицензии.
     * @return Лицензия, если она найдена, или null, если не найдена.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public License getLicenseById(@PathVariable Long id) {
        Optional<License> license = licenseRepository.findById(id); // Ищем лицензию по ID
        return license.orElse(null); // Возвращаем лицензию или null, если не найдена
    }

    /**
     * Создание новой лицензии.
     * Доступ только для пользователей с ролью ROLE_ADMIN.
     *
     * @param license Объект лицензии, который нужно создать.
     * @return Сохраненная лицензия.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public License createLicense(@RequestBody License license) {
        return licenseRepository.save(license); // Сохраняем лицензию в базе данных
    }

    /**
     * Обновление существующей лицензии.
     * Доступ только для пользователей с ролью ROLE_ADMIN.
     *
     * @param id      Идентификатор лицензии, которую нужно обновить.
     * @param license Объект лицензии с новыми данными.
     * @return Обновленная лицензия.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public License updateLicense(@PathVariable Long id, @RequestBody License license) {
        license.setId(id); // Устанавливаем ID для обновляемой лицензии
        return licenseRepository.save(license); // Сохраняем изменения
    }

    /**
     * Удаление лицензии по ID.
     * Доступ только для пользователей с ролью ROLE_ADMIN.
     *
     * @param id Идентификатор лицензии, которую нужно удалить.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteLicense(@PathVariable Long id) {
        licenseRepository.deleteById(id); // Удаляем лицензию по ID
    }
}
