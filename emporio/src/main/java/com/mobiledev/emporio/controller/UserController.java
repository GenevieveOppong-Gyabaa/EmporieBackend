package com.mobiledev.emporio.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobiledev.emporio.dto.ApiError;
import com.mobiledev.emporio.dto.RegisterRequest;
import com.mobiledev.emporio.model.Role;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.UserRepository;
import com.mobiledev.emporio.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/profile")
    public ResponseEntity<?> profile(@RequestParam Long userId, HttpServletRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(new ApiError("User not found"));
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || !authUsername.equals(user.getUsername())) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only access your own profile."));
        }
        return ResponseEntity.ok("This is your profile data (JWT verified).");
    }

    @PostMapping("/become-seller")
    public ResponseEntity<?> becomeSeller(@RequestParam Long userId, HttpServletRequest request) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.badRequest().body(new ApiError("User not found"));
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || !authUsername.equals(user.getUsername())) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only update your own profile."));
        }
        if (user.getRole() == Role.SELLER) return ResponseEntity.ok("Already a seller");
        user.setRole(Role.SELLER);
        userRepository.save(user);
        return ResponseEntity.ok("You are now a seller!");
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        if (request.getEmail() == null || request.getPassword() == null) {
            return ResponseEntity.badRequest().body(new ApiError("Missing required fields"));
        }
        if (userRepository.findAll().stream().anyMatch(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(request.getEmail()))) {
            return ResponseEntity.badRequest().body(new ApiError("Email already registered"));
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail()); // Use email as username
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(Role.BUYER);
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }
}