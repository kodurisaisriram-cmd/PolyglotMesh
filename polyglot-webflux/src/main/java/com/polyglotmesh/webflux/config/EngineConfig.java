package com.polyglotmesh.webflux.config;

import com.polyglotmesh.service.PolyglotExecutionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration providing a managed singleton bean for the GraalVM PolyglotExecutionService.
 * Ensures the engine and its watchdog are initialized once and shared across requests.
 */
@Configuration
public class EngineConfig {

    private static final Logger log = LoggerFactory.getLogger(EngineConfig.class);

    @Bean
    public PolyglotExecutionService polyglotExecutionService() {
        log.info("Initializing managed GraalVM PolyglotExecutionService bean");
        return new PolyglotExecutionService();
    }
}
