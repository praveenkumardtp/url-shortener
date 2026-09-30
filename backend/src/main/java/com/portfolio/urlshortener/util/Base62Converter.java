package com.portfolio.urlshortener.util;

import java.security.SecureRandom;

public final class Base62Converter {

    private static final String BASE62_ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    private Base62Converter() {}

    public static String encode(long value) {
        if (value <= 0) return "0";
        StringBuilder sb = new StringBuilder();
        long num = value;
        while (num > 0) {
            int remainder = (int) (num % 62);
            sb.append(BASE62_ALPHABET.charAt(remainder));
            num /= 62;
        }
        return sb.reverse().toString();
    }

    public static String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(BASE62_ALPHABET.charAt(RANDOM.nextInt(BASE62_ALPHABET.length())));
        }
        return sb.toString();
    }
}
