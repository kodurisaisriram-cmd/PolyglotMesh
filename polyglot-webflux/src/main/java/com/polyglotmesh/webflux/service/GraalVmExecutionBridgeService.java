package com.polyglotmesh.webflux.service;

import com.polyglotmesh.webflux.dto.ExecutionRequestDto;
import com.polyglotmesh.webflux.dto.ExecutionResponseDto;
import com.polyglotmesh.webflux.exception.EngineUnavailableException;
import com.polyglotmesh.webflux.exception.UnsupportedLanguageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Reactive bridge service that offloads synchronous GraalVM code execution
 * onto Project Reactor's boundedElastic scheduler to prevent blocking Netty event-loop threads.
 *
 * Designed to integrate with com.polyglotmesh.service.PolyglotExecutionService from polyglot-engine.
 */
@Service
public class GraalVmExecutionBridgeService implements ExecutionService {

    private static final Logger log = LoggerFactory.getLogger(GraalVmExecutionBridgeService.class);

    private static final String ENGINE_SERVICE_CLASS = "com.polyglotmesh.service.PolyglotExecutionService";
    private static final String ENGINE_REQUEST_CLASS = "com.polyglotmesh.model.ExecutionRequest";

    private final LanguageService languageService;
    private final Object engineInstance;
    private final Method executeMethod;
    private final Constructor<?> requestConstructor;
    private final boolean engineAvailable;

    public GraalVmExecutionBridgeService(LanguageService languageService) {
        this.languageService = languageService;
        Object instance = null;
        Method method = null;
        Constructor<?> constructor = null;
        boolean available = false;

        try {
            Class<?> serviceClass = Class.forName(ENGINE_SERVICE_CLASS);
            Class<?> requestClass = Class.forName(ENGINE_REQUEST_CLASS);

            instance = serviceClass.getDeclaredConstructor().newInstance();
            method = serviceClass.getMethod("execute", requestClass);
            constructor = requestClass.getConstructor(String.class, String.class, long.class);
            available = true;
            log.info("Successfully linked GraalVM PolyglotExecutionService engine into WebFlux bridge.");
        } catch (ClassNotFoundException e) {
            log.warn("GraalVM PolyglotEngine dependency (com.polyglotmesh:polyglot-engine) is not yet available on the classpath. " +
                    "WebFlux reactive foundation is active; integration will activate once polyglot-engine is merged into develop.");
        } catch (Exception e) {
            log.error("Failed to initialize PolyglotExecutionService engine bridge: {}", e.getMessage(), e);
        }

        this.engineInstance = instance;
        this.executeMethod = method;
        this.requestConstructor = constructor;
        this.engineAvailable = available;
    }

    public GraalVmExecutionBridgeService() {
        this(new LanguageService(new com.polyglotmesh.webflux.config.PolyglotEngineProperties()));
    }

    @Override
    public boolean isEngineAvailable() {
        return this.engineAvailable;
    }

    @Override
    public Mono<ExecutionResponseDto> execute(ExecutionRequestDto requestDto) {
        if (!engineAvailable) {
            log.warn("Execution request rejected: GraalVM engine module is pending integration.");
            return Mono.error(new EngineUnavailableException(
                    "GraalVM Polyglot Engine module (com.polyglotmesh:polyglot-engine:1.0.0-SNAPSHOT) " +
                    "is pending team merge from branch 'feature/backend-graalvm' into 'develop'. " +
                    "WebFlux reactive foundation is ready for direct module integration."));
        }

        validateLanguage(requestDto.getLanguage());

        // Offload blocking GraalVM execution to boundedElastic scheduler to protect Netty event-loop threads
        return Mono.fromCallable(() -> executeOnEngine(requestDto))
                .subscribeOn(Schedulers.boundedElastic())
                .doOnSubscribe(sub -> log.debug("Scheduled execution for language '{}' on boundedElastic scheduler", requestDto.getLanguage()))
                .doOnSuccess(res -> log.debug("Execution completed in {} ms (success={})", res.getExecutionTimeMs(), res.isSuccess()));
    }

    private ExecutionResponseDto executeOnEngine(ExecutionRequestDto requestDto) {
        log.info("Executing code snippet in language '{}' (timeout: {} ms) on worker thread [{}]",
                requestDto.getLanguage(), requestDto.getTimeoutMs(), Thread.currentThread().getName());

        try {
            String engineLanguage = languageService.toEngineLanguageId(requestDto.getLanguage());
            Object engineRequest = requestConstructor.newInstance(
                    engineLanguage,
                    requestDto.getCode(),
                    requestDto.getTimeoutMs()
            );

            Object engineResponse = executeMethod.invoke(engineInstance, engineRequest);

            // Extract response fields via reflection from ExecutionResponse
            Class<?> responseClass = engineResponse.getClass();
            boolean success = (boolean) responseClass.getMethod("isSuccess").invoke(engineResponse);
            String language = (String) responseClass.getMethod("getLanguage").invoke(engineResponse);
            String output = (String) responseClass.getMethod("getOutput").invoke(engineResponse);
            String error = (String) responseClass.getMethod("getError").invoke(engineResponse);
            long executionTimeMs = (long) responseClass.getMethod("getExecutionTimeMs").invoke(engineResponse);

            return new ExecutionResponseDto(success, language, output, error, executionTimeMs);

        } catch (Exception e) {
            log.error("Engine execution invocation error: {}", e.getMessage(), e);
            return ExecutionResponseDto.failure(
                    requestDto.getLanguage(),
                    "[Internal Engine Bridge Error] " + e.getMessage(),
                    0
            );
        }
    }

    private void validateLanguage(String language) {
        if (!languageService.isSupported(language)) {
            throw new UnsupportedLanguageException(language != null ? language : "null");
        }
    }
}
