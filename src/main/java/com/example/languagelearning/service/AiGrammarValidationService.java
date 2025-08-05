package com.example.languagelearning.service;

import com.example.languagelearning.config.AiPromptsConfig;
import com.example.languagelearning.model.Word;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class AiGrammarValidationService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AiPromptsConfig promptsConfig;

    @Autowired
    public AiGrammarValidationService(ChatClient.Builder chatClientBuilder, AiPromptsConfig promptsConfig) {
        this.chatClient = chatClientBuilder.build();
        this.promptsConfig = promptsConfig;
    }

    public GrammarValidationResult validateSentence(String userSentence, Word word, String grammarTopic) {
        log.info("Validating sentence: '{}' for word: '{}' with grammar topic: '{}'", userSentence, word.getOriginalWord(), grammarTopic);

        try {
            String prompt = buildValidationPrompt(userSentence, word, grammarTopic);

            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return parseAiResponse(aiResponse, userSentence, word, grammarTopic);
        } catch (Exception e) {
            log.error("Error validating sentence with AI", e);
            return new GrammarValidationResult(false,
                    "Error validating sentence. Please try again.",
                    null,
                    "AI validation service is temporarily unavailable.");
        }
    }

    private String buildValidationPrompt(String userSentence, Word word, String grammarTopic) {
        return String.format(promptsConfig.getMainPrompt(), 
                userSentence, word.getOriginalWord(), word.getTranslation(), grammarTopic);
    }

    private GrammarValidationResult parseAiResponse(String aiResponse, String userSentence, Word word, String grammarTopic) {
        try {
            JsonNode root = objectMapper.readTree(aiResponse);

            boolean isCorrect = root.has("isCorrect") && root.get("isCorrect").asBoolean(false);
            String feedback = root.has("feedback") ? root.get("feedback").asText(null) : null;
            String correction = root.has("correction") ? root.get("correction").asText(null) : userSentence;
            String explanation = root.has("explanation") ? root.get("explanation").asText(null) : generateGrammarExplanation(grammarTopic);

            // Fallback: feedback na podstawie isCorrect
            if (feedback == null || feedback.isBlank() ||
                    (isCorrect && feedback.toLowerCase().contains("incorrect")) ||
                    (!isCorrect && feedback.toLowerCase().contains("correct"))) {
                feedback = isCorrect ? "Great job! Your sentence is correct." : "Your sentence needs improvement.";
            }

            return new GrammarValidationResult(isCorrect, feedback, correction, explanation);
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", aiResponse, e);
            return new GrammarValidationResult(false,
                    "Error processing AI response. Please try again.",
                    null,
                    generateGrammarExplanation(grammarTopic));
        }
    }

    private String generateGrammarExplanation(String grammarTopic) {
        return promptsConfig.getGrammarExplanation(grammarTopic);
    }

    public record GrammarValidationResult(
            boolean isCorrect,
            String feedback,
            String correction,
            String explanation) {
    }
} 