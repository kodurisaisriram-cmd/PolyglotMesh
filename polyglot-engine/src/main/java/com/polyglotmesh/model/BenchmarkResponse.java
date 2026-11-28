package com.polyglotmesh.model;

public class BenchmarkResponse {
    private String language;
    private long polyglotTimeMs;
    private long mockRestTimeMs;
    private double speedupRatio;
    private long latencySavedMs;
    private String output;

    public BenchmarkResponse() {}

    public BenchmarkResponse(String language, long polyglotTimeMs, long mockRestTimeMs, String output) {
        this.language = language;
        this.polyglotTimeMs = Math.max(polyglotTimeMs, 1); // Avoid division by zero
        this.mockRestTimeMs = mockRestTimeMs;
        this.speedupRatio = Math.round(((double) mockRestTimeMs / this.polyglotTimeMs) * 10.0) / 10.0;
        this.latencySavedMs = Math.max(0, mockRestTimeMs - this.polyglotTimeMs);
        this.output = output;
    }

    public String getLanguage() { return language; }
    public long getPolyglotTimeMs() { return polyglotTimeMs; }
    public long getMockRestTimeMs() { return mockRestTimeMs; }
    public double getSpeedupRatio() { return speedupRatio; }
    public long getLatencySavedMs() { return latencySavedMs; }
    public String getOutput() { return output; }
}