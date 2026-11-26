package com.polyglotmesh.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the PolyglotMesh Spring Boot WebFlux reactive backend.
 * Running on Oracle GraalVM JDK 21.
 */
@SpringBootApplication
public class WebfluxApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebfluxApplication.class, args);
    }
}
