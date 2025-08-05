package com.example.languagelearning.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "ai.grammar.validation")
public class AiPromptsConfig {
    
    private String mainPrompt;
    private Map<String, String> grammarExplanations;
    
    public String getGrammarExplanation(String grammarTopic) {
        String key = grammarTopic.toLowerCase().replace(" ", "-");
        return grammarExplanations.getOrDefault(key, grammarExplanations.get("default"));
    }
} 