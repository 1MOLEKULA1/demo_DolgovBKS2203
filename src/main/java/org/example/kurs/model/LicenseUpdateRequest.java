package org.example.kurs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LicenseUpdateRequest {

    private String Code;        // Код лицензии для поиска лицензии, которая будет обновлена
    private String newExpirationDate; // Новая дата окончания лицензии
}
