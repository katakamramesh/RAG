package com.example.ragchat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatMessageDTO {
    private String sender;
    private String content;
    private String context;
}