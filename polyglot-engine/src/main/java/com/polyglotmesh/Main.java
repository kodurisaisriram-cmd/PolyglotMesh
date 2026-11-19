package com.polyglotmesh;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
         System.out.println("PolyglotMesh - Python Engine");
        
        // Milestone 1: Basic Python print execution
        runBasicPython();

        // Milestone 2: Python script with variables & math
        runPythonPricingAlgorithm();

        // Milestone 3: Capturing Python stdout into a Java String
        runPythonWithCapturedOutput();
    }

    /**
     * Milestone 1: Direct Python execution
     */
    private static void runBasicPython() {
        System.out.println("[Milestone 1] Executing Basic Python Statement:");
        try (Context context = Context.newBuilder("python").build()) {
            // Executing Python directly inside JVM
            context.eval("python", "print('Hello from Python!')");
        } catch (Exception e) {
            System.err.println("Python Execution Error: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * Milestone 2: Multi-line Python calculation
     */
    private static void runPythonPricingAlgorithm() {
        System.out.println("[Milestone 2] Executing Python Dynamic Pricing Engine:");
        try (Context context = Context.newBuilder("python").build()) {
            String pythonScript = 
                "base_price = 100\n" +
                "demand_factor = 1.5\n" +
                "final_price = base_price * demand_factor\n" +
                "final_price"; // last expression is returned as Value

            Value result = context.eval("python", pythonScript);
            System.out.println("  Calculated Final Price: $" + result.asDouble());
        } catch (Exception e) {
            System.err.println("Pricing Error: " + e.getMessage());
        }
        System.out.println();
    }

    /**
     * Milestone 3: Capturing Python console output into a Java String
     */
    private static void runPythonWithCapturedOutput() {
        System.out.println("[Milestone 3] Capturing Python stdout into Java variable:");

        ByteArrayOutputStream capturedOutputStream = new ByteArrayOutputStream();

        // Redirect Python's stdout to our in-memory Java stream
        try (Context context = Context.newBuilder("python")
                .out(capturedOutputStream)
                .build()) {

            String pythonCode = 
                "print('--- Python Execution Report ---')\n" +
                "items = ['Laptop', 'Mouse', 'Keyboard']\n" +
                "for i, item in enumerate(items, 1):\n" +
                "    print(f'{i}. {item} processed')\n";

            context.eval("python", pythonCode);

            // Convert captured stream into Java String
            String outputFromPython = capturedOutputStream.toString(StandardCharsets.UTF_8).trim();

            System.out.println(">>> Result successfully captured by Java <<<");
            System.out.println(outputFromPython);

        } catch (Exception e) {
            System.err.println("Capture Error: " + e.getMessage());
        }

        System.out.println("Python Milestone 100% Complete!");
         }
}