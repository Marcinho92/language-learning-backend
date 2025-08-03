package com.example.languagelearning.service;

import com.example.languagelearning.dto.GrammarPracticeResponse;
import com.example.languagelearning.dto.TranslationCheckResponse;
import com.example.languagelearning.model.Word;
import com.example.languagelearning.repository.WordRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
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

    private static final String[] CSV_HEADERS = {"originalWord", "translation", "language", "proficiencyLevel", "exampleUsage", "explanation"};

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
                csvContent.append(String.format("%s,%s,%s,%d,%s,%s%n",
                        escapeCsvField(word.getOriginalWord()),
                        escapeCsvField(word.getTranslation()),
                        escapeCsvField(word.getLanguage()),
                        word.getProficiencyLevel(),
                        escapeCsvField(word.getExampleUsage()),
                        escapeCsvField(word.getExplanation())
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
            String expectedHeader = String.join(",", CSV_HEADERS);
            if (!expectedHeader.equals(line)) {
                throw new RuntimeException("Invalid CSV format. Expected header: " + expectedHeader);
            }

            List<Word> wordsToSave = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] data = parseCsvLine(line);
                if (data.length >= 4) {
                    Word word = new Word();
                    word.setOriginalWord(data[0].trim());
                    word.setTranslation(data[1].trim());
                    word.setLanguage(data[2].trim());
                    word.setProficiencyLevel(Integer.parseInt(data[3].trim()));
                    
                    // Handle optional fields
                    if (data.length > 4) {
                        word.setExampleUsage(data[4].trim());
                    }
                    if (data.length > 5) {
                        word.setExplanation(data[5].trim());
                    }
                    
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
                    // Double quotes inside quoted field (escaped quote)
                    currentField.append('"');
                    i++;
                } else {
                    // Start/end of quoted field
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                // End of field (only if not inside quotes)
                fields.add(currentField.toString().trim());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }

        // Add the last field
        fields.add(currentField.toString().trim());
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
            existingWord.setLanguage(updatedWord.getLanguage());
            existingWord.setExampleUsage(updatedWord.getExampleUsage());
            existingWord.setExplanation(updatedWord.getExplanation());
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
    public Word getRandomWord(String language) {
        log.info("Getting random word with language: {}", language);
        try {
            List<Word> words;
            if (language != null) {
                words = wordRepository.findByLanguage(language);
            } else {
                words = wordRepository.findAll();
            }

            if (words.isEmpty()) {
                log.warn("No words found with given criteria: language={}", language);
                return null; // Return null instead of throwing exception
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
    public TranslationCheckResponse checkTranslation(Long id, String translation) {
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
            
            TranslationCheckResponse response = new TranslationCheckResponse();
            response.setCorrect(isCorrect);
            response.setCorrectTranslation(word.getTranslation());
            response.setExampleUsage(word.getExampleUsage());
            response.setExplanation(word.getExplanation());
            response.setMessage(isCorrect ? "Correct!" : "Incorrect. The correct answer is: " + word.getTranslation());
            
            return response;
        } catch (Exception e) {
            log.error("Error checking translation for word id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    public List<Word> bulkImport(List<Word> words) {
        log.info("Starting bulk import of {} words", words.size());
        try {
            // Validate all words before saving
            for (Word word : words) {
                if (!validateWord(word)) {
                    throw new IllegalArgumentException("Invalid word data: " + word.getOriginalWord());
                }
            }

            // Save all words in a single transaction
            List<Word> savedWords = wordRepository.saveAll(words);
            log.info("Successfully bulk imported {} words", savedWords.size());
            return savedWords;
        } catch (Exception e) {
            log.error("Error bulk importing words", e);
            throw e;
        }
    }

    @Transactional
    public int bulkDelete(List<Long> wordIds) {
        log.info("Starting bulk delete of {} words", wordIds.size());
        try {
            // Delete all words in a single transaction
            int deletedCount = wordRepository.deleteByIdIn(wordIds);
            log.info("Successfully bulk deleted {} words", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("Error bulk deleting words", e);
            throw e;
        }
    }

    // Grammar Practice Methods
    private static final String[] GRAMMAR_TOPICS = {
        "Present Simple", "Present Continuous", "Past Simple", "Past Continuous",
        "Present Perfect", "Past Perfect", "Future Simple", "First Conditional",
        "Second Conditional", "Third Conditional", "Passive Voice", "Reported Speech",
        "Modal Verbs", "Gerunds and Infinitives", "Relative Clauses"
    };

    @Transactional(readOnly = true)
    public GrammarPracticeResponse getRandomGrammarPractice() {
        log.info("Getting random grammar practice");
        
        // Get random word
        Word randomWord = getRandomWord(null);
        if (randomWord == null) {
            throw new RuntimeException("No words available for grammar practice");
        }
        
        // Get random grammar topic
        String grammarTopic = GRAMMAR_TOPICS[random.nextInt(GRAMMAR_TOPICS.length)];
        
        log.info("Selected word: {} with grammar topic: {}", randomWord.getOriginalWord(), grammarTopic);
        return new GrammarPracticeResponse(randomWord, grammarTopic);
    }

    @Transactional(readOnly = true)
    public GrammarPracticeResponse validateGrammarPractice(Long wordId, String userSentence, String grammarTopic) {
        log.info("Validating grammar practice for wordId: {}, sentence: {}, topic: {}", wordId, userSentence, grammarTopic);
        
        Word word = getWord(wordId);
        if (word == null) {
            throw new EntityNotFoundException("Word not found with id: " + wordId);
        }
        
        // Simplified validation - just check if sentence contains the word
        boolean containsWord = userSentence.toLowerCase().contains(word.getOriginalWord().toLowerCase()) ||
                             userSentence.toLowerCase().contains(word.getTranslation().toLowerCase());
        
        // For now, accept any sentence that contains the word
        // Grammar validation is too complex to implement accurately
        boolean isCorrect = containsWord;
        
        String feedback = isCorrect ? 
            "Great job! Your sentence contains the word correctly. Remember to practice the grammar structure." : 
            "Try again. Make sure to use the word '" + word.getOriginalWord() + "' in your sentence.";
        
        String explanation = generateGrammarExplanation(grammarTopic);
        
        log.info("Grammar practice validation result: {}", isCorrect);
        return new GrammarPracticeResponse(word, grammarTopic, isCorrect, feedback, explanation);
    }
    

    

    
    private String generateGrammarExplanation(String grammarTopic) {
        switch (grammarTopic.toLowerCase()) {
            case "present simple":
                return "Present Simple is used for habits, routines, and general truths.\n\n" +
                       "Structure: Subject + base verb (add 's' for 3rd person singular)\n" +
                       "Examples:\n" +
                       "• I work every day.\n" +
                       "• She works in an office.\n" +
                       "• They like coffee.\n" +
                       "• He doesn't like tea.";
            case "present continuous":
                return "Present Continuous is used for actions happening now or around now.\n\n" +
                       "Structure: Subject + be (am/is/are) + verb + ing\n" +
                       "Examples:\n" +
                       "• I am working now.\n" +
                       "• She is reading a book.\n" +
                       "• They are studying English.\n" +
                       "• We are not sleeping.";
            case "present perfect":
                return "Present Perfect is used for actions that started in the past and continue to the present.\n\n" +
                       "Structure: Subject + have/has + past participle\n" +
                       "Examples:\n" +
                       "• I have worked here for 5 years.\n" +
                       "• She has finished her homework.\n" +
                       "• They have never been to Paris.\n" +
                       "• We haven't seen that movie.";
            case "past simple":
                return "Past Simple is used for completed actions in the past.\n\n" +
                       "Structure: Subject + past form of verb (regular: +ed, irregular: special form)\n" +
                       "Examples:\n" +
                       "• I worked yesterday.\n" +
                       "• She went to the store.\n" +
                       "• They studied all night.\n" +
                       "• He didn't like the movie.";
            case "past perfect":
                return "Past Perfect is used for actions that happened before another past action.\n\n" +
                       "Structure: Subject + had + past participle\n" +
                       "Examples:\n" +
                       "• I had finished my work before she arrived.\n" +
                       "• She had already eaten when I called.\n" +
                       "• They had never seen such a beautiful sunset.\n" +
                       "• We hadn't met before the party.";
            case "future simple":
                return "Future Simple is used for predictions and spontaneous decisions.\n\n" +
                       "Structure: Subject + will + base verb\n" +
                       "Examples:\n" +
                       "• I will help you with that.\n" +
                       "• She will be here tomorrow.\n" +
                       "• They will probably come to the party.\n" +
                       "• We won't be late.";
            case "first conditional":
                return "First Conditional is used for real possibilities in the future.\n\n" +
                       "Structure: If + present simple, will + base verb\n" +
                       "Examples:\n" +
                       "• If it rains, I will stay home.\n" +
                       "• If you study hard, you will pass the exam.\n" +
                       "• She will be happy if you call her.\n" +
                       "• We will go to the beach if the weather is nice.";
            case "second conditional":
                return "Second Conditional is used for unreal or hypothetical situations.\n\n" +
                       "Structure: If + past simple, would + base verb\n" +
                       "Examples:\n" +
                       "• If I had money, I would buy a car.\n" +
                       "• If you studied more, you would get better grades.\n" +
                       "• She would travel the world if she could.\n" +
                       "• We would be rich if we won the lottery.";
            case "passive voice":
                return "Passive Voice is used when the focus is on the action, not the doer.\n\n" +
                       "Structure: Subject + be + past participle (+ by + agent)\n" +
                       "Examples:\n" +
                       "• The book was written by Shakespeare.\n" +
                       "• The house is being built.\n" +
                       "• The letter has been sent.\n" +
                       "• The car was stolen last night.";
            default:
                return "Practice using this grammar structure in your sentences.\n\n" +
                       "Make sure to use the given word in your sentence and apply the grammar topic correctly.";
        }
    }
} 
