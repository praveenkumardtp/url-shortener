package com.portfolio.urlshortener.exception;

public class AliasAlreadyExistsException extends UrlShortenerException {
    public AliasAlreadyExistsException(String alias) {
        super(String.format("Custom alias '%s' is already reserved. Please choose another.", alias));
    }
}
