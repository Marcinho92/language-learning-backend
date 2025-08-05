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
        log.info("AiGrammarValidationService initialized with promptsConfig: {}", promptsConfig);
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
        String mainPrompt = promptsConfig.getMainPrompt();
        if (mainPrompt == null) {
            log.error("Main prompt is null! Configuration not loaded properly.");
            // Fallback to hardcoded prompt
            return String.format("""
                    You are an English grammar teacher. Your task is to validate if a student's sentence is correct according to the given grammar topic and contains the required word.
                    
                    Student's sentence: "%s"
                    Required word to use: "%s" (translation: "%s")
                    Grammar topic: "%s"
                    
                    Please analyze the sentence and respond in the following JSON format:
                    {
                        "isCorrect": true/false,
                        "feedback": "Brief feedback about the sentence",
                        "correction": "Corrected version of the sentence (if incorrect)",
                        "explanation": "Detailed explanation of the grammar rules applied"
                    }
                    
                    Rules:
                    1. Check if the sentence contains the required word (either original or translation)
                    2. Check if the sentence follows the grammar topic rules
                    3. Provide helpful feedback for improvement
                    4. If incorrect, provide a corrected version
                    5. Give a brief explanation of the grammar rules
                    
                    Grammar topics and their rules:
                    - Present Simple: Subject + base verb (add 's' for 3rd person singular)
                    - Present Continuous: Subject + be (am/is/are) + verb + ing
                    - Past Simple: Subject + past form of verb
                    - Past Continuous: Subject + was/were + verb + ing
                    - Present Perfect: Subject + have/has + past participle
                    - Past Perfect: Subject + had + past participle
                    - Future Simple: Subject + will + base verb
                    - First Conditional: If + present simple, will + base verb
                    - Second Conditional: If + past simple, would + base verb
                    - Third Conditional: If + past perfect, would have + past participle
                    - Passive Voice: Subject + be + past participle
                    - Modal Verbs: Subject + modal verb + base verb
                    - Gerunds and Infinitives: verb + ing or to + base verb
                    - Relative Clauses: Noun + relative pronoun + clause
                    - Reported Speech: Subject + reporting verb + that + reported clause
                    
                    Respond only with valid JSON.
                    """, userSentence, word.getOriginalWord(), word.getTranslation(), grammarTopic);
        }
        
        log.debug("Using main prompt from configuration");
        return String.format(mainPrompt, 
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