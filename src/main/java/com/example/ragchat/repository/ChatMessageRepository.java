package com.example.ragchat.repository;

import com.example.ragchat.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {
    List<ChatMessage> findBySessionIdOrderByTimestampAsc(String sessionId, Pageable pageable);
    void deleteBySessionId(String sessionId);
}