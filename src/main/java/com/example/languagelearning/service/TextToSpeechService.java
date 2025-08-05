package com.example.languagelearning.service;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.audio.CreateSpeechRequest;
import com.theokanning.openai.audio.CreateSpeechRequest.Model;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class TextToSpeechService {

    private final OpenAiService openAiService;
    private final Map<String, String> languageToVoiceMap = new HashMap<>();

    public TextToSpeechService(@Value("${openai.api.key:}") String apiKey) {
        this.openAiService = new OpenAiService(apiKey, Duration.ofSeconds(60));
        
        // Mapowanie języków na głosy OpenAI
        languageToVoiceMap.put("en", "alloy");
        languageToVoiceMap.put("pl", "alloy");
        languageToVoiceMap.put("es", "alloy");
        languageToVoiceMap.put("fr", "alloy");
        languageToVoiceMap.put("de", "alloy");
        languageToVoiceMap.put("it", "alloy");
        languageToVoiceMap.put("pt", "alloy");
        languageToVoiceMap.put("ru", "alloy");
        languageToVoiceMap.put("ja", "alloy");
        languageToVoiceMap.put("ko", "alloy");
        languageToVoiceMap.put("zh", "alloy");
    }

    public String generateAudioBase64(String text, String language) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        try {
            String voice = languageToVoiceMap.getOrDefault(language.toLowerCase(), "alloy");
            
            log.info("Generating audio for text: '{}' in language: '{}' with voice: '{}'", text, language, voice);
            
            CreateSpeechRequest request = CreateSpeechRequest.builder()
                    .model(Model.TTS_1)
                    .input(text)
                    .voice(com.theokanning.openai.audio.CreateSpeechRequest.Voice.valueOf(voice.toUpperCase()))
                    .responseFormat("mp3")
                    .speed(1.0)
                    .build();

            byte[] audioBytes = openAiService.createSpeech(request).getBody().bytes();
            
            // Convert to Base64
            String base64Audio = java.util.Base64.getEncoder().encodeToString(audioBytes);
            
            log.info("Successfully generated audio for text: '{}'", text);
            return base64Audio;
            
        } catch (Exception e) {
            log.error("Error generating audio for text: {}", text, e);
            return null;
        }
    }
} 