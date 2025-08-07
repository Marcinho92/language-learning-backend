package com.example.languagelearning.dto;

public class TranslationVerificationResponse {
    private boolean isCorrect;
    private String correctTranslation;
    private String feedback;
    private String explanation;

    public TranslationVerificationResponse() {}

    public TranslationVerificationResponse(boolean isCorrect, String correctTranslation, String feedback, String explanation) {
        this.isCorrect = isCorrect;
        this.correctTranslation = correctTranslation;
        this.feedback = feedback;
        this.explanation = explanation;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public String getCorrectTranslation() {
        return correctTranslation;
    }

    public void setCorrectTranslation(String correctTranslation) {
        this.correctTranslation = correctTranslation;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
} 