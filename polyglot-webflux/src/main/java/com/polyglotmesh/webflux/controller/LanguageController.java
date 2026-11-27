package com.polyglotmesh.webflux.controller;

import com.polyglotmesh.webflux.dto.SupportedLanguagesResponse;
import com.polyglotmesh.webflux.service.LanguageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Reactive REST Controller exposing supported guest languages and execution boundary parameters.
 * Provides the frontend with language metadata, aliases, and Monaco editor mappings.
 */
@RestController
@RequestMapping("/api/v1/languages")
public class LanguageController {

    private static final Logger log = LoggerFactory.getLogger(LanguageController.class);

    private final LanguageService languageService;

    public LanguageController(LanguageService languageService) {
        this.languageService = languageService;
    }

    /**
     * Retrieves the supported language runtimes, aliases, and execution boundary configurations.
     *
     * @return Mono containing the SupportedLanguagesResponse
     */
    @GetMapping
    public Mono<ResponseEntity<SupportedLanguagesResponse>> getSupportedLanguages() {
        log.debug("Received request for supported languages and runtime limits");
        return Mono.fromSupplier(languageService::getSupportedLanguagesResponse)
                .map(ResponseEntity::ok);
    }
}
