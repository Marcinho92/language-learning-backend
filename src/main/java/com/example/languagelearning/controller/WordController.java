package com.example.languagelearning.controller;

import com.example.languagelearning.dto.TranslationCheckResponse;
import com.example.languagelearning.model.Word;
import com.example.languagelearning.service.WordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
@CrossOrigin(origins = {
    "http://localhost:3000", 
    "https://language-learning-frontend.railway.app",
    "https://language-learning-frontend-production.up.railway.app"
})
public class WordController {
    private final WordService wordService;

    @GetMapping
    public ResponseEntity<List<Word>> getAllWords() {
        return ResponseEntity.ok(wordService.getAllWords());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Word> getWord(@PathVariable Long id) {
        return ResponseEntity.ok(wordService.getWord(id));
    }

    @PostMapping
    public ResponseEntity<Word> createWord(@Valid @RequestBody Word word) {
        log.info("Received request to create word: {}", word);
        try {
            Word createdWord = wordService.createWord(word);
            log.info("Successfully created word with ID: {}", createdWord.getId());
            return ResponseEntity.ok(createdWord);
        } catch (Exception e) {
            log.error("Error creating word: {}", word, e);
            throw e;
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Word> updateWord(@PathVariable Long id, @Valid @RequestBody Word word) {
        return ResponseEntity.ok(wordService.updateWord(id, word));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWord(@PathVariable Long id) {
        wordService.deleteWord(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/random")
    public ResponseEntity<Object> getRandomWord(
            @RequestParam(required = false) String language) {
        Word randomWord = wordService.getRandomWord(language);
        if (randomWord == null) {
            return ResponseEntity.ok(Map.of(
                "message", "Baza słów jest pusta. Dodaj słowa, aby rozpocząć naukę.",
                "isEmpty", true
            ));
        }
        return ResponseEntity.ok(randomWord);
    }

    @PostMapping("/{id}/check")
    public ResponseEntity<TranslationCheckResponse> checkTranslation(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        String translation = request.get("translation");
        return ResponseEntity.ok(wordService.checkTranslation(id, translation));
    }

    @GetMapping(value = "/export", produces = "text/csv;charset=UTF-8")
    public ResponseEntity<byte[]> exportToCsv() {
        byte[] csvContent = wordService.exportToCsv();
        return ResponseEntity.ok()
                .header("Content-Type", "text/csv;charset=UTF-8")
                .header("Content-Disposition", "attachment; filename=\"vocabulary.csv\"")
                .body(csvContent);
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importFromCsv(@RequestParam("file") MultipartFile file) {
        log.info("Received request to import words from CSV file: {}", file.getOriginalFilename());
        try {
            if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
                throw new IllegalArgumentException("Only CSV files are supported");
            }

            wordService.importFromCsv(file);
            log.info("Successfully imported words from CSV file");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error importing words from CSV", e);
            throw e;
        }
    }
} 