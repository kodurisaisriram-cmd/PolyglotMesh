package com.polyglotmesh.service;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResponse;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class PolyglotExecutionService {

    /**
     * Executes code based on request DTO
     */
    public ExecutionResponse execute(ExecutionRequest request) {
        return execute(request.getLanguage(), request.getCode());
    }

    /**
     * Core execution method supporting Python and JavaScript
     */
    public ExecutionResponse execute(String language, String code) {
        long startTime = System.currentTimeMillis();

        // Normalize language identifiers
        String langId = normalizeLanguage(language);
        if (langId == null) {
            long timeTaken = System.currentTimeMillis() - startTime;
            return new ExecutionResponse(false, language, null, 
                    "Unsupported language: '" + language + "'. Supported: python, js", timeTaken);
        }

        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();

        // Create isolated GraalVM Context with redirected stdout
        try (Context context = Context.newBuilder(langId)
                .out(capturedOutput)
                .err(capturedOutput)
                .build()) {

            Value evalResult = context.eval(langId, code);

            String stdoutOutput = capturedOutput.toString(StandardCharsets.UTF_8).trim();

            // If script printed output, use it; otherwise, use returned expression value
            String finalOutput = !stdoutOutput.isEmpty() ? stdoutOutput : 
                                 (evalResult != null && !evalResult.isNull() ? evalResult.toString() : "");

            long executionTimeMs = System.currentTimeMillis() - startTime;
            return new ExecutionResponse(true, langId, finalOutput, null, executionTimeMs);

        } catch (Exception e) {
            long executionTimeMs = System.currentTimeMillis() - startTime;
            return new ExecutionResponse(false, langId, null, e.getMessage(), executionTimeMs);
        }
    }

    /**
     * Convenience method for Python
     */
    public ExecutionResponse executePython(String code) {
        return execute("python", code);
    }

    /**
     * Convenience method for JavaScript
     */
    public ExecutionResponse executeJavaScript(String code) {
        return execute("js", code);
    }

    /**
     * Normalizes language input (e.g., 'javascript' -> 'js', 'py' -> 'python')
     */
    private String normalizeLanguage(String lang) {
        if (lang == null) return null;
        String clean = lang.trim().toLowerCase();
        if (clean.equals("python") || clean.equals("py")) {
            return "python";
        }
        if (clean.equals("javascript") || clean.equals("js")) {
            return "js";
        }
        return null;
    }
}