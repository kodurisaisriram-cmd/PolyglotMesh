# PolyglotMesh — WebFlux Reactive Backend Module (`polyglot-webflux`)

## 1. Module Overview

`polyglot-webflux` is the reactive backend foundation of **PolyglotMesh**, a GraalVM-powered multi-language runtime sandbox platform. Built on **Spring Boot WebFlux** and **Project Reactor**, this module is engineered to provide high-throughput, non-blocking asynchronous REST APIs for code execution and system observability.

Running on **Oracle GraalVM JDK 21**, the module incorporates an architectural bridge designed to offload CPU-intensive, sandboxed polyglot language execution (Python, JavaScript) onto dedicated elastic worker threads while maintaining 100% non-blocking Netty event loops.

---

## 2. Developer Role & Assignment

- **Developer**: Meemansa
- **Role**: Java Development Associate L1 Intern
- **Branch**: `feature/backend-webflux`
- **Assigned Responsibilities**:
  - Initializing the Spring Boot WebFlux reactive project on GraalVM JDK 21.
  - Designing clean, scalable package structures following SOLID principles.
  - Implementing reactive health check and monitoring endpoints.
  - Establishing the reactive execution API contract (`POST /api/v1/execute`).
  - Designing a non-blocking bridge pattern for GraalVM Polyglot Engine integration.
  - Implementing automated reactive tests with JUnit 5, `WebTestClient`, and Reactor Test.

---

## 3. Technology Stack

| Technology | Version | Purpose |
| :--- | :--- | :--- |
| **Java / GraalVM** | `21.0.12 LTS` | Runtime environment with Polyglot capabilities |
| **Spring Boot** | `3.5.6` | Reactive web framework (`spring-boot-starter-webflux`) |
| **Embedded Server** | **Eclipse Netty** | Event-driven asynchronous, non-blocking network application framework |
| **Project Reactor** | `Core 3.7.x` | Reactive programming library implementing Reactive Streams |
| **Jakarta Validation** | `3.0.x` | Declarative payload validation annotations |
| **Spring Boot Actuator** | `3.5.6` | Operational health and readiness monitoring |
| **Testing** | JUnit 5, Mockito, `WebTestClient`, `StepVerifier` | Unit, integration, and reactive stream verification |
| **Build Tool** | **Apache Maven** `3.9.15` | Dependency management and build lifecycle |

---

## 4. Architecture & Reactive Concurrency Model

```
+-----------------------------------------------------------------------------------+
|                                 CLIENT / FRONTEND                                 |
|                       (React / Monaco Editor / Postman)                           |
+-----------------------------------------------------------------------------------+
                                       |
                                       | HTTP Request (Non-blocking I/O)
                                       v
+-----------------------------------------------------------------------------------+
|                             NETTY EVENT LOOP WORKERS                              |
|                          [reactor-http-epoll / nio]                               |
|                  (Handles 10,000+ concurrent network connections)                  |
+-----------------------------------------------------------------------------------+
                                       |
                   +-------------------+-------------------+
                   |                                       |
         GET /api/v1/health                       POST /api/v1/execute
                   |                                       |
                   v                                       v
        +----------------------+               +-----------------------+
        |   HealthController   |               |  ExecutionController  |
        +----------------------+               +-----------------------+
                   |                                       |
                   | Returns Mono<HealthResponse>          | Validates request
                   v                                       v
        +----------------------+               +-----------------------+
        | 200 OK (Instantaneous)|              |   ExecutionService    |
        +----------------------+               +-----------------------+
                                                           |
                                                           | subscribeOn(Schedulers.boundedElastic())
                                                           v
                                            +-----------------------------+
                                            |   DEDICATED BOUNDED WORKER   |
                                            |     [polyglot-worker-1..10]   |
                                            | (Offloads blocking GraalVM) |
                                            +-----------------------------+
                                                           |
                                                           | Synchronous invoke
                                                           v
                                            +-----------------------------+
                                            |   PolyglotExecutionService  |
                                            |  (Managed Spring Singleton)  |
                                            | Context: Python / JavaScript|
                                            +-----------------------------+
```

### Why Non-Blocking Matters (The Cardinal Rule of WebFlux)
In traditional Spring MVC (Tomcat), each incoming HTTP request is assigned a dedicated thread from a thread pool. When code blocks (e.g. waiting for code execution or database I/O), that thread sits idle. Under high concurrent load, the thread pool is exhausted, leading to request queuing and timeouts.

In **Spring WebFlux (Netty)**, a small, fixed number of event-loop threads (typically matching CPU core count) handle all incoming network traffic. **Never block a Netty event-loop thread.** If `.block()` is called on an event loop, the entire server stalls.

To safely execute synchronous GraalVM guest scripts, `ReactiveExecutionService` wraps execution in:
```java
Mono.fromCallable(() -> engine.execute(engineRequest))
    .subscribeOn(this.executionScheduler);
```
Where `executionScheduler` is a dedicated bounded elastic scheduler created with:
```java
Schedulers.newBoundedElastic(threadPoolSize, taskQueueCapacity, "polyglot-worker")
```
This isolates CPU-intensive GraalVM context compilation and evaluation to dedicated worker threads (`polyglot-worker-1..10`), preserving Netty event-loop threads (`reactor-http-nio-*`) for handling high-volume concurrent network I/O. Proper lifecycle management is ensured via `@PreDestroy` which cleanly disposes the scheduler on application shutdown.

---

## 5. Folder & Package Structure

```
polyglot-webflux/
├── pom.xml                                   # Maven build descriptor (Spring Boot 3.5.6)
├── README.md                                 # Module technical documentation
└── src/
    ├── main/
    │   ├── java/com/polyglotmesh/webflux/
    │   │   ├── WebfluxApplication.java       # Spring Boot main entrypoint
    │   │   ├── config/
    │   │   │   ├── EngineConfig.java         # Managed GraalVM PolyglotExecutionService bean
│   │   │   │   ├── PolyglotEngineProperties.java # Bounded execution properties
    │   │   │   └── WebFluxConfig.java        # CORS and WebFlux configuration
    │   │   ├── controller/
    │   │   │   ├── HealthController.java     # GET /api/v1/health reactive controller
    │   │   │   ├── LanguageController.java   # GET /api/v1/languages supported runtimes
    │   │   │   └── ExecutionController.java  # POST /api/v1/execute & status controller
    │   │   ├── dto/
    │   │   │   ├── HealthResponse.java       # Health check DTO
    │   │   │   ├── LanguageInfoDto.java      # Language metadata and Monaco mapping
    │   │   │   ├── SupportedLanguagesResponse.java # GET /api/v1/languages payload
    │   │   │   ├── ExecutionRequestDto.java  # Validated execution request payload
    │   │   │   ├── ExecutionResponseDto.java # Execution outcome payload
    │   │   │   └── ErrorResponse.java        # Standardized API error payload
    │   │   ├── exception/
    │   │   │   ├── EngineUnavailableException.java
    │   │   │   ├── UnsupportedLanguageException.java
    │   │   │   └── GlobalExceptionHandler.java # Reactive @RestControllerAdvice
    │   │   └── service/
    │   │       ├── ExecutionService.java     # Reactive service interface
    │   │       ├── LanguageService.java      # Normalization and language catalog
    │   │       └── ReactiveExecutionService.java # Primary reactive execution service (bounded worker)
│   │   │       └── GraalVmExecutionBridgeService.java # Classpath fallback/diagnostic bridge
    │   └── resources/
    │       └── application.yml               # Reactive server and logging configuration
    └── test/
        └── java/com/polyglotmesh/webflux/
            ├── WebfluxApplicationTests.java  # Spring context loading test
            ├── controller/
            │   ├── HealthControllerTest.java # WebTestClient health & actuator tests
            │   ├── LanguageControllerTest.java # WebTestClient language endpoint tests
            │   └── ExecutionControllerTest.java # Validation & status tests
            └── service/
                ├── LanguageServiceTest.java  # Normalization & alias unit tests
                └── ReactiveExecutionServiceTest.java # Reactive execution & worker thread verification tests
│               └── GraalVmExecutionBridgeServiceTest.java # StepVerifier reactive test
```

---

## 6. Detailed Class Explanations (For Technical Assessment)

### Entrypoint & Configuration
1. **`WebfluxApplication`**:
   Annotated with `@SpringBootApplication`. Boots the application context using `SpringApplication.run()` and launches the embedded Netty server on port 8080.
2. **`PolyglotEngineProperties`**:
   Annotated with `@ConfigurationProperties(prefix = "polyglot.engine")`. Centralizes bounded execution defaults (default timeout: 10,000 ms, min timeout: 100 ms, max timeout: 30,000 ms, max code length: 20,000 characters, threadPoolSize: 10, taskQueueCapacity: 100).
3. **`EngineConfig`**:
   Annotated with `@Configuration`. Declares a Spring-managed singleton `@Bean` for `com.polyglotmesh.service.PolyglotExecutionService`. Ensures proper engine lifecycle and single instance reuse across all reactive executions without recreating the GraalVM engine per request.
4. **`WebFluxConfig`**:
   Implements `WebFluxConfigurer`. Configures Cross-Origin Resource Sharing (CORS) to permit frontend requests from Vite/React (`http://localhost:5173`) and Next.js/React (`http://localhost:3000`).

### REST Controllers
4. **`HealthController`**:
   Exposes `GET /api/v1/health`. Returns `Mono<ResponseEntity<HealthResponse>>`. Demonstrates reactive non-blocking response streaming without thread pauses.
5. **`LanguageController`**:
   Exposes `GET /api/v1/languages`. Returns `Mono<ResponseEntity<SupportedLanguagesResponse>>`. Supplies the frontend with supported runtimes (Python, JavaScript), accepted aliases (`py`, `js`), Monaco editor IDs, and execution limit boundaries.
6. **`ExecutionController`**:
   Exposes `POST /api/v1/execute` and diagnostic `GET /api/v1/execute/status`. Validates payloads with Jakarta Bean Validation (`@Valid`) and passes requests to `ExecutionService`.

### Services & Concurrency Bridge
7. **`LanguageService`**:
   Manages language normalization and aliases at the API boundary. Converts incoming aliases (e.g. `py` -> `python`, `js` -> `javascript`) and maps canonical languages to internal GraalVM engine identifiers (`javascript` -> `js`). Strictly rejects unsupported languages (e.g. Ruby, Java).
8. **`ExecutionService`**:
   Defines the contract for non-blocking code execution (`Mono<ExecutionResponseDto> execute(ExecutionRequestDto requestDto)`).
9. **`ReactiveExecutionService`**:
   Annotated with `@Service` and `@Primary`. Implements `ExecutionService`. Handles non-blocking code execution by delegating blocking GraalVM engine calls to a dedicated bounded elastic scheduler (`polyglot-worker-1..10`, queue capacity: 100) via `Mono.fromCallable(...).subscribeOn(...)`. Normalizes input languages via `LanguageService`, constructs engine requests with validated bounds, and manages graceful scheduler disposal via `@PreDestroy`.
10. **`GraalVmExecutionBridgeService`**:
   Classpath fallback and diagnostic bridge. Detects whether `com.polyglotmesh.service.PolyglotExecutionService` exists on the classpath and provides status diagnostics for `GET /api/v1/execute/status`.

### DTOs & Validation
10. **`SupportedLanguagesResponse` & `LanguageInfoDto`**:
    Expose language catalog details, Monaco editor modes, and configured bounds to frontend clients.
11. **`ExecutionRequestDto`**:
    Validates incoming code snippets:
    - `@NotBlank` for language and code.
    - `@Pattern` restricting execution strictly to `python`, `py`, `javascript`, or `js`.
    - `@Size(max = 20000)` preventing payload memory overflow (20,000 characters ceiling).
    - `@Min(100)` and `@Max(30000)` validating execution timeout boundaries (100 ms to 30,000 ms).
12. **`ExecutionResponseDto`**:
    Standardized response containing `success`, `language`, `output`, `error`, and `executionTimeMs`.
13. **`HealthResponse`**:
    Contains `status` ("UP"), `application` ("PolyglotMesh"), `module` ("polyglot-webflux"), and server `timestamp`.

### Exception Handling
14. **`GlobalExceptionHandler`**:
    Annotated with `@RestControllerAdvice`. Translates reactive validation errors (`WebExchangeBindException`), malformed payloads (`ServerWebInputException`), missing engine errors (`EngineUnavailableException`), and unhandled exceptions into uniform, predictable `ErrorResponse` JSON objects containing `errorCode` and `success: false`.

---

## 7. Prerequisites & Environment

- **Operating System**: Windows 10/11 (or Linux/macOS)
- **JDK**: Oracle GraalVM JDK 21.0.12 LTS
  - Ensure `JAVA_HOME` points to your GraalVM installation:
    ```powershell
    $env:JAVA_HOME = "C:\Program Files\graalvm-jdk-21.0.12+7.1"
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
    ```
- **Maven**: Apache Maven 3.9.x
- **Git**: Configured with user credentials

---

## 8. Build & Test Instructions

### Build with Maven
Open terminal inside the repository root or `polyglot-webflux` directory:

```powershell
# Navigate to webflux directory
cd "E:\Infotact Project\PolyglotMesh\polyglot-webflux"

# Compile and run automated tests
mvn clean test

# Package into standalone executable JAR
mvn clean package
```

### Run the Application
```powershell
# Run with Spring Boot Maven plugin
mvn spring-boot:run

# Or run the packaged JAR directly
java -jar target/polyglot-webflux-0.0.1-SNAPSHOT.jar
```

The server binds to `http://127.0.0.1:8080`.

---

## 9. Eclipse IDE Import Instructions

1. Open **Eclipse IDE with Spring Tools (STS)**.
2. Select **File** -> **Import...**
3. Choose **Maven** -> **Existing Maven Projects** and click **Next**.
4. Browse to the root directory `E:\Infotact Project\PolyglotMesh\polyglot-webflux`.
5. Select `pom.xml` and click **Finish**.
6. Ensure Eclipse uses **Java 21**:
   - Right-click project -> **Properties** -> **Java Build Path** -> **Libraries**.
   - Verify `JRE System Library` is set to `GraalVM JDK 21`.
   - Under **Java Compiler**, ensure `Compiler compliance level` is set to `21`.
7. Right-click project -> **Run As** -> **Spring Boot App** or **JUnit Test**.

---

## 10. API Specification & Testing Guide

### 1. Health Check Endpoint
- **URL**: `GET http://localhost:8080/api/v1/health`
- **Headers**: `Accept: application/json`
- **Expected Status**: `200 OK`
- **Sample Response**:
```json
{
  "status": "UP",
  "application": "PolyglotMesh",
  "module": "polyglot-webflux",
  "timestamp": "2026-11-26T11:30:00.000000Z"
}
```

### 2. Spring Boot Actuator Health
- **URL**: `GET http://localhost:8080/actuator/health`
- **Expected Status**: `200 OK`
- **Sample Response**:
```json
{
  "status": "UP"
}
```

### 3. Supported Languages & Runtime Limits Endpoint (Week 2 Day 1)
- **URL**: `GET http://localhost:8080/api/v1/languages`
- **Headers**: `Accept: application/json`
- **Expected Status**: `200 OK`
- **Purpose**: Provides the frontend (React / Monaco Editor) with available runtime languages, accepted aliases, and execution boundaries.
- **Sample Response**:
```json
{
  "languages": [
    {
      "id": "python",
      "name": "Python",
      "aliases": ["python", "py"],
      "engineLanguage": "python",
      "monacoLanguage": "python",
      "version": "3.11 (GraalPy)"
    },
    {
      "id": "javascript",
      "name": "JavaScript",
      "aliases": ["javascript", "js"],
      "engineLanguage": "js",
      "monacoLanguage": "javascript",
      "version": "ES2023 (GraalJS)"
    }
  ],
  "defaultTimeoutMs": 10000,
  "minTimeoutMs": 100,
  "maxTimeoutMs": 30000,
  "maxCodeLength": 20000
}
```

### 4. Engine Integration Status Diagnostic
- **URL**: `GET http://localhost:8080/api/v1/execute/status`
- **Expected Status**: `200 OK`
- **Sample Response**:
```json
{
  "engineAvailable": true,
  "engineModule": "com.polyglotmesh:polyglot-engine:1.0.0-SNAPSHOT",
  "integrationState": "INTEGRATED",
  "supportedLanguages": ["python", "js"],
  "reactiveBridge": "Schedulers.newBoundedElastic (polyglot-worker)"
}
```

### 5. Code Execution Endpoint (Live Reactive Execution)
- **URL**: `POST http://localhost:8080/api/v1/execute`
- **Headers**: `Content-Type: application/json`
- **Validation Rules**:
  - `language`: Required, non-blank. Must match `python`, `py`, `javascript`, or `js` (case-insensitive).
  - `code`: Required, non-blank. Maximum length: **20,000 characters**.
  - `timeoutMs`: Optional (defaults to `10000`). Bounded between **100 ms** and **30,000 ms**.
- **Sample Request Payload (Python)**:
```json
{
  "language": "python",
  "code": "print('Hello PolyglotMesh')",
  "timeoutMs": 15000
}
```
- **Successful Response (Python)**:
```json
{
  "success": true,
  "language": "python",
  "output": "Hello PolyglotMesh\n",
  "error": null,
  "executionTimeMs": 142
}
```
- **Sample Request Payload (JavaScript)**:
```json
{
  "language": "javascript",
  "code": "console.log('Hello PolyglotMesh');",
  "timeoutMs": 5000
}
```
- **Successful Response (JavaScript)**:
```json
{
  "success": true,
  "language": "javascript",
  "output": "Hello PolyglotMesh\n",
  "error": null,
  "executionTimeMs": 36
}
```

### 6. Validation Error Examples
- **Request with Unsupported Language (`ruby` or `java`)**:
  - Status: `400 Bad Request`
  - Body:
```json
{
  "success": false,
  "status": 400,
  "errorCode": "VALIDATION_FAILED",
  "error": "Validation Failed",
  "message": "Request body contains invalid fields",
  "path": "/api/v1/execute",
  "details": ["language: Unsupported language. Currently supported languages: python, javascript (aliases: py, js)"]
}
```
- **Request with Empty Code**:
  - Status: `400 Bad Request`
  - Details: `["code: Code cannot be empty"]`
- **Request with Excessive Code (> 20,000 chars)**:
  - Status: `400 Bad Request`
  - Details: `["code: Code size exceeds maximum limit of 20000 characters"]`
- **Request with Out-of-Bounds Timeout (< 100 ms or > 30,000 ms)**:
  - Status: `400 Bad Request`
  - Details: `["timeoutMs: Timeout cannot exceed 30000 ms (30 seconds)"]`
- **Request with Malformed JSON Payload**:
  - Status: `400 Bad Request`
  - Error: `Bad Request`
  - ErrorCode: `MALFORMED_REQUEST`

---

## 11. GraalVM Integration Status & Team Coordination

### Current Integration Status: `INTEGRATED` (Week 2 Day 2)
The GraalVM execution engine (`com.polyglotmesh:polyglot-engine:1.0.0-SNAPSHOT`) was integrated into `polyglot-webflux` via standard Maven coordinates.
- **Managed Lifecycle**: `EngineConfig` exposes `PolyglotExecutionService` as a singleton bean.
- **Dedicated Concurrency**: `ReactiveExecutionService` delegates execution to a bounded worker scheduler (`polyglot-worker-1..10`, queue capacity: 100), ensuring Netty event loops are never blocked.
- **Supported Guest Runtimes**: Python (`GraalPy 24.1.0`) and JavaScript (`GraalJS 24.1.0`).
- **Engine Execution**: Both Python scripts (`print(...)`) and JavaScript scripts (`console.log(...)`) execute live with output capture, status reporting, and timeout watchdog support.
