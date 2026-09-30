package com.portfolio.urlshortener.mapper;

import com.portfolio.urlshortener.dto.response.ShortUrlResponse;
import com.portfolio.urlshortener.dto.response.UrlAnalyticsResponse;
import com.portfolio.urlshortener.entity.ShortUrl;
import com.portfolio.urlshortener.entity.UrlClickAnalytics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ShortUrlMapper {

    @Value("${app.shortener.base-url:http://localhost:8082}")
    private String baseUrl;

    public ShortUrlResponse toResponse(ShortUrl entity) {
        if (entity == null) return null;
        String fullShortUrl = baseUrl + "/" + entity.getShortCode();
        return ShortUrlResponse.builder()
                .id(entity.getId())
                .longUrl(entity.getLongUrl())
                .shortCode(entity.getShortCode())
                .shortUrl(fullShortUrl)
                .customAlias(entity.getCustomAlias())
                .clickCount(entity.getClickCount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .isExpired(entity.isExpired())
                .build();
    }

    public UrlAnalyticsResponse toAnalyticsResponse(ShortUrl entity, List<UrlClickAnalytics> clicks) {
        if (entity == null) return null;
        String fullShortUrl = baseUrl + "/" + entity.getShortCode();

        List<UrlAnalyticsResponse.ClickEventDTO> clickDTOs = (clicks != null)
                ? clicks.stream().map(c -> UrlAnalyticsResponse.ClickEventDTO.builder()
                        .clickedAt(c.getClickedAt())
                        .ipAddress(c.getIpAddress())
                        .userAgent(c.getUserAgent())
                        .referrer(c.getReferrer())
                        .build()).collect(Collectors.toList())
                : Collections.emptyList();

        return UrlAnalyticsResponse.builder()
                .shortCode(entity.getShortCode())
                .longUrl(entity.getLongUrl())
                .shortUrl(fullShortUrl)
                .totalClicks(entity.getClickCount())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .expiresAt(entity.getExpiresAt())
                .isExpired(entity.isExpired())
                .recentClicks(clickDTOs)
                .build();
    }
}
