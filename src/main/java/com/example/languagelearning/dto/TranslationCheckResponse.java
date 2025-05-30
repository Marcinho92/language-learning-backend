package com.example.languagelearning.dto;

import lombok.Data;

@Data
public class TranslationCheckResponse {
    private final boolean correct;
    private final String message;

    public static TranslationCheckResponse correct() {
        return new TranslationCheckResponse(true, "Translation is correct!");
    }

    public static TranslationCheckResponse incorrect() {
        return new TranslationCheckResponse(false, "Translation is incorrect. Try again!");
    }
} 