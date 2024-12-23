package org.example.Dolgov.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LicenseUpdate {

    private String Code;        // Код лицензии для поиска лицензии, которая будет обновлена
    private String newExpirationDate; // Новая дата окончания лицензии
}
