package com.example.languagelearning.service;

import com.example.languagelearning.model.Word;
import com.example.languagelearning.repository.WordRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordService {
    private final WordRepository wordRepository;
    private final Random random = new Random();

    @PersistenceContext
    private EntityManager entityManager;

    private static final String[] CSV_HEADERS = {"originalWord", "translation", "language", "difficultyLevel", "proficiencyLevel"};

    @Transactional(readOnly = true)
    public byte[] exportToCsv() {
        log.info("Starting CSV export");
        List<Word> words = getAllWords();

        try {
            // Create a string with the CSV content first
            StringBuilder csvContent = new StringBuilder();
            
            // Add headers
            csvContent.append(String.join(",", CSV_HEADERS)).append("\n");

            // Add data
            for (Word word : words) {
                csvContent.append(String.format("%s,%s,%s,%d,%d%n",
                        escapeCsvField(word.getOriginalWord()),
                        escapeCsvField(word.getTranslation()),
                        escapeCsvField(word.getLanguage()),
                        word.getDifficultyLevel(),
                        word.getProficiencyLevel()
                ));
            }

            // Convert to bytes using UTF-16LE encoding
            byte[] contentBytes = csvContent.toString().getBytes(StandardCharsets.UTF_16LE);
            
            // Add UTF-16LE BOM (FF FE)
            byte[] bom = new byte[] { (byte)0xFF, (byte)0xFE };
            byte[] result = new byte[bom.length + contentBytes.length];
            System.arraycopy(bom, 0, result, 0, bom.length);
            System.arraycopy(contentBytes, 0, result, bom.length, contentBytes.length);

            log.info("Successfully exported {} words to CSV with UTF-16LE encoding", words.size());
            return result;
        } catch (Exception e) {
            log.error("Error exporting words to CSV", e);
            throw new RuntimeException("Error exporting to CSV: " + e.getMessage());
        }
    }

    @Transactional
    public void importFromCsv(MultipartFile file) {
        log.info("Starting CSV import from file: {}", file.getOriginalFilename());
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line = reader.readLine(); // Skip header
            if (!CSV_HEADERS.equals(line)) {
                throw new RuntimeException("Invalid CSV format. Expected header: " + CSV_HEADERS);
            }

            List<Word> wordsToSave = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = line.split(",");
                if (data.length == 5) {
                    Word word = new Word();
                    word.setOriginalWord(data[0].trim());
                    word.setTranslation(data[1].trim());
                    word.setLanguage(data[2].trim());
                    word.setDifficultyLevel(Integer.parseInt(data[3].trim()));
                    word.setProficiencyLevel(Integer.parseInt(data[4].trim()));
                    wordsToSave.add(word);
                } else {
                    log.warn("Invalid CSV line format: {}", line);
                }
            }

            if (!wordsToSave.isEmpty()) {
                wordRepository.saveAll(wordsToSave);
                log.info("Successfully imported {} words", wordsToSave.size());
            }
        } catch (IOException e) {
            log.error("Error reading CSV file", e);
            throw new RuntimeException("Error importing CSV: " + e.getMessage());
        }
    }

    private boolean validateWord(Word word) {
        return word.getOriginalWord() != null && !word.getOriginalWord().trim().isEmpty() &&
                word.getTranslation() != null && !word.getTranslation().trim().isEmpty() &&
                word.getLanguage() != null && !word.getLanguage().trim().isEmpty() &&
                word.getDifficultyLevel() != null && word.getDifficultyLevel() >= 1 && word.getDifficultyLevel() <= 3 &&
                word.getProficiencyLevel() != null && word.getProficiencyLevel() >= 1 && word.getProficiencyLevel() <= 5;
    }

    private String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // If the field contains comma, newline, or quotes, wrap it in quotes and escape existing quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    // Double quotes inside quoted field
                    currentField.append('"');
                    i++;
                } else {
                    // Start/end of quoted field
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of field
                fields.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }

        fields.add(currentField.toString());
        return fields.toArray(new String[0]);
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Word> getAllWords() {
        log.info("Starting getAllWords() method");
        try {
            log.info("Executing native SQL query");
            Query query = entityManager.createNativeQuery(
                    "SELECT * FROM words ORDER BY id", Word.class);
            List<Word> words = query.getResultList();
            log.info("Retrieved {} words from database", words.size());
            if (words.isEmpty()) {
                log.warn("No words found in database!");
            } else {
                for (Word word : words) {
                    log.info("Word found: {}", word);
                }
            }
            return words;
        } catch (Exception e) {
            log.error("Error retrieving all words", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Word getWord(Long id) {
        log.info("Getting word with id: {}", id);
        try {
            Word word = wordRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Word not found with id: " + id));
            log.info("Found word: {}", word);
            return word;
        } catch (EntityNotFoundException e) {
            log.warn("Word not found with id: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error getting word with id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public Word createWord(Word word) {
        log.info("Creating new word: {}", word);
        try {
            word.setProficiencyLevel(1);
            Word savedWord = wordRepository.save(word);
            log.info("Successfully created word: {}", savedWord);
            return savedWord;
        } catch (Exception e) {
            log.error("Error creating word: {}", word, e);
            throw e;
        }
    }

    @Transactional
    public Word updateWord(Long id, Word updatedWord) {
        log.info("Updating word with id: {} with data: {}", id, updatedWord);
        try {
            Word existingWord = getWord(id);
            existingWord.setOriginalWord(updatedWord.getOriginalWord());
            existingWord.setTranslation(updatedWord.getTranslation());
            existingWord.setDifficultyLevel(updatedWord.getDifficultyLevel());
            existingWord.setLanguage(updatedWord.getLanguage());
            Word savedWord = wordRepository.save(existingWord);
            log.info("Successfully updated word: {}", savedWord);
            return savedWord;
        } catch (Exception e) {
            log.error("Error updating word with id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public void deleteWord(Long id) {
        log.info("Deleting word with id: {}", id);
        try {
            Word word = getWord(id);
            wordRepository.delete(word);
            log.info("Successfully deleted word with id: {}", id);
        } catch (Exception e) {
            log.error("Error deleting word with id: {}", id, e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public Word getRandomWord(String language, Integer difficultyLevel) {
        log.info("Getting random word with language: {} and difficultyLevel: {}", language, difficultyLevel);
        try {
            List<Word> words;
            if (language != null && difficultyLevel != null) {
                words = wordRepository.findByLanguageAndDifficultyLevel(language, difficultyLevel);
            } else if (language != null) {
                words = wordRepository.findByLanguage(language);
            } else if (difficultyLevel != null) {
                words = wordRepository.findByDifficultyLevel(difficultyLevel);
            } else {
                words = wordRepository.findAll();
            }

            if (words.isEmpty()) {
                log.warn("No words found with given criteria: language={}, difficultyLevel={}", language, difficultyLevel);
                throw new EntityNotFoundException("No words found with given criteria");
            }

            log.info("Found {} words matching criteria", words.size());

            List<Word> weightedList = new ArrayList<>();
            for (Word word : words) {
                int weight = 6 - word.getProficiencyLevel();
                for (int i = 0; i < weight; i++) {
                    weightedList.add(word);
                }
            }

            Word selectedWord = weightedList.get(random.nextInt(weightedList.size()));
            log.info("Selected random word: {}", selectedWord);
            return selectedWord;
        } catch (Exception e) {
            log.error("Error getting random word", e);
            throw e;
        }
    }

    @Transactional
    public boolean checkTranslation(Long id, String translation) {
        log.info("Checking translation for word id: {} with translation: {}", id, translation);
        try {
            Word word = getWord(id);
            boolean isCorrect = word.getTranslation().equalsIgnoreCase(translation.trim());

            if (isCorrect) {
                word.setProficiencyLevel(Math.min(word.getProficiencyLevel() + 1, 5));
                log.info("Correct answer for word '{}'. Proficiency increased to: {}",
                        word.getOriginalWord(), word.getProficiencyLevel());
            } else {
                word.setProficiencyLevel(Math.max(word.getProficiencyLevel() - 1, 1));
                log.info("Incorrect answer for word '{}'. Proficiency decreased to: {}",
                        word.getOriginalWord(), word.getProficiencyLevel());
            }

            wordRepository.save(word);
            log.info("Translation check result: {}", isCorrect);
            return isCorrect;
        } catch (Exception e) {
            log.error("Error checking translation for word id: {}", id, e);
            throw e;
        }
    }
} 
