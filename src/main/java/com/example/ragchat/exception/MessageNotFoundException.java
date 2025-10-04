package com.example.ragchat.exception;

public class MessageNotFoundException extends RuntimeException {
    public MessageNotFoundException(String messageId) {
        super("Message not found with ID: " + messageId);
    }
}