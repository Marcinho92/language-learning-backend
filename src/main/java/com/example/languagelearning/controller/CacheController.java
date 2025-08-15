package com.example.languagelearning.controller;

import com.example.languagelearning.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheService cacheService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, String>> getCacheStats() {
        log.info("Getting cache statistics");
        try {
            String stats = cacheService.getStats();
            return ResponseEntity.ok(Map.of("stats", stats));
        } catch (Exception e) {
            log.error("Error getting cache stats", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get cache statistics"));
        }
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearAllCache() {
        log.info("Clearing all cache");
        try {
            cacheService.clearAll();
            return ResponseEntity.ok(Map.of("message", "All cache cleared successfully"));
        } catch (Exception e) {
            log.error("Error clearing cache", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to clear cache"));
        }
    }

    @DeleteMapping("/words")
    public ResponseEntity<Map<String, String>> clearWordsCache() {
        log.info("Clearing words cache");
        try {
            cacheService.delete("words::all", "words::random_null");
            return ResponseEntity.ok(Map.of("message", "Words cache cleared successfully"));
        } catch (Exception e) {
            log.error("Error clearing words cache", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to clear words cache"));
        }
    }

    @DeleteMapping("/grammar-practice")
    public ResponseEntity<Map<String, String>> clearGrammarPracticeCache() {
        log.info("Clearing grammar practice cache");
        try {
            cacheService.delete("grammar-practice::random");
            return ResponseEntity.ok(Map.of("message", "Grammar practice cache cleared successfully"));
        } catch (Exception e) {
            log.error("Error clearing grammar practice cache", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to clear grammar practice cache"));
        }
    }

    @DeleteMapping("/ai-responses")
    public ResponseEntity<Map<String, String>> clearAiResponsesCache() {
        log.info("Clearing AI responses cache");
        try {
            // This will clear all AI responses cache entries
            // In a production environment, you might want to be more selective
            cacheService.delete("ai-responses::*");
            return ResponseEntity.ok(Map.of("message", "AI responses cache cleared successfully"));
        } catch (Exception e) {
            log.error("Error clearing AI responses cache", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to clear AI responses cache"));
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> checkCacheHealth() {
        log.info("Checking cache health");
        try {
            // Try to set and get a test value
            String testKey = "health_check";
            String testValue = "ok";
            
            cacheService.set(testKey, testValue, 60); // 60 seconds TTL
            var result = cacheService.get(testKey, String.class);
            
            if (result.isPresent() && testValue.equals(result.get())) {
                cacheService.delete(testKey);
                return ResponseEntity.ok(Map.of("status", "healthy", "message", "Cache is working properly"));
            } else {
                return ResponseEntity.status(503)
                        .body(Map.of("status", "unhealthy", "message", "Cache read/write test failed"));
            }
        } catch (Exception e) {
            log.error("Cache health check failed", e);
            return ResponseEntity.status(503)
                    .body(Map.of("status", "unhealthy", "message", "Cache health check failed: " + e.getMessage()));
        }
    }
}
