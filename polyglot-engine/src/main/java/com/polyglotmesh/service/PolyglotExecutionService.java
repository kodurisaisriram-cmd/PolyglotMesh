package com.polyglotmesh.service;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResponse;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class PolyglotExecutionService {

    public ExecutionResponse execute(ExecutionRequest request) {
        if (request == null) {
            return new ExecutionResponse(false, null, null, "ExecutionRequest cannot be null", 0);
        }
        return execute(request.getLanguage(), request.getCode());
    }

    public ExecutionResponse execute(String language, String code) {
        long startTime = System.currentTimeMillis();

        // 1. Validate Language
        String langId = normalizeLanguage(language);
        if (langId == null) {
            long timeTaken = System.currentTimeMillis() - startTime;
            return new ExecutionResponse(false, language, null, 
                    "Unsupported language: '" + language + "'. Supported: python, js", timeTaken);
        }

        // 2. Validate Code Payload
        if (code == null || code.trim().isEmpty()) {
            long timeTaken = System.currentTimeMillis() - startTime;
            return new ExecutionResponse(false, langId, null, "Code cannot be empty", timeTaken);
        }

        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();

        // 3. Safe In-Memory Execution
        try (Context context = Context.newBuilder(langId)
                .out(capturedOutput)
                .err(capturedOutput)
                .build()) {

            Value evalResult = context.eval(langId, code);

            String stdoutOutput = capturedOutput.toString(StandardCharsets.UTF_8).trim();
            String finalOutput = !stdoutOutput.isEmpty() ? stdoutOutput : 
                                 (evalResult != null && !evalResult.isNull() ? evalResult.toString() : "");

            long executionTimeMs = System.currentTimeMillis() - startTime;
            return new ExecutionResponse(true, langId, finalOutput, null, executionTimeMs);

        } catch (PolyglotException pe) {
            long executionTimeMs = System.currentTimeMillis() - startTime;
            String errorMessage;

            if (pe.isSyntaxError()) {
                errorMessage = "[Syntax Error] " + pe.getMessage();
            } else if (pe.isGuestException()) {
                errorMessage = "[Runtime Error] " + pe.getMessage();
            } else if (pe.isCancelled()) {
                errorMessage = "[Timeout Error] Execution was cancelled or timed out.";
            } else {
                errorMessage = "[Execution Error] " + pe.getMessage();
            }

            return new ExecutionResponse(false, langId, null, errorMessage, executionTimeMs);

        } catch (Exception e) {
            long executionTimeMs = System.currentTimeMillis() - startTime;
            return new ExecutionResponse(false, langId, null, "[Internal Error] " + e.getMessage(), executionTimeMs);
        }
    }

    public ExecutionResponse executePython(String code) {
        return execute("python", code);
    }

    public ExecutionResponse executeJavaScript(String code) {
        return execute("js", code);
    }

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