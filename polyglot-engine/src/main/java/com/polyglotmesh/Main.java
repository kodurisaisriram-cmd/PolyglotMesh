package com.polyglotmesh;

import com.polyglotmesh.model.ExecutionResponse;
import com.polyglotmesh.service.PolyglotExecutionService;

public class Main {
    public static void main(String[] args) {
        PolyglotExecutionService service = new PolyglotExecutionService();

        // Test 1: Safe Math & Logic (Should SUCCEED)
        System.out.println("[Test 1] Safe Python Algorithm:");
        ExecutionResponse r1 = service.execute("python", "x = 50 * 3\nprint(f'Safe Result: {x}')");
        printAudit(r1);

        // Test 2: Malicious Attack - Host File System Read (Should be BLOCKED)
        System.out.println("[Test 2] Malicious File System Access Attempt:");
        String fileAttackCode = "with open('C:\\\\Windows\\\\System32\\\\drivers\\\\etc\\\\hosts', 'r') as f:\n    print(f.read())";
        ExecutionResponse r2 = service.execute("python", fileAttackCode);
        printAudit(r2);

        // Test 3: Malicious Attack - OS Command Execution (Should be BLOCKED)
        System.out.println("[Test 3] Malicious OS Command Spawn Attempt:");
        String osAttackCode = "import os\nos.system('calc.exe')";
        ExecutionResponse r3 = service.execute("python", osAttackCode);
        printAudit(r3);

        // Test 4: Malicious Attack - Network Socket Connection (Should be BLOCKED)
        System.out.println("[Test 4] Malicious Network Socket Attempt:");
        String socketAttackCode = "import socket\ns = socket.socket(socket.AF_INET, socket.SOCK_STREAM)\ns.connect(('www.google.com', 80))";
        ExecutionResponse r4 = service.execute("python", socketAttackCode);
        printAudit(r4);

       }

    private static void printAudit(ExecutionResponse res) {
        if (res.isSuccess()) {
            System.out.println("  Status : ALLOWED");
            System.out.println("  Output : " + res.getOutput());
        } else {
            System.out.println("  Status : BLOCKED BY SANDBOX");
            System.out.println("  Reason : " + res.getError());
        }
        System.out.println("  Latency: " + res.getExecutionTimeMs() + " ms\n");
    }
}