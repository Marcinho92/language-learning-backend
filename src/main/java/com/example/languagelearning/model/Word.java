package com.example.languagelearning.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "words")
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Original word cannot be empty")
    @Column(nullable = false)
    private String originalWord;

    @NotBlank(message = "Translation cannot be empty")
    @Column(nullable = false)
    private String translation;

    @NotBlank(message = "Language cannot be empty")
    @Column(nullable = false)
    private String language;

    @NotNull(message = "Difficulty level cannot be null")
    @Min(value = 1, message = "Difficulty level must be at least 1")
    @Max(value = 3, message = "Difficulty level must be at most 3")
    @Column(nullable = false)
    private Integer difficultyLevel;

    @Min(value = 1, message = "Proficiency level must be at least 1")
    @Max(value = 5, message = "Proficiency level must be at most 5")
    @Column(nullable = false)
    private Integer proficiencyLevel = 1;

    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
} 