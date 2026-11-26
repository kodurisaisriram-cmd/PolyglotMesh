package com.polyglotmesh.webflux.service;

import com.polyglotmesh.webflux.dto.ExecutionRequestDto;
import com.polyglotmesh.webflux.dto.ExecutionResponseDto;
import reactor.core.publisher.Mono;

/**
 * Service contract for reactive code execution across supported polyglot runtimes.
 */
public interface ExecutionService {

    /**
     * Executes the given code snippet asynchronously without blocking Netty event loops.
     *
     * @param requestDto validated request containing language, code, and timeout
     * @return Mono emitting ExecutionResponseDto upon completion
     */
    Mono<ExecutionResponseDto> execute(ExecutionRequestDto requestDto);

    /**
     * Checks if the underlying GraalVM polyglot execution engine is currently loaded and available.
     *
     * @return true if engine is available on the classpath, false otherwise
     */
    boolean isEngineAvailable();
}
