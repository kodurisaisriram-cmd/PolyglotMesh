package com.polyglotmesh.model;

public class ExecutionResponse {
    private boolean success;
    private String language;
    private String output;
    private String error;
    private long executionTimeMs;

    public ExecutionResponse() {
    }

    public ExecutionResponse(boolean success, String language, String output, String error, long executionTimeMs) {
        this.success = success;
        this.language = language;
        this.output = output;
        this.error = error;
        this.executionTimeMs = executionTimeMs;
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
        return String.format(
            "ExecutionResponse [success=%s, language=%s, time=%dms, output='%s']",
            success, language, executionTimeMs, output
        );
    }
}
