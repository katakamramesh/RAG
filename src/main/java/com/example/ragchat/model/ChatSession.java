package com.example.ragchat.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "chat_sessions")
public class ChatSession {
    @Id
    private String id;
    private String userId;
    private String name;
    private boolean favorite;
    private Date createdAt;
    private Date updatedAt;
}