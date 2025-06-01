package com.example.languagelearning.repository;

import com.example.languagelearning.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findByUserEmail(String email);
    List<Word> findByUserEmailAndLanguage(String email, String language);
    List<Word> findByUserEmailAndDifficultyLevel(String email, Integer difficultyLevel);
    List<Word> findByUserEmailAndLanguageAndDifficultyLevel(String email, String language, Integer difficultyLevel);
    Optional<Word> findByOriginalWordAndUserEmail(String originalWord, String email);
} 