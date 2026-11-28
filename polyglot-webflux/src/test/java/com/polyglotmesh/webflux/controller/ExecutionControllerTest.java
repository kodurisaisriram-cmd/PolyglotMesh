package com.polyglotmesh.webflux.controller;

import com.polyglotmesh.webflux.dto.ExecutionRequestDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient(timeout = "30000")
class ExecutionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("GET /api/v1/execute/status returns HTTP 200 with engine integration state INTEGRATED")
    void testEngineStatusEndpoint() {
        webTestClient.get()
                .uri("/api/v1/execute/status")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.engineAvailable").isEqualTo(true)
                .jsonPath("$.integrationState").isEqualTo("INTEGRATED")
                .jsonPath("$.engineModule").isEqualTo("com.polyglotmesh:polyglot-engine")
                .jsonPath("$.supportedLanguages[0]").isEqualTo("python")
                .jsonPath("$.supportedLanguages[1]").isEqualTo("js")
                .jsonPath("$.reactiveBridge").isEqualTo("Schedulers.boundedElastic");
    }

    @Test
    @DisplayName("POST /api/v1/execute with missing language returns HTTP 400 Bad Request")
    void testExecuteMissingLanguageReturns400() {
        ExecutionRequestDto invalidRequest = new ExecutionRequestDto(null, "print('Hello')", 5000L);

        webTestClient.post()
                .uri("/api/v1/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Validation Failed")
                .jsonPath("$.details").isNotEmpty();
    }

    @Test
    @DisplayName("POST /api/v1/execute with unsupported language returns HTTP 400 Bad Request")
    void testExecuteUnsupportedLanguageReturns400() {
        ExecutionRequestDto unsupportedLanguageRequest = new ExecutionRequestDto("ruby", "puts 'Hello'", 5000L);

        webTestClient.post()
                .uri("/api/v1/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(unsupportedLanguageRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Validation Failed");
    }

    @Test
    @DisplayName("POST /api/v1/execute with blank code returns HTTP 400 Bad Request")
    void testExecuteBlankCodeReturns400() {
        ExecutionRequestDto blankCodeRequest = new ExecutionRequestDto("python", "   ", 5000L);

        webTestClient.post()
                .uri("/api/v1/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(blankCodeRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Validation Failed");
    }

    @Test
    @DisplayName("POST /api/v1/execute with timeout less than 100 ms returns HTTP 400 Bad Request")
    void testExecuteInvalidTimeoutReturns400() {
        ExecutionRequestDto invalidTimeoutRequest = new ExecutionRequestDto("python", "print('Hello')", 50L);

        webTestClient.post()
                .uri("/api/v1/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidTimeoutRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400);
    }

    @Test
    @DisplayName("POST /api/v1/execute with timeout exceeding 30000 ms returns HTTP 400 Bad Request")
    void testExecuteTimeoutExceedingMaxReturns400() {
        ExecutionRequestDto excessiveTimeoutRequest = new ExecutionRequestDto("python", "print('Hello')", 35000L);

        webTestClient.post()
                .uri("/api/v1/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(excessiveTimeoutRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Validation Failed");
    }

    @Test
    @DisplayName("POST /api/v1/execute with code exceeding 20000 characters returns HTTP 400 Bad Request")
    void testExecuteExcessiveCodeLengthReturns400() {
        String oversizedCode = "a".repeat(20001);
        ExecutionRequestDto oversizedRequest = new ExecutionRequestDto("python", oversizedCode, 5000L);

        webTestClient.post()
                .uri("/api/v1/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(oversizedRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Validation Failed");
    }

    @Test
    @DisplayName("POST /api/v1/execute with malformed JSON body returns HTTP 400 Bad Request")
    void testExecuteMalformedJsonReturns400() {
        String malformedJson = "{ language: 'python', code: 'print(1)' "; // Missing closing brace

        webTestClient.post()
                .uri("/api/v1/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(malformedJson)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").isEqualTo("Bad Request");
    }

    @Test
    @DisplayName("POST /api/v1/execute with valid Python script executes on GraalVM engine and returns 200 OK")
    void testExecutePythonScriptReturns200() {
        ExecutionRequestDto validPythonRequest = new ExecutionRequestDto("python", "print('Hello PolyglotMesh')", 25000L);

        webTestClient.post()
                .uri("/api/v1/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validPythonRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.language").isEqualTo("python")
                .jsonPath("$.output").isEqualTo("Hello PolyglotMesh")
                .jsonPath("$.error").isEmpty()
                .jsonPath("$.executionTimeMs").isNumber();
    }

    @Test
    @DisplayName("POST /api/v1/execute with valid JavaScript script executes on GraalVM engine and returns 200 OK")
    void testExecuteJavaScriptScriptReturns200() {
        ExecutionRequestDto validJsRequest = new ExecutionRequestDto("javascript", "console.log('Hello PolyglotMesh');", 15000L);

        webTestClient.post()
                .uri("/api/v1/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validJsRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.language").isEqualTo("javascript")
                .jsonPath("$.output").isEqualTo("Hello PolyglotMesh")
                .jsonPath("$.error").isEmpty()
                .jsonPath("$.executionTimeMs").isNumber();
    }

    @Test
    @DisplayName("POST /api/v1/execute with valid alias 'py' normalizes and executes on GraalVM engine")
    void testExecuteWithPyAliasPassesValidation() {
        ExecutionRequestDto pyRequest = new ExecutionRequestDto("py", "print('Hello from py')", 25000L);

        webTestClient.post()
                .uri("/api/v1/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(pyRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.language").isEqualTo("python")
                .jsonPath("$.output").isEqualTo("Hello from py");
    }

    @Test
    @DisplayName("POST /api/v1/execute with valid alias 'js' normalizes and executes on GraalVM engine")
    void testExecuteWithJsAliasPassesValidation() {
        ExecutionRequestDto jsRequest = new ExecutionRequestDto("js", "console.log('Hello from js');", 15000L);

        webTestClient.post()
                .uri("/api/v1/execute")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.language").isEqualTo("javascript")
                .jsonPath("$.output").isEqualTo("Hello from js");
    }
}
