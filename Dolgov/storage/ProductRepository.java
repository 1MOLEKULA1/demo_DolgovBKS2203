package org.example.Dolgov.storage;

import org.example.Dolgov.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Метод для поиска продукта по уникальному атрибуту (например, по имени)
    Optional<Product> findByName(String name);
}
