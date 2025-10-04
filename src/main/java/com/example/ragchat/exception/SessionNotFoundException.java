package com.example.ragchat.exception;

public class SessionNotFoundException extends RuntimeException {
    public SessionNotFoundException(String sessionId) {
        super("Session not found with ID: " + sessionId);
    }
}