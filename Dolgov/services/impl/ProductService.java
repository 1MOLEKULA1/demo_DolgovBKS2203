package org.example.Dolgov.services.impl;

import org.example.Dolgov.entity.Product;
import org.example.Dolgov.storage.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

//TODO: 1. saveProduct - потенциально создаёте дубликаты продуктов (Александр)


@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Создание или обновление продукта с проверкой на дубликаты
    public Product saveProduct(Product product) {
        // Проверка, существует ли продукт с таким же названием
        Optional<Product> existingProduct = productRepository.findByName(product.getName());
        if (existingProduct.isPresent()) {
            // Если продукт с таким названием существует, можно вернуть его (или обновить)
            // В данном случае, возвращаем существующий продукт
            return existingProduct.get();
        } else {
            // Если продукта нет, сохраняем новый
            return productRepository.save(product);
        }
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
