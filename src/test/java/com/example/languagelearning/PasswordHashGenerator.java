package com.example.languagelearning;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    
    @Test
    public void generateHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = "test123";
        String hash = encoder.encode(password);
        
        System.out.println("\n=== Password Hash Generation ===");
        System.out.println("Password: " + password);
        System.out.println("Generated hash: " + hash);
        System.out.println("Verification: " + encoder.matches(password, hash));
        System.out.println("==============================\n");
    }
} 