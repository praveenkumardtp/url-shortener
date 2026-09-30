package com.portfolio.urlshortener.repository;

import com.portfolio.urlshortener.entity.UrlClickAnalytics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlClickAnalyticsRepository extends JpaRepository<UrlClickAnalytics, Long> {
    Page<UrlClickAnalytics> findByShortUrlIdOrderByClickedAtDesc(Long shortUrlId, Pageable pageable);
    long countByShortUrlId(Long shortUrlId);
}
