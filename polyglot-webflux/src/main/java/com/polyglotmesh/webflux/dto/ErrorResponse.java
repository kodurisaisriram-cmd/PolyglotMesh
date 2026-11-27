package com.polyglotmesh.webflux.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Standardized error response body returned by the reactive REST API.
 */
public class ErrorResponse {

    private boolean success = false;
    private int status;
    private String errorCode;
    private String error;
    private String message;
    private Instant timestamp;
    private String path;
    private List<String> details = new ArrayList<>();

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this(status, error, error, message, path, new ArrayList<>());
    }

    public ErrorResponse(int status, String error, String message, String path, List<String> details) {
        this(status, error, error, message, path, details);
    }

    public ErrorResponse(int status, String errorCode, String error, String message, String path, List<String> details) {
        this.success = false;
        this.status = status;
        this.errorCode = (errorCode != null) ? errorCode : error;
        this.error = error;
        this.message = message;
        this.timestamp = Instant.now();
        this.path = path;
        this.details = (details != null) ? details : new ArrayList<>();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details;
    }
}
