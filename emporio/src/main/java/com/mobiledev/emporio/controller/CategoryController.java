package com.mobiledev.emporio.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobiledev.emporio.dto.ApiError;
import com.mobiledev.emporio.dto.RecommendedCategoryDto;
import com.mobiledev.emporio.model.Category;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.CategoryRepository;
import com.mobiledev.emporio.repositories.ProductRepository;
import com.mobiledev.emporio.repositories.UserRepository;
import com.mobiledev.emporio.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category, @RequestParam Long adminId, HttpServletRequest request) {
        // Only admins can create categories
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null) return ResponseEntity.status(403).body(new ApiError("Access denied: Admins only."));
        // Assume you have a UserRepository autowired
        User admin = null;
        try { admin = userRepository.findById(adminId).orElse(null); } catch (Exception ignored) {}
        if (admin == null || admin.getRole() == null || !admin.getRole().name().equals("ADMIN") || !authUsername.equals(admin.getUsername())) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: Admins only."));
        }
        return ResponseEntity.ok(categoryRepository.save(category));
    }

    @GetMapping
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @GetMapping("/recommended")
    public ResponseEntity<List<RecommendedCategoryDto>> getRecommendedCategories() {
        try {
            List<Object[]> results = productRepository.findCategoriesWithProductCounts();
            List<RecommendedCategoryDto> recommendedCategories = results.stream()
                .map(result -> {
                    Long categoryId = (Long) result[0];
                    String categoryName = (String) result[1];
                    Long productCount = (Long) result[2];
                    return new RecommendedCategoryDto(categoryId, categoryName, productCount);
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(recommendedCategories);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public Category getCategory(@PathVariable Long id) {
        return categoryRepository.findById(id).orElse(null);
    }
} 