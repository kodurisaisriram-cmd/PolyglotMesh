package com.polyglotmesh.webflux.dto;

/**
 * Data Transfer Object representing the result of a code execution.
 * Compatible with com.polyglotmesh.model.ExecutionResponse from polyglot-engine.
 */
public class ExecutionResponseDto {

    private boolean success;
    private String language;
    private String output;
    private String error;
    private long executionTimeMs;

    public ExecutionResponseDto() {
    }

    public ExecutionResponseDto(boolean success, String language, String output, String error, long executionTimeMs) {
        this.success = success;
        this.language = language;
        this.output = output;
        this.error = error;
        this.executionTimeMs = executionTimeMs;
    }

    public static ExecutionResponseDto success(String language, String output, long executionTimeMs) {
        return new ExecutionResponseDto(true, language, output, null, executionTimeMs);
    }

    public static ExecutionResponseDto failure(String language, String error, long executionTimeMs) {
        return new ExecutionResponseDto(false, language, null, error, executionTimeMs);
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }

    @Override
    public String toString() {
        return "ExecutionResponseDto{" +
                "success=" + success +
                ", language='" + language + '\'' +
                ", output='" + output + '\'' +
                ", error='" + error + '\'' +
                ", executionTimeMs=" + executionTimeMs +
                '}';
    }
}
