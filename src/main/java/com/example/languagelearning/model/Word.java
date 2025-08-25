package com.example.languagelearning.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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

    @Min(value = 1, message = "Proficiency level must be at least 1")
    @Max(value = 5, message = "Proficiency level must be at most 5")
    @Column(nullable = false)
    private Integer proficiencyLevel = 1;

    @Column(columnDefinition = "TEXT")
    private String exampleUsage;

    @Column(columnDefinition = "TEXT")
    private String explanation;
} 