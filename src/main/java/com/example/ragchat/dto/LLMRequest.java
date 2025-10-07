// LLMRequest.java
package com.example.ragchat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LLMRequest {
    private String model;
    private List<Map<String, String>> messages;
    
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    
    private Double temperature;
}
