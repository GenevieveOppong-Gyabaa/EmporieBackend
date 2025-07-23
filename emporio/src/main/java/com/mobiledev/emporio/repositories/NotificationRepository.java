package com.mobiledev.emporio.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mobiledev.emporio.model.Notification;
import com.mobiledev.emporio.model.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
} 