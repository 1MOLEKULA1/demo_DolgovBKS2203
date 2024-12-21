package org.example.Dolgov.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO (Data Transfer Object) для ответа с результатом аутентификации.
 * Этот класс используется для передачи данных о пользователе после успешной аутентификации.
 */
@Data  // Генерирует геттеры, сеттеры, методы equals(), hashCode() и toString()
@AllArgsConstructor  // Генерирует конструктор с параметрами для всех полей
public class AuthenticationResponse {

    private String email;  // Электронная почта пользователя
    private String token;  // Токен JWT, который выдается после успешной аутентификации
}