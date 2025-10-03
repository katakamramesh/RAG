package com.example.ragchat.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "chat_messages")
public class ChatMessage {
    @Id
    private String id;
    private String sessionId;
    private String sender;
    private String content;
    private String context; // optional
    private Date timestamp;
}