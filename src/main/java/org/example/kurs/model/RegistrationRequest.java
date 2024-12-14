package org.example.kurs.model;

import lombok.Data;

/**
 * DTO (Data Transfer Object) для регистрации нового пользователя.
 * Используется для передачи данных от клиента к серверу при регистрации.
 */
@Data  // Аннотация Lombok для автоматической генерации геттеров, сеттеров, toString, equals и hashCode методов
public class RegistrationRequest {

    private String username;  // Имя пользователя
    private String email;     // Электронная почта пользователя
    private String password;  // Пароль пользователя

}