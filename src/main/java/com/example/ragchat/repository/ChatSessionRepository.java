package com.example.ragchat.repository;

import com.example.ragchat.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    List<ChatSession> findByUserId(String userId);
}
