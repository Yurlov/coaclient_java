package com.coursera.oauth2_0.exception;

public class TokenNotGeneratedException extends Exception {

    private static final long serialVersionUID = 1L;

    public TokenNotGeneratedException(String message) {
        super(message);
    }
}
