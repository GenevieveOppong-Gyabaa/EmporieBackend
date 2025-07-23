package com.mobiledev.emporio.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}

class ApiError {
    private String error;
    public ApiError(String error) { this.error = error; }
    public String getError() { return error; }
}