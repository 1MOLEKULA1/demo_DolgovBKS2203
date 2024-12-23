package org.example.Dolgov.entity;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO для запроса на создание новой лицензии.
 * Содержит информацию о продукте, владельце, типе лицензии и других параметрах.
 */
@Getter  // Генерация геттеров для всех полей
@Setter  // Генерация сеттеров для всех полей
public class LicenseCreate {

    private Long productId;        // Идентификатор продукта, к которому будет прикреплена лицензия

    private Long ownerId;          // Идентификатор владельца лицензии

    private Long licenseTypeId;    // Идентификатор типа лицензии (например, временная или постоянная)

    private String description;    // Описание лицензии, дополнительные сведения о лицензии

    private Integer deviceCount;   // Количество устройств, на которые можно установить эту лицензию
}