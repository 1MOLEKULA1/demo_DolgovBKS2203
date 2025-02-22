package org.example.Dolgov.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "users")  // Аннотируем класс как сущность для базы данных с таблицей "users"
@Getter  // Генерирует геттеры для всех полей
@Setter  // Генерирует сеттеры для всех полей
@AllArgsConstructor  // Генерирует конструктор со всеми полями
@NoArgsConstructor   // Генерирует конструктор без параметров
public class ApplicationUser {

    @Id  // Обозначаем поле как идентификатор сущности
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Автоматическая генерация идентификатора (ID) с использованием стратегии идентификации
    private Long id;

    private String username;  // Имя пользователя
    private String password;  // Пароль пользователя
    private String email;     // Электронная почта пользователя

    @Enumerated(EnumType.STRING)  // Указывает, что роль будет храниться как строка в базе данных
    private ApplicationRole role;  // Роль пользователя, которая определена как перечисление ApplicationRole

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)  // Связь с сущностью License (один ко многим)
    private List<License> licenses;  // Список лицензий, принадлежащих пользователю

    /**
     * Геттеры и сеттеры для всех полей генерируются автоматически с использованием Lombok.
     * Поэтому эти методы (getUsername, setUsername и другие) не обязательны, но они могут быть полезны для явного указания
     * работы с полями, если Lombok используется не везде.
     */
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}