package org.example.Dolgov.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ApplicationRole {
    // Роль USER с правом на чтение
    USER(Set.of(Permission.READ)),

    // Роль ADMIN с правами на чтение и модификацию
    ADMIN(Set.of(Permission.READ, Permission.MODIFICATION));

    private final Set<Permission> permissions;

    // Метод для получения прав доступа в виде GrantedAuthority
    public Set<GrantedAuthority> getGrantedAuthorities() {
        // Преобразуем каждое право из перечисления Permission в SimpleGrantedAuthority
        Set<GrantedAuthority> grantedAuthorities = permissions.stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toSet());

        // Добавляем роль (например, "ROLE_USER" или "ROLE_ADMIN")
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + name()));

        return grantedAuthorities;
    }
}