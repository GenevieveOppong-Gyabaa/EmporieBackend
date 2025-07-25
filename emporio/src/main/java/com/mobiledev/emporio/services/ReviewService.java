package com.mobiledev.emporio.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mobiledev.emporio.dto.ReviewRequest;
import com.mobiledev.emporio.dto.ReviewResponse;
import com.mobiledev.emporio.model.Product;
import com.mobiledev.emporio.model.Review;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.ProductRepository;
import com.mobiledev.emporio.repositories.ReviewRepository;
import com.mobiledev.emporio.repositories.UserRepository;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;

    public ReviewResponse submitReview(ReviewRequest req) {
        Review review = new Review();
        review.setReviewer(userRepository.findById(req.getReviewerId()).orElse(null));
        review.setReviewee(req.getRevieweeId() != null ? userRepository.findById(req.getRevieweeId()).orElse(null) : null);
        review.setRating(req.getRating());
        review.setComment(req.getComment());
        if (req.getProductId() != null) {
            Product product = productRepository.findById(req.getProductId()).orElse(null);
            review.setProduct(product);
            review.setType("PRODUCT");
        } else {
            review.setType(req.getType());
        }
        review.setStatus("PENDING");
        review.setCreatedAt(LocalDateTime.now());
        review = reviewRepository.save(review);
        return toResponse(review);
    }

    public List<ReviewResponse> getAppReviews() {
        return reviewRepository.findByTypeAndStatus("APP", "APPROVED").stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ReviewResponse> getSellerReviews(Long sellerId) {
        User seller = userRepository.findById(sellerId).orElse(null);
        if (seller == null) return List.of();
        return reviewRepository.findByRevieweeAndTypeAndStatus(seller, "SELLER", "APPROVED").stream().map(this::toResponse).collect(Collectors.toList());
    }

    public List<ReviewResponse> getPendingReviews() {
        return reviewRepository.findByStatus("PENDING").stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ReviewResponse approveReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) return null;
        review.setStatus("APPROVED");
        review = reviewRepository.save(review);
        return toResponse(review);
    }

    public ReviewResponse rejectReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId).orElse(null);
        if (review == null) return null;
        review.setStatus("REJECTED");
        review = reviewRepository.save(review);
        return toResponse(review);
    }

    public double getAverageRatingForProduct(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdAndTypeAndStatus(productId, "PRODUCT", "APPROVED");
        if (reviews == null || reviews.isEmpty()) return 0.0;
        double sum = 0.0;
        int count = 0;
        for (Review r : reviews) {
            if (r.getRating() != null) {
                sum += r.getRating();
                count++;
            }
        }
        return count == 0 ? 0.0 : sum / count;
    }

    public int getReviewCountForProduct(Long productId) {
        List<Review> reviews = reviewRepository.findByProductIdAndTypeAndStatus(productId, "PRODUCT", "APPROVED");
        return reviews == null ? 0 : reviews.size();
    }

    private ReviewResponse toResponse(Review review) {
        ReviewResponse resp = new ReviewResponse();
        resp.setId(review.getId());
        resp.setReviewerUsername(review.getReviewer() != null ? review.getReviewer().getUsername() : null);
        resp.setRevieweeUsername(review.getReviewee() != null ? review.getReviewee().getUsername() : null);
        resp.setRating(review.getRating());
        resp.setComment(review.getComment());
        resp.setType(review.getType());
        resp.setStatus(review.getStatus());
        resp.setCreatedAt(review.getCreatedAt());
        return resp;
    }
} 