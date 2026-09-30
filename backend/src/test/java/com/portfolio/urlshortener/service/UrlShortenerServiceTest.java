package com.portfolio.urlshortener.service;

import com.portfolio.urlshortener.dto.request.CreateShortUrlRequest;
import com.portfolio.urlshortener.dto.response.ShortUrlResponse;
import com.portfolio.urlshortener.entity.ShortUrl;
import com.portfolio.urlshortener.entity.UrlClickAnalytics;
import com.portfolio.urlshortener.entity.UrlStatus;
import com.portfolio.urlshortener.exception.AliasAlreadyExistsException;
import com.portfolio.urlshortener.exception.UrlExpiredException;
import com.portfolio.urlshortener.exception.UrlNotFoundException;
import com.portfolio.urlshortener.mapper.ShortUrlMapper;
import com.portfolio.urlshortener.repository.ShortUrlRepository;
import com.portfolio.urlshortener.repository.UrlClickAnalyticsRepository;
import com.portfolio.urlshortener.service.impl.UrlCacheService;
import com.portfolio.urlshortener.service.impl.UrlShortenerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlShortenerServiceTest {

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private UrlClickAnalyticsRepository analyticsRepository;

    @Mock
    private UrlCacheService cacheService;

    @Spy
    private ShortUrlMapper mapper = new ShortUrlMapper();

    @InjectMocks
    private UrlShortenerServiceImpl shortenerService;

    private ShortUrl sampleUrl;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(shortenerService, "defaultExpirationDays", 30);
        ReflectionTestUtils.setField(mapper, "baseUrl", "http://localhost:8082");

        sampleUrl = ShortUrl.builder()
                .id(1L)
                .longUrl("https://spring.io/projects/spring-boot")
                .shortCode("springBootDocs")
                .clickCount(0L)
                .status(UrlStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    @Test
    @DisplayName("URL Creation: Generates short URL with unique code and caches in Redis")
    void testCreateShortUrl_Success() {
        when(shortUrlRepository.existsByShortCode(anyString())).thenReturn(false);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(inv -> {
            ShortUrl u = inv.getArgument(0);
            u.setId(10L);
            return u;
        });

        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .longUrl("https://github.com/praveenkumardtp")
                .expirationDays(14)
                .build();

        ShortUrlResponse response = shortenerService.createShortUrl(request);

        assertNotNull(response);
        assertEquals("https://github.com/praveenkumardtp", response.getLongUrl());
        assertNotNull(response.getShortCode());
        assertEquals(UrlStatus.ACTIVE, response.getStatus());

        verify(shortUrlRepository, times(1)).save(any(ShortUrl.class));
        verify(cacheService, times(1)).cacheUrl(eq(response.getShortCode()), eq("https://github.com/praveenkumardtp"));
    }

    @Test
    @DisplayName("URL Creation: Uses requested custom alias when available")
    void testCreateShortUrl_CustomAlias_Success() {
        when(shortUrlRepository.existsByShortCode("my-custom-link")).thenReturn(false);
        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .longUrl("https://linkedin.com")
                .customAlias("my-custom-link")
                .build();

        ShortUrlResponse response = shortenerService.createShortUrl(request);

        assertNotNull(response);
        assertEquals("my-custom-link", response.getShortCode());
        verify(shortUrlRepository, times(1)).save(any(ShortUrl.class));
    }

    @Test
    @DisplayName("URL Creation: Throws AliasAlreadyExistsException when alias is already taken")
    void testCreateShortUrl_CustomAlias_AlreadyExists() {
        when(shortUrlRepository.existsByShortCode("taken-alias")).thenReturn(true);

        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .longUrl("https://google.com")
                .customAlias("taken-alias")
                .build();

        assertThrows(AliasAlreadyExistsException.class, () -> shortenerService.createShortUrl(request));
        verify(shortUrlRepository, never()).save(any(ShortUrl.class));
    }

    @Test
    @DisplayName("Redirect: Resolves original long URL, increments click counter, and logs click analytics")
    void testResolveAndRecordClick_Success() {
        when(shortUrlRepository.findByShortCode("springBootDocs")).thenReturn(Optional.of(sampleUrl));
        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));
        when(analyticsRepository.save(any(UrlClickAnalytics.class))).thenAnswer(inv -> inv.getArgument(0));

        String targetUrl = shortenerService.resolveAndRecordClick(
                "springBootDocs", "192.168.1.50", "Mozilla/5.0 Chrome", "https://google.com"
        );

        assertEquals("https://spring.io/projects/spring-boot", targetUrl);
        assertEquals(1L, sampleUrl.getClickCount());

        verify(shortUrlRepository, times(1)).save(sampleUrl);
        verify(analyticsRepository, times(1)).save(any(UrlClickAnalytics.class));
    }

    @Test
    @DisplayName("Redirect: Throws UrlNotFoundException for non-existent short code")
    void testResolveAndRecordClick_NotFound_ThrowsException() {
        when(shortUrlRepository.findByShortCode("unknown123")).thenReturn(Optional.empty());

        assertThrows(UrlNotFoundException.class, () -> shortenerService.resolveAndRecordClick(
                "unknown123", "127.0.0.1", "curl", null
        ));
        verify(analyticsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Redirect: Throws UrlExpiredException when expiration timestamp has passed")
    void testResolveAndRecordClick_Expired_ThrowsException() {
        sampleUrl.setExpiresAt(LocalDateTime.now().minusDays(1)); // Expired yesterday

        when(shortUrlRepository.findByShortCode("springBootDocs")).thenReturn(Optional.of(sampleUrl));

        assertThrows(UrlExpiredException.class, () -> shortenerService.resolveAndRecordClick(
                "springBootDocs", "127.0.0.1", "Safari", null
        ));

        assertEquals(UrlStatus.EXPIRED, sampleUrl.getStatus());
        verify(cacheService, times(1)).evictUrl("springBootDocs");
        verify(analyticsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Short-Code Uniqueness: Retries generation if collision occurs on random code")
    void testShortCode_UniquenessCheck() {
        // First collision, second succeeds
        when(shortUrlRepository.existsByShortCode(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        when(shortUrlRepository.save(any(ShortUrl.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateShortUrlRequest request = CreateShortUrlRequest.builder()
                .longUrl("https://example.org")
                .build();

        ShortUrlResponse res = shortenerService.createShortUrl(request);

        assertNotNull(res);
        verify(shortUrlRepository, times(2)).existsByShortCode(anyString());
    }
}
