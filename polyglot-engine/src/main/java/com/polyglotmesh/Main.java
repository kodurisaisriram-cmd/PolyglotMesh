package com.polyglotmesh;

import com.polyglotmesh.model.BenchmarkResponse;
import com.polyglotmesh.service.PolyglotBenchmarkService;
import com.polyglotmesh.service.PolyglotExecutionService;

public class Main {
    public static void main(String[] args) {
        PolyglotExecutionService execService = new PolyglotExecutionService();
        PolyglotBenchmarkService benchmarkService = new PolyglotBenchmarkService(execService);

        // Pre-warm the Python engine so benchmark reflects real production latency
        System.out.println("Warming up execution engines...");
        execService.execute("python", "pass");
        System.out.println("Ready!\n");

        // [Benchmark 1] Python Pricing Engine
        System.out.println("[Benchmark 1] Python Pricing Engine Execution:");
        String pythonPricingCode = 
            "data = {'basePrice': 500, 'demand': 1.4}\n" +
            "print(f'Final Price: ${data[\"basePrice\"] * data[\"demand\"]}')";

        BenchmarkResponse b1 = benchmarkService.benchmark("python", pythonPricingCode);
        printBenchmarkCard(b1);

        // [Benchmark 2] JavaScript Tier Validation Logic
        System.out.println("[Benchmark 2] JavaScript Tier Validation Execution:");
        String jsValidationCode = 
            "const score = 88;\n" +
            "console.log(score >= 80 ? 'Tier: Gold Enterprise' : 'Tier: Standard');";

        BenchmarkResponse b2 = benchmarkService.benchmark("js", jsValidationCode);
        printBenchmarkCard(b2);

       }

    private static void printBenchmarkCard(BenchmarkResponse b) {
        String cleanOutput = b.getOutput() != null ? b.getOutput().replace("\n", " ").trim() : "None";
        System.out.printf("│ Language          : %-28s %n", b.getLanguage().toUpperCase());
        System.out.printf("│ In-Memory Polyglot: %-5d ms (Zero network)      %n", b.getPolyglotTimeMs());
        System.out.printf("│ Traditional REST  : %-5d ms (Simulated Micro)   %n", b.getMockRestTimeMs());
        System.out.printf("│ Speedup Factor    : %-5.1fx FASTER!             %n", b.getSpeedupRatio());
        System.out.printf("│ Latency Saved     : %-5d ms per request         %n", b.getLatencySavedMs());
        System.out.printf("│ Output            : %-28s %n", cleanOutput);
        }
}