package org.example.kurs.controller;

import lombok.RequiredArgsConstructor;
import org.example.kurs.model.ApplicationUser;
import org.example.kurs.model.ApplicationRole;
import org.example.kurs.model.RegistrationRequest;
import org.example.kurs.repository.ApplicationUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegistrationController {

    private final ApplicationUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Метод для регистрации пользователя
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {
        // Проверка, существует ли уже такой email в базе данных
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            // Если email уже используется, возвращаем статус 409 (Conflict)
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already in use");
        }

        // Создание нового пользователя
        ApplicationUser newUser = new ApplicationUser();
        newUser.setUsername(request.getUsername()); // Устанавливаем имя пользователя
        newUser.setEmail(request.getEmail()); // Устанавливаем email пользователя
        newUser.setPassword(passwordEncoder.encode(request.getPassword())); // Кодируем пароль перед сохранением
        newUser.setRole(ApplicationRole.USER); // Устанавливаем роль по умолчанию (USER)

        // Сохраняем нового пользователя в базе данных
        userRepository.save(newUser);

        // Возвращаем успешный ответ с кодом 201 (Created) и сообщением о регистрации
        return ResponseEntity.status(HttpStatus.CREATED).body("The user has been successfully registered");
    }
}