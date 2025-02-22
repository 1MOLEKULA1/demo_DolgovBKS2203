package org.example.Dolgov.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data  // Генерирует геттеры, сеттеры, методы equals(), hashCode() и toString()
@AllArgsConstructor  // Генерирует конструктор с параметрами для всех полей
@NoArgsConstructor   // Генерирует конструктор без параметров
public class AuthenticationRequest {

    private String email;    // Электронная почта пользователя
    private String password; // Пароль пользователя
}