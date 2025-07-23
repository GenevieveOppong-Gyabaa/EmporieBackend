package com.mobiledev.emporio.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.mobiledev.emporio.model.Notification;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.NotificationRepository;

@Service
public class NotificationService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void sendOrderConfirmation(String to, String orderDetails) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Order Confirmation");
        message.setText("Thank you for your order!\n\n" + orderDetails);
        mailSender.send(message);
    }

    public void sendPasswordReset(String to, String resetToken) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Password Reset Request");
        message.setText("To reset your password, use this token: " + resetToken);
        mailSender.send(message);
    }

    public void sendNewMessageNotification(String to, String fromUser, String messagePreview) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("New Message Received");
        message.setText("You have a new message from " + fromUser + ":\n\n" + messagePreview);
        mailSender.send(message);
    }

    public void createNotification(User user, String title, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notificationRepository.save(notification);
        sendWebSocketNotification(user, notification);
    }
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    public void sendProductSoldEmail(String to, String details) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Product Sold!");
        message.setText(details);
        mailSender.send(message);
    }

    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id).orElse(null);
    }
    public void saveNotification(Notification notification) {
        notificationRepository.save(notification);
    }

    public void sendWebSocketNotification(User user, Notification notification) {
        if (user != null && user.getUsername() != null) {
            messagingTemplate.convertAndSendToUser(user.getUsername(), "/queue/notifications", notification);
        }
    }
} 