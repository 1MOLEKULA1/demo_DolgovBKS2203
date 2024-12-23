package org.example.Dolgov.controllers;

import lombok.RequiredArgsConstructor;
import org.example.Dolgov.entity.ApplicationUser;
import org.example.Dolgov.entity.ApplicationRole;
import org.example.Dolgov.entity.RegistrationRequest;
import org.example.Dolgov.storage.ApplicationUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class RegistrationUsersController {

    private final ApplicationUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Метод для регистрации нового пользователя
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegistrationRequest request) {
        try {
            // Проверяем сложность пароля
            if (!isPasswordStrong(request.getPassword())) {
                return weakPasswordResponse();
            }

            // Проверяем, существует ли пользователь с таким email
            if (isEmailAlreadyInUse(request.getEmail())) {
                return emailConflictResponse();
            }

            // Создаем нового пользователя
            ApplicationUser newUser = createNewUser(request);

            // Сохраняем пользователя в базе данных
            saveNewUser(newUser);

            // Возвращаем успешный ответ
            return successfulRegistrationResponse();
        } catch (Exception e) {
            return handleGeneralError(e);
        }
    }

    // Проверка, существует ли уже такой email
    private boolean isEmailAlreadyInUse(String email) {
        boolean emailExists = userRepository.findByEmail(email).isPresent();
        if (emailExists) {
            // Логируем информацию о конфликте
            System.out.println("Email " + email + " уже используется.");
        }
        return emailExists;
    }

    // Проверка сложности пароля
    private boolean isPasswordStrong(String password) {
        if (password == null || password.length() < 10) {
            return false;
        }
        boolean hasUpper = false, hasLower = false, hasDigit = false, hasSpecial = false;
        for (char ch : password.toCharArray()) {
            if (Character.isUpperCase(ch)) hasUpper = true;
            if (Character.isLowerCase(ch)) hasLower = true;
            if (Character.isDigit(ch)) hasDigit = true;
            if (!Character.isLetterOrDigit(ch)) hasSpecial = true;
        }
        return hasUpper && hasLower && hasDigit && hasSpecial;
    }

    // Ответ на слабый пароль
    private ResponseEntity<?> weakPasswordResponse() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Password must be at least 10 characters long and include uppercase, lowercase, digits, and special characters.");
    }

    // Ответ на конфликт с email
    private ResponseEntity<?> emailConflictResponse() {
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Email is already in use");
    }

    // Создание нового пользователя на основе данных из запроса
    private ApplicationUser createNewUser(RegistrationRequest request) {
        ApplicationUser newUser = new ApplicationUser();
        newUser.setUsername(request.getUsername()); // Устанавливаем имя пользователя
        newUser.setEmail(request.getEmail()); // Устанавливаем email
        newUser.setPassword(passwordEncoder.encode(request.getPassword())); // Кодируем пароль
        newUser.setRole(ApplicationRole.USER); // Устанавливаем роль по умолчанию
        return newUser;
    }

    // Сохранение пользователя в базе данных
    private void saveNewUser(ApplicationUser newUser) {
        userRepository.save(newUser);
        System.out.println("Пользователь " + newUser.getEmail() + " успешно зарегистрирован.");
    }

    // Ответ об успешной регистрации
    private ResponseEntity<?> successfulRegistrationResponse() {
        return ResponseEntity.status(HttpStatus.CREATED).body("The user has been successfully registered");
    }

    // Обработка других ошибок
    private ResponseEntity<?> handleGeneralError(Exception e) {
        System.err.println("Ошибка регистрации: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Произошла ошибка при регистрации");
    }
}
