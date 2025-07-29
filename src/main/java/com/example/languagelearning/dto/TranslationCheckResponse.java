package com.example.languagelearning.dto;

import lombok.Data;

@Data
public class TranslationCheckResponse {
    private boolean correct;
    private String correctTranslation;
    private String exampleUsage;
    private String explanation;
    private String message;
} 