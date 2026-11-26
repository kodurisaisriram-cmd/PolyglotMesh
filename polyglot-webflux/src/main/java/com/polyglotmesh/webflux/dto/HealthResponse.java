package com.polyglotmesh.webflux.dto;

import java.time.Instant;

/**
 * Data Transfer Object representing the health status of the WebFlux reactive backend.
 */
public class HealthResponse {

    private String status;
    private String application;
    private String module;
    private Instant timestamp;

    public HealthResponse() {
    }

    public HealthResponse(String status, String application, String module, Instant timestamp) {
        this.status = status;
        this.application = application;
        this.module = module;
        this.timestamp = timestamp;
    }

    public static HealthResponse up() {
        return new HealthResponse("UP", "PolyglotMesh", "polyglot-webflux", Instant.now());
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
