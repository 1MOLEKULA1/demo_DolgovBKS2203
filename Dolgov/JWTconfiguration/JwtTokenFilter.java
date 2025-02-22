package org.example.Dolgov.JWTconfiguration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor // Аннотация, автоматически генерирует конструктор с параметрами для final полей
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider; // Сервис для работы с JWT токенами
    private final UserDetailsService userDetailsService; // Сервис для получения информации о пользователе

    // Метод, выполняющий проверку и валидацию токена для каждого запроса
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Извлекаем токен из заголовка запроса
        String token = resolveToken(request);

        // Если токен существует и он валиден, устанавливаем аутентификацию в SecurityContext
        if (token != null && jwtTokenProvider.validateToken(token)) {
            // Получаем аутентификацию из токена и устанавливаем её в контекст безопасности
            SecurityContextHolder.getContext().setAuthentication(jwtTokenProvider.getAuthentication(token));
        }

        // Продолжаем выполнение фильтра (передаем управление следующему фильтру в цепочке)
        filterChain.doFilter(request, response);
    }

    // Метод для извлечения токена из заголовка Authorization
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        // Проверяем, начинается ли заголовок с "Bearer" и если да, то извлекаем сам токен
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // Возвращаем токен без префикса "Bearer "
        }
        return null; // Если токен не найден, возвращаем null
    }
}