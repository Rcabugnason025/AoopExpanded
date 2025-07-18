/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("           MOTORPH PAYROLL SYSTEM - JUNIT TEST SUITE");
        System.out.println("=".repeat(60));
        
        // Create launcher
        Launcher launcher = LauncherFactory.create();
        
        // Create summary listener
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        
        // Build discovery request
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("test"))
                .build();
        
        // Execute tests
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
        
        // Print summary
        TestExecutionSummary summary = listener.getSummary();
        printTestSummary(summary);
    }
    
    private static void printTestSummary(TestExecutionSummary summary) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                        TEST EXECUTION SUMMARY");
        System.out.println("=".repeat(60));
        
        System.out.printf("Tests Found     : %d%n", summary.getTestsFoundCount());
        System.out.printf("Tests Started   : %d%n", summary.getTestsStartedCount());
        System.out.printf("Tests Successful: %d%n", summary.getTestsSucceededCount());
        System.out.printf("Tests Skipped   : %d%n", summary.getTestsSkippedCount());
        System.out.printf("Tests Aborted   : %d%n", summary.getTestsAbortedCount());
        System.out.printf("Tests Failed    : %d%n", summary.getTestsFailedCount());
        
        System.out.println("\nExecution Time  : " + summary.getTotalTime().toMillis() + " ms");
        
        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\n" + "!".repeat(60));
            System.out.println("                         FAILED TESTS");
            System.out.println("!".repeat(60));
            
            summary.getFailures().forEach(failure -> {
                System.out.println("❌ " + failure.getTestIdentifier().getDisplayName());
                System.out.println("   " + failure.getException().getMessage());
                System.out.println();
            });
        }
        
        System.out.println("\n" + "=".repeat(60));
        if (summary.getTestsFailedCount() == 0) {
            System.out.println("🎉 ALL TESTS PASSED! The payroll system is working correctly.");
        } else {
            System.out.println("⚠️  Some tests failed. Please review the implementation.");
        }
        System.out.println("=".repeat(60));
    }
}
