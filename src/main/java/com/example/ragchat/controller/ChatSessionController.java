package com.example.ragchat.controller;

import com.example.ragchat.dto.ChatMessageDTO;
import com.example.ragchat.dto.ChatSessionDTO;
import com.example.ragchat.model.ChatMessage;
import com.example.ragchat.model.ChatSession;
import com.example.ragchat.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService service;

    // Create session
    @PostMapping("/sessions")
    public ResponseEntity<ChatSession> createSession(@RequestBody ChatSessionDTO dto) {
        ChatSession session = service.createSession(dto.getUserId(), dto.getName());
        return ResponseEntity.ok(session);
    }

    // Rename session
    @PatchMapping("/sessions/{id}/rename")
    public ResponseEntity<Void> renameSession(
            @PathVariable String id,
            @RequestParam String name) {
        service.renameSession(id, name);
        return ResponseEntity.ok().build();
    }

    // Mark/unmark as favorite
    @PatchMapping("/sessions/{id}/favorite")
    public ResponseEntity<Void> markFavorite(
            @PathVariable String id,
            @RequestParam boolean favorite) {
        service.markFavorite(id, favorite);
        return ResponseEntity.ok().build();
    }

    // Delete session
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable String id) {
        service.deleteSession(id);
        return ResponseEntity.ok().build();
    }

    // Add message
    @PostMapping("/sessions/{id}/messages")
    public ResponseEntity<ChatMessage> addMessage(
            @PathVariable String id,
            @RequestBody ChatMessageDTO dto) {
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
        List<ChatMessage> messages = service.getMessages(id, skip, limit);
        return ResponseEntity.ok(messages);
    }

    // Health check
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}