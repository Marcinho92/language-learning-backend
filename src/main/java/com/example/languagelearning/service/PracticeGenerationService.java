package com.example.languagelearning.service;

import com.example.languagelearning.dto.PracticeGenerationRequest;
import com.example.languagelearning.dto.PracticeGenerationResponse;
import com.example.languagelearning.dto.TranslationVerificationRequest;
import com.example.languagelearning.dto.TranslationVerificationResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PracticeGenerationService {
    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public PracticeGenerationService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @org.springframework.cache.annotation.Cacheable(value = "ai-responses", key = "'practice_' + #request.sourceLanguage() + '_' + #request.targetLanguage() + '_' + #request.level() + '_' + #request.sentenceCount() + '_' + #request.topic()")
    public PracticeGenerationResponse generatePracticeText(PracticeGenerationRequest request) {
        String prompt = buildPrompt(request);
        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        return new PracticeGenerationResponse(aiResponse);
    }

    @org.springframework.cache.annotation.Cacheable(value = "ai-responses", key = "'verify_' + #request.sourceLanguage() + '_' + #request.sourceText() + '_' + #request.targetLanguage() + '_' + #request.userTranslation()")
    public TranslationVerificationResponse verifyTranslation(TranslationVerificationRequest request) {
        String prompt = buildVerificationPrompt(request);
        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        return parseVerificationResponse(aiResponse, request);
    }

    private String buildPrompt(PracticeGenerationRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("Wygeneruj tekst do ćwiczenia tłumaczenia dla użytkownika.");
        sb.append(" Język tekstu: ").append(request.sourceLanguage()).append(".");
        sb.append(" Użytkownik ma przetłumaczyć tekst na: ").append(request.targetLanguage()).append(".");
        sb.append(" Poziom trudności: ").append(request.level()).append(".");
        sb.append(" Długość tekstu: ").append(request.sentenceCount()).append(" zdań.");
        if (request.topic() != null && !request.topic().isBlank()) {
            sb.append(" Temat: ").append(request.topic()).append(".");
        }
        sb.append(" Tekst powinien być naturalny, spójny i odpowiedni do poziomu.");
        return sb.toString();
    }

    private String buildVerificationPrompt(TranslationVerificationRequest request) {
        return String.format("""
            Sprawdź tłumaczenie z języka %s na język %s.
            
            Oryginalny tekst: "%s"
            Tłumaczenie użytkownika: "%s"
            
            Zwróć odpowiedź w formacie JSON:
            {
                "isCorrect": true/false,
                "correctTranslation": "poprawne tłumaczenie",
                "feedback": "krótka informacja zwrotna",
                "explanation": "wyjaśnienie błędów lub pochwała"
            }
            
            Oceń czy tłumaczenie jest poprawne, naturalne i oddaje sens oryginału.
            """, 
            request.sourceLanguage(), 
            request.targetLanguage(),
            request.sourceText(),
            request.userTranslation()
        );
    }

    private TranslationVerificationResponse parseVerificationResponse(String aiResponse, TranslationVerificationRequest request) {
        try {
            JsonNode root = objectMapper.readTree(aiResponse);
            
            boolean isCorrect = root.has("isCorrect") && root.get("isCorrect").asBoolean(false);
            String aiCorrectTranslation = root.has("correctTranslation") ? root.get("correctTranslation").asText() : request.userTranslation();
            String aiFeedback = root.has("feedback") ? root.get("feedback").asText() : "";
            String explanation = root.has("explanation") ? root.get("explanation").asText() : "";

            String correctTranslation;
            String feedback;
            if (isCorrect) {
                correctTranslation = request.userTranslation();
                feedback = aiFeedback != null && !aiFeedback.isBlank() ? aiFeedback : "Tłumaczenie jest poprawne.";
            } else {
                correctTranslation = aiCorrectTranslation;
                feedback = aiFeedback != null && !aiFeedback.isBlank() ? aiFeedback : "Tłumaczenie jest niepoprawne.";
            }
            
            return new TranslationVerificationResponse(isCorrect, correctTranslation, feedback, explanation);
        } catch (Exception e) {
            // Fallback w przypadku błędu parsowania JSON
            return new TranslationVerificationResponse(
                false, 
                "Nie udało się zweryfikować tłumaczenia", 
                "Błąd weryfikacji", 
                "Spróbuj ponownie lub sprawdź poprawność tłumaczenia samodzielnie."
            );
        }
    }
}