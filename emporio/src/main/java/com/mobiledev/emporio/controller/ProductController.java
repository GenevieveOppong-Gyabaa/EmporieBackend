package com.mobiledev.emporio.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mobiledev.emporio.model.Category;
import com.mobiledev.emporio.model.Product;
import com.mobiledev.emporio.model.Role;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.CategoryRepository;
import com.mobiledev.emporio.repositories.ProductRepository;
import com.mobiledev.emporio.repositories.UserRepository;
import com.mobiledev.emporio.security.JwtUtil;
import com.mobiledev.emporio.services.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/deals")
    public List<Product> getDeals() {
        return productRepository.findByOnDealTrue();
    }

    @PostMapping("/{productId}/upload-images")
    public ResponseEntity<?> uploadProductImages(@PathVariable Long productId, @RequestParam Long userId, @RequestParam("images") List<MultipartFile> images) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRole() != Role.SELLER) {
            return ResponseEntity.status(403).body("Only sellers can upload product images.");
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return ResponseEntity.badRequest().body("Product not found");
        List<String> imageUrls = product.getImageUrls() != null ? new ArrayList<>(product.getImageUrls()) : new ArrayList<>();
        String uploadDir = "src/main/resources/static/uploads/";
        for (MultipartFile image : images) {
            if (image.isEmpty()) continue;
            String filename = System.currentTimeMillis() + "_" + StringUtils.cleanPath(image.getOriginalFilename());
            Path filePath = Paths.get(uploadDir, filename);
            try {
                Files.createDirectories(filePath.getParent());
                Files.copy(image.getInputStream(), filePath);
                String url = "/uploads/" + filename;
                imageUrls.add(url);
            } catch (IOException e) {
                return ResponseEntity.status(500).body("Failed to upload image: " + filename);
            }
        }
        product.setImageUrls(imageUrls);
        productRepository.save(product);
        return ResponseEntity.ok(imageUrls);
    }

    @GetMapping("/by-category/{categoryId}")
    public List<Product> getProductsByCategory(@PathVariable Long categoryId) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) return List.of();
        return productRepository.findAll().stream()
            .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
            .toList();
    }

    @GetMapping("/by-tag")
    public List<Product> getProductsByTag(@RequestParam String tag) {
        return productRepository.findAll().stream()
            .filter(p -> p.getTags() != null && p.getTags().contains(tag))
            .toList();
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody Product product, @RequestParam Long userId, @RequestParam(required = false) Long categoryId, @RequestParam(required = false) List<String> tags, HttpServletRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(new ApiError(errorMsg));
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRole() != Role.SELLER) {
            return ResponseEntity.status(403).body(new ApiError("Only sellers can create products."));
        }
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || !authUsername.equals(user.getUsername())) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only create products as yourself."));
        }
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            product.setCategory(category);
        }
        if (tags != null) {
            product.setTags(tags);
        }
        product.setSeller(user);
        return ResponseEntity.ok(productService.createProduct(product, user));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId, @Valid @RequestBody Product product, @RequestParam Long userId, @RequestParam(required = false) Long categoryId, @RequestParam(required = false) List<String> tags, HttpServletRequest request, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(new ApiError(errorMsg));
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRole() != Role.SELLER) {
            return ResponseEntity.status(403).body(new ApiError("Only sellers can update products."));
        }
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || !authUsername.equals(user.getUsername())) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only update your own products."));
        }
        Product existing = productRepository.findById(productId).orElse(null);
        if (existing == null) return ResponseEntity.badRequest().body(new ApiError("Product not found"));
        if (!existing.getSeller().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(new ApiError("You can only update your own products."));
        }
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            product.setCategory(category);
        }
        if (tags != null) {
            product.setTags(tags);
        }
        return ResponseEntity.ok(productService.updateProduct(productId, product, user));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(@PathVariable Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return ResponseEntity.badRequest().body("Product not found");
        product.setViews(product.getViews() + 1);
        productRepository.save(product);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId, @RequestParam Long userId, HttpServletRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRole() != Role.SELLER) {
            return ResponseEntity.status(403).body(new ApiError("Only sellers can delete products."));
        }
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || !authUsername.equals(user.getUsername())) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only delete your own products."));
        }
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) return ResponseEntity.badRequest().body(new ApiError("Product not found"));
        if (!product.getSeller().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(new ApiError("You can only delete your own products."));
        }
        productRepository.deleteById(productId);
        return ResponseEntity.ok("Product deleted");
    }
}

class ApiError {
    private String error;
    public ApiError(String error) { this.error = error; }
    public String getError() { return error; }
}
