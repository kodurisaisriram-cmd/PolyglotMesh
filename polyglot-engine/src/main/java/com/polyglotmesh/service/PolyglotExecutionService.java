package com.polyglotmesh.service;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResponse;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PolyglotExecutionService {

    private final ScheduledExecutorService watchdog = Executors.newScheduledThreadPool(2);

    public ExecutionResponse execute(ExecutionRequest request) {
        if (request == null) {
            return new ExecutionResponse(false, null, null, "ExecutionRequest cannot be null", 0);
        }
        return execute(request.getLanguage(), request.getCode(), request.getTimeoutMs());
    }

    public ExecutionResponse execute(String language, String code) {
        return execute(language, code, 10000); // Default 10s timeout
    }

    public ExecutionResponse execute(String language, String code, long timeoutMs) {
        long startTime = System.currentTimeMillis();

        String langId = normalizeLanguage(language);
        if (langId == null) {
            long timeTaken = System.currentTimeMillis() - startTime;
            return new ExecutionResponse(false, language, null, 
                    "Unsupported language: '" + language + "'. Supported: python, js", timeTaken);
        }

        if (code == null || code.trim().isEmpty()) {
            long timeTaken = System.currentTimeMillis() - startTime;
            return new ExecutionResponse(false, langId, null, "Code cannot be empty", timeTaken);
        }

        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();

        // Safe Context with timeout watchdog
        try (Context context = Context.newBuilder(langId)
                .out(capturedOutput)
                .err(capturedOutput)
                .allowNativeAccess(false)
                .allowCreateProcess(false)
                .allowCreateThread(false)
                .allowHostAccess(HostAccess.NONE)
                .build()) {

            // Schedule the timeout watchdog
            ScheduledFuture<?> cancelTask = watchdog.schedule(() -> {
                context.close(true); // Force kill if timeout exceeded
            }, timeoutMs, TimeUnit.MILLISECONDS);

            Value evalResult;
            try {
                evalResult = context.eval(langId, code);
            } finally {
                cancelTask.cancel(false); // Cancel timer if code finishes normally
            }

            String stdoutOutput = capturedOutput.toString(StandardCharsets.UTF_8).trim();
            String finalOutput = !stdoutOutput.isEmpty() ? stdoutOutput : 
                                 (evalResult != null && !evalResult.isNull() ? evalResult.toString() : "");

            long executionTimeMs = System.currentTimeMillis() - startTime;
            return new ExecutionResponse(true, langId, finalOutput, null, executionTimeMs);

        } catch (PolyglotException pe) {
            long executionTimeMs = System.currentTimeMillis() - startTime;
            String errorMessage;

            if (pe.isCancelled()) {
                errorMessage = String.format("[TIMEOUT ERROR] Execution exceeded maximum time limit of %d ms. Forcefully aborted.", timeoutMs);
            } else if (pe.getMessage().toLowerCase().contains("denied") || 
                       pe.getMessage().toLowerCase().contains("permission") ||
                       pe.getMessage().toLowerCase().contains("operation not permitted") ||
                       pe.getMessage().toLowerCase().contains("security")) {
                errorMessage = "[SECURITY SANDBOX BLOCKED] " + pe.getMessage();
            } else if (pe.isSyntaxError()) {
                errorMessage = "[Syntax Error] " + pe.getMessage();
            } else if (pe.isGuestException()) {
                errorMessage = "[Runtime Error] " + pe.getMessage();
            } else {
                errorMessage = "[Execution Error] " + pe.getMessage();
            }

            return new ExecutionResponse(false, langId, null, errorMessage, executionTimeMs);

        } catch (Exception e) {
            long executionTimeMs = System.currentTimeMillis() - startTime;
            return new ExecutionResponse(false, langId, null, "[Internal Error] " + e.getMessage(), executionTimeMs);
        }
    }

    private String normalizeLanguage(String lang) {
        if (lang == null) return null;
        String clean = lang.trim().toLowerCase();
        if (clean.equals("python") || clean.equals("py")) return "python";
        if (clean.equals("javascript") || clean.equals("js")) return "js";
        return null;
    }
}