package com.polyglotmesh;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("      PolyglotMesh - Multi-Language      ");
        System.out.println("=========================================\n");

        // ----------------- PYTHON -----------------
        System.out.println(">>> PYTHON EXECUTION ENGINE <<<");
        runPythonPricingAlgorithm();
        runPythonWithCapturedOutput();

        // ----------------- JAVASCRIPT -----------------
        System.out.println(">>> JAVASCRIPT EXECUTION ENGINE <<<");
        runBasicJavaScript();
        runJavaScriptValidation();

        // ----------------- UNIFIED POLYGLOT ------------------
        System.out.println(">>> UNIFIED POLYGLOT TEST <<<");
        executeAnyCode("python", "print('Python Engine: Active & Running')");
        executeAnyCode("js", "console.log('JavaScript Engine: Active & Running');");

         }

    // ==================== PYTHON METHODS ====================

    private static void runPythonPricingAlgorithm() {
        System.out.println("[Python] Executing Dynamic Pricing Engine:");
        try (Context context = Context.newBuilder("python").build()) {
            String script = 
                "base_price = 100\n" +
                "demand_factor = 1.5\n" +
                "final_price = base_price * demand_factor\n" +
                "final_price";

            Value result = context.eval("python", script);
            System.out.println("  Calculated Price: $" + result.asDouble());
        } catch (Exception e) {
            System.err.println("  Python Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void runPythonWithCapturedOutput() {
        System.out.println("[Python] Capturing stdout stream into Java:");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (Context context = Context.newBuilder("python").out(output).build()) {
            context.eval("python", "print('Python says: Processing complete!')");
            System.out.println("  Captured: " + output.toString(StandardCharsets.UTF_8).trim());
        } catch (Exception e) {
            System.err.println("  Capture Error: " + e.getMessage());
        }
        System.out.println();
    }

    // ==================== JAVASCRIPT METHODS =================

    private static void runBasicJavaScript() {
        System.out.println("[JavaScript] Executing Basic console.log:");
        try (Context context = Context.newBuilder("js").build()) {
            context.eval("js", "console.log('Hello from JavaScript!');");
        } catch (Exception e) {
            System.err.println("  JS Error: " + e.getMessage());
        }
        System.out.println();
    }

    private static void runJavaScriptValidation() {
        System.out.println("[JavaScript] Executing Validation Logic:");
        try (Context context = Context.newBuilder("js").build()) {
            String jsScript = 
                "const price = 150;\n" +
                "const tier = price > 100 ? 'Premium Tier' : 'Standard Tier';\n" +
                "console.log('Evaluated Price: $' + price + ' -> ' + tier);\n" +
                "tier;";

            Value result = context.eval("js", jsScript);
            System.out.println("  Returned to Java: " + result.asString());
        } catch (Exception e) {
            System.err.println("  Validation Error: " + e.getMessage());
        }
        System.out.println();
    }

    // ==================== UNIFIED RUNNER ============================

    private static void executeAnyCode(String language, String code) {
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();
        try (Context context = Context.newBuilder("python", "js")
                .out(capturedOutput)
                .build()) {

            context.eval(language, code);
            String result = capturedOutput.toString(StandardCharsets.UTF_8).trim();
            System.out.println("  [" + language.toUpperCase() + " Output]: " + result);

        } catch (Exception e) {
            System.err.println("  Error: " + e.getMessage());
        }
    }
}