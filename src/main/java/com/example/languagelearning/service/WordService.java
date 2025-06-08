package com.example.languagelearning.service;

import com.example.languagelearning.model.Word;
import com.example.languagelearning.repository.WordRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class WordService {
    private final WordRepository wordRepository;
    private final Random random = new Random();

    public List<Word> getAllWords() {
        return wordRepository.findAll();
    }

    public Word getWord(Long id) {
        return wordRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Word not found"));
    }

    public Word createWord(Word word) {
        log.info("Creating word: {}", word);
        try {
            word.setProficiencyLevel(1);
            Word savedWord = wordRepository.save(word);
            log.info("Successfully created word with ID: {}", savedWord.getId());
            return savedWord;
        } catch (Exception e) {
            log.error("Error creating word: {}", e.getMessage(), e);
            throw e;
        }
    }

    public Word updateWord(Long id, Word updatedWord) {
        Word word = getWord(id);
        word.setOriginalWord(updatedWord.getOriginalWord());
        word.setTranslation(updatedWord.getTranslation());
        word.setDifficultyLevel(updatedWord.getDifficultyLevel());
        word.setLanguage(updatedWord.getLanguage());
        return wordRepository.save(word);
    }

    public void deleteWord(Long id) {
        Word word = getWord(id);
        wordRepository.delete(word);
    }

    public Word getRandomWord(String language, Integer difficultyLevel) {
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
            throw new EntityNotFoundException("No words found with given criteria");
        }

        List<Word> weightedList = new ArrayList<>();
        for (Word word : words) {
            int weight = 6 - word.getProficiencyLevel();
            for (int i = 0; i < weight; i++) {
                weightedList.add(word);
            }
        }

        return weightedList.get(random.nextInt(weightedList.size()));
    }

    public boolean checkTranslation(Long id, String translation) {
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
        return isCorrect;
    }
} 