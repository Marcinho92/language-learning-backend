package com.example.languagelearning.controller;

import com.example.languagelearning.model.Word;
import com.example.languagelearning.service.WordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {
    private final WordService wordService;

    @GetMapping
    public ResponseEntity<List<Word>> getAllWords(Authentication authentication) {
        return ResponseEntity.ok(wordService.getAllWords(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Word> getWord(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(wordService.getWord(id, authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<Word> createWord(@Valid @RequestBody Word word, Authentication authentication) {
        log.info("Received request to create word: {}", word);
        try {
            Word createdWord = wordService.createWord(word, authentication.getName());
            log.info("Successfully created word with ID: {}", createdWord.getId());
            return ResponseEntity.ok(createdWord);
        } catch (Exception e) {
            log.error("Error creating word: {}", word, e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Word> updateWord(@PathVariable Long id, @Valid @RequestBody Word word, Authentication authentication) {
        return ResponseEntity.ok(wordService.updateWord(id, word, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWord(@PathVariable Long id, Authentication authentication) {
        wordService.deleteWord(id, authentication.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/random")
    public ResponseEntity<Word> getRandomWord(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer difficultyLevel,
            Authentication authentication) {
        return ResponseEntity.ok(wordService.getRandomWord(language, difficultyLevel, authentication.getName()));
    }

    @PostMapping("/{id}/check")
    public ResponseEntity<Boolean> checkTranslation(
            @PathVariable Long id,
            @RequestParam String translation,
            Authentication authentication) {
        return ResponseEntity.ok(wordService.checkTranslation(id, translation, authentication.getName()));
    }
} 