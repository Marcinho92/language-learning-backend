package com.example.languagelearning.repository;

import com.example.languagelearning.model.Word;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findByLanguage(String language);
    List<Word> findByDifficultyLevel(Integer difficultyLevel);
    List<Word> findByLanguageAndDifficultyLevel(String language, Integer difficultyLevel);
    Optional<Word> findByOriginalWord(String originalWord);
} 