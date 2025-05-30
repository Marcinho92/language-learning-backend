package com.example.languagelearning.model;

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

    @NotBlank(message = "Translation/description cannot be empty")
    @Column(nullable = false)
    private String translation;

    @NotNull(message = "Difficulty level must be specified")
    @Min(value = 1, message = "Difficulty level must be between 1 and 3")
    @Max(value = 3, message = "Difficulty level must be between 1 and 3")
    @Column(nullable = false)
    private Integer difficultyLevel;

    @NotBlank(message = "Language cannot be empty")
    @Column(nullable = false)
    private String language;
} 