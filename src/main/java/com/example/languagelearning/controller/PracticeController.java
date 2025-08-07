package com.example.languagelearning.controller;

import com.example.languagelearning.dto.PracticeGenerationRequest;
import com.example.languagelearning.dto.PracticeGenerationResponse;
import com.example.languagelearning.dto.TranslationVerificationRequest;
import com.example.languagelearning.dto.TranslationVerificationResponse;
import com.example.languagelearning.service.PracticeGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practice")
@CrossOrigin(origins = {
    "http://localhost:3000", 
    "https://language-learning-frontend.railway.app",
    "https://language-learning-frontend-production.up.railway.app",
    "https://www.langlearn.top",
    "https://langlearn.top"
})
public class PracticeController {
    private final PracticeGenerationService practiceGenerationService;

    @Autowired
    public PracticeController(PracticeGenerationService practiceGenerationService) {
        this.practiceGenerationService = practiceGenerationService;
    }

    @PostMapping("/generate")
    public PracticeGenerationResponse generatePractice(@RequestBody PracticeGenerationRequest request) {
        return practiceGenerationService.generatePracticeText(request);
    }

    @PostMapping("/verify")
    public TranslationVerificationResponse verifyTranslation(@RequestBody TranslationVerificationRequest request) {
        return practiceGenerationService.verifyTranslation(request);
    }
}