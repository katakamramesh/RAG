package com.example.ragchat.controller;

import com.example.ragchat.dto.ChatSessionDTO;
import com.example.ragchat.dto.ChatMessageDTO;
import com.example.ragchat.model.ChatSession;
import com.example.ragchat.model.ChatMessage;
import com.example.ragchat.service.ChatSessionService;
import com.example.ragchat.util.RAGUtils;
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

    private ChatSessionService service;

    private RAGUtils ragUtils;

    @Value("${api.key}")
    private String apiKey;

    @PostConstruct
    public void printApiKey() {
        System.out.println("Loaded API_KEY: " + apiKey);
    }

    // Create session
    @PostMapping("/sessions")
    public ChatSession createSession(@RequestBody ChatSessionDTO dto,// @RequestHeader HttpServletRequest req,
                                     @RequestHeader String apiKey) {
       // ragUtils.checkApiKey(req, apiKey);
        return service.createSession(dto.getUserId(), dto.getName());
    }

    // Rename session
    @PatchMapping("/sessions/{id}/rename")
    public void renameSession(@PathVariable String id, @RequestParam String name, HttpServletRequest req,
                              @RequestHeader String apiKey) {
        ragUtils.checkApiKey(req, apiKey);
        service.renameSession(id, name);
    }

    // Mark/unmark as favorite
    @PatchMapping("/sessions/{id}/favorite")
    public void markFavorite(@PathVariable String id, @RequestParam boolean favorite, HttpServletRequest req,
                             @RequestHeader String apiKey) {
        ragUtils.checkApiKey(req, apiKey);
        service.markFavorite(id, favorite);
    }

    // Delete session
    @DeleteMapping("/sessions/{id}")
    public void deleteSession(@PathVariable String id, HttpServletRequest req,
                              @RequestHeader String apiKey) {
        ragUtils.checkApiKey(req, apiKey);
        service.deleteSession(id);
    }

    // Add message
    @PostMapping("/sessions/{id}/messages")
    public ChatMessage addMessage(@PathVariable String id, @RequestBody ChatMessageDTO dto, HttpServletRequest req,
                                  @RequestHeader String apiKey) {
        ragUtils.checkApiKey(req, apiKey);
        return service.addMessage(id, dto.getSender(), dto.getContent(), dto.getContext());
    }

    // Retrieve messages (pagination)
    @GetMapping("/sessions/messages")
    public List<ChatMessage> getMessages(
            @RequestParam String id//,
            //HttpServletRequest req,
            //@RequestHeader String apiKey
    ) {
        //ragUtils.checkApiKey(req, apiKey);
        System.out.println("inside controller for get api");
        return service.getMessages(id, 0, 20);
    }

    // Health check
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}