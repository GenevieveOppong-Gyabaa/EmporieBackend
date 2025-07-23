package com.mobiledev.emporio.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.mobiledev.emporio.model.ChatMessage;
import com.mobiledev.emporio.model.User;
import com.mobiledev.emporio.repositories.ChatMessageRepository;

@Service
public class ChatService {

      @Autowired
    private ChatMessageRepository chatMessageRepository;

    public ChatMessage getMessageById(Long id) {
        return chatMessageRepository.findById(id).orElse(null);
    }
    public ChatMessage saveMessage(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getMessagesBetween(User sender, User receiver) {
        return chatMessageRepository.findBySenderAndReceiver(sender, receiver);
    }

    public List<ChatMessage> getMessagesBetween(User sender, User receiver, Pageable pageable) {
        // You may need to add a corresponding method in ChatMessageRepository for pagination
        return chatMessageRepository.findBySenderAndReceiver(sender, receiver, pageable);
    }

    public List<ChatMessage> getMessagesForReceiver(User receiver) {
        return chatMessageRepository.findByReceiver(receiver);
    }
}
