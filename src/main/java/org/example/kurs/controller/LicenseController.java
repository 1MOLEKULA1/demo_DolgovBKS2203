package org.example.kurs.controller;

import org.example.kurs.model.License;
import org.example.kurs.repository.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
     *
     * @return Список всех лицензий.
     */
    @GetMapping
    public List<License> getAllLicenses() {
        return licenseRepository.findAll(); // Получаем список всех лицензий
    }

    /**
     * Получение лицензии по ID.
     *
     * @param id Идентификатор лицензии.
     * @return Лицензия, если она найдена, или null, если не найдена.
     */
    @GetMapping("/{id}")
    public License getLicenseById(@PathVariable Long id) {
        Optional<License> license = licenseRepository.findById(id); // Ищем лицензию по ID
        return license.orElse(null); // Возвращаем лицензию или null, если не найдена
    }

    /**
     * Создание новой лицензии.
     *
     * @param license Объект лицензии, который нужно создать.
     * @return Сохраненная лицензия.
     */
    @PostMapping
    public License createLicense(@RequestBody License license) {
        return licenseRepository.save(license); // Сохраняем лицензию в базе данных
    }

    /**
     * Обновление существующей лицензии.
     *
     * @param id      Идентификатор лицензии, которую нужно обновить.
     * @param license Объект лицензии с новыми данными.
     * @return Обновленная лицензия.
     */
    @PutMapping("/{id}")
    public License updateLicense(@PathVariable Long id, @RequestBody License license) {
        license.setId(id); // Устанавливаем ID для обновляемой лицензии
        return licenseRepository.save(license); // Сохраняем изменения
    }

    /**
     * Удаление лицензии по ID.
     *
     * @param id Идентификатор лицензии, которую нужно удалить.
     */
    @DeleteMapping("/{id}")
    public void deleteLicense(@PathVariable Long id) {
        licenseRepository.deleteById(id); // Удаляем лицензию по ID
    }
}