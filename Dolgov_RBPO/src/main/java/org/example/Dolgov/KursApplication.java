package org.example.Dolgov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // Аннотация, которая обозначает этот класс как главный для запуска Spring Boot приложения.
public class KursApplication {

    // Точка входа в приложение, запуск Spring Boot приложения
    public static void main(String[] args) {
        SpringApplication.run(KursApplication.class, args); // Запуск приложения
    }

}