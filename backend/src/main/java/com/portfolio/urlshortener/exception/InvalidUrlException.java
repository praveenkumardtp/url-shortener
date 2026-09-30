package com.portfolio.urlshortener.exception;

public class InvalidUrlException extends UrlShortenerException {
    public InvalidUrlException(String message) {
        super(message);
    }
}
