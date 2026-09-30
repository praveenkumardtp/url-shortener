package com.portfolio.urlshortener.repository;

import com.portfolio.urlshortener.entity.ShortUrl;
import com.portfolio.urlshortener.entity.UrlStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
    Page<ShortUrl> findAllByOrderByCreatedAtDesc(Pageable pageable);
    long countByStatus(UrlStatus status);

    @Query("SELECT COALESCE(SUM(u.clickCount), 0) FROM ShortUrl u")
    Long sumTotalClicks();
}
