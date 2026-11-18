package com.polyglotmesh;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Language;
import org.graalvm.polyglot.Value;

public class Main {
    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("      PolyglotMesh - Day 2 Explorer      ");
        System.out.println("=========================================\n");

        // 1. Inspect installed guest languages in GraalVM
        inspectInstalledLanguages();

        // 2. Explore Polyglot Context & Value API
        explorePolyglotExecution();
    }

    private static void inspectInstalledLanguages() {
        System.out.println("[Step 1] Checking Installed GraalVM Languages:");
        try (Engine engine = Engine.create()) {
            for (String languageId : engine.getLanguages().keySet()) {
                Language lang = engine.getLanguages().get(languageId);
                System.out.printf("  • ID: %-10s | Name: %-20s | Version: %s%n",
                        languageId, lang.getName(), lang.getVersion());
            }
        }
        System.out.println();
    }

    private static void explorePolyglotExecution() {
        System.out.println("[Step 2] Testing Context & Value API:");

        // Create an isolated Polyglot Context
        try (Context context = Context.newBuilder().build()) {
            
            // Execute a simple JavaScript expression
            String jsCode = "40 + 2";
            Value result = context.eval("js", jsCode);

            System.out.println("  Executed Code   : " + jsCode);
            System.out.println("  Raw Value       : " + result);
            System.out.println("  Is Number?      : " + result.isNumber());
            System.out.println("  Converted to Java: " + result.asInt());

            // Execute string manipulation
            Value greeting = context.eval("js", "'Polyglot' + 'Mesh'");
            System.out.println("  String Result   : " + greeting.asString());
        } catch (Exception e) {
            System.err.println("Execution Error: " + e.getMessage());
        }
        System.out.println("\n=========================================");
        System.out.println(" Day 2 Polyglot Exploration Completed! ");
        System.out.println("=========================================");
    }
}