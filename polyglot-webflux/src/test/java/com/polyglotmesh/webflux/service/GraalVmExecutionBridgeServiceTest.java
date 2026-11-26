package com.polyglotmesh.webflux.service;

import com.polyglotmesh.webflux.dto.ExecutionRequestDto;
import com.polyglotmesh.webflux.dto.ExecutionResponseDto;
import com.polyglotmesh.webflux.exception.EngineUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertFalse;

class GraalVmExecutionBridgeServiceTest {

    private GraalVmExecutionBridgeService bridgeService;

    @BeforeEach
    void setUp() {
        bridgeService = new GraalVmExecutionBridgeService();
    }

    @Test
    @DisplayName("Verify bridge reports engine unlinked when polyglot-engine is not on classpath")
    void testEngineAvailabilityWhenPending() {
        assertFalse(bridgeService.isEngineAvailable(),
                "Engine should report unavailable until polyglot-engine is merged and linked");
    }

    @Test
    @DisplayName("Verify reactive pipeline emits EngineUnavailableException via StepVerifier")
    void testReactivePipelineErrorEmission() {
        ExecutionRequestDto request = new ExecutionRequestDto("python", "print('Hello')", 5000L);

        Mono<ExecutionResponseDto> executionMono = bridgeService.execute(request);

        StepVerifier.create(executionMono)
                .expectError(EngineUnavailableException.class)
                .verify();
    }
}
