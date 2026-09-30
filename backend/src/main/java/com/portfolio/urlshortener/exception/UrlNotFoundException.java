package com.portfolio.urlshortener.exception;

public class UrlNotFoundException extends UrlShortenerException {
    public UrlNotFoundException(String shortCode) {
        super(String.format("No URL mapping found for short code: '%s'", shortCode));
    }
}
