package com.mobiledev.emporio.services;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.mobiledev.emporio.model.Role;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.UserRepository;

@Service
public class UserService {
    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final Map<String, String> resetTokens = new HashMap<>(); // email -> token

    @Autowired
    private NotificationService notificationService;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public void register(String username, String password) {
        if (repo.findByUsername(username) != null) {
            throw new RuntimeException("Username already taken");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setRole(Role.BUYER); // Always assign BUYER role
        repo.save(user);
    }

    public boolean authenticate(String email, String password) {
        User user = repo.findByEmail(email);
        return user != null && encoder.matches(password, user.getPassword());
    }

    public boolean changePassword(String username, String oldPassword, String newPassword) {
        User user = repo.findByUsername(username);
        if (user != null && encoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(encoder.encode(newPassword));
            repo.save(user);
            notificationService.createNotification(user, "Password Changed", "Your password was changed successfully.");
            // Optionally, send email notification for password change
            return true;
        }
        return false;
    }

    public String createResetToken(String username) {
        User user = repo.findByUsername(username);
        if (user == null) return null;
        String token = UUID.randomUUID().toString();
        resetTokens.put(username, token);
        notificationService.createNotification(user, "Password Reset Requested", "A password reset was requested for your account.");
        notificationService.sendPasswordReset(user.getUsername(), token);
        return token;
    }

    public boolean resetPassword(String username, String token, String newPassword) {
        String validToken = resetTokens.get(username);
        if (validToken != null && validToken.equals(token)) {
            User user = repo.findByUsername(username);
            user.setPassword(encoder.encode(newPassword));
            repo.save(user);
            resetTokens.remove(username);
            notificationService.createNotification(user, "Password Reset Successful", "Your password was reset successfully.");
            // Optionally, send email notification for password reset success
            return true;
        }
        return false;
    }
    // public User findByUsername(String username) {
       

    //     throw new UnsupportedOperationException("findByUsername not implemented yet");

    // }
}