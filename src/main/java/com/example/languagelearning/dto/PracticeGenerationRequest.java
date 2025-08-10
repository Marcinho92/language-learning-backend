package com.example.languagelearning.dto;

public record PracticeGenerationRequest(
    String sourceLanguage,
    String targetLanguage,
    String level,
    int sentenceCount,
    String topic
) {}