package com.example.ragchat.service;

import com.example.ragchat.model.ChatSession;
import com.example.ragchat.model.ChatMessage;
import com.example.ragchat.repository.ChatSessionRepository;
import com.example.ragchat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSessionService {
    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;

    public ChatSession createSession(String userId, String name) {
        log.info("Service: Creating session for userId={}, name={}", userId, name);
        ChatSession session = ChatSession.builder()
                .userId(userId)
                .name(name)
                .favorite(false)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        System.out.println("-----------" + userId + "---------" + name);
        return sessionRepo.save(session);
    }

    public void renameSession(String sessionId, String name) {
        log.info("Service: Renaming session {} to '{}'", sessionId, name);
        ChatSession session = sessionRepo.findById(sessionId).orElseThrow();
        session.setName(name);
        session.setUpdatedAt(new Date());
        sessionRepo.save(session);
    }

    public void markFavorite(String sessionId, boolean favorite) {
        log.info("Service: Marking session {} as favorite={}", sessionId, favorite);
        ChatSession session = sessionRepo.findById(sessionId).orElseThrow();
        session.setFavorite(favorite);
        session.setUpdatedAt(new Date());
        sessionRepo.save(session);
    }

    public void deleteSession(String sessionId) {
        log.info("Service: Deleting session {}", sessionId);
        messageRepo.deleteBySessionId(sessionId);
        sessionRepo.deleteById(sessionId);
    }

    public ChatMessage addMessage(String sessionId, String sender, String content, String context) {
        log.info("Service: Adding message to session {}: sender={}, content={}", sessionId, sender, content);
        ChatMessage msg = ChatMessage.builder()
                .sessionId(sessionId)
                .sender(sender)
                .content(content)
                .context(context)
                .timestamp(new Date())
                .build();
        return messageRepo.save(msg);
    }

    public List<ChatMessage> getMessages(String sessionId, int skip, int limit) {
        log.info("Service: Retrieving messages for session {} (skip={}, limit={})", sessionId, skip, limit);
        List<ChatMessage> chatMessagesList = List.of();
        try{
            chatMessagesList = messageRepo.findBySessionIdOrderByTimestampAsc(sessionId, PageRequest.of(skip / limit, limit));
        }catch (Exception e){
            log.error(e.getMessage());
        }
        return chatMessagesList;
    }
}