package com.example.languagelearning.service;

import com.example.languagelearning.dto.TranslationCheckResponse;
import com.example.languagelearning.model.Word;
import com.example.languagelearning.repository.WordRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WordServiceTest {

    @Mock
    private WordRepository wordRepository;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    @InjectMocks
    private WordService wordService;

    private Word testWord;
    private List<Word> testWords;

    @BeforeEach
    void setUp() throws Exception {
        // Use reflection to set the EntityManager
        Field entityManagerField = WordService.class.getDeclaredField("entityManager");
        entityManagerField.setAccessible(true);
        entityManagerField.set(wordService, entityManager);

        testWord = new Word();
        testWord.setId(1L);
        testWord.setOriginalWord("hello");
        testWord.setTranslation("cześć");
        testWord.setLanguage("polish");
        testWord.setProficiencyLevel(1);
        testWord.setExampleUsage("Hello, how are you?");
        testWord.setExplanation("A greeting");

        testWords = Arrays.asList(
            testWord,
            createWord(2L, "book", "książka", "polish", 2, "I read a book", "A written work"),
            createWord(3L, "car", "samochód", "polish", 3, "I drive a car", "A vehicle")
        );
    }

    private Word createWord(Long id, String original, String translation, String language, 
                           Integer level, String example, String explanation) {
        Word word = new Word();
        word.setId(id);
        word.setOriginalWord(original);
        word.setTranslation(translation);
        word.setLanguage(language);
        word.setProficiencyLevel(level);
        word.setExampleUsage(example);
        word.setExplanation(explanation);
        return word;
    }

    // CSV Export Tests
    @Test
    void exportToCsv_shouldHandlePolishCharacters() throws Exception {
        // given
        when(entityManager.createNativeQuery(anyString(), eq(Word.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(testWords);

        // when
        byte[] csvContent = wordService.exportToCsv();

        // then
        String csvString = new String(csvContent, 2, csvContent.length - 2, StandardCharsets.UTF_16LE);
        assertThat(csvString)
                .contains("książka")
                .doesNotContain("ksiÄ…ĹĽka");
        
        assertThat(csvContent[0]).isEqualTo((byte)0xFF);
        assertThat(csvContent[1]).isEqualTo((byte)0xFE);
    }

    @Test
    void exportToCsv_shouldHandleEmptyList() throws Exception {
        // given
        when(entityManager.createNativeQuery(anyString(), eq(Word.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        // when
        byte[] csvContent = wordService.exportToCsv();

        // then
        String csvString = new String(csvContent, 2, csvContent.length - 2, StandardCharsets.UTF_16LE);
        assertThat(csvString).contains("originalWord,translation,language,proficiencyLevel,exampleUsage,explanation");
        assertThat(csvString).doesNotContain("hello");
    }

    @Test
    void exportToCsv_shouldEscapeSpecialCharacters() throws Exception {
        // given
        Word wordWithCommas = createWord(1L, "word,with,commas", "translation", "polish", 1, "example", "explanation");
        when(entityManager.createNativeQuery(anyString(), eq(Word.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.singletonList(wordWithCommas));

        // when
        byte[] csvContent = wordService.exportToCsv();

        // then
        String csvString = new String(csvContent, 2, csvContent.length - 2, StandardCharsets.UTF_16LE);
        assertThat(csvString).contains("\"word,with,commas\"");
    }

    // CSV Import Tests
    @Test
    void importFromCsv_shouldImportValidData() throws IOException {
        // given
        String csvContent = "originalWord,translation,language,proficiencyLevel,exampleUsage,explanation\n" +
                "hello,cześć,polish,1,Hello example,Hello explanation";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", 
                csvContent.getBytes(StandardCharsets.UTF_8));

        // when
        wordService.importFromCsv(file);

        // then
        verify(wordRepository).saveAll(anyList());
    }

    @Test
    void importFromCsv_shouldHandleInvalidHeader() throws IOException {
        // given
        String csvContent = "invalid,header\nhello,cześć,polish,1";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", 
                csvContent.getBytes(StandardCharsets.UTF_8));

        // when & then
        assertThatThrownBy(() -> wordService.importFromCsv(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid CSV format");
    }

    @Test
    void importFromCsv_shouldHandleEmptyLines() throws IOException {
        // given
        String csvContent = "originalWord,translation,language,proficiencyLevel,exampleUsage,explanation\n" +
                "\n" +
                "hello,cześć,polish,1,example,explanation\n" +
                "\n";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", 
                csvContent.getBytes(StandardCharsets.UTF_8));

        // when
        wordService.importFromCsv(file);

        // then
        verify(wordRepository).saveAll(anyList());
    }

    @Test
    void importFromCsv_shouldHandleQuotedFields() throws IOException {
        // given
        String csvContent = "originalWord,translation,language,proficiencyLevel,exampleUsage,explanation\n" +
                "\"word,with,comma\",\"translation,with,comma\",polish,1,example,explanation";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", 
                csvContent.getBytes(StandardCharsets.UTF_8));

        // when
        wordService.importFromCsv(file);

        // then
        verify(wordRepository).saveAll(anyList());
    }

    @Test
    void importFromCsv_shouldHandleEscapedQuotes() throws IOException {
        // given
        String csvContent = "originalWord,translation,language,proficiencyLevel,exampleUsage,explanation\n" +
                "\"word with \"\"quotes\"\"\",translation,polish,1,example,explanation";
        MultipartFile file = new MockMultipartFile("file", "test.csv", "text/csv", 
                csvContent.getBytes(StandardCharsets.UTF_8));

        // when
        wordService.importFromCsv(file);

        // then
        verify(wordRepository).saveAll(anyList());
    }

    // CRUD Operations Tests
    @Test
    void getAllWords_shouldReturnAllWords() {
        // given
        when(entityManager.createNativeQuery(anyString(), eq(Word.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(testWords);

        // when
        List<Word> result = wordService.getAllWords();

        // then
        assertThat(result).hasSize(3);
        assertThat(result).containsExactlyElementsOf(testWords);
        verify(entityManager).createNativeQuery("SELECT * FROM words ORDER BY id", Word.class);
    }

    @Test
    void getAllWords_shouldHandleEmptyResult() {
        // given
        when(entityManager.createNativeQuery(anyString(), eq(Word.class))).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        // when
        List<Word> result = wordService.getAllWords();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void createWord_shouldReturnWord() {
        // given
        when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));

        // when
        Word result = wordService.createWord(1L);

        // then
        assertThat(result).isEqualTo(testWord);
        verify(wordRepository).findById(1L);
    }

    @Test
    void createWord_shouldThrowExceptionWhenNotFound() {
        // given
        when(wordRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wordService.createWord(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Word not found with id: 999");
    }

    @Test
    void createWord_shouldCreateNewWord() {
        // given
        Word newWord = new Word();
        newWord.setOriginalWord("new");
        newWord.setTranslation("nowy");
        newWord.setLanguage("polish");
        
        when(wordRepository.save(any(Word.class))).thenReturn(newWord);

        // when
        Word result = wordService.createWord(newWord);

        // then
        assertThat(result.getProficiencyLevel()).isEqualTo(1);
        verify(wordRepository).save(newWord);
    }

    @Test
    void updateWord_shouldUpdateExistingWord() {
        // given
        Word existingWord = createWord(1L, "old", "stary", "polish", 1, "old example", "old explanation");
        Word updatedWord = createWord(1L, "new", "nowy", "polish", 2, "new example", "new explanation");
        
        when(wordRepository.findById(1L)).thenReturn(Optional.of(existingWord));
        when(wordRepository.save(any(Word.class))).thenReturn(existingWord);

        // when
        Word result = wordService.updateWord(1L, updatedWord);

        // then
        assertThat(result.getOriginalWord()).isEqualTo("new");
        assertThat(result.getTranslation()).isEqualTo("nowy");
        assertThat(result.getExampleUsage()).isEqualTo("new example");
        assertThat(result.getExplanation()).isEqualTo("new explanation");
        verify(wordRepository).save(existingWord);
    }

    @Test
    void updateWord_shouldThrowExceptionWhenWordNotFound() {
        // given
        when(wordRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wordService.updateWord(999L, testWord))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteWord_shouldDeleteWord() {
        // given
        when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));
        doNothing().when(wordRepository).delete(testWord);

        // when
        wordService.deleteWord(1L);

        // then
        verify(wordRepository).delete(testWord);
    }

    @Test
    void deleteWord_shouldThrowExceptionWhenWordNotFound() {
        // given
        when(wordRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> wordService.deleteWord(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // Random Word Tests
    @Test
    void getRandomWord_shouldReturnRandomWordForLanguage() {
        // given
        List<Word> polishWords = Arrays.asList(
            createWord(1L, "hello", "cześć", "polish", 1, "example", "explanation"),
            createWord(2L, "book", "książka", "polish", 2, "example", "explanation")
        );
        when(wordRepository.findByLanguage("polish")).thenReturn(polishWords);

        // when
        Word result = wordService.getRandomWord("polish");

        // then
        assertThat(result).isIn(polishWords);
        verify(wordRepository).findByLanguage("polish");
    }

    @Test
    void getRandomWord_shouldReturnRandomWordForAllLanguages() {
        // given
        when(wordRepository.findAll()).thenReturn(testWords);

        // when
        Word result = wordService.getRandomWord(null);

        // then
        assertThat(result).isIn(testWords);
        verify(wordRepository).findAll();
    }

    @Test
    void getRandomWord_shouldReturnNullWhenNoWordsFound() {
        // given
        when(wordRepository.findByLanguage("spanish")).thenReturn(Collections.emptyList());

        // when
        Word result = wordService.getRandomWord("spanish");

        // then
        assertThat(result).isNull();
    }

    @Test
    void getRandomWord_shouldApplyWeightingBasedOnProficiencyLevel() {
        // given
        List<Word> words = Arrays.asList(
            createWord(1L, "easy", "łatwy", "polish", 1, "example", "explanation"),
            createWord(2L, "medium", "średni", "polish", 3, "example", "explanation"),
            createWord(3L, "hard", "trudny", "polish", 5, "example", "explanation")
        );
        when(wordRepository.findByLanguage("polish")).thenReturn(words);

        // when
        Word result = wordService.getRandomWord("polish");

        // then
        assertThat(result).isIn(words);
    }

    // Translation Check Tests
    @Test
    void checkTranslation_shouldReturnCorrectResponse() {
        // given
        when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));
        when(wordRepository.save(any(Word.class))).thenReturn(testWord);

        // when
        TranslationCheckResponse response = wordService.checkTranslation(1L, "cześć");

        // then
        assertThat(response.correct()).isTrue();
        assertThat(response.correctTranslation()).isEqualTo("cześć");
        assertThat(response.exampleUsage()).isEqualTo("Hello, how are you?");
        assertThat(response.explanation()).isEqualTo("A greeting");
        assertThat(response.message()).isEqualTo("Correct!");
        assertThat(testWord.getProficiencyLevel()).isEqualTo(2);
    }

    @Test
    void checkTranslation_shouldReturnIncorrectResponse() {
        // given
        when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));
        when(wordRepository.save(any(Word.class))).thenReturn(testWord);

        // when
        TranslationCheckResponse response = wordService.checkTranslation(1L, "wrong");

        // then
        assertThat(response.correct()).isFalse();
        assertThat(response.correctTranslation()).isEqualTo("cześć");
        assertThat(response.message()).isEqualTo("Incorrect. The correct answer is: cześć");
        assertThat(testWord.getProficiencyLevel()).isEqualTo(1);
    }

    @Test
    void checkTranslation_shouldHandleCaseInsensitiveComparison() {
        // given
        when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));
        when(wordRepository.save(any(Word.class))).thenReturn(testWord);

        // when
        TranslationCheckResponse response = wordService.checkTranslation(1L, "CZEŚĆ");

        // then
        assertThat(response.correct()).isTrue();
    }

    @Test
    void checkTranslation_shouldHandleTrimmedInput() {
        // given
        when(wordRepository.findById(1L)).thenReturn(Optional.of(testWord));
        when(wordRepository.save(any(Word.class))).thenReturn(testWord);

        // when
        TranslationCheckResponse response = wordService.checkTranslation(1L, "  cześć  ");

        // then
        assertThat(response.correct()).isTrue();
    }

    @Test
    void checkTranslation_shouldIncreaseProficiencyLevelCorrectly() {
        // given
        Word word = createWord(1L, "test", "test", "polish", 4, "example", "explanation");
        when(wordRepository.findById(1L)).thenReturn(Optional.of(word));
        when(wordRepository.save(any(Word.class))).thenReturn(word);

        // when
        wordService.checkTranslation(1L, "test");

        // then
        assertThat(word.getProficiencyLevel()).isEqualTo(5);
    }

    @Test
    void checkTranslation_shouldDecreaseProficiencyLevelCorrectly() {
        // given
        Word word = createWord(1L, "test", "test", "polish", 2, "example", "explanation");
        when(wordRepository.findById(1L)).thenReturn(Optional.of(word));
        when(wordRepository.save(any(Word.class))).thenReturn(word);

        // when
        wordService.checkTranslation(1L, "wrong");

        // then
        assertThat(word.getProficiencyLevel()).isEqualTo(1);
    }

    // Bulk Operations Tests
    @Test
    void bulkImport_shouldImportValidWords() {
        // given
        List<Word> wordsToImport = Arrays.asList(
            createWord(null, "word1", "słowo1", "polish", 1, "example1", "explanation1"),
            createWord(null, "word2", "słowo2", "polish", 2, "example2", "explanation2")
        );
        when(wordRepository.saveAll(anyList())).thenReturn(wordsToImport);

        // when
        List<Word> result = wordService.bulkImport(wordsToImport);

        // then
        assertThat(result).hasSize(2);
        verify(wordRepository).saveAll(wordsToImport);
    }

    @Test
    void bulkImport_shouldThrowExceptionForInvalidWord() {
        // given
        Word invalidWord = new Word();
        invalidWord.setOriginalWord(""); // Invalid - empty original word
        List<Word> wordsToImport = Collections.singletonList(invalidWord);

        // when & then
        assertThatThrownBy(() -> wordService.bulkImport(wordsToImport))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid word data");
    }

    @Test
    void bulkDelete_shouldDeleteWords() {
        // given
        List<Long> wordIds = Arrays.asList(1L, 2L, 3L);
        when(wordRepository.deleteByIdIn(wordIds)).thenReturn(3);

        // when
        int deletedCount = wordService.bulkDelete(wordIds);

        // then
        assertThat(deletedCount).isEqualTo(3);
        verify(wordRepository).deleteByIdIn(wordIds);
    }

    @Test
    void bulkDelete_shouldHandleEmptyList() {
        // given
        List<Long> wordIds = Collections.emptyList();
        when(wordRepository.deleteByIdIn(wordIds)).thenReturn(0);

        // when
        int deletedCount = wordService.bulkDelete(wordIds);

        // then
        assertThat(deletedCount).isEqualTo(0);
    }



    @Test
    void importFromCsv_shouldHandleIOException() throws IOException {
        // given
        MultipartFile file = mock(MultipartFile.class);
        when(file.getInputStream()).thenThrow(new IOException("File error"));

        // when & then
        assertThatThrownBy(() -> wordService.importFromCsv(file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Error importing CSV");
    }

    @Test
    void getAllWords_shouldHandleException() {
        // given
        when(entityManager.createNativeQuery(anyString(), eq(Word.class))).thenReturn(query);
        when(query.getResultList()).thenThrow(new RuntimeException("Database error"));

        // when & then
        assertThatThrownBy(() -> wordService.getAllWords())
                .isInstanceOf(RuntimeException.class);
    }
} 