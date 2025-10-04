package com.example.ragchat.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    @Value("${api.keys}")
    private String apiKeys;

    // Paths that don't require API key authentication
    private static final List<String> EXCLUDED_PATHS = Arrays.asList(
            "/api/health",
            "/swagger-ui",
            "/v3/api-docs",
            "/swagger-resources",
            "/webjars"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Skip authentication for excluded paths
        if (shouldSkipAuthentication(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Get API key from header
        String providedApiKey = request.getHeader("X-API-KEY");

        // Validate API key
        if (providedApiKey == null || providedApiKey.trim().isEmpty()) {
            sendUnauthorizedResponse(response, "Missing API Key");
            return;
        }

        // Check if provided key is in the list of valid keys
        Set<String> validKeys = Arrays.stream(apiKeys.split(","))
                .map(String::trim)
                .collect(Collectors.toSet());

        if (!validKeys.contains(providedApiKey)) {
            sendUnauthorizedResponse(response, "Invalid API Key");
            return;
        }

        // API key is valid, continue with the request
        filterChain.doFilter(request, response);
    }

    /**
     * Check if the request path should skip authentication
     */
    private boolean shouldSkipAuthentication(String requestURI) {
        return EXCLUDED_PATHS.stream()
                .anyMatch(requestURI::startsWith);
    }

    /**
     * Send unauthorized response with proper JSON format
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(String.format("{\"error\": \"%s\", \"status\": 401}", message));
    }
}