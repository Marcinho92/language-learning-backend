package com.example.languagelearning.controller;

import com.example.languagelearning.dto.GrammarPracticeResponse;
import com.example.languagelearning.dto.TranslationCheckResponse;
import com.example.languagelearning.model.Word;
import com.example.languagelearning.service.WordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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

    @GetMapping("/")
    public ResponseEntity<Object> getWordsRedirect() {
        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .header("Location", "/api/words/paginated?page=0&size=20")
                .build();
    }

    @GetMapping("/paginated")
    public ResponseEntity<Object> getWordsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        // Walidacja parametrów
        if (page < 0) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Page number must be non-negative", "page", page));
        }
        
        if (size <= 0) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Page size must be greater than zero", "size", size));
        }
        
        // Maksymalny rozmiar strony (zabezpieczenie przed zbyt dużymi zapytaniami)
        if (size > 5000) {
            size = 5000;
        }
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : 
            Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Word> words = wordService.getWordsPaginated(pageable);
        
        return ResponseEntity.ok(words);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Word> getWord(@PathVariable Long id) {
        return ResponseEntity.ok(wordService.getWord(id));
    }

    @PostMapping
    public ResponseEntity<Word> createWord(@Valid @RequestBody Word word) {
        try {
            Word createdWord = wordService.createWord(word);
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
        try {
            if (!requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".csv")) {
                throw new IllegalArgumentException("Only CSV files are supported");
            }

            wordService.importFromCsv(file);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error importing words from CSV", e);
            throw e;
        }
    }

    @PostMapping("/import/csv")
    public ResponseEntity<Void> importFromCsvAlias(@RequestParam("file") MultipartFile file) {
        return importFromCsv(file);
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkImport(@RequestBody List<Word> words) {
        try {
            List<Word> importedWords = wordService.bulkImport(words);
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
        try {
            log.info("Bulk delete request received with {} wordIds: {}", wordIds.size(), wordIds);
            int deletedCount = wordService.bulkDelete(wordIds);
            log.info("Successfully deleted {} words", deletedCount);
            return ResponseEntity.ok(Map.of(
                    "message", "Successfully deleted " + deletedCount + " words",
                    "deletedCount", deletedCount
            ));
        } catch (Exception e) {
            log.error("Error bulk deleting words", e);
            throw e;
        }
    }

    @DeleteMapping("/bulk-delete")
    public ResponseEntity<Map<String, Object>> bulkDeleteAlias(@RequestBody Map<String, List<Long>> request) {
        try {
            List<Long> wordIds = request.get("wordIds");
            if (wordIds == null || wordIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "wordIds is required and cannot be empty"));
            }
            return bulkDelete(wordIds);
        } catch (Exception e) {
            log.error("Error in bulkDeleteAlias", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid request format: " + e.getMessage()));
        }
    }

    // Grammar Practice Endpoints
    @GetMapping("/grammar-practice")
    public ResponseEntity<GrammarPracticeResponse> getRandomGrammarPractice() {
        try {
            GrammarPracticeResponse response = wordService.getRandomGrammarPractice();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating grammar practice", e);
            throw e;
        }
    }

    @PostMapping("/grammar-practice/validate")
    public ResponseEntity<GrammarPracticeResponse> validateGrammarPractice(
            @RequestBody Map<String, Object> request) {
        try {
            Long wordId = Long.valueOf(request.get("wordId").toString());
            String userSentence = (String) request.get("userSentence");
            String grammarTopic = (String) request.get("grammarTopic");

            GrammarPracticeResponse response = wordService.validateGrammarPractice(wordId, userSentence, grammarTopic);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error validating grammar practice", e);
            throw e;
        }
    }

    @PostMapping("/grammar-practice/audio")
    public ResponseEntity<Map<String, String>> generateAudio(
            @RequestBody Map<String, String> request) {
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