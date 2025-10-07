package com.example.ragchat.service;

import com.example.ragchat.exception.LLMException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @Value("${llm.api.url}")
    private String apiUrl;

    @Value("${llm.model}")
    private String model;

    @Value("${llm.max.tokens:1000}")
    private int maxTokens;

    @Value("${llm.temperature:0.7}")
    private double temperature;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public LLMService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Send a query to LLM and get a response
     */
    public String query(String userMessage, String context) {
        log.info("Sending query to LLM: {}", userMessage);
        log.debug("API URL: {}", apiUrl);

        // Validate configuration
        if (apiUrl == null || apiUrl.isEmpty() || !apiUrl.startsWith("http")) {
            throw new LLMException("Invalid LLM_API_URL configuration. Current value: " + apiUrl);
        }

        if (apiKey == null || apiKey.isEmpty()) {
            throw new LLMException("LLM_API_KEY is not configured");
        }

        try {
            // Detect API type and use appropriate format
            if (isOpenAICompatible()) {
                return queryOpenAIFormat(userMessage, context);
            } else {
                return queryHuggingFaceFormat(userMessage, context);
            }
        } catch (Exception e) {
            log.error("Error querying LLM: {}", e.getMessage(), e);
            throw new LLMException("Failed to get response from LLM: " + e.getMessage());
        }
    }

    /**
     * Check if API uses OpenAI-compatible format (Groq, Together AI, etc.)
     */
    private boolean isOpenAICompatible() {
        return apiUrl.contains("openai") ||
                apiUrl.contains("groq.com") ||
                apiUrl.contains("together.xyz") ||
                apiUrl.contains("chat/completions");
    }

    /**
     * Query using OpenAI-compatible format (Groq, Together AI, etc.)
     */
    private String queryOpenAIFormat(String userMessage, String context) {
        log.info("Using OpenAI-compatible format");

        Map<String, Object> request = new HashMap<>();
        request.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();

        // System message
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "You are a helpful AI assistant.");
        messages.add(systemMsg);

        // Context if available
        if (context != null && !context.trim().isEmpty()) {
            Map<String, String> contextMsg = new HashMap<>();
            contextMsg.put("role", "system");
            contextMsg.put("content", "Context: " + context);
            messages.add(contextMsg);
        }

        // User message
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        request.put("messages", messages);
        request.put("max_tokens", maxTokens);
        request.put("temperature", temperature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        return parseOpenAIResponse(response.getBody());
    }

    /**
     * Query using Hugging Face format
     */
    private String queryHuggingFaceFormat(String userMessage, String context) {
        log.info("Using Hugging Face format");

        String prompt = buildHuggingFacePrompt(userMessage, context);

        Map<String, Object> request = new HashMap<>();
        request.put("inputs", prompt);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("max_new_tokens", maxTokens);
        parameters.put("temperature", temperature);
        parameters.put("return_full_text", false);
        request.put("parameters", parameters);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        return parseHuggingFaceResponse(response.getBody());
    }

    /**
     * Build prompt for Hugging Face
     */
    private String buildHuggingFacePrompt(String userMessage, String context) {
        StringBuilder prompt = new StringBuilder();

        if (context != null && !context.trim().isEmpty()) {
            prompt.append("Context: ").append(context).append("\n\n");
        }

        prompt.append("Question: ").append(userMessage).append("\n");
        prompt.append("Answer:");

        return prompt.toString();
    }

    /**
     * Parse OpenAI-compatible response
     */
    private String parseOpenAIResponse(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.has("choices") && jsonNode.get("choices").isArray()) {
                JsonNode choices = jsonNode.get("choices");
                if (choices.size() > 0) {
                    JsonNode message = choices.get(0).get("message");
                    if (message != null && message.has("content")) {
                        return message.get("content").asText().trim();
                    }
                }
            }

            if (jsonNode.has("error")) {
                String error = jsonNode.get("error").asText();
                throw new LLMException("API error: " + error);
            }

            throw new LLMException("Unexpected response format");

        } catch (Exception e) {
            log.error("Error parsing response: {}", e.getMessage());
            throw new LLMException("Failed to parse response: " + e.getMessage());
        }
    }

    /**
     * Parse Hugging Face response
     */
    private String parseHuggingFaceResponse(String responseBody) {
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);

            if (jsonNode.isArray() && jsonNode.size() > 0) {
                JsonNode firstResult = jsonNode.get(0);
                if (firstResult.has("generated_text")) {
                    return firstResult.get("generated_text").asText().trim();
                }
            }

            if (jsonNode.has("error")) {
                String error = jsonNode.get("error").asText();
                throw new LLMException("Hugging Face API error: " + error);
            }

            throw new LLMException("Unexpected response format from Hugging Face");

        } catch (Exception e) {
            log.error("Error parsing Hugging Face response: {}", e.getMessage());
            throw new LLMException("Failed to parse response: " + e.getMessage());
        }
    }

    /**
     * Query with conversation history
     */
    public String queryWithHistory(String userMessage, String context,
                                   List<Map<String, String>> conversationHistory) {
        log.info("Sending query with conversation history");

        try {
            if (isOpenAICompatible()) {
                return queryOpenAIFormatWithHistory(userMessage, context, conversationHistory);
            } else {
                return queryHuggingFaceFormatWithHistory(userMessage, context, conversationHistory);
            }
        } catch (Exception e) {
            log.error("Error querying LLM with history: {}", e.getMessage(), e);
            throw new LLMException("Failed to get response: " + e.getMessage());
        }
    }

    private String queryOpenAIFormatWithHistory(String userMessage, String context,
                                                List<Map<String, String>> conversationHistory) {
        Map<String, Object> request = new HashMap<>();
        request.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();

        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", "You are a helpful AI assistant.");
        messages.add(systemMsg);

        if (context != null && !context.trim().isEmpty()) {
            Map<String, String> contextMsg = new HashMap<>();
            contextMsg.put("role", "system");
            contextMsg.put("content", "Context: " + context);
            messages.add(contextMsg);
        }

        if (conversationHistory != null) {
            messages.addAll(conversationHistory);
        }

        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);

        request.put("messages", messages);
        request.put("max_tokens", maxTokens);
        request.put("temperature", temperature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        return parseOpenAIResponse(response.getBody());
    }

    private String queryHuggingFaceFormatWithHistory(String userMessage, String context,
                                                     List<Map<String, String>> conversationHistory) {
        StringBuilder prompt = new StringBuilder();

        if (context != null && !context.trim().isEmpty()) {
            prompt.append("Context: ").append(context).append("\n\n");
        }

        prompt.append("Conversation:\n");
        if (conversationHistory != null) {
            for (Map<String, String> msg : conversationHistory) {
                String role = msg.get("role");
                String content = msg.get("content");
                prompt.append(role).append(": ").append(content).append("\n");
            }
        }

        prompt.append("user: ").append(userMessage).append("\n");
        prompt.append("assistant:");

        Map<String, Object> request = new HashMap<>();
        request.put("inputs", prompt.toString());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("max_new_tokens", maxTokens);
        parameters.put("temperature", temperature);
        parameters.put("return_full_text", false);
        request.put("parameters", parameters);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        return parseHuggingFaceResponse(response.getBody());
    }
}