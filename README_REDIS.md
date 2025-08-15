# Redis Cache Configuration

## Overview
This application uses Redis for caching to improve performance. Redis is configured to cache frequently accessed data like words, grammar practice exercises, and AI responses.

## Features

### Cache Types
- **Words Cache** (`words`): Caches word data with 1-hour TTL
- **Grammar Practice Cache** (`grammar-practice`): Caches grammar practice exercises with 15-minute TTL
- **AI Responses Cache** (`ai-responses`): Caches AI-generated content with 24-hour TTL

### Cache Keys
- `words::all` - All words list
- `words::{id}` - Individual word by ID
- `words::random_{language}` - Random word by language
- `grammar-practice::random` - Random grammar practice
- `ai-responses::practice_{params}` - Practice generation responses
- `ai-responses::verify_{params}` - Translation verification responses

## Configuration

### Local Development
Redis is configured via Docker Compose:
```yaml
redis:
  image: redis:7-alpine
  ports:
    - "6379:6379"
  command: redis-server --appendonly yes
  volumes:
    - redis-data:/data
```

### Railway Deployment
Redis is automatically provisioned as a service in Railway. Environment variables are automatically set:
- `REDIS_HOST` - Redis service hostname
- `REDIS_PORT` - Redis service port (usually 6379)
- `REDIS_PASSWORD` - Redis password (if configured)
- `REDIS_DATABASE` - Redis database number

## Cache Management Endpoints

### Get Cache Statistics
```http
GET /api/cache/stats
```

### Check Cache Health
```http
GET /api/cache/health
```

### Clear All Cache
```http
DELETE /api/cache/clear
```

### Clear Specific Cache Types
```http
DELETE /api/cache/words
DELETE /api/cache/grammar-practice
DELETE /api/cache/ai-responses
```

## Cache Annotations

### @Cacheable
Used to cache method results:
```java
@Cacheable(value = "words", key = "#id")
public Word getWord(Long id) { ... }
```

### @CacheEvict
Used to clear cache when data changes:
```java
@CacheEvict(value = "words", allEntries = true)
public Word createWord(Word word) { ... }
```

## Performance Benefits

1. **Reduced Database Queries**: Frequently accessed words are cached
2. **Faster AI Responses**: Similar AI requests return cached responses
3. **Improved User Experience**: Faster loading times for common operations
4. **Reduced API Costs**: Fewer calls to OpenAI API for repeated requests

## Monitoring

### Cache Hit/Miss Logging
The application logs cache hits and misses for monitoring:
```
Cache hit for key: words::all
Cache miss for key: words::123
```

### Cache Statistics
Monitor cache size and performance via the `/api/cache/stats` endpoint.

## Troubleshooting

### Common Issues

1. **Redis Connection Failed**
   - Check if Redis service is running
   - Verify connection settings in `application.yml`
   - Check Railway service status

2. **Cache Not Working**
   - Verify `@EnableCaching` is present
   - Check cache annotations are correct
   - Monitor cache logs for errors

3. **Memory Issues**
   - Monitor Redis memory usage
   - Adjust TTL values if needed
   - Consider implementing cache eviction policies

### Health Check
Use `/api/cache/health` to verify Redis connectivity and basic functionality.

## Best Practices

1. **Cache Key Design**: Use descriptive, unique keys
2. **TTL Management**: Set appropriate expiration times
3. **Cache Invalidation**: Clear cache when data changes
4. **Memory Monitoring**: Monitor Redis memory usage
5. **Error Handling**: Gracefully handle cache failures

## Local Testing

1. Start Redis: `docker-compose up redis`
2. Start application: `./mvnw spring-boot:run`
3. Test cache endpoints: `curl http://localhost:8080/api/cache/health`
4. Monitor cache behavior in application logs
