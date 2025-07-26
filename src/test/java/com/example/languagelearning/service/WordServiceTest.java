package com.example.languagelearning.service;

import com.example.languagelearning.model.Word;
import com.example.languagelearning.repository.WordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class WordServiceTest {

    @Mock
    private WordRepository wordRepository;

    @Spy
    @InjectMocks
    private WordService wordService;

    @Test
    void exportToCsv_shouldHandlePolishCharacters() throws Exception {
        // given
        Word word = new Word();
        word.setOriginalWord("książka");
        word.setTranslation("book");
        word.setLanguage("polish");
        word.setProficiencyLevel(1);
        word.setExampleUsage("Czytam książkę każdego wieczoru.");
        word.setExplanation("A book is a written or printed work consisting of pages glued or sewn together along one side.");

        doReturn(Collections.singletonList(word)).when(wordService).getAllWords();

        // when
        byte[] csvContent = wordService.exportToCsv();

        // then
        // Skip the BOM (first 2 bytes) when converting to string
        String csvString = new String(csvContent, 2, csvContent.length - 2, StandardCharsets.UTF_16LE);
        assertThat(csvString)
                .contains("książka")
                .doesNotContain("ksiÄ…ĹĽka");
        
        // Print the actual content for debugging
        System.out.println("Actual CSV content (UTF-16LE):");
        System.out.println(csvString);
        
        // Verify BOM
        assertThat(csvContent[0]).isEqualTo((byte)0xFF);
        assertThat(csvContent[1]).isEqualTo((byte)0xFE);
        
        // Print the raw bytes of the word for debugging
        System.out.println("Raw bytes of 'książka' in UTF-16LE:");
        byte[] wordBytes = "książka".getBytes(StandardCharsets.UTF_16LE);
        for (byte b : wordBytes) {
            System.out.printf("%02X ", b);
        }
        System.out.println();
    }
} 