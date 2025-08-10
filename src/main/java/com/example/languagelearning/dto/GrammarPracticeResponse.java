package com.example.languagelearning.dto;

import com.example.languagelearning.model.Word;
import lombok.Data;

@Data
public class GrammarPracticeResponse {
    private Word word;
    private String grammarTopic;
    private boolean isCorrect;
    private String feedback;
    private String correction;
    private String explanation;
    private String audioUrl;

    public GrammarPracticeResponse() {}

    public GrammarPracticeResponse(Word word, String grammarTopic, boolean isCorrect, String feedback, String correction, String explanation, String audioUrl) {
        this.word = word;
        this.grammarTopic = grammarTopic;
        this.isCorrect = isCorrect;
        this.feedback = feedback;
        this.correction = correction;
        this.explanation = explanation;
        this.audioUrl = audioUrl;
    }
} 