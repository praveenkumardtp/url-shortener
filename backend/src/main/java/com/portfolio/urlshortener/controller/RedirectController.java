package com.portfolio.urlshortener.controller;

import com.portfolio.urlshortener.service.UrlShortenerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@Tag(name = "Redirect Service", description = "High-speed URL redirection with Redis cache lookup and analytics logging")
public class RedirectController {

    private final UrlShortenerService shortenerService;

    @GetMapping("/{shortCode}")
    @Operation(summary = "Redirect to target long URL", description = "Resolves short code, records click event, and sends HTTP 302 Found redirect")
    public ResponseEntity<Void> redirect(
            @PathVariable String shortCode,
            HttpServletRequest request) {

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        String destinationUrl = shortenerService.resolveAndRecordClick(shortCode, ip, userAgent, referrer);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(destinationUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
