package com.example.languagelearning.repository;

import com.example.languagelearning.model.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {
    List<Word> findByLanguage(String language);
    Optional<Word> findByOriginalWord(String originalWord);
    int deleteByIdIn(List<Long> ids);
    
    @Query(value = "SELECT w FROM Word w ORDER BY w.id",
           countQuery = "SELECT COUNT(w) FROM Word w")
    Page<Word> findAllWithPagination(Pageable pageable);
    
    @Query(value = "SELECT w FROM Word w WHERE LOWER(w.originalWord) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(w.translation) LIKE LOWER(CONCAT('%', :search, '%'))",
           countQuery = "SELECT COUNT(w) FROM Word w WHERE LOWER(w.originalWord) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(w.translation) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Word> findByOriginalWordOrTranslationContainingIgnoreCase(String search, Pageable pageable);
} 