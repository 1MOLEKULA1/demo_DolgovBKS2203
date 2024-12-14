package org.example.kurs.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Перечисление, представляющее различные разрешения для пользователей в системе.
 * Каждое разрешение связано с определенным действием, которое может выполняться в системе.
 */
@Getter  // Генерация геттера для поля permission
@AllArgsConstructor  // Генерация конструктора с параметром для permission
public enum Permission {

    // Перечисление доступных разрешений
    READ("read"),  // Разрешение на чтение
    MODIFICATION("modification");  // Разрешение на изменение

    private final String permission;  // Строковое значение разрешения

}