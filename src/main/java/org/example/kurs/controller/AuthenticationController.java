package org.example.kurs.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.kurs.configuration.JwtTokenProvider;
import org.example.kurs.model.ApplicationUser;
import org.example.kurs.model.AuthenticationRequest;
import org.example.kurs.model.AuthenticationResponse;
import org.example.kurs.repository.ApplicationUserRepository;

/**
 * Контроллер для обработки запросов аутентификации.
 * Реализует маршруты для входа в систему и получения JWT-токена.
 */
@RestController
@RequestMapping("/auth") // Базовый маршрут для аутентификационных запросов
@RequiredArgsConstructor // Автоматическое создание конструктора для final полей
public class AuthenticationController {

    private final ApplicationUserRepository ApplicationUserRepository; // Репозиторий пользователей
    private final AuthenticationManager authenticationManager; // Менеджер аутентификации
    private final JwtTokenProvider jwtTokenProvider; // Провайдер для работы с JWT

    /**
     * Метод для обработки запроса на вход в систему.
     *
     * @param request Объект с данными аутентификации (email и пароль).
     * @return ResponseEntity с JWT-токеном или сообщением об ошибке.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest request) {
        try {
            String email = request.getEmail(); // Извлекаем email из запроса

            // Ищем пользователя в базе данных по email
            ApplicationUser user = ApplicationUserRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Проверяем email и пароль через AuthenticationManager
            authenticationManager
                    .authenticate(
                            new UsernamePasswordAuthenticationToken(
                                    email, request.getPassword()) // Создаем токен для проверки
                    );

            // Генерируем JWT-токен на основе email и ролей пользователя
            String token = jwtTokenProvider
                    .createToken(email, user.getRole().getGrantedAuthorities());

            // Возвращаем email и токен в успешном ответе
            return ResponseEntity.ok(new AuthenticationResponse(email, token));
        } catch (AuthenticationException ex) {
            // Если аутентификация не удалась, возвращаем 401 Unauthorized
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("An error in the mail or password"); // Сообщение об ошибке
        }
    }
}