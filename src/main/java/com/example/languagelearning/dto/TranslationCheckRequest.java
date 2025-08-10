package com.example.languagelearning.dto;

import jakarta.validation.constraints.NotBlank;

public record TranslationCheckRequest(
    @NotBlank(message = "Original word cannot be empty")
    String originalWord,
    
    @NotBlank(message = "Translation cannot be empty")
    String translation
) {} 