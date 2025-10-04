package com.example.ragchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatMessageDTO {

    @NotBlank(message = "Sender is required and cannot be empty")
    @Size(min = 1, max = 50, message = "Sender must be between 1 and 50 characters")
    private String sender;

    @NotBlank(message = "Content is required and cannot be empty")
    @Size(min = 1, max = 10000, message = "Content must be between 1 and 10000 characters")
    private String content;

    @Size(max = 50000, message = "Context cannot exceed 50000 characters")
    private String context; // Optional field
}