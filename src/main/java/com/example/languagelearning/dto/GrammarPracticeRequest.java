package com.example.languagelearning.dto;

public class GrammarPracticeRequest {
    private Long wordId;
    private String userSentence;
    private String grammarTopic;

    // Constructors
    public GrammarPracticeRequest() {}

    public GrammarPracticeRequest(Long wordId, String userSentence, String grammarTopic) {
        this.wordId = wordId;
        this.userSentence = userSentence;
        this.grammarTopic = grammarTopic;
    }

    // Getters and Setters
    public Long getWordId() {
        return wordId;
    }

    public void setWordId(Long wordId) {
        this.wordId = wordId;
    }

    public String getUserSentence() {
        return userSentence;
    }

    public void setUserSentence(String userSentence) {
        this.userSentence = userSentence;
    }

    public String getGrammarTopic() {
        return grammarTopic;
    }

    public void setGrammarTopic(String grammarTopic) {
        this.grammarTopic = grammarTopic;
    }
} 