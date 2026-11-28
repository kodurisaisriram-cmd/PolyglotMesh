package com.polyglotmesh.service;

import com.polyglotmesh.model.BenchmarkResponse;
import com.polyglotmesh.model.ExecutionResponse;

public class PolyglotBenchmarkService {

    private final PolyglotExecutionService executionService;

    public PolyglotBenchmarkService(PolyglotExecutionService executionService) {
        this.executionService = executionService;
    }

    /**
     * Executes code natively in GraalVM and benchmarks against simulated REST latency
     */
    public BenchmarkResponse benchmark(String language, String code) {
        // 1. Real GraalVM in-memory execution
        ExecutionResponse polyglotResponse = executionService.execute(language, code);

        long polyglotLatency = polyglotResponse.getExecutionTimeMs();

        // 2. Simulated Traditional Microservice REST Call:
        // Includes network trip (10-18ms) + JSON serialization overhead (3-5ms) + compute
        long simulatedNetworkTripMs = 12 + (long) (Math.random() * 8); // 12-20ms network latency
        long simulatedSerializationMs = 3 + (long) (Math.random() * 3); // 3-6ms JSON serialization
        long mockRestLatency = polyglotLatency + simulatedNetworkTripMs + simulatedSerializationMs;

        return new BenchmarkResponse(
            language,
            polyglotLatency,
            mockRestLatency,
            polyglotResponse.getOutput()
        );
    }
}