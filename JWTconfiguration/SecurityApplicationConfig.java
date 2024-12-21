package org.example.Dolgov.JWTconfiguration;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Класс конфигурации безопасности для Spring Security.
 * Настраивает обработку запросов, фильтры и аутентификацию.
 */
@Configuration
@EnableMethodSecurity // Включает использование аннотаций для ограничения доступа на уровне методов
@RequiredArgsConstructor // Автоматически создает конструктор для final полей
public class SecurityApplicationConfig {

    // Компонент для работы с JWT токенами
    private final JwtTokenProvider jwtTokenProvider;

    // Сервис для загрузки данных пользователя
    private final UserDetailsService userDetailsService;

    /**
     * Регистрация фильтра для проверки JWT токенов.
     *
     * @return Экземпляр JwtTokenFilter.
     */
    @Bean
    public JwtTokenFilter jwtTokenFilter() {
        return new JwtTokenFilter(jwtTokenProvider, userDetailsService);
    }

    /**
     * Настройка цепочки фильтров для обработки запросов.
     *
     * @param http Экземпляр HttpSecurity для настройки безопасности.
     * @return Настроенная цепочка фильтров.
     * @throws Exception В случае ошибок настройки.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Отключение защиты CSRF, так как приложение использует JWT (не поддерживает сессии)
                .csrf(AbstractHttpConfigurer::disable)

                // Настройка политики сессий: все запросы будут обрабатываться без состояния (stateless)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Настройка авторизации запросов
                .authorizeHttpRequests(authz -> authz
                        // Открытый доступ для маршрутов входа и регистрации
                        .requestMatchers("/auth/login", "/auth/register").permitAll()
                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )

                // Добавление фильтра JWT перед стандартным фильтром аутентификации
                .addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        // Возвращаем настроенную цепочку
        return http.build();
    }

    /**
     * Настройка менеджера аутентификации.
     *
     * @param authenticationConfiguration Конфигурация аутентификации.
     * @return Экземпляр AuthenticationManager.
     * @throws Exception В случае ошибок настройки.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * Бин для шифрования паролей с использованием BCrypt.
     * Уровень сложности шифрования установлен на 12.
     *
     * @return Экземпляр PasswordEncoder для работы с паролями.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}