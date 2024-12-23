package org.example.Dolgov.controllers;

import org.example.Dolgov.entity.ApplicationUser;
import org.example.Dolgov.storage.ApplicationUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/api/users") // Базовый маршрут для всех методов контроллера
public class ApplicationUserController {

    @Autowired // Внедрение зависимости для работы с репозиторием пользователей
    private ApplicationUserRepository userRepository;

    /**
     * Получение всех пользователей.
     *
     * @return Список всех пользователей из базы данных.
     */
    @GetMapping
    public List<ApplicationUser> getAllUsers() {
        // Возвращаем список всех пользователей, найденных в репозитории
        return userRepository.findAll();
    }

    /**
     * Получение пользователя по идентификатору.
     *
     * @param id Идентификатор пользователя.
     * @return Объект пользователя или null, если пользователь не найден.
     */
    @GetMapping("/{id}")
    public ApplicationUser getUserById(@PathVariable Long id) {
        // Ищем пользователя в репозитории по ID
        Optional<ApplicationUser> user = userRepository.findById(id);

        // Если пользователь найден, возвращаем его; иначе возвращаем null
        return user.orElse(null);
    }

    /**
     * Создание нового пользователя.
     *
     * @param user Объект пользователя, полученный из тела запроса.
     * @return Созданный пользователь с присвоенным ID.
     */
    @PostMapping
    public ApplicationUser createUser(@RequestBody ApplicationUser user) {
        // Сохраняем нового пользователя в базе данных
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
    public ApplicationUser updateUser(@PathVariable Long id, @RequestBody ApplicationUser user) {
        // Устанавливаем ID, чтобы обновить данные существующего пользователя
        user.setId(id);

        // Сохраняем изменения в базе данных
        return userRepository.save(user);
    }

    /**
     * Удаление пользователя по идентификатору.
     *
     * @param id Идентификатор пользователя, который нужно удалить.
     */
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        // Удаляем пользователя из базы данных по ID
        userRepository.deleteById(id);
    }
}