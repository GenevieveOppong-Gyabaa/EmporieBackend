package com.mobiledev.emporio.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mobiledev.emporio.security.JwtUtil;
import com.mobiledev.emporio.services.NotificationService;
import com.mobiledev.emporio.services.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class PasswordController {
    private final UserService service;
    private final NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    public PasswordController(UserService service, NotificationService notificationService) {
        this.service = service;
        this.notificationService = notificationService;
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> body, HttpServletRequest request, Authentication auth) {
        String username = auth.getName();
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || !authUsername.equals(username)) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only change your own password."));
        }
        boolean success = service.changePassword(username, body.get("oldPassword"), body.get("newPassword"));
        return ResponseEntity.ok(success ? "Password changed successfully." : "Old password is incorrect.");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = body.get("username");
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || !authUsername.equals(username)) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only reset your own password."));
        }
        String token = service.createResetToken(username);
        if (token != null) {
            notificationService.sendPasswordReset(username, token);
            return ResponseEntity.ok("Reset token: " + token);
        } else {
            return ResponseEntity.badRequest().body(new ApiError("User not found."));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String username = body.get("username");
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || !authUsername.equals(username)) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only reset your own password."));
        }
        boolean success = service.resetPassword(username, body.get("token"), body.get("newPassword"));
        return ResponseEntity.ok(success ? "Password reset successful." : "Invalid token or username.");
    }
}

class ApiError {
    private String error;
    public ApiError(String error) { this.error = error; }
    public String getError() { return error; }
}