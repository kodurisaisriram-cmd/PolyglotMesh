package com.polyglotmesh.webflux.exception;

/**
 * Thrown when the polyglot execution engine is not available or pending module integration.
 */
public class EngineUnavailableException extends RuntimeException {

    public EngineUnavailableException(String message) {
        super(message);
    }

    public EngineUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
