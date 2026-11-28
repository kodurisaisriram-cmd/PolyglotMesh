package com.polyglotmesh.webflux.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Strongly-typed configuration properties for the PolyglotMesh execution engine.
 * Maps to the "polyglot.engine" configuration namespace in application.yml.
 */
@Component
@ConfigurationProperties(prefix = "polyglot.engine")
public class PolyglotEngineProperties {

    public static final long DEFAULT_TIMEOUT_MS = 10000L;
    public static final long MIN_TIMEOUT_MS = 100L;
    public static final long MAX_TIMEOUT_MS = 30000L;
    public static final int MAX_CODE_LENGTH = 20000;
    public static final int DEFAULT_THREAD_POOL_SIZE = 10;
    public static final int DEFAULT_TASK_QUEUE_CAPACITY = 100;

    private long defaultTimeoutMs = DEFAULT_TIMEOUT_MS;
    private long minTimeoutMs = MIN_TIMEOUT_MS;
    private long maxTimeoutMs = MAX_TIMEOUT_MS;
    private int maxCodeLength = MAX_CODE_LENGTH;
    private int threadPoolSize = DEFAULT_THREAD_POOL_SIZE;
    private int taskQueueCapacity = DEFAULT_TASK_QUEUE_CAPACITY;
    private List<String> supportedLanguages = new ArrayList<>(List.of("python", "javascript"));

    public PolyglotEngineProperties() {
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public int getTaskQueueCapacity() {
        return taskQueueCapacity;
    }

    public void setTaskQueueCapacity(int taskQueueCapacity) {
        this.taskQueueCapacity = taskQueueCapacity;
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

    public List<String> getSupportedLanguages() {
        return supportedLanguages;
    }

    public void setSupportedLanguages(List<String> supportedLanguages) {
        this.supportedLanguages = supportedLanguages;
    }
}
