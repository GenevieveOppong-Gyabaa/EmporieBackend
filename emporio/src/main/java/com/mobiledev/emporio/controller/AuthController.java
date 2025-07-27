package com.mobiledev.emporio.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobiledev.emporio.dto.UserDTO;
import com.mobiledev.emporio.security.JwtUtil;
import com.mobiledev.emporio.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

class AuthError {
    private String error;
    public AuthError(String error) { this.error = error; }
    public String getError() { return error; }
}

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserService service;
    private final JwtUtil jwt;
    private final JwtUtil jwtUtil;

    public AuthController(UserService service, JwtUtil jwt) {
        this.service = service;
        this.jwt = jwt;
        this.jwtUtil = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(new AuthError(errorMsg));
        }
        service.register(user.getEmail(), user.getPassword());
        String accessToken = jwt.generateToken(user.getEmail());
        String refreshToken = jwt.generateRefreshToken(user.getEmail());
        com.mobiledev.emporio.model.User dbUser = service.getUserByEmail(user.getEmail());
        return ResponseEntity.ok(Map.of(
            "id", dbUser.getId(),
            "email", dbUser.getEmail(),
            "accessToken", accessToken,
            "refreshToken", refreshToken
        ));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@Valid @RequestBody UserDTO user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(new AuthError(errorMsg));
        }
        // WARNING: This should be protected in production with a secret key or admin approval
        service.registerAdmin(user.getEmail(), user.getPassword());
        return ResponseEntity.ok(jwt.generateToken("ADMIN"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserDTO user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(new AuthError(errorMsg));
        }
        if (service.authenticate(user.getEmail(), user.getPassword())) {
            String accessToken = jwt.generateToken(user.getEmail());
            String refreshToken = jwt.generateRefreshToken(user.getEmail());
            com.mobiledev.emporio.model.User dbUser = service.getUserByEmail(user.getEmail());
            return ResponseEntity.ok(Map.of(
                "id", dbUser.getId(),
                "email", dbUser.getEmail(),
                "accessToken", accessToken,
                "refreshToken", refreshToken
            ));
        } else {
            return ResponseEntity.status(401).body(new AuthError("Invalid credentials"));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || !jwt.isRefreshTokenValid(refreshToken)) {
            return ResponseEntity.status(401).body(new AuthError("Invalid or expired refresh token"));
        }
        String username = jwt.extractUsername(refreshToken);
        String newAccessToken = jwt.generateToken(username);
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/become-seller")
    public ResponseEntity<?> becomeSeller(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null) {
            return ResponseEntity.status(401).body(new AuthError("Authentication required"));
        }
        
        try {
            service.promoteToSeller(authUsername);
            return ResponseEntity.ok(Map.of("message", "Successfully promoted to seller"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new AuthError(e.getMessage()));
        }
    }

    // Alternative endpoint that accepts email directly (for frontend convenience)
    @PostMapping("/promote-to-seller")
    public ResponseEntity<?> promoteToSellerByEmail(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(new AuthError("Email is required"));
        }
        
        try {
            service.promoteToSeller(email);
            return ResponseEntity.ok(Map.of("message", "Successfully promoted to seller"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(new AuthError(e.getMessage()));
        }
    }
}