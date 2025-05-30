package com.example.languagelearning.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TranslationCheckRequest {
    @NotBlank(message = "Original word cannot be empty")
    private String originalWord;
    
    @NotBlank(message = "Translation cannot be empty")
    private String translation;
} 