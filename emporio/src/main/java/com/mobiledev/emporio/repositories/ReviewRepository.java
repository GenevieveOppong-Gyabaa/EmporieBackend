package com.mobiledev.emporio.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobiledev.emporio.model.Review;
import com.mobiledev.emporio.model.User;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTypeAndStatus(String type, String status);
    List<Review> findByRevieweeAndTypeAndStatus(User reviewee, String type, String status);
    List<Review> findByStatus(String status);
    List<Review> findByProductIdAndTypeAndStatus(Long productId, String type, String status);
} 