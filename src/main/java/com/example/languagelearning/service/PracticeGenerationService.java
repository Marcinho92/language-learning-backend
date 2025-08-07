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

    public PracticeGenerationResponse generatePracticeText(PracticeGenerationRequest request) {
        String prompt = buildPrompt(request);
        String aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
        return new PracticeGenerationResponse(aiResponse);
    }

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
        sb.append(" Język tekstu: ").append(request.getSourceLanguage()).append(".");
        sb.append(" Użytkownik ma przetłumaczyć tekst na: ").append(request.getTargetLanguage()).append(".");
        sb.append(" Poziom trudności: ").append(request.getLevel()).append(".");
        sb.append(" Długość tekstu: ").append(request.getSentenceCount()).append(" zdań.");
        if (request.getTopic() != null && !request.getTopic().isBlank()) {
            sb.append(" Temat: ").append(request.getTopic()).append(".");
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
            request.getSourceLanguage(), 
            request.getTargetLanguage(),
            request.getSourceText(),
            request.getUserTranslation()
        );
    }

    private TranslationVerificationResponse parseVerificationResponse(String aiResponse, TranslationVerificationRequest request) {
        try {
            JsonNode root = objectMapper.readTree(aiResponse);
            
            boolean isCorrect = root.has("isCorrect") && root.get("isCorrect").asBoolean(false);
            String correctTranslation = root.has("correctTranslation") ? root.get("correctTranslation").asText() : request.getUserTranslation();
            String feedback = root.has("feedback") ? root.get("feedback").asText() : "";
            String explanation = root.has("explanation") ? root.get("explanation").asText() : "";
            
            // Fallback dla poprawnych tłumaczeń
            if (isCorrect && (correctTranslation == null || correctTranslation.isBlank())) {
                correctTranslation = request.getUserTranslation();
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