package com.mobiledev.emporio.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.mobiledev.emporio.model.ChatMessage;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.UserRepository;
import com.mobiledev.emporio.security.JwtUtil;
import com.mobiledev.emporio.services.ChatService;
import com.mobiledev.emporio.services.NotificationService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ChatController {

    
    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private JwtUtil jwtUtil;

    @MessageMapping("/chat.sendMessage")
    public void send(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        chatService.saveMessage(message);
        messagingTemplate.convertAndSendToUser(
            message.getReceiver().getUsername(),
            "/queue/messages",
            message
        );
        // Send email notification to receiver
        if (message.getReceiver() != null && message.getReceiver().getUsername() != null) {
            String to = message.getReceiver().getUsername(); // assuming username is email
            String fromUser = message.getSender() != null ? message.getSender().getUsername() : "Unknown";
            String preview = message.getContent() != null ? message.getContent() : "(image or file)";
            notificationService.sendNewMessageNotification(to, fromUser, preview);
            // In-app notification
            notificationService.createNotification(message.getReceiver(), "New Chat Message", "You have a new message from " + fromUser);
        }
    }

    @MessageMapping("/chat.typing")
    public void typing(@RequestParam String receiverUsername, String typingInfo) {
        messagingTemplate.convertAndSendToUser(receiverUsername, "/queue/typing", typingInfo);
    }

    @MessageMapping("/chat.read")
    public void markAsRead(Long messageId) {
        ChatMessage message = chatService.getMessageById(messageId);
        if (message != null) {
            message.setRead(true);
            chatService.saveMessage(message);
            messagingTemplate.convertAndSendToUser(
                message.getSender().getUsername(),
                "/queue/read",
                messageId
            );
        }
    }

    @MessageMapping("/chat.delivered")
    public void markAsDelivered(Long messageId) {
        ChatMessage message = chatService.getMessageById(messageId);
        if (message != null) {
            message.setDelivered(true);
            chatService.saveMessage(message);
            messagingTemplate.convertAndSendToUser(
                message.getSender().getUsername(),
                "/queue/delivered",
                messageId
            );
        }
    }

    @PostMapping("/chat/uploadImage")
    public ChatMessage uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("receiver") String receiver, @RequestParam("sender") String sender) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Invalid file type. Only JPEG and PNG are allowed.");
        }

        File directory = new File(uploadPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(uploadPath, fileName);
        Files.copy(file.getInputStream(), filePath);

        String imageUrl = "/images/" + fileName;
        ChatMessage imageMessage = new ChatMessage();
        User senderUser = userRepository.findByUsername(sender);
        User receiverUser = userRepository.findByUsername(receiver);
        imageMessage.setSender(senderUser);
        imageMessage.setReceiver(receiverUser);
        imageMessage.setImageUrl(imageUrl);
        imageMessage.setMessageType("IMAGE");
        imageMessage.setTimestamp(LocalDateTime.now());

        chatService.saveMessage(imageMessage);
        messagingTemplate.convertAndSendToUser(receiverUser.getUsername(), "/queue/messages", imageMessage);
        return imageMessage;
    }

    @GetMapping("/images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Path file = Paths.get(uploadPath).resolve(filename);
        Resource resource;
        try {
            resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read the file!");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/chat/history")
    @ResponseBody
    public ResponseEntity<?> getChatHistory(@RequestParam String user1, @RequestParam String user2, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, HttpServletRequest request) {
        String authUsername = jwtUtil.extractUsernameFromRequest(request);
        if (authUsername == null || (!authUsername.equals(user1) && !authUsername.equals(user2))) {
            return ResponseEntity.status(403).body(new ApiError("Access denied: You can only access your own chat history."));
        }
        User sender = userRepository.findByUsername(user1);
        User receiver = userRepository.findByUsername(user2);
        if (sender == null || receiver == null) return ResponseEntity.badRequest().body(new ApiError("User not found"));
        Pageable pageable = PageRequest.of(page, size);
        List<ChatMessage> messages1 = chatService.getMessagesBetween(sender, receiver, pageable);
        List<ChatMessage> messages2 = chatService.getMessagesBetween(receiver, sender, pageable);
        List<ChatMessage> all = new java.util.ArrayList<>();
        all.addAll(messages1);
        all.addAll(messages2);
        all.sort(java.util.Comparator.comparing(ChatMessage::getTimestamp));
        return ResponseEntity.ok(all);
    }
}

class ApiError {
    private String error;
    public ApiError(String error) { this.error = error; }
    public String getError() { return error; }
}
