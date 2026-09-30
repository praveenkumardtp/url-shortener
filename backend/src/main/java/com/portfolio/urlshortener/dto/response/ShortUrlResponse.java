package com.portfolio.urlshortener.dto.response;

import com.portfolio.urlshortener.entity.UrlStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShortUrlResponse {
    private Long id;
    private String longUrl;
    private String shortCode;
    private String shortUrl; // Full clickable short URL
    private String customAlias;
    private Long clickCount;
    private UrlStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isExpired;
}
