package org.example.Dolgov.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Сущность для представления лицензии.
 * Содержит информацию о коде лицензии, владельце, продукте, типе лицензии и других атрибутах.
 */
@Entity  // Аннотация для указания, что это сущность JPA, которая будет связана с таблицей в базе данных
@Table(name = "licenses")  // Название таблицы в базе данных, с которой будет работать эта сущность
@Getter  // Аннотация Lombok для генерации геттеров для всех полей
@Setter  // Аннотация Lombok для генерации сеттеров для всех полей
@AllArgsConstructor  // Генерация конструктора со всеми полями
@NoArgsConstructor  // Генерация конструктора без параметров
public class License {

    @Id  // Указывает, что это поле является идентификатором (ключом) сущности
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Автоматическая генерация значения идентификатора
    @Column(name = "id")  // Название колонки в базе данных для этого поля
    private Long id;

    @Column(name = "code", nullable = false)  // Указывает, что поле является обязательным для заполнения
    private String code;  // Код лицензии, обязательное поле

    @ManyToOne  // Указывает, что лицензия связана с пользователем, и один пользователь может иметь много лицензий
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = true)  // Связь с таблицей пользователей
    private ApplicationUser user;  // Ссылка на владельца лицензии (пользователя)

    @ManyToOne  // Связь с владельцем лицензии
    @JoinColumn(name = "owner_id", referencedColumnName = "id", nullable = false)  // Обязательное поле для владельца
    private ApplicationUser owner;  // Ссылка на владельца лицензии

    @ManyToOne  // Связь с продуктом, на который эта лицензия выдана
    @JoinColumn(name = "product_id", referencedColumnName = "id", nullable = false)  // Обязательное поле для продукта
    private Product product;  // Ссылка на продукт, к которому привязана лицензия

    @ManyToOne  // Связь с типом лицензии
    @JoinColumn(name = "type_id", referencedColumnName = "id", nullable = false)  // Обязательное поле для типа лицензии
    private LicenseType licenseType;  // Ссылка на объект типа лицензии

    @Column(name = "first_activation_date", nullable = false)  // Дата первой активации лицензии, обязательная для заполнения
    private Date firstActivationDate;  // Дата активации лицензии

    @Column(name = "ending_date", nullable = false)  // Дата окончания срока действия лицензии
    private Date endingDate;  // Дата окончания действия лицензии

    @Column(name = "blocked")  // Статус лицензии (заблокирована или нет)
    private Boolean blocked;  // Статус блокировки лицензии

    @Column(name = "device_count")  // Количество устройств, на которые можно установить лицензию
    private Integer deviceCount;  // Количество устройств

    @Column(name = "duration")  // Длительность лицензии в днях
    private Integer duration;  // Длительность лицензии в днях

    @Column(name = "description")  // Описание лицензии
    private String description;  // Описание лицензии
}