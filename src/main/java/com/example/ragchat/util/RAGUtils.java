package com.example.ragchat.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class RAGUtils {

    @Value("${api.key}")
    private String apiKey;

    // API Key Auth
    public void checkApiKey(HttpServletRequest request, String apiKey) {
        String header = request.getHeader("X-API-KEY");
        if (header == null || !header.equals(apiKey)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API Key");
        }
    }
}
