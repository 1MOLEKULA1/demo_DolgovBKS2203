package org.example.Dolgov.controllers;

import org.example.Dolgov.entity.ApplicationUser;
import org.example.Dolgov.storage.ApplicationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

//TODO: 1. Нет разграничения доступа (Александр)


@RestController
@RequestMapping("/api/users")
public class ApplicationUserController {

    @Autowired
    private ApplicationUserRepository userRepository;

    /**
     * Получение всех пользователей.
     *
     * @return Список всех пользователей из базы данных.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Доступ только для пользователей с ролью ROLE_ADMIN
    public List<ApplicationUser> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Получение пользователя по идентификатору.
     *
     * @param id Идентификатор пользователя.
     * @return Объект пользователя или null, если пользователь не найден.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')") // Доступ для пользователей с ролью ROLE_USER или ROLE_ADMIN
    public ApplicationUser getUserById(@PathVariable Long id) {
        Optional<ApplicationUser> user = userRepository.findById(id);
        return user.orElse(null);
    }

    /**
     * Создание нового пользователя.
     *
     * @param user Объект пользователя, полученный из тела запроса.
     * @return Созданный пользователь с присвоенным ID.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // Доступ только для пользователей с ролью ROLE_ADMIN
    public ApplicationUser createUser(@RequestBody ApplicationUser user) {
        return userRepository.save(user);
    }

    /**
     * Обновление существующего пользователя.
     *
     * @param id   Идентификатор пользователя, который нужно обновить.
     * @param user Новый объект пользователя с обновленными данными.
     * @return Обновленный объект пользователя.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")// Доступ только для пользователей с ролью ROLE_ADMIN
    public ApplicationUser updateUser(@PathVariable Long id, @RequestBody ApplicationUser user) {
        user.setId(id);
        return userRepository.save(user);
    }

    /**
     * Удаление пользователя по идентификатору.
     *
     * @param id Идентификатор пользователя, который нужно удалить.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Доступ только для пользователей с ролью ROLE_ADMIN
    public void deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
    }
}
