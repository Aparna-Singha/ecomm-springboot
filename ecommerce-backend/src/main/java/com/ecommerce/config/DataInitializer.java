package com.ecommerce.config;

import com.ecommerce.model.Product;
import com.ecommerce.model.User;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public DataInitializer(UserRepository userRepository, ProductRepository productRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    @Override
    public void run(String... args) {
        // Initialize sample users
        if (userRepository.count() == 0) {
            User user1 = new User();
            user1.setName("John Doe");
            user1.setEmail("john@example.com");
            user1.setPhone("9876543210");
            user1.setAddress("123 Main Street, Mumbai, Maharashtra 400001");
            userRepository.save(user1);

            User user2 = new User();
            user2.setName("Jane Smith");
            user2.setEmail("jane@example.com");
            user2.setPhone("9876543211");
            user2.setAddress("456 Park Avenue, Delhi, Delhi 110001");
            userRepository.save(user2);

            log.info("Sample users created");
        }

        // Initialize sample products
        if (productRepository.count() == 0) {
            Product product1 = new Product();
            product1.setName("Wireless Bluetooth Headphones");
            product1.setDescription("High-quality wireless headphones with noise cancellation");
            product1.setPrice(new BigDecimal("2999.00"));
            product1.setStock(50);
            product1.setCategory("Electronics");
            product1.setImageUrl("https://example.com/headphones.jpg");
            productRepository.save(product1);

            Product product2 = new Product();
            product2.setName("Smart Watch Pro");
            product2.setDescription("Feature-rich smartwatch with health monitoring");
            product2.setPrice(new BigDecimal("5999.00"));
            product2.setStock(30);
            product2.setCategory("Electronics");
            product2.setImageUrl("https://example.com/smartwatch.jpg");
            productRepository.save(product2);

            Product product3 = new Product();
            product3.setName("Cotton T-Shirt");
            product3.setDescription("Comfortable 100% cotton t-shirt");
            product3.setPrice(new BigDecimal("499.00"));
            product3.setStock(100);
            product3.setCategory("Clothing");
            product3.setImageUrl("https://example.com/tshirt.jpg");
            productRepository.save(product3);

            Product product4 = new Product();
            product4.setName("Running Shoes");
            product4.setDescription("Lightweight running shoes with cushioning");
            product4.setPrice(new BigDecimal("3499.00"));
            product4.setStock(40);
            product4.setCategory("Footwear");
            product4.setImageUrl("https://example.com/shoes.jpg");
            productRepository.save(product4);

            Product product5 = new Product();
            product5.setName("Laptop Backpack");
            product5.setDescription("Water-resistant backpack with laptop compartment");
            product5.setPrice(new BigDecimal("1299.00"));
            product5.setStock(60);
            product5.setCategory("Accessories");
            product5.setImageUrl("https://example.com/backpack.jpg");
            productRepository.save(product5);

            log.info("Sample products created");
        }
    }
}
