package com.mobiledev.emporio.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobiledev.emporio.dto.ApiError;
import com.mobiledev.emporio.dto.SellerDashboardDto;
import com.mobiledev.emporio.dto.SellerProductDto;
import com.mobiledev.emporio.model.Product;
import com.mobiledev.emporio.model.Role;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.CartItemRepository;
import com.mobiledev.emporio.repositories.OrderItemRepository;
import com.mobiledev.emporio.repositories.ProductRepository;
import com.mobiledev.emporio.repositories.UserRepository;
import com.mobiledev.emporio.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/seller")
public class SellerController {
    @Autowired private ProductRepository productRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    // Helper: get user and check role
    private User getUserOrForbidden(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRole() != Role.SELLER) throw new RuntimeException("Forbidden: Not a seller");
        return user;
    }

    @GetMapping("/{userId}/dashboard")
    public ResponseEntity<?> getDashboard(@PathVariable Long userId, HttpServletRequest request) {
        User seller = getUserOrForbidden(userId);
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || !authUsername.equals(seller.getUsername())) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only access your own dashboard."));
        }
        List<Product> products = productRepository.findBySeller(seller);
        int productsListed = products.size();
        long inCarts = cartItemRepository.countByProductIn(products);
        long itemsSold = orderItemRepository.countByProductIn(products);
        Double earnings = orderItemRepository.sumEarningsByProductIn(products);
        if (earnings == null) earnings = 0.0;
        return ResponseEntity.ok(new SellerDashboardDto(productsListed, inCarts, itemsSold, earnings));
    }

    @GetMapping("/{userId}/products")
    public ResponseEntity<?> getSellerProducts(@PathVariable Long userId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, HttpServletRequest request) {
        User seller = getUserOrForbidden(userId);
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || !authUsername.equals(seller.getUsername())) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only access your own products."));
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> productPage = productRepository.findBySeller(seller, pageable);
        List<Product> products = productPage.getContent();
        List<SellerProductDto> dtos = products.stream().map(product -> {
            int views = product.getViews(); // Use the actual views field from Product
            long carts = cartItemRepository.countByProductIn(List.of(product));
            long sold = orderItemRepository.countByProductIn(List.of(product));
            return new SellerProductDto(product.getId(), product.getName(), views, carts, sold, product.getPrice());
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
} 