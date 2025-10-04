package com.example.ragchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatSessionDTO {

    @NotBlank(message = "User ID is required and cannot be empty")
    @Size(min = 1, max = 100, message = "User ID must be between 1 and 100 characters")
    private String userId;

    @NotBlank(message = "Session name is required and cannot be empty")
    @Size(min = 1, max = 200, message = "Session name must be between 1 and 200 characters")
    private String name;
}