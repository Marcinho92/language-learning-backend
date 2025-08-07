package com.example.languagelearning.service;

import com.example.languagelearning.dto.PracticeGenerationRequest;
import com.example.languagelearning.dto.PracticeGenerationResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PracticeGenerationService {
    private final ChatClient chatClient;

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
}