package com.polyglotmesh.webflux.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object returned by GET /api/v1/languages.
 * Exposes supported guest languages, their aliases, and configurable execution boundary limits.
 */
public class SupportedLanguagesResponse {

    private List<LanguageInfoDto> languages = new ArrayList<>();
    private long defaultTimeoutMs;
    private long minTimeoutMs;
    private long maxTimeoutMs;
    private int maxCodeLength;

    public SupportedLanguagesResponse() {
    }

    public SupportedLanguagesResponse(List<LanguageInfoDto> languages, long defaultTimeoutMs, long minTimeoutMs, long maxTimeoutMs, int maxCodeLength) {
        this.languages = languages != null ? languages : new ArrayList<>();
        this.defaultTimeoutMs = defaultTimeoutMs;
        this.minTimeoutMs = minTimeoutMs;
        this.maxTimeoutMs = maxTimeoutMs;
        this.maxCodeLength = maxCodeLength;
    }

    public List<LanguageInfoDto> getLanguages() {
        return languages;
    }

    public void setLanguages(List<LanguageInfoDto> languages) {
        this.languages = languages;
    }

    public long getDefaultTimeoutMs() {
        return defaultTimeoutMs;
    }

    public void setDefaultTimeoutMs(long defaultTimeoutMs) {
        this.defaultTimeoutMs = defaultTimeoutMs;
    }

    public long getMinTimeoutMs() {
        return minTimeoutMs;
    }

    public void setMinTimeoutMs(long minTimeoutMs) {
        this.minTimeoutMs = minTimeoutMs;
    }

    public long getMaxTimeoutMs() {
        return maxTimeoutMs;
    }

    public void setMaxTimeoutMs(long maxTimeoutMs) {
        this.maxTimeoutMs = maxTimeoutMs;
    }

    public int getMaxCodeLength() {
        return maxCodeLength;
    }

    public void setMaxCodeLength(int maxCodeLength) {
        this.maxCodeLength = maxCodeLength;
    }

    @Override
    public String toString() {
        return "SupportedLanguagesResponse{" +
                "languages=" + languages +
                ", defaultTimeoutMs=" + defaultTimeoutMs +
                ", minTimeoutMs=" + minTimeoutMs +
                ", maxTimeoutMs=" + maxTimeoutMs +
                ", maxCodeLength=" + maxCodeLength +
                '}';
    }
}
