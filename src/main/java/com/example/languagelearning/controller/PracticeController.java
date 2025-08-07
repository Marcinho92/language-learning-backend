package com.example.languagelearning.controller;

import com.example.languagelearning.dto.PracticeGenerationRequest;
import com.example.languagelearning.dto.PracticeGenerationResponse;
import com.example.languagelearning.service.PracticeGenerationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practice")
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
}