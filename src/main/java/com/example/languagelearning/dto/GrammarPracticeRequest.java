package com.example.languagelearning.dto;

public record GrammarPracticeRequest(
    Long wordId,
    String userSentence,
    String grammarTopic
) {} 