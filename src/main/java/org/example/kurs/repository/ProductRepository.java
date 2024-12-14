package org.example.kurs.repository;

import org.example.kurs.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Метод для поиска продукта по ID
    Optional<Product> findById(Long id);

}
