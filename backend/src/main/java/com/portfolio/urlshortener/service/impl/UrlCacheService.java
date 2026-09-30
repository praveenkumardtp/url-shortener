package com.portfolio.urlshortener.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UrlCacheService {

    private static final Logger log = LoggerFactory.getLogger(UrlCacheService.class);
    private static final String CACHE_PREFIX = "shorturl:";

    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Value("${app.shortener.cache-ttl-minutes:60}")
    private long cacheTtlMinutes;

    public Optional<String> getLongUrl(String shortCode) {
        if (redisTemplate == null) return Optional.empty();
        try {
            String cached = redisTemplate.opsForValue().get(CACHE_PREFIX + shortCode);
            if (cached != null) {
                log.debug("Redis CACHE HIT for short code '{}' -> {}", shortCode, cached);
                return Optional.of(cached);
            }
        } catch (Exception ex) {
            log.warn("Redis lookup failed, falling back to database: {}", ex.getMessage());
        }
        return Optional.empty();
    }

    public void cacheUrl(String shortCode, String longUrl) {
        if (redisTemplate == null) return;
        try {
            redisTemplate.opsForValue().set(CACHE_PREFIX + shortCode, longUrl, cacheTtlMinutes, TimeUnit.MINUTES);
            log.debug("Cached short code '{}' in Redis with TTL {}m", shortCode, cacheTtlMinutes);
        } catch (Exception ex) {
            log.warn("Redis caching failed: {}", ex.getMessage());
        }
    }

    public void evictUrl(String shortCode) {
        if (redisTemplate == null) return;
        try {
            redisTemplate.delete(CACHE_PREFIX + shortCode);
            log.debug("Evicted short code '{}' from Redis cache", shortCode);
        } catch (Exception ex) {
            log.warn("Redis eviction failed: {}", ex.getMessage());
        }
    }
}
