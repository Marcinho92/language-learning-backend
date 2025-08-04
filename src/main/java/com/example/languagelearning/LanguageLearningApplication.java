package com.example.languagelearning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LanguageLearningApplication {
    public static void main(String[] args) {
        System.out.println("OPENAI_API_KEY: " + System.getenv("OPENAI_API_KEY"));
        SpringApplication.run(LanguageLearningApplication.class, args);
    }
} 