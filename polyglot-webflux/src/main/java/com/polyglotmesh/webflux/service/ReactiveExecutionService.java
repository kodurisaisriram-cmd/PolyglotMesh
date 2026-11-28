package com.polyglotmesh.webflux.service;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResponse;
import com.polyglotmesh.service.PolyglotExecutionService;
import com.polyglotmesh.webflux.config.PolyglotEngineProperties;
import com.polyglotmesh.webflux.dto.ExecutionRequestDto;
import com.polyglotmesh.webflux.dto.ExecutionResponseDto;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Reactive service integrating the GraalVM polyglot engine into Spring Boot WebFlux.
 *
 * Adheres to reactive non-blocking architecture by delegating blocking engine executions
 * to a dedicated, bounded elastic scheduler ("polyglot-worker") to protect Netty event-loop threads.
 */
@Service
@Primary
public class ReactiveExecutionService implements ExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ReactiveExecutionService.class);

    private final PolyglotExecutionService engine;
    private final LanguageService languageService;
    private final Scheduler executionScheduler;

    public ReactiveExecutionService(PolyglotExecutionService engine,
                                    LanguageService languageService,
                                    PolyglotEngineProperties properties) {
        this.engine = engine;
        this.languageService = languageService;
        this.executionScheduler = Schedulers.newBoundedElastic(
                properties.getThreadPoolSize(),
                properties.getTaskQueueCapacity(),
                "polyglot-worker"
        );
        log.info("Initialized ReactiveExecutionService with bounded scheduler (threads: {}, queue: {})",
                properties.getThreadPoolSize(), properties.getTaskQueueCapacity());
    }

    @Override
    public boolean isEngineAvailable() {
        return this.engine != null;
    }

    @Override
    public Mono<ExecutionResponseDto> execute(ExecutionRequestDto requestDto) {
        String canonicalLanguage = languageService.normalize(requestDto.getLanguage());
        String engineLanguageId = languageService.toEngineLanguageId(requestDto.getLanguage());

        ExecutionRequest engineRequest = new ExecutionRequest(
                engineLanguageId,
                requestDto.getCode(),
                requestDto.getTimeoutMs()
        );

        log.info("Submitting execution for language '{}' (engineId: '{}', timeout: {} ms) to dedicated scheduler",
                canonicalLanguage, engineLanguageId, requestDto.getTimeoutMs());

        return Mono.fromCallable(() -> {
                    log.debug("Executing script on worker thread [{}]", Thread.currentThread().getName());
                    return engine.execute(engineRequest);
                })
                .subscribeOn(executionScheduler)
                .map(response -> {
                    log.debug("Execution completed: success={}, time={} ms on thread [{}]",
                            response.isSuccess(), response.getExecutionTimeMs(), Thread.currentThread().getName());
                    return new ExecutionResponseDto(
                            response.isSuccess(),
                            canonicalLanguage,
                            response.getOutput(),
                            response.getError(),
                            response.getExecutionTimeMs()
                    );
                });
    }

    @PreDestroy
    public void shutdown() {
        if (!executionScheduler.isDisposed()) {
            log.info("Shutting down dedicated GraalVM execution scheduler");
            executionScheduler.dispose();
        }
    }
}
