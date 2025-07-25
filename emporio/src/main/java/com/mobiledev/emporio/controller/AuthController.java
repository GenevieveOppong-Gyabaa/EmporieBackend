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

    public AuthController(UserService service, JwtUtil jwt) {
        this.service = service;
        this.jwt = jwt;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody UserDTO user, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(new AuthError(errorMsg));
        }
        service.register(user.getEmail(), user.getPassword());
        return ResponseEntity.ok(jwt.generateToken("BUYER"));
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
            return ResponseEntity.ok(Map.of("accessToken", accessToken, "refreshToken", refreshToken));
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
}