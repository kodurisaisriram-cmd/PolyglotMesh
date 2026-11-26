package com.polyglotmesh.webflux.exception;

/**
 * Thrown when an unsupported programming language is requested.
 */
public class UnsupportedLanguageException extends RuntimeException {

    private final String requestedLanguage;

    public UnsupportedLanguageException(String requestedLanguage) {
        super(String.format("Unsupported language: '%s'. Supported languages: python, js", requestedLanguage));
        this.requestedLanguage = requestedLanguage;
    }

    public String getRequestedLanguage() {
        return requestedLanguage;
    }
}
