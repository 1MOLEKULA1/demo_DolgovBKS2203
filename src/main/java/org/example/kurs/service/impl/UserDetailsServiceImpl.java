package org.example.kurs.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.example.kurs.model.ApplicationUser;
import org.example.kurs.model.UserDetailsImpl;
import org.example.kurs.repository.ApplicationUserRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ApplicationUserRepository userRepository;

    // Метод для загрузки пользователя по email
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Ищем пользователя в базе данных по email
        ApplicationUser user = userRepository.findByEmail(email)
                // Если пользователь не найден, выбрасываем исключение
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Возвращаем объект UserDetails на основе данных пользователя
        return UserDetailsImpl.fromApplicationUser(user);
    }
}