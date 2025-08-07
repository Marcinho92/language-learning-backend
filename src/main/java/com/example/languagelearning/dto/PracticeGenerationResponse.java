package com.example.languagelearning.dto;

public class PracticeGenerationResponse {
    private String exerciseText;

    public PracticeGenerationResponse() {}

    public PracticeGenerationResponse(String exerciseText) {
        this.exerciseText = exerciseText;
    }

    public String getExerciseText() {
        return exerciseText;
    }

    public void setExerciseText(String exerciseText) {
        this.exerciseText = exerciseText;
    }
}