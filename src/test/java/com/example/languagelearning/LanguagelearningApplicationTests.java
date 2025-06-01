package com.example.languagelearning;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class LanguagelearningApplicationTests {

    @Test
    void verifyTestUserPassword() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "test123";
        String currentHash = "$2a$10$mkmwkiHbpahTFhXIm56C8udcu50.EYfXBR8ioDnqBb0HU1katknkC";
        
        System.out.println("\n=== Password Verification Test ===");
        System.out.println("Password: " + password);
        System.out.println("Current hash from data.sql: " + currentHash);
        System.out.println("Verification result: " + encoder.matches(password, currentHash));
        
        assert encoder.matches(password, currentHash) : "Password should match the hash from data.sql";
        System.out.println("Test passed successfully!");
        System.out.println("=====================================\n");
    }
} 