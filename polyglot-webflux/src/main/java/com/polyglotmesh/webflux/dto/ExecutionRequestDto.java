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
        message = "Unsupported language. Currently supported languages: python, javascript (aliases: py, js)"
    )
    private String language;

    @NotBlank(message = "Code cannot be empty")
    @Size(max = 20000, message = "Code size exceeds maximum limit of 20000 characters")
    private String code;

    @Min(value = 100, message = "Timeout must be at least 100 ms")
    @Max(value = 30000, message = "Timeout cannot exceed 30000 ms (30 seconds)")
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

    /**
     * Returns the normalized canonical language identifier ("python" or "javascript").
     *
     * @return canonical language name, or original trimmed lower-case language string
     */
    public String getNormalizedLanguage() {
        if (language == null) {
            return null;
        }
        String clean = language.trim().toLowerCase();
        if (clean.equals("python") || clean.equals("py")) {
            return "python";
        }
        if (clean.equals("javascript") || clean.equals("js")) {
            return "javascript";
        }
        return clean;
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
