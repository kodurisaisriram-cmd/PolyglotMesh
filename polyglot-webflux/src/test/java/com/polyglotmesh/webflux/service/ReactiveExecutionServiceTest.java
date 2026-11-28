package com.polyglotmesh.webflux.service;

import com.polyglotmesh.service.PolyglotExecutionService;
import com.polyglotmesh.webflux.config.PolyglotEngineProperties;
import com.polyglotmesh.webflux.dto.ExecutionRequestDto;
import com.polyglotmesh.webflux.dto.ExecutionResponseDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReactiveExecutionServiceTest {

    private PolyglotExecutionService engine;
    private LanguageService languageService;
    private PolyglotEngineProperties properties;
    private ReactiveExecutionService executionService;

    @BeforeEach
    void setUp() {
        engine = new PolyglotExecutionService();
        properties = new PolyglotEngineProperties();
        languageService = new LanguageService(properties);
        executionService = new ReactiveExecutionService(engine, languageService, properties);
    }

    @AfterEach
    void tearDown() {
        executionService.shutdown();
    }

    @Test
    @DisplayName("Verify engine availability reports true when engine is injected")
    void testIsEngineAvailable() {
        assertTrue(executionService.isEngineAvailable());
    }

    @Test
    @DisplayName("Execute Python script successfully via reactive pipeline")
    void testExecutePythonScript() {
        ExecutionRequestDto request = new ExecutionRequestDto("python", "print('Hello PolyglotMesh')", 5000L);

        Mono<ExecutionResponseDto> responseMono = executionService.execute(request);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertTrue(response.isSuccess());
                    assertEquals("python", response.getLanguage());
                    assertEquals("Hello PolyglotMesh", response.getOutput());
                    assertNull(response.getError());
                    assertTrue(response.getExecutionTimeMs() >= 0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Execute JavaScript script successfully via reactive pipeline")
    void testExecuteJavaScriptScript() {
        ExecutionRequestDto request = new ExecutionRequestDto("javascript", "console.log('Hello PolyglotMesh');", 5000L);

        Mono<ExecutionResponseDto> responseMono = executionService.execute(request);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertTrue(response.isSuccess());
                    assertEquals("javascript", response.getLanguage());
                    assertEquals("Hello PolyglotMesh", response.getOutput());
                    assertNull(response.getError());
                    assertTrue(response.getExecutionTimeMs() >= 0);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Execute Python with alias 'py' normalizes canonical language and executes")
    void testExecuteWithPyAlias() {
        ExecutionRequestDto request = new ExecutionRequestDto("py", "print(10 + 20)", 5000L);

        Mono<ExecutionResponseDto> responseMono = executionService.execute(request);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertTrue(response.isSuccess());
                    assertEquals("python", response.getLanguage());
                    assertEquals("30", response.getOutput());
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("Execute JavaScript with alias 'js' normalizes canonical language and executes")
    void testExecuteWithJsAlias() {
        ExecutionRequestDto request = new ExecutionRequestDto("js", "console.log(40 + 2);", 5000L);

        Mono<ExecutionResponseDto> responseMono = executionService.execute(request);

        StepVerifier.create(responseMono)
                .assertNext(response -> {
                    assertTrue(response.isSuccess());
                    assertEquals("javascript", response.getLanguage());
                    assertEquals("42", response.getOutput());
                })
                .verifyComplete();
    }
}
