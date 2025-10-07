package com.example.ragchat.service;

import com.example.ragchat.dto.ChatMessageDTO;
import com.example.ragchat.dto.ChatSessionDTO;
import com.example.ragchat.exception.InvalidInputException;
import com.example.ragchat.exception.SessionNotFoundException;
import com.example.ragchat.model.ChatSession;
import com.example.ragchat.model.ChatMessage;
import com.example.ragchat.repository.ChatSessionRepository;
import com.example.ragchat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatSessionService {
    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final ModelMapper modelMapper;

    /**
     * Create a new chat session
     */
    public ChatSession createSession(String userId, String name) {
        log.info("Service: Creating session for userId={}, name={}", userId, name);

        if (userId == null || userId.trim().isEmpty()) {
            throw new InvalidInputException("User ID cannot be empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Session name cannot be empty");
        }

        ChatSessionDTO dto = new ChatSessionDTO(userId, name);
        ChatSession session = modelMapper.map(dto, ChatSession.class);
        session.setFavorite(false);
        session.setCreatedAt(new Date());
        session.setUpdatedAt(new Date());

        return sessionRepo.save(session);
    }

    /**
     * Rename an existing session
     */
    public void renameSession(Long sessionId, String name) {
        log.info("Service: Renaming session {} to '{}'", sessionId, name);

        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("Session name cannot be empty");
        }

        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(String.valueOf(sessionId)));

        session.setName(name);
        session.setUpdatedAt(new Date());
        sessionRepo.save(session);
    }

    /**
     * Mark or unmark session as favorite
     */
    public void markFavorite(Long sessionId, boolean favorite) {
        log.info("Service: Marking session {} as favorite={}", sessionId, favorite);

        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(String.valueOf(sessionId)));

        session.setFavorite(favorite);
        session.setUpdatedAt(new Date());
        sessionRepo.save(session);
    }

    /**
     * Delete a session and all its messages
     */
    @Transactional
    public void deleteSession(Long sessionId) {
        log.info("Service: Deleting session {}", sessionId);

        if (!sessionRepo.existsById(sessionId)) {
            throw new SessionNotFoundException(String.valueOf(sessionId));
        }

        messageRepo.deleteBySessionId(sessionId);
        sessionRepo.deleteById(sessionId);
    }

    /**
     * Add a message to a session
     */
    public ChatMessage addMessage(Long sessionId, String sender, String content, String context) {
        log.info("Service: Adding message to session {}: sender={}, content={}", sessionId, sender, content);

        if (!sessionRepo.existsById(sessionId)) {
            throw new SessionNotFoundException(String.valueOf(sessionId));
        }
        if (sender == null || sender.trim().isEmpty()) {
            throw new InvalidInputException("Sender cannot be empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new InvalidInputException("Message content cannot be empty");
        }

        ChatMessageDTO dto = new ChatMessageDTO(sender, content, context);
        ChatMessage msg = modelMapper.map(dto, ChatMessage.class);
        msg.setSessionId(sessionId);
        msg.setTimestamp(new Date());

        return messageRepo.save(msg);
    }

    /**
     * Retrieve messages for a session with pagination
     */
    public List<ChatMessage> getMessages(Long sessionId, int skip, int limit) {
        log.info("Service: Retrieving messages for session {} (skip={}, limit={})", sessionId, skip, limit);

        if (!sessionRepo.existsById(sessionId)) {
            throw new SessionNotFoundException(String.valueOf(sessionId));
        }
        if (skip < 0) {
            throw new InvalidInputException("Skip value cannot be negative");
        }
        if (limit <= 0 || limit > 100) {
            throw new InvalidInputException("Limit value must be between 1 and 100");
        }

        int page = skip / limit;
        return messageRepo.findBySessionIdOrderByTimestampAsc(sessionId, PageRequest.of(page, limit));
    }

    /**
     * Get session by ID
     */
    public ChatSession getSessionById(Long sessionId) {
        log.info("Service: Retrieving session {}", sessionId);
        return sessionRepo.findById(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(String.valueOf(sessionId)));
    }

    /**
     * Get all sessions for a user
     */
    public List<ChatSession> getSessionsByUserId(String userId) {
        log.info("Service: Retrieving sessions for user {}", userId);

        if (userId == null || userId.trim().isEmpty()) {
            throw new InvalidInputException("User ID cannot be empty");
        }

        return sessionRepo.findByUserId(userId);
    }
}
