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
                                            |   BOUNDED ELASTIC THREADS   |
                                            |     [boundedElastic-1..N]   |
                                            | (Offloads blocking GraalVM) |
                                            +-----------------------------+
                                                           |
                                                           | Synchronous invoke
                                                           v
                                            +-----------------------------+
                                            |   PolyglotExecutionService  |
                                            |  (GraalVM Polyglot Engine)  |
                                            | Context: Python / JavaScript|
                                            +-----------------------------+
```

### Why Non-Blocking Matters (The Cardinal Rule of WebFlux)
In traditional Spring MVC (Tomcat), each incoming HTTP request is assigned a dedicated thread from a thread pool. When code blocks (e.g. waiting for code execution or database I/O), that thread sits idle. Under high concurrent load, the thread pool is exhausted, leading to request queuing and timeouts.

In **Spring WebFlux (Netty)**, a small, fixed number of event-loop threads (typically matching CPU core count) handle all incoming network traffic. **Never block a Netty event-loop thread.** If `.block()` is called on an event loop, the entire server stalls.

To safely execute synchronous GraalVM guest scripts, `GraalVmExecutionBridgeService` wraps execution in:
```java
Mono.fromCallable(() -> engine.execute(request))
    .subscribeOn(Schedulers.boundedElastic());
```
This cleanly shifts blocking CPU work to a dedicated pool (`boundedElastic`), allowing Netty to keep accepting new network requests uninterrupted.

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
    │   │   │   └── WebFluxConfig.java        # CORS and WebFlux configuration
    │   │   ├── controller/
    │   │   │   ├── HealthController.java     # GET /api/v1/health reactive controller
    │   │   │   └── ExecutionController.java  # POST /api/v1/execute & status controller
    │   │   ├── dto/
    │   │   │   ├── HealthResponse.java       # Health check DTO
    │   │   │   ├── ExecutionRequestDto.java  # Validated execution request payload
    │   │   │   ├── ExecutionResponseDto.java # Execution outcome payload
    │   │   │   └── ErrorResponse.java        # Standardized API error payload
    │   │   ├── exception/
    │   │   │   ├── EngineUnavailableException.java
    │   │   │   ├── UnsupportedLanguageException.java
    │   │   │   └── GlobalExceptionHandler.java # Reactive @RestControllerAdvice
    │   │   └── service/
    │   │       ├── ExecutionService.java     # Reactive service interface
    │   │       └── GraalVmExecutionBridgeService.java # Non-blocking GraalVM bridge
    │   └── resources/
    │       └── application.yml               # Reactive server and logging configuration
    └── test/
        └── java/com/polyglotmesh/webflux/
            ├── WebfluxApplicationTests.java  # Spring context loading test
            ├── controller/
            │   ├── HealthControllerTest.java # WebTestClient health & actuator tests
            │   └── ExecutionControllerTest.java # Validation & status tests
            └── service/
                └── GraalVmExecutionBridgeServiceTest.java # StepVerifier reactive test
```

---

## 6. Detailed Class Explanations (For Technical Assessment)

### Entrypoint & Configuration
1. **`WebfluxApplication`**:
   Annotated with `@SpringBootApplication`. Boots the application context using `SpringApplication.run()` and launches the embedded Netty server on port 8080.
2. **`WebFluxConfig`**:
   Implements `WebFluxConfigurer`. Configures Cross-Origin Resource Sharing (CORS) to permit frontend requests from Vite/React (`http://localhost:5173`) and Next.js/React (`http://localhost:3000`).

### REST Controllers
3. **`HealthController`**:
   Exposes `GET /api/v1/health`. Returns `Mono<ResponseEntity<HealthResponse>>`. Demonstrates reactive non-blocking response streaming without thread pauses.
4. **`ExecutionController`**:
   Exposes `POST /api/v1/execute` and diagnostic `GET /api/v1/execute/status`. Validates payloads with Jakarta Bean Validation (`@Valid`) and passes requests to `ExecutionService`.

### Services & Concurrency Bridge
5. **`ExecutionService`**:
   Defines the contract for non-blocking code execution (`Mono<ExecutionResponseDto> execute(ExecutionRequestDto requestDto)`).
6. **`GraalVmExecutionBridgeService`**:
   The architectural bridge. Detects whether `com.polyglotmesh.service.PolyglotExecutionService` exists on the classpath. If unlinked (engine pending branch merge), it cleanly throws `EngineUnavailableException`. When linked, it delegates execution on `Schedulers.boundedElastic()`.

### DTOs & Validation
7. **`ExecutionRequestDto`**:
   Validates incoming code snippets:
   - `@NotBlank` for language and code.
   - `@Pattern` restricting execution strictly to `python`, `py`, `javascript`, or `js`.
   - `@Size(max = 65536)` preventing payload memory overflow (64 KB ceiling).
   - `@Min(100)` and `@Max(60000)` validating execution timeout boundaries.
8. **`ExecutionResponseDto`**:
   Standardized response containing `success`, `language`, `output`, `error`, and `executionTimeMs`.
9. **`HealthResponse`**:
   Contains `status` ("UP"), `application` ("PolyglotMesh"), `module` ("polyglot-webflux"), and server `timestamp`.

### Exception Handling
10. **`GlobalExceptionHandler`**:
    Annotated with `@RestControllerAdvice`. Translates reactive validation errors (`WebExchangeBindException`), missing engine errors (`EngineUnavailableException`), and unhandled exceptions into uniform, predictable `ErrorResponse` JSON objects.

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

### 3. Engine Integration Status Diagnostic
- **URL**: `GET http://localhost:8080/api/v1/execute/status`
- **Expected Status**: `200 OK`
- **Sample Response**:
```json
{
  "engineAvailable": false,
  "engineModule": "com.polyglotmesh:polyglot-engine",
  "integrationState": "PENDING_MERGE",
  "supportedLanguages": ["python", "js"],
  "reactiveBridge": "Schedulers.boundedElastic"
}
```

### 4. Code Execution Endpoint (Contract)
- **URL**: `POST http://localhost:8080/api/v1/execute`
- **Headers**: `Content-Type: application/json`
- **Sample Request Payload**:
```json
{
  "language": "python",
  "code": "print('Hello')",
  "timeoutMs": 15000
}
```
- **Expected Successful Response (When Engine Linked)**:
```json
{
  "success": true,
  "language": "python",
  "output": "Hello",
  "error": null,
  "executionTimeMs": 142
}
```
- **Response While Engine is Pending Merge**:
  - Status: `503 Service Unavailable`
  - Body:
```json
{
  "status": 503,
  "error": "Engine Service Unavailable",
  "message": "GraalVM Polyglot Engine module (com.polyglotmesh:polyglot-engine:1.0.0-SNAPSHOT) is pending team merge from branch 'feature/backend-graalvm' into 'develop'. WebFlux reactive foundation is ready for direct module integration.",
  "timestamp": "2026-11-26T11:30:00.000000Z",
  "path": "/api/v1/execute",
  "details": []
}
```

### 5. Validation Error Examples
- **Request with Unsupported Language (`ruby`)**:
  - Status: `400 Bad Request`
  - Error: `Validation Failed`
  - Details: `["language: Unsupported language. Currently supported languages: python, js"]`
- **Request with Empty Code**:
  - Status: `400 Bad Request`
  - Error: `Validation Failed`
  - Details: `["code: Code cannot be empty"]`

---

## 11. GraalVM Integration Status & Team Coordination

### Current Integration Status: `PENDING_MERGE`
Teammate Aakash developed the initial `polyglot-engine` on branch `feature/backend-graalvm`. As of Week 1:
1. `polyglot-engine` has not yet been reviewed or merged into `develop`.
2. The engine POM contains invalid metadata:
   - `<modelVersion>3.9.16</modelVersion>` (must be corrected to `4.0.0`).
   - XML namespace schema URL points to `3.9.16.xsd` (must be `maven-4.0.0.xsd`).
3. Compiled `.class` files in `target/classes/` were committed to `feature/backend-graalvm` due to missing root `.gitignore` Java rules (which have now been fixed on `feature/backend-webflux`).

### Week 2 Integration Checklist
Once teammate Aakash corrects the engine POM and merges `polyglot-engine` into `develop`:
1. Add the engine dependency in `polyglot-webflux/pom.xml`:
   ```xml
   <dependency>
       <groupId>com.polyglotmesh</groupId>
       <artifactId>polyglot-engine</artifactId>
       <version>1.0.0-SNAPSHOT</version>
   </dependency>
   ```
2. Rebuild the project (`mvn clean compile`). `GraalVmExecutionBridgeService` will automatically detect `com.polyglotmesh.service.PolyglotExecutionService` on the classpath and seamlessly activate live execution without any architectural refactoring.
