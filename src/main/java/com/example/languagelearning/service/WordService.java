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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordService {
    private final WordRepository wordRepository;
    private final AiGrammarValidationService aiValidationService;
    private final TextToSpeechService textToSpeechService;
    private final Random random = new Random(System.currentTimeMillis());

    @PersistenceContext
    private EntityManager entityManager;

    private static final String[] CSV_HEADERS = {"originalWord", "translation", "language", "proficiencyLevel", "exampleUsage", "explanation"};

    public byte[] exportToCsv() {
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
            byte[] bom = new byte[]{(byte) 0xFF, (byte) 0xFE};
            byte[] result = new byte[bom.length + contentBytes.length];
            System.arraycopy(bom, 0, result, 0, bom.length);
            System.arraycopy(contentBytes, 0, result, bom.length, contentBytes.length);

            return result;
        } catch (Exception e) {
            log.error("Error exporting words to CSV", e);
            throw new RuntimeException("Error exporting to CSV: " + e.getMessage());
        }
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "words", allEntries = true)
    public void importFromCsv(MultipartFile file) {
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
                    Word word = createWord(data);
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

    @NotNull
    private static Word createWord(String[] data) {
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
        return word;
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
    @org.springframework.cache.annotation.Cacheable(value = "words", key = "'all'")
    public List<Word> getAllWords() {
        try {
            Query query = entityManager.createNativeQuery(
                    "SELECT * FROM words ORDER BY id", Word.class);
            List<Word> words = query.getResultList();
            if (words.isEmpty()) {
                log.warn("No words found in database!");
            }
            return words;
        } catch (Exception e) {
            log.error("Error retrieving all words", e);
            throw e;
        }
    }

    public Page<Word> getWordsPaginated(Pageable pageable, String search) {
        if (search != null && !search.trim().isEmpty()) {
            return wordRepository.findByOriginalWordOrTranslationContainingIgnoreCase(search.trim(), pageable);
        }
        return wordRepository.findAll(pageable);
    }

    @org.springframework.cache.annotation.Cacheable(value = "words", key = "#id")
    public Word getWord(Long id) {
        try {
            return wordRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Word not found with id: " + id));
        } catch (EntityNotFoundException e) {
            log.warn("Word not found with id: {}", id);
            throw e;
        } catch (Exception e) {
            log.error("Error getting word with id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "words", allEntries = true)
    public Word createWord(Word word) {
        try {
            word.setProficiencyLevel(1);
            return wordRepository.save(word);
        } catch (Exception e) {
            log.error("Error creating word: {}", word, e);
            throw e;
        }
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "words", allEntries = true)
    public Word updateWord(Long id, Word updatedWord) {
        try {
            Word existingWord = getWord(id);
            existingWord.setOriginalWord(updatedWord.getOriginalWord());
            existingWord.setTranslation(updatedWord.getTranslation());
            existingWord.setLanguage(updatedWord.getLanguage());
            existingWord.setExampleUsage(updatedWord.getExampleUsage());
            existingWord.setExplanation(updatedWord.getExplanation());
            return wordRepository.save(existingWord);
        } catch (Exception e) {
            log.error("Error updating word with id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "words", allEntries = true)
    public void deleteWord(Long id) {
        try {
            Word word = getWord(id);
            wordRepository.delete(word);
        } catch (Exception e) {
            log.error("Error deleting word with id: {}", id, e);
            throw e;
        }
    }

    public Word getRandomWord(String language) {
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

            List<Word> weightedList = new ArrayList<>();
            for (Word word : words) {
                int weight = 6 - word.getProficiencyLevel();
                for (int i = 0; i < weight; i++) {
                    weightedList.add(word);
                }
            }

            // Use current timestamp for better randomization
            int randomIndex = (int) (System.currentTimeMillis() % weightedList.size());
            return weightedList.get(randomIndex);
        } catch (Exception e) {
            log.error("Error getting random word", e);
            throw e;
        }
    }

    @Transactional
    public TranslationCheckResponse checkTranslation(Long id, String translation) {
        try {
            Word word = getWord(id);
            boolean isCorrect = word.getTranslation().equalsIgnoreCase(translation.trim());

            if (isCorrect) {
                word.setProficiencyLevel(Math.min(word.getProficiencyLevel() + 1, 5));
            } else {
                word.setProficiencyLevel(Math.max(word.getProficiencyLevel() - 1, 1));
            }

            wordRepository.save(word);

            return new TranslationCheckResponse(
                    isCorrect,
                    word.getTranslation(),
                    word.getExampleUsage(),
                    word.getExplanation(),
                    isCorrect ? "Correct!" : "Incorrect. The correct answer is: " + word.getTranslation()
            );
        } catch (Exception e) {
            log.error("Error checking translation for word id: {}", id, e);
            throw e;
        }
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "words", allEntries = true)
    public List<Word> bulkImport(List<Word> words) {
        try {
            // Validate all words before saving
            for (Word word : words) {
                if (!validateWord(word)) {
                    throw new IllegalArgumentException("Invalid word data: " + word.getOriginalWord());
                }
            }

            return wordRepository.saveAll(words);
        } catch (Exception e) {
            log.error("Error bulk importing words", e);
            throw e;
        }
    }

    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "words", allEntries = true)
    public int bulkDelete(List<Long> wordIds) {
        try {
            return wordRepository.deleteByIdIn(wordIds);
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

    public GrammarPracticeResponse getRandomGrammarPractice() {
        log.info("Getting random grammar practice");

        // Get random word
        Word randomWord = getRandomWord(null);
        if (randomWord == null) {
            log.warn("No words available for grammar practice - returning empty response");
            return new GrammarPracticeResponse(null, null, false, 
                "Brak słów w bazie danych. Dodaj słowa, aby rozpocząć ćwiczenia gramatyczne.", 
                null, 
                "Aby rozpocząć ćwiczenia gramatyczne, musisz najpierw dodać słowa do bazy danych.", 
                null);
        }

        // Get random grammar topic
        String grammarTopic = GRAMMAR_TOPICS[random.nextInt(GRAMMAR_TOPICS.length)];

        log.info("Selected word: {} with grammar topic: {}", randomWord.getOriginalWord(), grammarTopic);

        // Generate explanation for the grammar topic
        String explanation = generateGrammarExplanation(grammarTopic);

        return new GrammarPracticeResponse(randomWord, grammarTopic, false, null, null, explanation, null);
    }

    public String generateAudio(String text, String language) {
        try {
            String defaultLanguage = language != null ? language : "en";
            return textToSpeechService.generateAudioBase64(text, defaultLanguage);
        } catch (Exception e) {
            log.error("Error generating audio", e);
            return null;
        }
    }

    public GrammarPracticeResponse validateGrammarPractice(Long wordId, String userSentence, String grammarTopic) {
        Word word = getWord(wordId);
        if (word == null) {
            throw new EntityNotFoundException("Word not found with id: " + wordId);
        }

        AiGrammarValidationService.GrammarValidationResult validationResult =
                aiValidationService.validateSentence(userSentence, word, grammarTopic);

        String textToAudio = validationResult.correction() != null && !validationResult.correction().trim().isEmpty()
                ? validationResult.correction()
                : userSentence;

        String audioUrl = getAudioUrl(textToAudio, word);

        return new GrammarPracticeResponse(word, grammarTopic, validationResult.isCorrect(),
                validationResult.feedback(), validationResult.correction(), validationResult.explanation(), audioUrl);
    }

    @Nullable
    private String getAudioUrl(String textToAudio, Word word) {
        if (textToAudio != null && !textToAudio.trim().isEmpty()) {
            String language = word.getLanguage() != null ? word.getLanguage() : "en";
            return textToSpeechService.generateAudioBase64(textToAudio, language);
        } else {
            log.warn("Text to audio is null or empty: '{}'", textToAudio);
            return null;
        }
    }

    private String generateGrammarExplanation(String grammarTopic) {
        return switch (grammarTopic.toLowerCase()) {
            case "present simple" -> """
                    Present Simple is used for habits, routines, and general truths.
                    
                    Structure: Subject + base verb (add 's' for 3rd person singular)
                    Examples:
                    • I work every day.
                    • She works in an office.
                    • They like coffee.
                    • He doesn't like tea.""";
            case "present continuous" -> """
                    Present Continuous is used for actions happening now or around now.
                    
                    Structure: Subject + be (am/is/are) + verb + ing
                    Examples:
                    • I am working now.
                    • She is reading a book.
                    • They are studying English.
                    • We are not sleeping.""";
            case "present perfect" -> """
                    Present Perfect is used for actions that started in the past and continue to the present.
                    
                    Structure: Subject + have/has + past participle
                    Examples:
                    • I have worked here for 5 years.
                    • She has finished her homework.
                    • They have never been to Paris.
                    • We haven't seen that movie.""";
            case "past simple" -> """
                    Past Simple is used for completed actions in the past.
                    
                    Structure: Subject + past form of verb (regular: +ed, irregular: special form)
                    Examples:
                    • I worked yesterday.
                    • She went to the store.
                    • They studied all night.
                    • He didn't like the movie.""";
            case "past perfect" -> """
                    Past Perfect is used for actions that happened before another past action.
                    
                    Structure: Subject + had + past participle
                    Examples:
                    • I had finished my work before she arrived.
                    • She had already eaten when I called.
                    • They had never seen such a beautiful sunset.
                    • We hadn't met before the party.""";
            case "future simple" -> """
                    Future Simple is used for predictions and spontaneous decisions.
                    
                    Structure: Subject + will + base verb
                    Examples:
                    • I will help you with that.
                    • She will be here tomorrow.
                    • They will probably come to the party.
                    • We won't be late.""";
            case "first conditional" -> """
                    First Conditional is used for real possibilities in the future.
                    
                    Structure: If + present simple, will + base verb
                    Examples:
                    • If it rains, I will stay home.
                    • If you study hard, you will pass the exam.
                    • She will be happy if you call her.
                    • We will go to the beach if the weather is nice.""";
            case "second conditional" -> """
                    Second Conditional is used for unreal or hypothetical situations.
                    
                    Structure: If + past simple, would + base verb
                    Examples:
                    • If I had money, I would buy a car.
                    • If you studied more, you would get better grades.
                    • She would travel the world if she could.
                    • We would be rich if we won the lottery.""";
            case "passive voice" -> """
                    Passive Voice is used when the focus is on the action, not the doer.
                    
                    Structure: Subject + be + past participle (+ by + agent)
                    Examples:
                    • The book was written by Shakespeare.
                    • The house is being built.
                    • The letter has been sent.
                    • The car was stolen last night.""";
            case "modal verbs" -> """
                    Modal Verbs express ability, possibility, permission, obligation, and advice.
                    
                    Structure: Subject + modal verb + base verb
                    Common modal verbs: can, could, may, might, must, shall, should, will, would
                    Examples:
                    • I can speak English.
                    • You should study harder.
                    • She must finish her work.
                    • They might come to the party.
                    • We could help you with that.""";
            case "gerunds and infinitives" -> """
                    Gerunds and Infinitives are verb forms used as nouns.
                    
                    Gerund Structure: verb + ing (used as subject, object, after prepositions)
                    Infinitive Structure: to + base verb (used after certain verbs, adjectives)
                    Examples:
                    • I enjoy reading books. (gerund)
                    • She wants to learn English. (infinitive)
                    • Swimming is good exercise. (gerund as subject)
                    • It's important to study regularly. (infinitive)""";
            case "relative clauses" -> """
                    Relative Clauses provide additional information about a noun.
                    
                    Structure: Noun + relative pronoun (who, which, that, where, when) + clause
                    Examples:
                    • The man who lives next door is a doctor.
                    • The book that I bought is very interesting.
                    • The place where I grew up is beautiful.
                    • The time when we met was perfect.""";
            case "reported speech" -> """
                    Reported Speech is used to report what someone said.
                    
                    Structure: Subject + reporting verb + that + reported clause (tense changes)
                    Examples:
                    • She said that she was tired.
                    • He told me that he would come.
                    • They mentioned that they had finished.
                    • I asked if she could help.""";
            case "past continuous" -> """
                    Past Continuous is used for actions that were in progress at a specific time in the past.
                    
                    Structure: Subject + was/were + verb + ing
                    Examples:
                    • I was working when you called.
                    • She was reading a book at 8 PM.
                    • They were studying all night.
                    • We were not sleeping during the storm.""";
            case "third conditional" -> """
                    Third Conditional is used for unreal situations in the past.
                    
                    Structure: If + past perfect, would have + past participle
                    Examples:
                    • If I had studied harder, I would have passed the exam.
                    • If she had known, she would have told us.
                    • They would have won if they had played better.
                    • We would have been rich if we had invested earlier.""";
            default -> """
                    Practice using this grammar structure in your sentences.
                    
                    Make sure to use the given word in your sentence and apply the grammar topic correctly.""";
        };
    }
} 
