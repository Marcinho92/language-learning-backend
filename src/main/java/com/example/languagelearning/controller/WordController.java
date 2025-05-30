package com.example.languagelearning.controller;

import com.example.languagelearning.dto.TranslationCheckRequest;
import com.example.languagelearning.dto.TranslationCheckResponse;
import com.example.languagelearning.model.Word;
import com.example.languagelearning.service.WordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
public class WordController {
    private final WordService wordService;

    @GetMapping
    public ResponseEntity<List<Word>> getAllWords(
            @RequestParam(required = false) String language,
            @RequestParam(required = false) Integer difficultyLevel) {
        if (language != null && difficultyLevel != null) {
            return ResponseEntity.ok(wordService.getWordsByLanguageAndDifficultyLevel(language, difficultyLevel));
        } else if (language != null) {
            return ResponseEntity.ok(wordService.getWordsByLanguage(language));
        } else if (difficultyLevel != null) {
            return ResponseEntity.ok(wordService.getWordsByDifficultyLevel(difficultyLevel));
        }
        return ResponseEntity.ok(wordService.getAllWords());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Word> getWordById(@PathVariable Long id) {
        return ResponseEntity.ok(wordService.getWordById(id));
    }

    @PostMapping
    public ResponseEntity<Word> createWord(@Valid @RequestBody Word word) {
        return new ResponseEntity<>(wordService.createWord(word), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Word> updateWord(@PathVariable Long id, @Valid @RequestBody Word word) {
        return ResponseEntity.ok(wordService.updateWord(id, word));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWord(@PathVariable Long id) {
        wordService.deleteWord(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/random")
    public ResponseEntity<Word> getRandomWord() {
        return ResponseEntity.ok(wordService.getRandomWord());
    }

    @PostMapping("/check-translation")
    public ResponseEntity<TranslationCheckResponse> checkTranslation(@Valid @RequestBody TranslationCheckRequest request) {
        boolean isCorrect = wordService.checkTranslation(request.getOriginalWord(), request.getTranslation());
        TranslationCheckResponse response = isCorrect ? 
            TranslationCheckResponse.correct() : 
            TranslationCheckResponse.incorrect();
        return ResponseEntity.ok(response);
    }
} 