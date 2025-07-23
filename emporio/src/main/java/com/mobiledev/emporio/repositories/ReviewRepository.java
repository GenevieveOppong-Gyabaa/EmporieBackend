package com.mobiledev.emporio.repositories;

import com.mobiledev.emporio.model.Review;
import com.mobiledev.emporio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByTypeAndStatus(String type, String status);
    List<Review> findByRevieweeAndTypeAndStatus(User reviewee, String type, String status);
    List<Review> findByStatus(String status);
} 