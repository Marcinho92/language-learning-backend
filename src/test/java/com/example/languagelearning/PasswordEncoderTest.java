package com.example.languagelearning;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

public class PasswordEncoderTest {
    
    @Test
    public void verifyPasswords() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Test user verification
        String testUserHash = "$2a$10$mkmwkiHbpahTFhXIm56C8udcu50.EYfXBR8ioDnqBb0HU1katknkC";
        String testUserPassword = "test123";
        boolean testUserMatches = encoder.matches(testUserPassword, testUserHash);
        System.out.println("Test user password verification: " + testUserMatches);
        assertTrue(testUserMatches, "Test user password should match");
        
        // Working user verification
        String workingUserHash = "$2a$10$DskqAOODK30lsBpHdnUgmehkMSi/yV3wO7KKxizhisAnJtD3w2C5K";
        String workingUserPassword = "admin123";
        boolean workingUserMatches = encoder.matches(workingUserPassword, workingUserHash);
        System.out.println("Working user password verification: " + workingUserMatches);
        assertTrue(workingUserMatches, "Working user password should match");
        
        // Generate new hash for test user
        String newTestHash = encoder.encode(testUserPassword);
        System.out.println("New hash for test user password: " + newTestHash);
        assertTrue(encoder.matches(testUserPassword, newTestHash), "New hash should match test password");
    }
} 