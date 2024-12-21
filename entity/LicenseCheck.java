package org.example.Dolgov.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO для запроса проверки лицензии.
 * Содержит информацию о MAC-адресе и имени устройства для поиска лицензии.
 */
@Getter  // Генерация геттеров для всех полей
@Setter  // Генерация сеттеров для всех полей
@AllArgsConstructor  // Генерация конструктора со всеми полями
@NoArgsConstructor  // Генерация конструктора без параметров
public class LicenseCheck {

    private String macAddress;  // MAC-адрес устройства для поиска

    private String deviceName;  // Имя устройства для поиска
}