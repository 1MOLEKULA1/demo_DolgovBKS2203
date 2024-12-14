package org.example.kurs.controller;

import org.example.kurs.model.Product;
import org.example.kurs.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // Конструктор контроллера с внедрением зависимости (сервиса для работы с продуктами)
    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Метод для создания или обновления продукта
    @PostMapping
    public ResponseEntity<Product> createOrUpdateProduct(@RequestBody Product product) {
        // Сохраняем продукт в базе данных (создание или обновление)
        Product savedProduct = productService.saveProduct(product);
        // Возвращаем ответ с созданным или обновленным продуктом и статусом CREATED
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    // Метод для получения продукта по его ID
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        // Ищем продукт по ID
        Optional<Product> product = productService.getProductById(id);
        // Если продукт найден, возвращаем его с кодом 200 (OK), иначе - код 404 (NOT FOUND)
        return product.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Метод для получения всех продуктов
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        // Получаем список всех продуктов
        List<Product> products = productService.getAllProducts();
        // Возвращаем список продуктов с кодом 200 (OK)
        return ResponseEntity.ok(products);
    }

    // Метод для удаления продукта по ID
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        // Проверяем, существует ли продукт с данным ID
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            // Если продукт существует, удаляем его из базы данных
            productService.deleteProduct(id);
            // Возвращаем ответ с кодом 204 (No Content), что означает успешное удаление
            return ResponseEntity.noContent().build();
        } else {
            // Если продукт не найден, возвращаем код 404 (Not Found)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}