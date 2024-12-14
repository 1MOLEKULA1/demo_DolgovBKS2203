package org.example.kurs.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Сущность для хранения истории изменений лицензий.
 * Отслеживает изменения статуса лицензии и другие связанные с этим данные.
 */
@Entity  // Аннотация для указания, что этот класс является сущностью JPA
@Table(name = "license_history")  // Указывает имя таблицы в базе данных
@Getter  // Генерация геттеров для всех полей
@Setter  // Генерация сеттеров для всех полей
@AllArgsConstructor  // Генерация конструктора с параметрами для всех полей
@NoArgsConstructor   // Генерация конструктора без параметров
public class LicenseHistory {

    @Id  // Указывает, что это поле является первичным ключом
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Генерация значения ключа автоматически
    @Column(name = "id")  // Указывает имя колонки в базе данных
    private Long id;

    @Column(name = "license_id", nullable = false)  // Указывает колонку для идентификатора лицензии
    private Long licenseId;

    @Column(name = "user_id", nullable = false)  // Указывает колонку для идентификатора пользователя, связанного с лицензией
    private Long userId;

    @Column(name = "status", nullable = false)  // Статус лицензии (например, активна, заблокирована и т.д.)
    private String status;

    @Column(name = "change_date", nullable = false)  // Дата изменения статуса лицензии
    private Date changeDate;

    @Column(name = "description")  // Описание, поясняющее, что произошло с лицензией
    private String description;
}