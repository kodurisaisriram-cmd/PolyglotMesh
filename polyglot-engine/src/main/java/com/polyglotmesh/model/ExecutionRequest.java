package com.polyglotmesh.model;

public class ExecutionRequest {
    private String language;
    private String code;
    private long timeoutMs = 3000; // Default 3 seconds timeout

    public ExecutionRequest() {
        
    }

    public ExecutionRequest(String language, String code) {
        this.language = language;
        this.code = code;
        this.timeoutMs = 3000;
    }

    public ExecutionRequest(String language, String code, long timeoutMs) {
        this.language = language;
        this.code = code;
        this.timeoutMs = timeoutMs;
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

    public long getTimeoutMs() { 
        return timeoutMs; 
    }

    public void setTimeoutMs(long timeoutMs) {
         this.timeoutMs = timeoutMs; 
        }
}