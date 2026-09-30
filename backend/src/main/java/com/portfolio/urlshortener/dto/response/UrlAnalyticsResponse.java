package com.portfolio.urlshortener.dto.response;

import com.portfolio.urlshortener.entity.UrlStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlAnalyticsResponse {
    private String shortCode;
    private String longUrl;
    private String shortUrl;
    private Long totalClicks;
    private UrlStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isExpired;
    private List<ClickEventDTO> recentClicks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClickEventDTO {
        private LocalDateTime clickedAt;
        private String ipAddress;
        private String userAgent;
        private String referrer;
    }
}
