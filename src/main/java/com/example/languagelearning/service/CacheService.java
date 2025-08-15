package com.example.languagelearning.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Autowired
    public CacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Save value to cache with default TTL
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            log.debug("Cached value for key: {}", key);
        } catch (Exception e) {
            log.error("Error caching value for key: {}", key, e);
        }
    }

    /**
     * Save value to cache with custom TTL
     */
    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Cached value for key: {} with TTL: {}", key, ttl);
        } catch (Exception e) {
            log.error("Error caching value for key: {} with TTL: {}", key, ttl, e);
        }
    }

    /**
     * Save value to cache with custom TTL in seconds
     */
    public void set(String key, Object value, long ttlSeconds) {
        try {
            redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
            log.debug("Cached value for key: {} with TTL: {} seconds", key, ttlSeconds);
        } catch (Exception e) {
            log.error("Error caching value for key: {} with TTL: {} seconds", key, ttlSeconds, e);
        }
    }

    /**
     * Get value from cache
     */
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("Cache hit for key: {}", key);
                return Optional.of((T) value);
            }
            log.debug("Cache miss for key: {}", key);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error retrieving value from cache for key: {}", key, e);
            return Optional.empty();
        }
    }

    /**
     * Check if key exists in cache
     */
    public boolean exists(String key) {
        try {
            Boolean exists = redisTemplate.hasKey(key);
            return exists != null && exists;
        } catch (Exception e) {
            log.error("Error checking if key exists: {}", key, e);
            return false;
        }
    }

    /**
     * Delete key from cache
     */
    public void delete(String key) {
        try {
            Boolean deleted = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Deleted cache key: {}", key);
            } else {
                log.debug("Cache key not found for deletion: {}", key);
            }
        } catch (Exception e) {
            log.error("Error deleting cache key: {}", key, e);
        }
    }

    /**
     * Delete multiple keys from cache
     */
    public void delete(String... keys) {
        try {
            Long deletedCount = redisTemplate.delete(java.util.Arrays.asList(keys));
            log.debug("Deleted {} cache keys", deletedCount);
        } catch (Exception e) {
            log.error("Error deleting cache keys", e);
        }
    }

    /**
     * Set expiration for existing key
     */
    public boolean expire(String key, Duration ttl) {
        try {
            Boolean expired = redisTemplate.expire(key, ttl);
            log.debug("Set expiration for key: {} with TTL: {}", key, ttl);
            return Boolean.TRUE.equals(expired);
        } catch (Exception e) {
            log.error("Error setting expiration for key: {}", key, e);
            return false;
        }
    }

    /**
     * Get TTL for key
     */
    public Optional<Duration> getTtl(String key) {
        try {
            Long ttlSeconds = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (ttlSeconds != null && ttlSeconds > 0) {
                return Optional.of(Duration.ofSeconds(ttlSeconds));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting TTL for key: {}", key, e);
            return Optional.empty();
        }
    }

    /**
     * Clear all cache
     */
    public void clearAll() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();
            log.info("Cleared all cache");
        } catch (Exception e) {
            log.error("Error clearing cache", e);
        }
    }

    /**
     * Get cache statistics
     */
    public String getStats() {
        try {
            return String.format("Cache size: %d keys", redisTemplate.getConnectionFactory().getConnection().dbSize());
        } catch (Exception e) {
            log.error("Error getting cache stats", e);
            return "Error getting cache stats";
        }
    }
}
