package org.example.Dolgov.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Класс сущности, представляющий устройство.
 * Содержит информацию об устройстве, такую как имя, MAC-адрес и ID пользователя.
 */
@Entity  // Аннотация для указания, что это сущность JPA
@Getter  // Аннотация Lombok для генерации геттеров
@Setter  // Аннотация Lombok для генерации сеттеров
@AllArgsConstructor  // Генерирует конструктор с параметрами для всех полей
@NoArgsConstructor  // Генерирует конструктор без параметров
@Table(name = "devices")  // Указывает на таблицу в базе данных, с которой будет связана эта сущность
public class Device {

    @Id  // Указывает, что это поле является идентификатором (ключом) таблицы
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Автоматическое генерирование значения ID
    @Column(name = "id")  // Указывает, что это поле соответствует колонке "id" в таблице
    private Long id;

    @Column(name = "name")  // Указывает, что это поле соответствует колонке "name" в таблице
    private String name;

    @Column(name = "mac_address")  // Указывает, что это поле соответствует колонке "mac_address" в таблице
    private String macAddress;

    @Column(name = "user_id")  // Указывает, что это поле соответствует колонке "user_id" в таблице
    private Long userId;
}