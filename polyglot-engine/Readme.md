# PolyglotMesh — Core Execution Engine (`polyglot-engine`)

A high-performance, in-memory multi-language sandbox engine powered by **Oracle GraalVM 21** and the **Truffle Framework**.

## 🚀 Key Features
- **Zero Network Overhead**: Executes Python and JavaScript directly inside the JVM memory space without Docker or external runtime processes.
- **Unified Polyglot Service**: Single entry-point service (`PolyglotExecutionService`) supporting dynamic language execution.
- **Crash-Proof Error Handling**: Specialized `PolyglotException` interceptor distinguishing Syntax Errors, Guest Runtime Exceptions, and Validation Errors.
- **Stream Capture**: In-memory redirection of guest `stdout`/`stderr` into standard Java strings.
- **Sub-Millisecond Execution Tracking**: Built-in benchmark timers tracking execution time in milliseconds.


## 🛠 Tech Stack & Requirements
- **Java**: Oracle GraalVM JDK 21+ (`Oracle GraalVM 21.0.12+7.1`)
- **Languages Supported**:
  - Python (`v3.11.7` via GraalPy)
  - JavaScript (`v23.1.12` via GraalJS)
- **Build Tool**: Apache Maven 3.9+
- **SDK**: `org.graalvm.polyglot` (v24.1.0)

## ⚡ Quick Start

### 1. Build Project
mvn clean compile

### 2. Run Verification Suite
mvn exec:java -Dexec.mainClass="com.polyglotmesh.Main"