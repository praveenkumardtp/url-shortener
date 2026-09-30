package com.portfolio.urlshortener.exception;

public class UrlExpiredException extends UrlShortenerException {
    public UrlExpiredException(String shortCode) {
        super(String.format("The short URL '%s' has expired and is no longer available.", shortCode));
    }
}
