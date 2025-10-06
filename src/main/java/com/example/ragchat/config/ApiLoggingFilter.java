package com.example.ragchat.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ApiLoggingFilter extends OncePerRequestFilter {

    private static final Logger API_LOGGER = LoggerFactory.getLogger("API_ACCESS_LOGGER");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        
        // Wrap request and response for content caching
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            // Continue with the filter chain
            filterChain.doFilter(wrappedRequest, wrappedResponse);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log API access details
            logApiAccess(wrappedRequest, wrappedResponse, duration);
            
            // Copy response content back to original response
            wrappedResponse.copyBodyToResponse();
        }
    }

    private void logApiAccess(ContentCachingRequestWrapper request,
                             ContentCachingResponseWrapper response,
                             long duration) {
        try {
            String method = request.getMethod();
            String uri = request.getRequestURI();
            String queryString = request.getQueryString();
            int status = response.getStatus();
            String clientIp = getClientIp(request);
            String userAgent = request.getHeader("User-Agent");
            String apiKey = request.getHeader("X-API-KEY");

            // Build log message
            StringBuilder logMessage = new StringBuilder();
            logMessage.append("API_ACCESS | ");
            logMessage.append("Method=").append(method).append(" | ");
            logMessage.append("URI=").append(uri);
            
            if (queryString != null) {
                logMessage.append("?").append(queryString);
            }
            
            logMessage.append(" | Status=").append(status).append(" | ");
            logMessage.append("Duration=").append(duration).append("ms | ");
            logMessage.append("IP=").append(clientIp).append(" | ");
            
            if (apiKey != null && !apiKey.isEmpty()) {
                // Mask API key for security (show first 4 and last 4 chars)
                String maskedKey = maskApiKey(apiKey);
                logMessage.append("APIKey=").append(maskedKey).append(" | ");
            }
            
            if (userAgent != null) {
                logMessage.append("UserAgent=").append(userAgent);
            }

            // Log request body for POST/PUT/PATCH (excluding sensitive data)
            if (shouldLogRequestBody(method)) {
                String requestBody = getRequestBody(request);
                if (requestBody != null && !requestBody.isEmpty()) {
                    logMessage.append(" | RequestBody=").append(requestBody);
                }
            }

            API_LOGGER.info(logMessage.toString());

        } catch (Exception e) {
            API_LOGGER.error("Error logging API access", e);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // Handle multiple IPs in X-Forwarded-For
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "****";
        }
        String firstPart = apiKey.substring(0, 4);
        String lastPart = apiKey.substring(apiKey.length() - 4);
        return firstPart + "****" + lastPart;
    }

    private boolean shouldLogRequestBody(String method) {
        return "POST".equalsIgnoreCase(method) || 
               "PUT".equalsIgnoreCase(method) || 
               "PATCH".equalsIgnoreCase(method);
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        try {
            byte[] content = request.getContentAsByteArray();
            if (content.length > 0) {
                String body = new String(content, StandardCharsets.UTF_8);
                // Limit body length in logs to prevent huge log files
                if (body.length() > 1000) {
                    return body.substring(0, 1000) + "... (truncated)";
                }
                return body;
            }
        } catch (Exception e) {
            API_LOGGER.warn("Could not read request body", e);
        }
        return null;
    }
}