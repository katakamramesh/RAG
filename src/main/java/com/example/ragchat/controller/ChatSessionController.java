package com.example.ragchat.controller;

import com.example.ragchat.dto.ChatMessageDTO;
import com.example.ragchat.dto.ChatSessionDTO;
import com.example.ragchat.model.ChatMessage;
import com.example.ragchat.model.ChatSession;
import com.example.ragchat.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ChatSessionController {

    private final ChatSessionService service;

    // Create session
    @PostMapping("/sessions")
    public ResponseEntity<ChatSession> createSession(@RequestBody ChatSessionDTO dto) {
        log.info("Creating session for userId={}, name={}", dto.getUserId(), dto.getName());
        ChatSession session = service.createSession(dto.getUserId(), dto.getName());
        return ResponseEntity.ok(session);
    }

    // Rename session
    @PatchMapping("/sessions/{id}/rename")
    public ResponseEntity<Void> renameSession(
            @PathVariable String id,
            @RequestParam String name) {
        log.info("Renaming session {} to '{}'", id, name);
        service.renameSession(id, name);
        return ResponseEntity.ok().build();
    }

    // Mark/unmark as favorite
    @PatchMapping("/sessions/{id}/favorite")
    public ResponseEntity<Void> markFavorite(
            @PathVariable String id,
            @RequestParam boolean favorite) {
        log.info("Marking session {} as favorite={}", id, favorite);
        service.markFavorite(id, favorite);
        return ResponseEntity.ok().build();
    }

    // Delete session
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable String id) {
        log.info("Deleting session {}", id);
        service.deleteSession(id);
        return ResponseEntity.ok().build();
    }

    // Add message
    @PostMapping("/sessions/{id}/messages")
    public ResponseEntity<ChatMessage> addMessage(
            @PathVariable String id,
            @RequestBody ChatMessageDTO dto) {
        log.info("Adding message to session {}: from {}, content='{}'", id, dto.getSender(), dto.getContent());
        ChatMessage message = service.addMessage(
                id,
                dto.getSender(),
                dto.getContent(),
                dto.getContext()
        );
        return ResponseEntity.ok(message);
    }

    // Retrieve messages (pagination)
    @GetMapping("/sessions/{id}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "20") int limit) {
        log.info("Retrieving messages for session {} (skip={}, limit={})", id, skip, limit);
        List<ChatMessage> messages = service.getMessages(id, skip, limit);
        return ResponseEntity.ok(messages);
    }

    // Health check
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.debug("Health check endpoint called");
        return ResponseEntity.ok("OK");
    }
}