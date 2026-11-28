package com.polyglotmesh.webflux.service;

import com.polyglotmesh.webflux.dto.ExecutionRequestDto;
import com.polyglotmesh.webflux.dto.ExecutionResponseDto;
import com.polyglotmesh.webflux.exception.EngineUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GraalVmExecutionBridgeServiceTest {

    private GraalVmExecutionBridgeService bridgeService;

    @BeforeEach
    void setUp() {
        bridgeService = new GraalVmExecutionBridgeService();
    }

    @Test
    @DisplayName("Verify bridge reports engine linked when polyglot-engine is on classpath")
    void testEngineAvailabilityWhenLinked() {
        assertTrue(bridgeService.isEngineAvailable(),
                "Engine should report available when polyglot-engine is linked on classpath");
    }

    @Test
    @DisplayName("Verify reflection bridge executes Python script successfully")
    void testBridgeExecutesPython() {
        ExecutionRequestDto request = new ExecutionRequestDto("python", "print('Bridge Hello')", 15000L);

        Mono<ExecutionResponseDto> executionMono = bridgeService.execute(request);

        StepVerifier.create(executionMono)
                .assertNext(response -> {
                    assertTrue(response.isSuccess());
                    assertEquals("Bridge Hello", response.getOutput());
                })
                .verifyComplete();
    }
}
