package com.mobiledev.emporio.model;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.mobiledev.emporio.repositories.CategoryRepository;

@Component
public class CategoryInitializer implements CommandLineRunner {
    @Autowired
    private CategoryRepository categoryRepository;

    private static final List<String> DEFAULT_CATEGORIES = Arrays.asList(
        "Electronics", "Fashion", "Home", "Beauty", "Health",
        "Toys", "Groceries", "Books", "Sports", "Other"
    );

    @Override
    public void run(String... args) {
        for (String catName : DEFAULT_CATEGORIES) {
            if (categoryRepository.findAll().stream().noneMatch(c -> c.getName().equalsIgnoreCase(catName))) {
                Category category = new Category();
                category.setName(catName);
                categoryRepository.save(category);
            }
        }
    }
} 