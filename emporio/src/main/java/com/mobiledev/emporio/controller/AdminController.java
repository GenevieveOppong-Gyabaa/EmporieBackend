package com.mobiledev.emporio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobiledev.emporio.model.Order;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.OrderRepository;
import com.mobiledev.emporio.repositories.ProductRepository;
import com.mobiledev.emporio.repositories.UserRepository;
import com.mobiledev.emporio.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private JwtUtil jwtUtil;

    // Helper method to check admin role (replace with real auth in production)
    private boolean isAdmin(User user) {
        return user != null && user.getRole() != null && user.getRole().name().equals("ADMIN");
    }

    private ResponseEntity<ApiError> forbidden() {
        return ResponseEntity.status(403).body(new ApiError("Access denied: Admins only."));
    }

    // List all users
    @GetMapping("/users")
    public ResponseEntity<?> listUsers(@RequestParam Long adminId, HttpServletRequest request) {
        User admin = userRepository.findById(adminId).orElse(null);
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (!isAdmin(admin) || authUsername == null || !authUsername.equals(admin.getUsername())) return forbidden();
        return ResponseEntity.ok(userRepository.findAll());
    }

    // Promote user to seller
    @PostMapping("/users/{userId}/promote")
    public ResponseEntity<?> promoteToSeller(@RequestParam Long adminId, @PathVariable Long userId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (!isAdmin(admin)) return ResponseEntity.status(403).body("Forbidden");
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");
        user.setRole(com.mobiledev.emporio.model.Role.SELLER);
        userRepository.save(user);
        return ResponseEntity.ok("User promoted to seller");
    }

    // Demote user to buyer
    @PostMapping("/users/{userId}/demote")
    public ResponseEntity<?> demoteToBuyer(@RequestParam Long adminId, @PathVariable Long userId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (!isAdmin(admin)) return ResponseEntity.status(403).body("Forbidden");
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");
        user.setRole(com.mobiledev.emporio.model.Role.BUYER);
        userRepository.save(user);
        return ResponseEntity.ok("User demoted to buyer");
    }

    // Ban user
    @PostMapping("/users/{userId}/ban")
    public ResponseEntity<?> banUser(@RequestParam Long adminId, @PathVariable Long userId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (!isAdmin(admin)) return ResponseEntity.status(403).body("Forbidden");
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");
        user.setRole(null); // Or add a banned flag/role
        userRepository.save(user);
        return ResponseEntity.ok("User banned");
    }

    // Unban user (set to buyer)
    @PostMapping("/users/{userId}/unban")
    public ResponseEntity<?> unbanUser(@RequestParam Long adminId, @PathVariable Long userId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (!isAdmin(admin)) return ResponseEntity.status(403).body("Forbidden");
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");
        user.setRole(com.mobiledev.emporio.model.Role.BUYER);
        userRepository.save(user);
        return ResponseEntity.ok("User unbanned");
    }

    // List all products
    @GetMapping("/products")
    public ResponseEntity<?> listProducts(@RequestParam Long adminId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (!isAdmin(admin)) return ResponseEntity.status(403).body("Forbidden");
        return ResponseEntity.ok(productRepository.findAll());
    }

    // Remove a product
    @DeleteMapping("/products/{productId}")
    public ResponseEntity<?> removeProduct(@RequestParam Long adminId, @PathVariable Long productId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (!isAdmin(admin)) return ResponseEntity.status(403).body("Forbidden");
        productRepository.deleteById(productId);
        return ResponseEntity.ok("Product removed");
    }

    // List all orders
    @GetMapping("/orders")
    public ResponseEntity<?> listOrders(@RequestParam Long adminId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (!isAdmin(admin)) return ResponseEntity.status(403).body("Forbidden");
        return ResponseEntity.ok(orderRepository.findAll());
    }

    // Mark order as paid (cash on delivery confirmation)
    @PutMapping("/orders/{orderId}/mark-paid")
    public ResponseEntity<?> markOrderAsPaid(@RequestParam Long adminId, @PathVariable Long orderId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (!isAdmin(admin)) return ResponseEntity.status(403).body("Forbidden");
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return ResponseEntity.badRequest().body("Order not found");
        order.setPaymentStatus("PAID");
        orderRepository.save(order);
        return ResponseEntity.ok("Order marked as paid");
    }
}

class ApiError {
    private String error;
    public ApiError(String error) { this.error = error; }
    public String getError() { return error; }
} 