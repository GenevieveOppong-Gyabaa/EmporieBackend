package com.mobiledev.emporio.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobiledev.emporio.dto.AddCartItemRequest;
import com.mobiledev.emporio.dto.CartDto;
import com.mobiledev.emporio.dto.UpdateCartItemRequest;
import com.mobiledev.emporio.exceptions.CartItemNotFoundException;
import com.mobiledev.emporio.exceptions.InvalidCartOperationException;
import com.mobiledev.emporio.exceptions.ProductNotFoundException;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.UserRepository;
import com.mobiledev.emporio.security.JwtUtil;
import com.mobiledev.emporio.services.CartService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<?> viewCart(@RequestParam Long userId, HttpServletRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(new ApiError("User not found"));
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || !authUsername.equals(user.getUsername())) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only access your own cart."));
        }
        CartDto cartDto = cartService.getCartDto(user);
        return ResponseEntity.ok(cartDto);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addItem(@RequestParam Long userId, @RequestBody AddCartItemRequest req) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");
        try {
            cartService.addToCart(user, req.getProductId(), req.getQuantity());
            return ResponseEntity.ok("Item added to cart");
        } catch (ProductNotFoundException | InvalidCartOperationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateItem(@RequestParam Long userId, @RequestBody UpdateCartItemRequest req) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");
        try {
            cartService.updateQuantity(user, req.getProductId(), req.getQuantity());
            return ResponseEntity.ok("Item updated");
        } catch (ProductNotFoundException | CartItemNotFoundException | InvalidCartOperationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeItem(@RequestParam Long userId, @PathVariable Long productId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");
        try {
            cartService.removeItem(user, productId);
            return ResponseEntity.ok("Item removed");
        } catch (ProductNotFoundException | CartItemNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(@RequestParam Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body("User not found");
        cartService.clearCart(user);
        return ResponseEntity.ok("Cart cleared");
    }
}

class ApiError {
    private String error;
    public ApiError(String error) { this.error = error; }
    public String getError() { return error; }
}


    

