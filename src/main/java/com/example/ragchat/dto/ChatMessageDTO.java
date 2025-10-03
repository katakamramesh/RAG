package com.example.ragchat.dto;

import lombok.Data;

@Data
public class ChatMessageDTO {
    private String sender;
    private String content;
    private String context;
}