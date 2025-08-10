package com.example.languagelearning.dto;

public record TranslationVerificationRequest(
    String sourceLanguage,
    String sourceText,
    String targetLanguage,
    String userTranslation
) {} 