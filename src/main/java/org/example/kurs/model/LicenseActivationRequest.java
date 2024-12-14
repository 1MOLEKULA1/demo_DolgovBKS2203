package org.example.kurs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO для запроса активации лицензии.
 * Содержит информацию о коде лицензии, MAC-адресе устройства и имени устройства, на которое активируется лицензия.
 */
@Getter  // Генерация геттеров для всех полей
@Setter  // Генерация сеттеров для всех полей
@AllArgsConstructor  // Генерация конструктора со всеми полями
@NoArgsConstructor  // Генерация конструктора без параметров
public class LicenseActivationRequest {

    private String code;  // Код лицензии, по которому мы будем искать лицензию для активации

    private String macAddress;  // MAC-адрес устройства, на которое активируется лицензия

    private String deviceName;  // Имя устройства, на которое активируется лицензия

}