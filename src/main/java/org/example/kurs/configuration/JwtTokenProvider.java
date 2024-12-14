package org.example.kurs.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Компонент для работы с JWT токенами.
 * Содержит функционал для создания, проверки токена и извлечения данных пользователя.
 */
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // Сервис для загрузки данных пользователя по имени
    private final UserDetailsService userDetailsService;

    // Секретный ключ для подписи токена, задается в application.properties
    @Value("${jwt.secret}")
    private String secret;

    // Время жизни токена в миллисекундах, задается в application.properties
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Генерация ключа для подписи на основе секретного ключа.
     *
     * @return Секретный ключ в формате Key.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Создание JWT токена для указанного пользователя и ролей.
     *
     * @param username   Имя пользователя.
     * @param authorities Роли/полномочия пользователя.
     * @return Созданный JWT токен в формате String.
     */
    public String createToken(String username, Set<GrantedAuthority> authorities) {
        // Устанавливаем имя пользователя как subject токена
        Claims claims = Jwts.claims().setSubject(username);

        // Добавляем роли пользователя в токен как список
        claims.put("auth", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList())
        );

        // Текущее время и время истечения токена
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        // Генерация токена
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Проверка валидности токена (подпись и время жизни).
     *
     * @param token JWT токен.
     * @return true, если токен валиден; false, если нет.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Извлечение имени пользователя (subject) из токена.
     *
     * @param token JWT токен.
     * @return Имя пользователя в формате String.
     */
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Получение объекта аутентификации на основе данных токена.
     *
     * @param token JWT токен.
     * @return Объект Authentication с данными пользователя и его ролями.
     */
    public Authentication getAuthentication(String token) {
        // Извлекаем имя пользователя из токена
        String username = getUsername(token);

        // Загружаем данные пользователя по имени
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Формируем объект аутентификации
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    /**
     * Извлечение ролей пользователя из HTTP-запроса.
     *
     * @param request HTTP-запрос с токеном.
     * @return Набор ролей пользователя.
     */
    public Set<String> getRolesFromRequest(HttpServletRequest request) {
        // Извлекаем токен из запроса
        String token = resolveToken(request);

        // Если токен отсутствует, выбрасываем исключение
        if (token == null) {
            throw new IllegalArgumentException("The token was not found in the request");
        }//Токен не найден в запросе

        // Декодируем токен и извлекаем роли
        return getRolesFromToken(token);
    }

    /**
     * Извлечение email пользователя из HTTP-запроса.
     *
     * @param request HTTP-запрос с токеном.
     * @return Email пользователя в формате String.
     */
    public String getEmailFromRequest(HttpServletRequest request) {
        // Извлекаем токен из запроса
        String token = resolveToken(request);

        // Если токен отсутствует, выбрасываем исключение
        if (token == null) {
            throw new IllegalArgumentException("The token was not found in the request");
        }//Токен не найден в запросе

        // Декодируем токен и извлекаем email
        return getEmailFromToken(token);
    }

    /**
     * Извлечение email (subject) пользователя из токена.
     *
     * @param token JWT токен.
     * @return Email пользователя в формате String.
     */
    public String getEmailFromToken(String token) {
        // Декодируем токен и получаем claims (данные из тела токена)
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Возвращаем subject (email)
        return claims.getSubject();
    }

    /**
     * Извлечение ролей пользователя из токена.
     *
     * @param token JWT токен.
     * @return Набор ролей пользователя.
     */
    public Set<String> getRolesFromToken(String token) {
        // Декодируем токен и получаем claims
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        // Извлекаем список ролей из claim "auth"
        List<String> roles = (List<String>) claims.get("auth");

        // Возвращаем роли в виде набора (Set)
        return roles.stream().collect(Collectors.toSet());
    }

    /**
     * Извлечение токена из HTTP-запроса.
     *
     * @param request HTTP-запрос.
     * @return JWT токен в формате String, либо null, если токен отсутствует.
     */
    private String resolveToken(HttpServletRequest request) {
        // Получаем значение заголовка Authorization из запроса
        String bearerToken = request.getHeader("Authorization");

        // Проверяем, что заголовок не пустой и начинается с "Bearer "
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // Возвращаем токен без префикса "Bearer "
            return bearerToken.substring(7);
        }

        // Если заголовок пуст или некорректный, возвращаем null
        return null;
    }
}