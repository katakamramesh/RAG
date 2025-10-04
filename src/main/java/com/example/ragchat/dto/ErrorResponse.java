// ===================================================================
// FILE: ErrorResponse.java
// Path: src/main/java/com/example/ragchat/dto/ErrorResponse.java
// ===================================================================
package com.example.ragchat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}