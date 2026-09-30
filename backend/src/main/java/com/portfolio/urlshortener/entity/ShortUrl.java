package com.portfolio.urlshortener.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "short_urls", indexes = {
    @Index(name = "idx_short_code", columnList = "shortCode", unique = true),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShortUrl {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2048)
    private String longUrl;

    @Column(nullable = false, unique = true, length = 32)
    private String shortCode;

    @Column(length = 64)
    private String customAlias;

    @Builder.Default
    @Column(nullable = false)
    private Long clickCount = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UrlStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.clickCount == null) this.clickCount = 0L;
        if (this.status == null) this.status = UrlStatus.ACTIVE;
    }

    public boolean isExpired() {
        return (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) || status == UrlStatus.EXPIRED;
    }
}
