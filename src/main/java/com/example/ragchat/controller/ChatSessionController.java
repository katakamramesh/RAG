package com.example.ragchat.controller;

import com.example.ragchat.dto.ChatMessageDTO;
import com.example.ragchat.dto.ChatQueryRequest;
import com.example.ragchat.dto.ChatSessionDTO;
import com.example.ragchat.model.ChatMessage;
import com.example.ragchat.model.ChatSession;
import com.example.ragchat.service.ChatSessionService;
import com.example.ragchat.service.LLMService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class ChatSessionController {

    private final ChatSessionService service;
    private final LLMService llmService;

    /**
     * Create a new chat session
     */
    @PostMapping("/sessions")
    public ResponseEntity<ChatSession> createSession(@Valid @RequestBody ChatSessionDTO dto) {
        log.info("Creating session for userId={}, name={}", dto.getUserId(), dto.getName());
        ChatSession session = service.createSession(dto.getUserId(), dto.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(session);
    }

    /**
     * Get session by ID
     */
    @GetMapping("/sessions/{id}")
    public ResponseEntity<ChatSession> getSession(@PathVariable String id) {
        log.info("Retrieving session {}", id);
        ChatSession session = service.getSessionById(id);
        return ResponseEntity.ok(session);
    }

    /**
     * Get all sessions for a user
     */
    @GetMapping("/sessions")
    public ResponseEntity<List<ChatSession>> getSessionsByUser(@RequestParam String userId) {
        log.info("Retrieving sessions for user {}", userId);
        List<ChatSession> sessions = service.getSessionsByUserId(userId);
        return ResponseEntity.ok(sessions);
    }

    /**
     * Rename a session
     */
    @PatchMapping("/sessions/{id}/rename")
    public ResponseEntity<Void> renameSession(
            @PathVariable String id,
            @RequestParam String name) {
        log.info("Renaming session {} to '{}'", id, name);
        service.renameSession(id, name);
        return ResponseEntity.ok().build();
    }

    /**
     * Mark or unmark session as favorite
     */
    @PatchMapping("/sessions/{id}/favorite")
    public ResponseEntity<Void> markFavorite(
            @PathVariable String id,
            @RequestParam boolean favorite) {
        log.info("Marking session {} as favorite={}", id, favorite);
        service.markFavorite(id, favorite);
        return ResponseEntity.ok().build();
    }

    /**
     * Delete a session
     */
    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable String id) {
        log.info("Deleting session {}", id);
        service.deleteSession(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Add a message to a session (without LLM)
     */
    @PostMapping("/sessions/{id}/messages")
    public ResponseEntity<ChatMessage> addMessage(
            @PathVariable String id,
            @Valid @RequestBody ChatMessageDTO dto) {
        log.info("Adding message to session {}: from {}, content='{}'", id, dto.getSender(), dto.getContent());
        ChatMessage message = service.addMessage(
                id,
                dto.getSender(),
                dto.getContent(),
                dto.getContext()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    /**
     * Query LLM and save both user query and LLM response
     */
    @PostMapping("/sessions/{id}/chat")
    public ResponseEntity<Map<String, Object>> chat(
            @PathVariable String id,
            @Valid @RequestBody ChatQueryRequest request) {

        log.info("Chat query for session {}: '{}'", id, request.getQuery());

        try {
            // Save user message
            ChatMessage userMessage = service.addMessage(
                    id,
                    "user",
                    request.getQuery(),
                    request.getContext()
            );

            // Get conversation history if requested
            String llmResponse;
            if (Boolean.TRUE.equals(request.getIncludeHistory())) {
                List<ChatMessage> history = service.getMessages(id, 0, 10);
                List<Map<String, String>> conversationHistory = history.stream()
                        .map(msg -> {
                            Map<String, String> m = new HashMap<>();
                            m.put("role", "assistant".equals(msg.getSender()) ? "assistant" : "user");
                            m.put("content", msg.getContent());
                            return m;
                        })
                        .collect(Collectors.toList());

                llmResponse = llmService.queryWithHistory(
                        request.getQuery(),
                        request.getContext(),
                        conversationHistory
                );
            } else {
                llmResponse = llmService.query(request.getQuery(), request.getContext());
            }

            // Save LLM response
            ChatMessage assistantMessage = service.addMessage(
                    id,
                    "assistant",
                    llmResponse,
                    null
            );

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("userMessage", userMessage);
            response.put("assistantMessage", assistantMessage);
            response.put("response", llmResponse);

            log.info("Chat completed successfully for session {}", id);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing chat for session {}: {}", id, e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to process chat");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * Simple LLM query without saving to database
     */
    @PostMapping("/llm/query")
    public ResponseEntity<Map<String, String>> queryLLM(
            @Valid @RequestBody ChatQueryRequest request) {

        log.info("Direct LLM query: '{}'", request.getQuery());

        try {
            String response = llmService.query(request.getQuery(), request.getContext());

            Map<String, String> result = new HashMap<>();
            result.put("query", request.getQuery());
            result.put("response", response);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error querying LLM: {}", e.getMessage(), e);

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to query LLM");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * Retrieve messages for a session with pagination
     */
    @GetMapping("/sessions/{id}/messages")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int skip,
            @RequestParam(defaultValue = "20") int limit) {
        log.info("Retrieving messages for session {} (skip={}, limit={})", id, skip, limit);
        List<ChatMessage> messages = service.getMessages(id, skip, limit);
        return ResponseEntity.ok(messages);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.debug("Health check endpoint called");
        return ResponseEntity.ok("OK");
    }
}