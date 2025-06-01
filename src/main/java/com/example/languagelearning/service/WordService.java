package com.example.languagelearning.service;

import com.example.languagelearning.model.User;
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
    private final UserService userService;
    private final Random random = new Random();

    public List<Word> getAllWords(String userEmail) {
        return wordRepository.findByUserEmail(userEmail);
    }

    public Word getWord(Long id, String userEmail) {
        return wordRepository.findById(id)
                .filter(word -> word.getUser().getEmail().equals(userEmail))
                .orElseThrow(() -> new EntityNotFoundException("Word not found"));
    }

    public Word createWord(Word word, String userEmail) {
        log.info("Creating word for user {}: {}", userEmail, word);
        try {
            User user = userService.getCurrentUser(userEmail);
            log.debug("Found user with email: {}", userEmail);
            
            word.setUser(user);
            word.setProficiencyLevel(1);
            
            Word savedWord = wordRepository.save(word);
            log.info("Successfully created word with ID: {}", savedWord.getId());
            return savedWord;
        } catch (Exception e) {
            log.error("Error creating word for user {}: {}", userEmail, e.getMessage(), e);
            throw e;
        }
    }

    public Word updateWord(Long id, Word updatedWord, String userEmail) {
        Word word = getWord(id, userEmail);
        word.setOriginalWord(updatedWord.getOriginalWord());
        word.setTranslation(updatedWord.getTranslation());
        word.setDifficultyLevel(updatedWord.getDifficultyLevel());
        word.setLanguage(updatedWord.getLanguage());
        return wordRepository.save(word);
    }

    public void deleteWord(Long id, String userEmail) {
        Word word = getWord(id, userEmail);
        wordRepository.delete(word);
    }

    public Word getRandomWord(String language, Integer difficultyLevel, String userEmail) {
        List<Word> words;
        if (language != null && difficultyLevel != null) {
            words = wordRepository.findByUserEmailAndLanguageAndDifficultyLevel(userEmail, language, difficultyLevel);
        } else if (language != null) {
            words = wordRepository.findByUserEmailAndLanguage(userEmail, language);
        } else if (difficultyLevel != null) {
            words = wordRepository.findByUserEmailAndDifficultyLevel(userEmail, difficultyLevel);
        } else {
            words = wordRepository.findByUserEmail(userEmail);
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

    public boolean checkTranslation(Long id, String translation, String userEmail) {
        Word word = getWord(id, userEmail);
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