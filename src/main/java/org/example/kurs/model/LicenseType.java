package org.example.kurs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Сущность для хранения типов лицензий.
 * Каждый тип лицензии может иметь различные характеристики, такие как продолжительность и описание.
 */
@Entity  // Аннотация для указания, что этот класс является сущностью JPA
@Table(name = "license_types")  // Указывает имя таблицы в базе данных
@Getter  // Генерация геттеров для всех полей
@Setter  // Генерация сеттеров для всех полей
@AllArgsConstructor  // Генерация конструктора с параметрами для всех полей
@NoArgsConstructor   // Генерация конструктора без параметров
public class LicenseType {

    @Id  // Указывает, что это поле является первичным ключом
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Генерация значения ключа автоматически
    @Column(name = "id")  // Указывает имя колонки в базе данных
    private Long id;

    @Column(name = "name", nullable = false)  // Название типа лицензии
    private String name;

    @Column(name = "default_duration", nullable = false)  // Продолжительность лицензии по умолчанию в днях
    private Integer defaultDuration;

    @Column(name = "description")  // Описание типа лицензии
    private String description;
}