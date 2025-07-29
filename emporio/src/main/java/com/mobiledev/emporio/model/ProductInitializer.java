package com.mobiledev.emporio.model;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mobiledev.emporio.repositories.CategoryRepository;
import com.mobiledev.emporio.repositories.ProductRepository;
import com.mobiledev.emporio.repositories.UserRepository;

@Component
public class ProductInitializer implements CommandLineRunner {
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) {
        // Only add sample products if no products exist
        if (productRepository.count() == 0) {
            addSampleProducts();
        }
    }

    private void addSampleProducts() {
        try {
            // Get categories
            List<Category> categories = categoryRepository.findAll();
            if (categories.isEmpty()) {
                return; // No categories available
            }

            // Get first user as seller (or create one if needed)
            List<User> users = userRepository.findAll();
            User seller = users.isEmpty() ? null : users.get(0);

            // Sample products data
            List<SampleProduct> sampleProducts = Arrays.asList(
                new SampleProduct("iPhone 15 Pro", "Latest iPhone model with advanced features", 999.99, "Electronics", 10),
                new SampleProduct("Samsung Galaxy S24", "Premium Android smartphone", 899.99, "Electronics", 8),
                new SampleProduct("Wireless Headphones", "Noise cancelling bluetooth headphones", 199.99, "Electronics", 15),
                new SampleProduct("Designer T-Shirt", "Premium cotton t-shirt", 29.99, "Fashion", 20),
                new SampleProduct("Jeans", "Comfortable denim jeans", 49.99, "Fashion", 12),
                new SampleProduct("Running Shoes", "Professional running shoes", 89.99, "Sports", 7),
                new SampleProduct("Yoga Mat", "Non-slip yoga mat", 25.99, "Sports", 18),
                new SampleProduct("Face Cream", "Hydrating face cream", 19.99, "Beauty", 25),
                new SampleProduct("Lipstick", "Long-lasting lipstick", 15.99, "Beauty", 30),
                new SampleProduct("Vitamins", "Daily multivitamin supplement", 12.99, "Health", 22),
                new SampleProduct("Protein Powder", "Whey protein powder", 34.99, "Health", 14),
                new SampleProduct("Teddy Bear", "Soft plush teddy bear", 24.99, "Toys", 16),
                new SampleProduct("Puzzle Set", "1000 piece jigsaw puzzle", 18.99, "Toys", 9),
                new SampleProduct("Organic Bananas", "Fresh organic bananas", 4.99, "Groceries", 50),
                new SampleProduct("Whole Grain Bread", "Fresh whole grain bread", 3.99, "Groceries", 35),
                new SampleProduct("Programming Book", "Learn Java programming", 39.99, "Books", 11),
                new SampleProduct("Novel", "Bestselling fiction novel", 14.99, "Books", 28),
                new SampleProduct("Coffee Table", "Modern coffee table", 199.99, "Home", 5),
                new SampleProduct("Lamp", "Elegant table lamp", 45.99, "Home", 13)
            );

            for (SampleProduct sample : sampleProducts) {
                Category category = categories.stream()
                    .filter(cat -> cat.getName().equalsIgnoreCase(sample.category))
                    .findFirst()
                    .orElse(categories.get(0)); // Default to first category if not found

                Product product = new Product();
                product.setName(sample.name);
                product.setDescription(sample.description);
                product.setPrice(sample.price);
                product.setStock(sample.stock);
                product.setCategory(category);
                product.setSeller(seller);
                product.setOnDeal(false);
                product.setViews(0);
                product.setImageUrls(Arrays.asList("sample-image.jpg"));
                product.setTags(Arrays.asList(sample.category.toLowerCase()));

                productRepository.save(product);
            }

            System.out.println("Sample products added successfully!");
        } catch (Exception e) {
            System.err.println("Error adding sample products: " + e.getMessage());
        }
    }

    private static class SampleProduct {
        String name, description, category;
        Double price;
        Integer stock;

        SampleProduct(String name, String description, Double price, String category, Integer stock) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.category = category;
            this.stock = stock;
        }
    }
}