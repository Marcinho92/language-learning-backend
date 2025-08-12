package com.example.languagelearning.controller;

import com.example.languagelearning.dto.GrammarPracticeResponse;
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

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;

@Slf4j
@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
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
        return ResponseEntity.ok(requireNonNullElseGet(randomWord, () -> Map.of(
                "message", "Baza słów jest pusta. Dodaj słowa, aby rozpocząć naukę.",
                "isEmpty", true
        )));
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
            if (!requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".csv")) {
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

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkImport(@RequestBody List<Word> words) {
        log.info("Received request to bulk import {} words", words.size());
        try {
            List<Word> importedWords = wordService.bulkImport(words);
            log.info("Successfully bulk imported {} words", importedWords.size());
            return ResponseEntity.ok(Map.of(
                    "message", "Successfully imported " + importedWords.size() + " words",
                    "importedCount", importedWords.size(),
                    "words", importedWords
            ));
        } catch (Exception e) {
            log.error("Error bulk importing words", e);
            throw e;
        }
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkDelete(@RequestBody List<Long> wordIds) {
        log.info("Received request to bulk delete {} words", wordIds.size());
        try {
            int deletedCount = wordService.bulkDelete(wordIds);
            log.info("Successfully bulk deleted {} words", deletedCount);
            return ResponseEntity.ok(Map.of(
                    "message", "Successfully deleted " + deletedCount + " words",
                    "deletedCount", deletedCount
            ));
        } catch (Exception e) {
            log.error("Error bulk deleting words", e);
            throw e;
        }
    }

    // Grammar Practice Endpoints
    @GetMapping("/grammar-practice")
    public ResponseEntity<GrammarPracticeResponse> getRandomGrammarPractice() {
        log.info("Received request for random grammar practice");
        try {
            GrammarPracticeResponse response = wordService.getRandomGrammarPractice();
            log.info("Successfully generated grammar practice with word: {} and topic: {}",
                    response.getWord().getOriginalWord(), response.getGrammarTopic());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating grammar practice", e);
            throw e;
        }
    }

    @PostMapping("/grammar-practice/validate")
    public ResponseEntity<GrammarPracticeResponse> validateGrammarPractice(
            @RequestBody Map<String, Object> request) {
        log.info("Received request to validate grammar practice");
        try {
            Long wordId = Long.valueOf(request.get("wordId").toString());
            String userSentence = (String) request.get("userSentence");
            String grammarTopic = (String) request.get("grammarTopic");

            GrammarPracticeResponse response = wordService.validateGrammarPractice(wordId, userSentence, grammarTopic);
            log.info("Successfully validated grammar practice. Result: {}", response.isCorrect());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating grammar practice", e);
            throw e;
        }
    }

    @PostMapping("/grammar-practice/audio")
    public ResponseEntity<Map<String, String>> generateAudio(
            @RequestBody Map<String, String> request) {
        log.info("Received request to generate audio");
        try {
            String text = request.get("text");
            String language = request.get("language");

            if (text == null || text.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Text is required"));
            }

            String audioBase64 = wordService.generateAudio(text, language);
            return ResponseEntity.ok(Map.of("audioBase64", audioBase64));
        } catch (Exception e) {
            log.error("Error generating audio", e);
            throw e;
        }
    }
} 