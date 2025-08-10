package com.example.languagelearning.service;

import com.theokanning.openai.service.OpenAiService;
import com.theokanning.openai.audio.CreateSpeechRequest;

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
            log.warn("Text is null or empty, cannot generate audio");
            return null;
        }

        try {
            String voice = languageToVoiceMap.getOrDefault(language.toLowerCase(), "alloy");
            
            log.info("Generating audio for text: '{}' in language: '{}' with voice: '{}'", text, language, voice);
            
            CreateSpeechRequest request = CreateSpeechRequest.builder()
                    .model("tts-1")
                    .input(text)
                    .voice(voice)
                    .responseFormat("mp3")
                    .speed(1.0)
                    .build();

            log.info("Sending request to OpenAI TTS API...");
            byte[] audioBytes = openAiService.createSpeech(request).bytes();
            log.info("Received {} bytes from OpenAI TTS API", audioBytes.length);
            
            // Convert to Base64
            String base64Audio = java.util.Base64.getEncoder().encodeToString(audioBytes);
            
            log.info("Successfully generated audio for text: '{}', Base64 length: {}", text, base64Audio.length());
            return base64Audio;
            
        } catch (Exception e) {
            log.error("Error generating audio for text: '{}' in language: '{}'", text, language, e);
            return null;
        }
    }
} 