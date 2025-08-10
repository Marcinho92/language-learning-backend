package com.example.languagelearning.dto;

public record TranslationVerificationResponse(
    boolean isCorrect,
    String correctTranslation,
    String feedback,
    String explanation
) {} 