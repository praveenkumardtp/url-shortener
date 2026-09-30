package com.portfolio.urlshortener.controller;

import com.portfolio.urlshortener.dto.request.CreateShortUrlRequest;
import com.portfolio.urlshortener.dto.response.ShortUrlResponse;
import com.portfolio.urlshortener.dto.response.UrlAnalyticsResponse;
import com.portfolio.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
@RequiredArgsConstructor
@Tag(name = "URL Management", description = "Endpoints for creating short URLs, fetching metrics, and analytics")
public class UrlShortenerController {

    private final UrlShortenerService shortenerService;

    @PostMapping
    @Operation(summary = "Shorten a long URL", description = "Generates a unique Base62 short code or uses custom alias with configurable expiration")
    public ResponseEntity<ShortUrlResponse> createShortUrl(@Valid @RequestBody CreateShortUrlRequest request) {
        ShortUrlResponse response = shortenerService.createShortUrl(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{shortCode}")
    @Operation(summary = "Get URL metadata by short code")
    public ResponseEntity<ShortUrlResponse> getUrlDetails(@PathVariable String shortCode) {
        return ResponseEntity.ok(shortenerService.getUrlByShortCode(shortCode));
    }

    @GetMapping("/{shortCode}/analytics")
    @Operation(summary = "Get click analytics and access history")
    public ResponseEntity<UrlAnalyticsResponse> getAnalytics(@PathVariable String shortCode) {
        return ResponseEntity.ok(shortenerService.getAnalytics(shortCode));
    }

    @GetMapping
    @Operation(summary = "List recent shortened URLs with pagination")
    public ResponseEntity<Page<ShortUrlResponse>> getRecentUrls(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(shortenerService.getRecentUrls(pageable));
    }

    @DeleteMapping("/{shortCode}")
    @Operation(summary = "Deactivate/disable a short URL")
    public ResponseEntity<Void> deleteShortUrl(@PathVariable String shortCode) {
        shortenerService.deleteShortUrl(shortCode);
        return ResponseEntity.noContent().build();
    }
}
