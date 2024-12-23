package org.example.Dolgov.services.impl;

import org.example.Dolgov.entity.Product;
import org.example.Dolgov.storage.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Создание или обновление продукта
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    // Получение продукта по ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Получение всех продуктов
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Удаление продукта по ID
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
