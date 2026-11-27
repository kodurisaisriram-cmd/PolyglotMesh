package com.polyglotmesh.webflux.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class LanguageControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("GET /api/v1/languages returns HTTP 200 with supported languages, aliases, and runtime limits")
    void testGetSupportedLanguages() {
        webTestClient.get()
                .uri("/api/v1/languages")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.languages").isArray()
                .jsonPath("$.languages.length()").isEqualTo(2)
                .jsonPath("$.languages[0].id").isEqualTo("python")
                .jsonPath("$.languages[0].name").isEqualTo("Python")
                .jsonPath("$.languages[0].aliases").isArray()
                .jsonPath("$.languages[0].engineLanguage").isEqualTo("python")
                .jsonPath("$.languages[0].monacoLanguage").isEqualTo("python")
                .jsonPath("$.languages[1].id").isEqualTo("javascript")
                .jsonPath("$.languages[1].name").isEqualTo("JavaScript")
                .jsonPath("$.languages[1].aliases").isArray()
                .jsonPath("$.languages[1].engineLanguage").isEqualTo("js")
                .jsonPath("$.languages[1].monacoLanguage").isEqualTo("javascript")
                .jsonPath("$.defaultTimeoutMs").isEqualTo(10000)
                .jsonPath("$.minTimeoutMs").isEqualTo(100)
                .jsonPath("$.maxTimeoutMs").isEqualTo(30000)
                .jsonPath("$.maxCodeLength").isEqualTo(20000);
    }
}
