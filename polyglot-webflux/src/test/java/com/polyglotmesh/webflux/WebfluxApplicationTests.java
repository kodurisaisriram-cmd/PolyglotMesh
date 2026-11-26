package com.polyglotmesh.webflux;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebfluxApplicationTests {

    @Test
    @DisplayName("Verify Spring Boot application context loads successfully on GraalVM JDK 21")
    void contextLoads() {
        // Test passes if application context initializes without exception
    }
}
