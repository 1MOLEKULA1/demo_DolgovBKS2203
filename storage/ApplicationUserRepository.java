package org.example.Dolgov.storage;

import org.example.Dolgov.entity.ApplicationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationUserRepository extends JpaRepository<ApplicationUser, Long> {

    // Поиск пользователя по email
    Optional<ApplicationUser> findByEmail(String email);

    // Проверка существования пользователя с заданным email
    boolean existsByEmail(String email);

    // Поиск пользователя по username
    Optional<ApplicationUser> findByUsername(String username);
}
