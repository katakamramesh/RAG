package com.example.ragchat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatQueryRequest {
    
    @NotBlank(message = "Query is required and cannot be empty")
    @Size(min = 1, max = 5000, message = "Query must be between 1 and 5000 characters")
    private String query;
    
    @Size(max = 50000, message = "Context cannot exceed 50000 characters")
    private String context;
    
    private Boolean includeHistory = true;
}
