package com.portfolio.urlshortener.service.impl;

import com.portfolio.urlshortener.dto.request.CreateShortUrlRequest;
import com.portfolio.urlshortener.dto.response.ShortUrlResponse;
import com.portfolio.urlshortener.dto.response.UrlAnalyticsResponse;
import com.portfolio.urlshortener.entity.ShortUrl;
import com.portfolio.urlshortener.entity.UrlClickAnalytics;
import com.portfolio.urlshortener.entity.UrlStatus;
import com.portfolio.urlshortener.exception.AliasAlreadyExistsException;
import com.portfolio.urlshortener.exception.UrlExpiredException;
import com.portfolio.urlshortener.exception.UrlNotFoundException;
import com.portfolio.urlshortener.mapper.ShortUrlMapper;
import com.portfolio.urlshortener.repository.ShortUrlRepository;
import com.portfolio.urlshortener.repository.UrlClickAnalyticsRepository;
import com.portfolio.urlshortener.service.UrlShortenerService;
import com.portfolio.urlshortener.util.Base62Converter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UrlShortenerServiceImpl implements UrlShortenerService {

    private static final Logger log = LoggerFactory.getLogger(UrlShortenerServiceImpl.class);

    private final ShortUrlRepository shortUrlRepository;
    private final UrlClickAnalyticsRepository analyticsRepository;
    private final UrlCacheService cacheService;
    private final ShortUrlMapper mapper;

    @Value("${app.shortener.default-expiration-days:30}")
    private int defaultExpirationDays;

    @Override
    @Transactional
    public ShortUrlResponse createShortUrl(CreateShortUrlRequest request) {
        String shortCode;

        if (request.getCustomAlias() != null && !request.getCustomAlias().trim().isEmpty()) {
            String alias = request.getCustomAlias().trim();
            if (shortUrlRepository.existsByShortCode(alias)) {
                throw new AliasAlreadyExistsException(alias);
            }
            shortCode = alias;
        } else {
            do {
                shortCode = Base62Converter.generateRandomCode(7);
            } while (shortUrlRepository.existsByShortCode(shortCode));
        }

        int expDays = (request.getExpirationDays() != null && request.getExpirationDays() > 0)
                ? request.getExpirationDays() : defaultExpirationDays;

        LocalDateTime expiresAt = LocalDateTime.now().plusDays(expDays);

        ShortUrl shortUrl = ShortUrl.builder()
                .longUrl(request.getLongUrl().trim())
                .shortCode(shortCode)
                .customAlias(request.getCustomAlias())
                .clickCount(0L)
                .status(UrlStatus.ACTIVE)
                .expiresAt(expiresAt)
                .build();

        ShortUrl saved = shortUrlRepository.save(shortUrl);
        cacheService.cacheUrl(shortCode, saved.getLongUrl());

        log.info("Created short URL: {} -> {}", shortCode, saved.getLongUrl());
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public String resolveAndRecordClick(String shortCode, String ipAddress, String userAgent, String referrer) {
        log.debug("Resolving short code: '{}'", shortCode);

        // Fetch DB entity to verify expiration & record metrics
        ShortUrl shortUrl = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        if (shortUrl.getStatus() == UrlStatus.DISABLED) {
            throw new UrlNotFoundException(shortCode);
        }

        if (shortUrl.isExpired()) {
            shortUrl.setStatus(UrlStatus.EXPIRED);
            shortUrlRepository.save(shortUrl);
            cacheService.evictUrl(shortCode);
            throw new UrlExpiredException(shortCode);
        }

        // Cache-aside warm up
        cacheService.cacheUrl(shortCode, shortUrl.getLongUrl());

        // Increment click metrics
        shortUrl.setClickCount(shortUrl.getClickCount() + 1);
        shortUrlRepository.save(shortUrl);

        // Log analytics event
        UrlClickAnalytics analytics = UrlClickAnalytics.builder()
                .shortUrl(shortUrl)
                .ipAddress(ipAddress != null ? ipAddress : "127.0.0.1")
                .userAgent(userAgent != null ? userAgent : "Unknown Browser")
                .referrer(referrer != null ? referrer : "Direct Navigation")
                .clickedAt(LocalDateTime.now())
                .build();
        analyticsRepository.save(analytics);

        log.info("Redirecting '{}' -> {} (Click #{})", shortCode, shortUrl.getLongUrl(), shortUrl.getClickCount());
        return shortUrl.getLongUrl();
    }

    @Override
    @Transactional(readOnly = true)
    public ShortUrlResponse getUrlByShortCode(String shortCode) {
        ShortUrl url = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
        return mapper.toResponse(url);
    }

    @Override
    @Transactional(readOnly = true)
    public UrlAnalyticsResponse getAnalytics(String shortCode) {
        ShortUrl url = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));

        Page<UrlClickAnalytics> clicks = analyticsRepository.findByShortUrlIdOrderByClickedAtDesc(
                url.getId(), PageRequest.of(0, 25)
        );

        return mapper.toAnalyticsResponse(url, clicks.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ShortUrlResponse> getRecentUrls(Pageable pageable) {
        return shortUrlRepository.findAllByOrderByCreatedAtDesc(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteShortUrl(String shortCode) {
        ShortUrl url = shortUrlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new UrlNotFoundException(shortCode));
        url.setStatus(UrlStatus.DISABLED);
        shortUrlRepository.save(url);
        cacheService.evictUrl(shortCode);
        log.info("Disabled short URL '{}'", shortCode);
    }
}
