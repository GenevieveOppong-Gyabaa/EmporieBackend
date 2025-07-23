package com.mobiledev.emporio.dto;

import java.time.LocalDateTime;

public class ReviewResponse {
    private Long id;
    private String reviewerUsername;
    private String revieweeUsername;
    private Integer rating;
    private String comment;
    private String type;
    private String status;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReviewerUsername() { return reviewerUsername; }
    public void setReviewerUsername(String reviewerUsername) { this.reviewerUsername = reviewerUsername; }
    public String getRevieweeUsername() { return revieweeUsername; }
    public void setRevieweeUsername(String revieweeUsername) { this.revieweeUsername = revieweeUsername; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
} 