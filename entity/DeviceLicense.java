package org.example.Dolgov.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * Класс сущности, представляющий связь между устройством и лицензией.
 * Содержит информацию о том, на каком устройстве активирована лицензия,
 * а также дату активации этой лицензии.
 */
@Entity  // Аннотация для указания, что это сущность JPA
@Getter  // Аннотация Lombok для генерации геттеров
@Setter  // Аннотация Lombok для генерации сеттеров
@AllArgsConstructor  // Генерирует конструктор с параметрами для всех полей
@NoArgsConstructor  // Генерирует конструктор без параметров
@Table(name = "device_license")  // Указывает на таблицу в базе данных, с которой будет связана эта сущность
public class DeviceLicense {

    @Id  // Указывает, что это поле является идентификатором (ключом) таблицы
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Автоматическое генерирование значения ID
    @Column(name = "id")  // Указывает, что это поле соответствует колонке "id" в таблице
    private Long id;

    @Column(name = "license_id")  // Указывает, что это поле соответствует колонке "license_id" в таблице
    private Long licenseId;

    @Column(name = "device_id")  // Указывает, что это поле соответствует колонке "device_id" в таблице
    private Long deviceId;

    @Column(name = "activation_date")  // Указывает, что это поле соответствует колонке "activation_date" в таблице
    private Date activationDate;  // Дата активации лицензии на устройстве
}