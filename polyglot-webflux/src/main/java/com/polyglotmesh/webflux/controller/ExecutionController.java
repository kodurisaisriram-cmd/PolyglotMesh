package com.polyglotmesh.webflux.controller;

import com.polyglotmesh.webflux.dto.ExecutionRequestDto;
import com.polyglotmesh.webflux.dto.ExecutionResponseDto;
import com.polyglotmesh.webflux.service.ExecutionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Reactive REST Controller handling code execution requests across polyglot languages.
 */
@RestController
@RequestMapping("/api/v1/execute")
public class ExecutionController {

    private static final Logger log = LoggerFactory.getLogger(ExecutionController.class);

    private final ExecutionService executionService;

    public ExecutionController(ExecutionService executionService) {
        this.executionService = executionService;
    }

    /**
     * Executes a validated polyglot code snippet asynchronously.
     *
     * @param requestDto validated execution request payload
     * @return Mono containing the execution result
     */
    @PostMapping
    public Mono<ResponseEntity<ExecutionResponseDto>> executeCode(
            @Valid @RequestBody ExecutionRequestDto requestDto) {
        log.info("Received execution request for language: '{}', timeout: {} ms",
                requestDto.getLanguage(), requestDto.getTimeoutMs());

        return executionService.execute(requestDto)
                .map(ResponseEntity::ok);
    }

    /**
     * Diagnostic endpoint exposing the current engine integration status and supported languages.
     *
     * @return Mono containing integration status details
     */
    @GetMapping("/status")
    public Mono<ResponseEntity<Map<String, Object>>> getEngineStatus() {
        boolean available = executionService.isEngineAvailable();
        Map<String, Object> status = Map.of(
                "engineAvailable", available,
                "engineModule", "com.polyglotmesh:polyglot-engine",
                "integrationState", available ? "INTEGRATED" : "PENDING_MERGE",
                "supportedLanguages", List.of("python", "js"),
                "reactiveBridge", "Schedulers.boundedElastic"
        );
        return Mono.just(ResponseEntity.ok(status));
    }
}
