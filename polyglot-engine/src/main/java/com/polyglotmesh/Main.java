package com.polyglotmesh;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResponse;
import com.polyglotmesh.service.PolyglotExecutionService;

public class Main {
    public static void main(String[] args) {
        PolyglotExecutionService service = new PolyglotExecutionService();

        // Test 1: Normal Python Script (Allowed 15s for initial cold start)
        System.out.println("[Test 1] Normal Python Script (Cold Start):");
        ExecutionRequest normalPython = new ExecutionRequest("python", "print('Fast script executed!')", 15000);
        ExecutionResponse r1 = service.execute(normalPython);
        printResult(r1);

        // Test 2: Warm Python Script (Runs in milliseconds!)
        System.out.println("[Test 2] Warm Python Script:");
        ExecutionRequest warmPython = new ExecutionRequest("python", "print('Warm Python executed instantly!')", 5000);
        ExecutionResponse r2 = service.execute(warmPython);
        printResult(r2);

        // Test 3: Python Infinite Loop Attack (while True: pass)
        System.out.println("[Test 3] Python Infinite Loop Attack (while True: pass):");
        System.out.println("  (Watchdog set to 3000 ms. Aborting in 3 seconds...)");
        ExecutionRequest infinitePython = new ExecutionRequest("python", "while True:\n    pass", 3000);
        ExecutionResponse r3 = service.execute(infinitePython);
        printResult(r3);

        // Test 4: JavaScript Infinite Loop Attack (for(;;){})
        System.out.println("[Test 4] JavaScript Infinite Loop Attack (for(;;){}):");
        System.out.println("  (Watchdog set to 2000 ms. Aborting in 2 seconds...)");
        ExecutionRequest infiniteJs = new ExecutionRequest("js", "for(;;){}", 2000);
        ExecutionResponse r4 = service.execute(infiniteJs);
        printResult(r4);

        }

    private static void printResult(ExecutionResponse res) {
        System.out.println("  Status   : " + (res.isSuccess() ? "SUCCESS" : "STOPPED / TIMED OUT"));
        System.out.println("  Duration : " + res.getExecutionTimeMs() + " ms");
        if (res.isSuccess()) {
            System.out.println("  Output   : " + res.getOutput());
        } else {
            System.out.println("  Error    : " + res.getError());
        }
        System.out.println();
    }
}