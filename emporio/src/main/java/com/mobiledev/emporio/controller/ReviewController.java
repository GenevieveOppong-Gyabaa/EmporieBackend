package com.mobiledev.emporio.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mobiledev.emporio.dto.ApiError;
import com.mobiledev.emporio.dto.ReviewRequest;
import com.mobiledev.emporio.dto.ReviewResponse;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.UserRepository;
import com.mobiledev.emporio.security.JwtUtil;
import com.mobiledev.emporio.services.ReviewService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    // Submit a review (app or seller)
    @PostMapping
    public ResponseEntity<?> submitReview(@Valid @RequestBody ReviewRequest req, BindingResult bindingResult, HttpServletRequest request) {
        if (bindingResult.hasErrors()) {
            String errorMsg = bindingResult.getAllErrors().get(0).getDefaultMessage();
            return ResponseEntity.badRequest().body(new ApiError(errorMsg));
        }
        User reviewer = userRepository.findById(req.getReviewerId()).orElse(null);
        if (reviewer == null) return ResponseEntity.badRequest().body(new ApiError("Reviewer not found"));
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || !authUsername.equals(reviewer.getUsername())) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only submit reviews as yourself."));
        }
        ReviewResponse resp = reviewService.submitReview(req);
        return ResponseEntity.ok(resp);
    }

    // Get approved app reviews
    @GetMapping("/app")
    public List<ReviewResponse> getAppReviews() {
        return reviewService.getAppReviews();
    }

    // Get approved reviews for a seller
    @GetMapping("/seller/{sellerId}")
    public List<ReviewResponse> getSellerReviews(@PathVariable Long sellerId) {
        return reviewService.getSellerReviews(sellerId);
    }

    // Admin: Get all pending reviews
    @GetMapping("/pending")
    public List<ReviewResponse> getPendingReviews(@RequestParam Long adminId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || admin.getRole() == null || !admin.getRole().name().equals("ADMIN")) return List.of();
        return reviewService.getPendingReviews();
    }

    // Admin: Approve a review
    @PostMapping("/{reviewId}/approve")
    public ResponseEntity<?> approveReview(@RequestParam Long adminId, @PathVariable Long reviewId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || admin.getRole() == null || !admin.getRole().name().equals("ADMIN")) return ResponseEntity.status(403).body("Forbidden");
        ReviewResponse resp = reviewService.approveReview(reviewId);
        return resp != null ? ResponseEntity.ok(resp) : ResponseEntity.badRequest().body("Review not found");
    }

    // Admin: Reject a review
    @PostMapping("/{reviewId}/reject")
    public ResponseEntity<?> rejectReview(@RequestParam Long adminId, @PathVariable Long reviewId) {
        User admin = userRepository.findById(adminId).orElse(null);
        if (admin == null || admin.getRole() == null || !admin.getRole().name().equals("ADMIN")) return ResponseEntity.status(403).body("Forbidden");
        ReviewResponse resp = reviewService.rejectReview(reviewId);
        return resp != null ? ResponseEntity.ok(resp) : ResponseEntity.badRequest().body("Review not found");
    }
} 