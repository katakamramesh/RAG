package com.example.ragchat.service;

import com.example.ragchat.dto.LLMRequest;
import com.example.ragchat.dto.LLMResponse;
import com.example.ragchat.exception.LLMException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
@Slf4j
public class LLMService {

    @Value("${llm.api.key}")
    private String apiKey;

    @Value("${llm.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    @Value("${llm.model:gpt-3.5-turbo}")
    private String model;

    @Value("${llm.max.tokens:1000}")
    private int maxTokens;

    @Value("${llm.temperature:0.7}")
    private double temperature;

    private final RestTemplate restTemplate;

    public LLMService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Send a query to the LLM and get a response
     */
    public String query(String userMessage, String context) {
        log.info("Sending query to LLM: {}", userMessage);

        try {
            // Build the request
            LLMRequest request = buildRequest(userMessage, context);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<LLMRequest> entity = new HttpEntity<>(request, headers);

            // Make API call
            ResponseEntity<LLMResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    LLMResponse.class
            );

            // Extract and return the response
            if (response.getBody() != null && 
                response.getBody().getChoices() != null && 
                !response.getBody().getChoices().isEmpty()) {
                
                String content = response.getBody().getChoices().get(0)
                        .getMessage().getContent();
                
                log.info("LLM response received successfully");
                return content;
            }

            throw new LLMException("Empty response from LLM");

        } catch (Exception e) {
            log.error("Error querying LLM: {}", e.getMessage(), e);
            throw new LLMException("Failed to get response from LLM: " + e.getMessage());
        }
    }

    /**
     * Build the LLM request with messages
     */
    private LLMRequest buildRequest(String userMessage, String context) {
        List<Map<String, String>> messages = new ArrayList<>();

        // System message with instructions
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", 
            "You are a helpful AI assistant. Use the provided context to answer questions accurately. " +
            "If the context doesn't contain relevant information, say so politely and provide a general answer.");
        messages.add(systemMessage);

        // Add context if available
        if (context != null && !context.trim().isEmpty()) {
            Map<String, String> contextMessage = new HashMap<>();
            contextMessage.put("role", "system");
            contextMessage.put("content", "Context: " + context);
            messages.add(contextMessage);
        }

        // User message
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        LLMRequest request = new LLMRequest();
        request.setModel(model);
        request.setMessages(messages);
        request.setMaxTokens(maxTokens);
        request.setTemperature(temperature);

        return request;
    }

    /**
     * Query with conversation history
     */
    public String queryWithHistory(String userMessage, String context, 
                                   List<Map<String, String>> conversationHistory) {
        log.info("Sending query to LLM with conversation history");

        try {
            LLMRequest request = new LLMRequest();
            request.setModel(model);
            request.setMaxTokens(maxTokens);
            request.setTemperature(temperature);

            List<Map<String, String>> messages = new ArrayList<>();

            // System message
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", 
                "You are a helpful AI assistant. Use the provided context and conversation history " +
                "to provide relevant and accurate answers.");
            messages.add(systemMessage);

            // Add context
            if (context != null && !context.trim().isEmpty()) {
                Map<String, String> contextMessage = new HashMap<>();
                contextMessage.put("role", "system");
                contextMessage.put("content", "Context: " + context);
                messages.add(contextMessage);
            }

            // Add conversation history
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                messages.addAll(conversationHistory);
            }

            // Current user message
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);

            request.setMessages(messages);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<LLMRequest> entity = new HttpEntity<>(request, headers);

            // Make API call
            ResponseEntity<LLMResponse> response = restTemplate.exchange(
                    apiUrl,
                    HttpMethod.POST,
                    entity,
                    LLMResponse.class
            );

            if (response.getBody() != null && 
                response.getBody().getChoices() != null && 
                !response.getBody().getChoices().isEmpty()) {
                
                String content = response.getBody().getChoices().get(0)
                        .getMessage().getContent();
                
                log.info("LLM response with history received successfully");
                return content;
            }

            throw new LLMException("Empty response from LLM");

        } catch (Exception e) {
            log.error("Error querying LLM with history: {}", e.getMessage(), e);
            throw new LLMException("Failed to get response from LLM: " + e.getMessage());
        }
    }
}
