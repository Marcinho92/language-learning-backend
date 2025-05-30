package com.example.languagelearning.service;

import com.example.languagelearning.model.Word;
import com.example.languagelearning.repository.WordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class WordService {
    private final WordRepository wordRepository;
    private final Random random = new Random();

    public List<Word> getAllWords() {
        return wordRepository.findAll();
    }

    public Word getWordById(Long id) {
        return wordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Word not found with id: " + id));
    }

    public List<Word> getWordsByLanguage(String language) {
        return wordRepository.findByLanguage(language);
    }

    public List<Word> getWordsByDifficultyLevel(Integer difficultyLevel) {
        return wordRepository.findByDifficultyLevel(difficultyLevel);
    }

    public List<Word> getWordsByLanguageAndDifficultyLevel(String language, Integer difficultyLevel) {
        return wordRepository.findByLanguageAndDifficultyLevel(language, difficultyLevel);
    }

    public Word createWord(Word word) {
        return wordRepository.save(word);
    }

    public Word updateWord(Long id, Word wordDetails) {
        Word word = getWordById(id);
        word.setOriginalWord(wordDetails.getOriginalWord());
        word.setTranslation(wordDetails.getTranslation());
        word.setDifficultyLevel(wordDetails.getDifficultyLevel());
        word.setLanguage(wordDetails.getLanguage());
        return wordRepository.save(word);
    }

    public void deleteWord(Long id) {
        Word word = getWordById(id);
        wordRepository.delete(word);
    }

    public Word getRandomWord() {
        long count = wordRepository.count();
        if (count == 0) {
            throw new EntityNotFoundException("No words found in the database");
        }
        
        List<Word> words = wordRepository.findAll();
        return words.get(random.nextInt(words.size()));
    }

    public boolean checkTranslation(String originalWord, String translation) {
        return wordRepository.findByOriginalWord(originalWord)
                .map(word -> word.getTranslation().equalsIgnoreCase(translation.trim()))
                .orElseThrow(() -> new EntityNotFoundException("Word not found: " + originalWord));
    }
} 