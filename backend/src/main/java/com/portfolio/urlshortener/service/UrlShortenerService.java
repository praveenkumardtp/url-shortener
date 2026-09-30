package com.portfolio.urlshortener.service;

import com.portfolio.urlshortener.dto.request.CreateShortUrlRequest;
import com.portfolio.urlshortener.dto.response.ShortUrlResponse;
import com.portfolio.urlshortener.dto.response.UrlAnalyticsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UrlShortenerService {
    ShortUrlResponse createShortUrl(CreateShortUrlRequest request);
    String resolveAndRecordClick(String shortCode, String ipAddress, String userAgent, String referrer);
    ShortUrlResponse getUrlByShortCode(String shortCode);
    UrlAnalyticsResponse getAnalytics(String shortCode);
    Page<ShortUrlResponse> getRecentUrls(Pageable pageable);
    void deleteShortUrl(String shortCode);
}
