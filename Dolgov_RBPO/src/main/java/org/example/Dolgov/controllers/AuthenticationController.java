package org.example.Dolgov.controllers;

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
import org.example.Dolgov.JWTconfiguration.JwtTokenProvider;
import org.example.Dolgov.entity.ApplicationUser;
import org.example.Dolgov.entity.AuthenticationRequest;
import org.example.Dolgov.entity.AuthenticationResponse;
import org.example.Dolgov.storage.ApplicationUserRepository;

/**
 * Контроллер для аутентификации пользователей.
 * Обеспечивает вход в систему и получение JWT-токена.
 */
@RestController
@RequestMapping("/auth") // Базовый путь для маршрутов аутентификации
@RequiredArgsConstructor // Автоматическое создание конструктора для обязательных полей
public class AuthenticationController {

    private final ApplicationUserRepository userRepository; // Репозиторий пользователей
    private final AuthenticationManager authManager; // Сервис для проверки учетных данных
    private final JwtTokenProvider tokenProvider; // Провайдер для создания и валидации JWT-токенов

    /**
     * Обработчик POST-запроса для входа в систему.
     *
     * @param authRequest объект с email и паролем пользователя.
     * @return ResponseEntity с JWT-токеном или ошибкой авторизации.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authRequest) {
        try {
            // Извлекаем email пользователя из запроса
            String email = authRequest.getEmail();

            // Проверяем наличие пользователя в базе данных по указанному email
            ApplicationUser user = getUserByEmail(email);

            // Проверяем соответствие email и пароля через AuthenticationManager
            authenticateUser(email, authRequest.getPassword());

            // Генерируем JWT-токен для аутентифицированного пользователя
            String token = generateJwtToken(user);

            // Возвращаем успешный ответ с токеном
            return createSuccessResponse(email, token);

        } catch (AuthenticationException e) {
            // Возвращаем ошибку авторизации с пояснением
            return createErrorResponse();
        }
    }

    /**
     * Получение пользователя по email из базы данных.
     *
     * @param email Email пользователя.
     * @return Найденный пользователь.
     */
    private ApplicationUser getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    /**
     * Аутентификация пользователя через AuthenticationManager.
     *
     * @param email    Email пользователя.
     * @param password Пароль пользователя.
     * @throws AuthenticationException если данные неверны.
     */
    private void authenticateUser(String email, String password) throws AuthenticationException {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
    }

    /**
     * Генерация JWT-токена для аутентифицированного пользователя.
     *
     * @param user Пользователь.
     * @return Сгенерированный токен.
     */
    private String generateJwtToken(ApplicationUser user) {
        return tokenProvider.createToken(user.getEmail(), user.getRole().getGrantedAuthorities());
    }

    /**
     * Формирование успешного ответа с токеном.
     *
     * @param email Email пользователя.
     * @param token JWT-токен.
     * @return Ответ с данными токена.
     */
    private ResponseEntity<?> createSuccessResponse(String email, String token) {
        return ResponseEntity.ok(new AuthenticationResponse(email, token));
    }

    /**
     * Формирование ошибки авторизации.
     *
     * @return Ответ с сообщением об ошибке.
     */
    private ResponseEntity<?> createErrorResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Incorrect email or password");
    }
}
