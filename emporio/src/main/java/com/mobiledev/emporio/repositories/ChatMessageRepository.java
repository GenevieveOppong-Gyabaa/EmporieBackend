package com.mobiledev.emporio.repositories;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.mobiledev.emporio.model.ChatMessage;
import com.mobiledev.emporio.model.User;


public interface ChatMessageRepository  extends JpaRepository <ChatMessage, Long> {
    List<ChatMessage> findBySenderAndReceiver(User sender, User receiver);
    List<ChatMessage> findByReceiver(User receiver);
    List<ChatMessage> findBySenderAndReceiver(User sender, User receiver, Pageable pageable);
}
