package com.polyglotmesh.webflux.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object representing an incoming code execution request.
 * Compatible with com.polyglotmesh.model.ExecutionRequest from polyglot-engine.
 */
public class ExecutionRequestDto {

    @NotBlank(message = "Language is required")
    @Pattern(
        regexp = "(?i)^(python|py|javascript|js)$",
        message = "Unsupported language. Currently supported languages: python, js"
    )
    private String language;

    @NotBlank(message = "Code cannot be empty")
    @Size(max = 65536, message = "Code size exceeds maximum limit of 64 KB (65536 characters)")
    private String code;

    @Min(value = 100, message = "Timeout must be at least 100 ms")
    @Max(value = 60000, message = "Timeout cannot exceed 60000 ms (60 seconds)")
    private Long timeoutMs = 10000L; // Default 10 seconds timeout

    public ExecutionRequestDto() {
    }

    public ExecutionRequestDto(String language, String code) {
        this.language = language;
        this.code = code;
        this.timeoutMs = 10000L;
    }

    public ExecutionRequestDto(String language, String code, Long timeoutMs) {
        this.language = language;
        this.code = code;
        this.timeoutMs = (timeoutMs != null) ? timeoutMs : 10000L;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Long getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(Long timeoutMs) {
        this.timeoutMs = (timeoutMs != null) ? timeoutMs : 10000L;
    }

    @Override
    public String toString() {
        return "ExecutionRequestDto{" +
                "language='" + language + '\'' +
                ", codeLength=" + (code != null ? code.length() : 0) +
                ", timeoutMs=" + timeoutMs +
                '}';
    }
}
