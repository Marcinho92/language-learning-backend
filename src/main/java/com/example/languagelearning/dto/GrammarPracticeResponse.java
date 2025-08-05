package com.example.languagelearning.dto;

import com.example.languagelearning.model.Word;

public class GrammarPracticeResponse {
    private Word word;
    private String grammarTopic;
    private boolean isCorrect;
    private String feedback;
    private String correction;
    private String explanation;

    // Constructors
    public GrammarPracticeResponse() {}

    public GrammarPracticeResponse(Word word, String grammarTopic) {
        this.word = word;
        this.grammarTopic = grammarTopic;
    }

    public GrammarPracticeResponse(Word word, String grammarTopic, boolean isCorrect, String feedback, String correction, String explanation) {
        this.word = word;
        this.grammarTopic = grammarTopic;
        this.isCorrect = isCorrect;
        this.feedback = feedback;
        this.correction = correction;
        this.explanation = explanation;
    }

    // Getters and Setters
    public Word getWord() {
        return word;
    }

    public void setWord(Word word) {
        this.word = word;
    }

    public String getGrammarTopic() {
        return grammarTopic;
    }

    public void setGrammarTopic(String grammarTopic) {
        this.grammarTopic = grammarTopic;
    }

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getCorrection() {
        return correction;
    }

    public void setCorrection(String correction) {
        this.correction = correction;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
} 