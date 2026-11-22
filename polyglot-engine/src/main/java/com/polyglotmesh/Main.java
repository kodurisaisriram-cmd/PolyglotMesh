package com.polyglotmesh;

import com.polyglotmesh.model.ExecutionRequest;
import com.polyglotmesh.model.ExecutionResponse;
import com.polyglotmesh.service.PolyglotExecutionService;

public class Main {
    public static void main(String[] args) {
        PolyglotExecutionService service = new PolyglotExecutionService();

        // Case 1: Valid Python code
        System.out.println("[Case 1] Valid Python Code:");
        ExecutionResponse r1 = service.execute("python", "print('Hello, PolyglotMesh!')");
        printResult(r1);

        // Case 2: Python Syntax Error (Missing closing parenthesis)
        System.out.println("[Case 2] Python Syntax Error (unclosed bracket):");
        ExecutionResponse r2 = service.execute("python", "print('Broken code");
        printResult(r2);

        // Case 3: Python Runtime Error (Division by Zero)
        System.out.println("[Case 3] Python Runtime Error (Division by Zero):");
        ExecutionResponse r3 = service.execute("python", "result = 100 / 0");
        printResult(r3);

        // Case 4: JavaScript Runtime Error (Calling undefined function)
        System.out.println("[Case 4] JavaScript Runtime Error (Undefined reference):");
        ExecutionResponse r4 = service.execute("js", "calculateNonExistentTax(500);");
        printResult(r4);

        // Case 5: Empty Code Submission
        System.out.println("[Case 5] Empty Code Submission:");
        ExecutionResponse r5 = service.execute("python", "   ");
        printResult(r5);

        }

    private static void printResult(ExecutionResponse res) {
        System.out.println("  Success : " + (res.isSuccess() ? "YES" : "NO (Handled gracefully)"));
        System.out.println("  Lang    : " + res.getLanguage());
        System.out.println("  Time    : " + res.getExecutionTimeMs() + " ms");
        if (res.isSuccess()) {
            System.out.println("  Output  : " + res.getOutput());
        } else {
            System.out.println("  Error   : " + res.getError());
        }
        System.out.println();
    }
}