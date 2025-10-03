package com.example.ragchat.controller;

import com.example.ragchat.dto.ChatSessionDTO;
import com.example.ragchat.dto.ChatMessageDTO;
import com.example.ragchat.model.ChatSession;
import com.example.ragchat.model.ChatMessage;
import com.example.ragchat.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService service;

    @Value("${api.key}")
    private String apiKey;

    @PostConstruct
    public void printApiKey() {
        System.out.println("Loaded API_KEY: " + apiKey);
    }

    // API Key Auth
    private void checkApiKey(HttpServletRequest request) {
        String header = request.getHeader("X-API-KEY");
        if (header == null || !header.equals(apiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API Key");
        }
    }

    // Create session
    @PostMapping("/sessions")
    public ChatSession createSession(@RequestBody ChatSessionDTO dto, HttpServletRequest req) {
        checkApiKey(req);
        return service.createSession(dto.getUserId(), dto.getName());
    }

    // Rename session
    @PatchMapping("/sessions/{id}/rename")
    public void renameSession(@PathVariable String id, @RequestParam String name, HttpServletRequest req) {
        checkApiKey(req);
        service.renameSession(id, name);
    }

    // Mark/unmark as favorite
    @PatchMapping("/sessions/{id}/favorite")
    public void markFavorite(@PathVariable String id, @RequestParam boolean favorite, HttpServletRequest req) {
        checkApiKey(req);
        service.markFavorite(id, favorite);
    }

    // Delete session
    @DeleteMapping("/sessions/{id}")
    public void deleteSession(@PathVariable String id, HttpServletRequest req) {
        checkApiKey(req);
        service.deleteSession(id);
    }

    // Add message
    @PostMapping("/sessions/{id}/messages")
    public ChatMessage addMessage(@PathVariable String id, @RequestBody ChatMessageDTO dto, HttpServletRequest req) {
        checkApiKey(req);
        return service.addMessage(id, dto.getSender(), dto.getContent(), dto.getContext());
    }

    // Retrieve messages (pagination)
    @GetMapping("/sessions/{id}/messages")
    public List<ChatMessage> getMessages(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "20") int limit,
            HttpServletRequest req
    ) {
        checkApiKey(req);
        return service.getMessages(id, skip, limit);
    }

    // Health check
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}