package org.example.kurs.model;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Set;

/**
 * Реализация интерфейса UserDetails для Spring Security, который предоставляет информацию о пользователе
 * для аутентификации и авторизации в системе.
 */
@Data  // Аннотация Lombok для генерации геттеров, сеттеров, equals, hashCode и toString
public class UserDetailsImpl implements UserDetails {

    private String username;             // Имя пользователя (email в данном случае)
    private String password;             // Пароль пользователя
    private Set<GrantedAuthority> authorities; // Права доступа пользователя
    private boolean isActive;            // Статус активности аккаунта пользователя

    /**
     * Проверяет, не истек срок действия аккаунта пользователя.
     * @return true, если аккаунт активен, иначе false.
     */
    @Override
    public boolean isAccountNonExpired() {
        return isActive;  // Если аккаунт активен, он не истек
    }

    /**
     * Проверяет, не заблокирован ли аккаунт пользователя.
     * @return true, если аккаунт не заблокирован, иначе false.
     */
    @Override
    public boolean isAccountNonLocked() {
        return isActive;  // Если аккаунт активен, он не заблокирован
    }

    /**
     * Проверяет, не истек ли срок действия учетных данных пользователя.
     * @return true, если учетные данные не истекли, иначе false.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return isActive;  // Если аккаунт активен, срок действия учетных данных не истек
    }

    /**
     * Проверяет, включен ли аккаунт пользователя.
     * @return true, если аккаунт включен, иначе false.
     */
    @Override
    public boolean isEnabled() {
        return isActive;  // Если аккаунт активен, он включен
    }

    /**
     * Статический метод для создания объекта UserDetails на основе ApplicationUser.
     * @param user Объект пользователя, на основе которого будет создан UserDetails.
     * @return Объект UserDetails, соответствующий данному пользователю.
     */
    public static UserDetails fromApplicationUser(ApplicationUser user) {
        return new User(
                user.getEmail(),  // Имя пользователя
                user.getPassword(),  // Пароль
                user.getRole().getGrantedAuthorities()  // Права доступа (роль пользователя)
        );
    }
}