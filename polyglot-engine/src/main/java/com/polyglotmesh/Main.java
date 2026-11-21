package com.polyglotmesh;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResponse;
import com.polyglotmesh.service.PolyglotExecutionService;

public class Main {
    public static void main(String[] args) {
        
        PolyglotExecutionService executionService = new PolyglotExecutionService();

        // 1. Test Python execution via Service
        System.out.println("[Test 1] Calling executionService for Python:");
        ExecutionRequest pythonRequest = new ExecutionRequest(
            "python",
            "base_price = 100\n" +
            "demand = 1.8\n" +
            "print(f'Calculated price: ${base_price * demand}')"
        );
        ExecutionResponse pythonResponse = executionService.execute(pythonRequest);
        printResponse(pythonResponse);

        // 2. Test JavaScript execution via Service
        System.out.println("[Test 2] Calling executionService for JavaScript:");
        ExecutionRequest jsRequest = new ExecutionRequest(
            "javascript",
            "const userRole = 'admin';\n" +
            "console.log(`Access granted for role: ${userRole.toUpperCase()}`);"
        );
        ExecutionResponse jsResponse = executionService.execute(jsRequest);
        printResponse(jsResponse);

        // 3. Test Unsupported Language Check
        System.out.println("[Test 3] Testing validation with unsupported language:");
        ExecutionResponse invalidResponse = executionService.execute("ruby", "puts 'hello'");
        printResponse(invalidResponse);

         }

    private static void printResponse(ExecutionResponse response) {
        System.out.println("  Status : " + (response.isSuccess() ? "SUCCESS " : "FAILED "));
        System.out.println("  Lang   : " + response.getLanguage());
        System.out.println("  Time   : " + response.getExecutionTimeMs() + " ms");
        if (response.isSuccess()) {
            System.out.println("  Output : " + response.getOutput());
        } else {
            System.out.println("  Error  : " + response.getError());
        }
        System.out.println();
    }
}