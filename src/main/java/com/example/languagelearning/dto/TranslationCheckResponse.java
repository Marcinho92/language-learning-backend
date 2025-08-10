package com.example.languagelearning.dto;

public record TranslationCheckResponse(
    boolean correct,
    String correctTranslation,
    String exampleUsage,
    String explanation,
    String message
) {} 