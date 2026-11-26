package com.polyglotmesh;

import com.polyglotmesh.model.ExecutionResponse;
import com.polyglotmesh.service.PolyglotExecutionService;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        PolyglotExecutionService service = new PolyglotExecutionService();

        // 1. Create a native Java HashMap
        Map<String, Object> pricingContext = new HashMap<>();
        pricingContext.put("productId", "PROD-ENTERPRISE-101");
        pricingContext.put("basePrice", 1200.0);
        pricingContext.put("demandFactor", 1.25);
        pricingContext.put("customerTier", "VIP");

        System.out.println("[Step 1] Initial Java HashMap State before Python:");
        pricingContext.forEach((k, v) -> System.out.println(k + " = " + v));
        System.out.println();

        // 2. Python script directly mutates the Java Map in memory (Zero JSON serialization!)
        String pythonScript = 
            "print('--- Python Execution Inside JVM Memory ---')\n" +
            "base = contextData['basePrice']\n" +
            "demand = contextData['demandFactor']\n" +
            "tier = contextData['customerTier']\n" +
            "\n" +
            "# Pricing algorithm\n" +
            "final_price = base * demand\n" +
            "if tier == 'VIP':\n" +
            "    final_price = final_price * 0.90 # 10% VIP Discount\n" +
            "\n" +
            "# Directly inject new calculated keys back into the Java Map!\n" +
            "contextData['finalPrice'] = final_price\n" +
            "contextData['discountApplied'] = '10% VIP Discount'\n" +
            "contextData['engineStatus'] = 'SUCCESS_ZERO_SERIALIZATION'\n" +
            "print(f'Python calculated final price: ${final_price}')\n";

        System.out.println("[Step 2] Executing Python with Direct Memory Reference...");
        ExecutionResponse response = service.executeWithData("python", pythonScript, pricingContext, 15000);

        System.out.println("\n[Step 3] Python Execution Result:");
        System.out.println("  Status  : " + (response.isSuccess() ? "SUCCESS" : "FAILED"));
        System.out.println("  Latency : " + response.getExecutionTimeMs() + " ms");
        System.out.println("  Console :\n" + response.getOutput());

        // 3. Verify in Java: The Java Map was updated in real time!
        System.out.println("\n[Step 4] Final Java HashMap State (Read in Java):");
        pricingContext.forEach((k, v) -> System.out.println(k + " = " + v));
    }
}