package com.polyglotmesh.webflux.controller;

import com.polyglotmesh.webflux.dto.HealthResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Reactive REST Controller providing health status for the polyglot-webflux module.
 */
@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private static final Logger log = LoggerFactory.getLogger(HealthController.class);

    @GetMapping
    public Mono<ResponseEntity<HealthResponse>> getHealth() {
        log.debug("Received health check request for polyglot-webflux");
        return Mono.fromSupplier(HealthResponse::up)
                .map(ResponseEntity::ok);
    }
}
