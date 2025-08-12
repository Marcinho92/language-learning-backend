package com.example.languagelearning.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Data
@Slf4j
@Component
@ConfigurationProperties(prefix = "ai.grammar.validation")
public class AiPromptsConfig {

    @PostConstruct
    public void init() {
        log.info("AiPromptsConfig initialized with mainPrompt length: {}",
                mainPrompt != null ? mainPrompt.length() : "null");
        log.info("AiPromptsConfig initialized with grammarExplanations size: {}",
                grammarExplanations != null ? grammarExplanations.size() : "null");
    }

    private String mainPrompt;
    private Map<String, String> grammarExplanations;

    public String getGrammarExplanation(String grammarTopic) {
        if (grammarExplanations == null) {
            log.error("Grammar explanations map is null! Configuration not loaded properly.");
            return "Practice using this grammar structure in your sentences.\n\nMake sure to use the given word in your sentence and apply the grammar topic correctly.";
        }

        String key = grammarTopic.toLowerCase().replace(" ", "-");
        String explanation = grammarExplanations.getOrDefault(key, grammarExplanations.get("default"));

        if (explanation == null) {
            log.warn("No grammar explanation found for topic: '{}' (key: '{}')", grammarTopic, key);
            return "Practice using this grammar structure in your sentences.\n\nMake sure to use the given word in your sentence and apply the grammar topic correctly.";
        }

        return explanation;
    }

} 