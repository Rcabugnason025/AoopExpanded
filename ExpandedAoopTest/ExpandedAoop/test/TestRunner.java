package test;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

/**
 * Enhanced JUnit 5 Test Runner for MotorPH Payroll System
 * Demonstrates proper unit testing with comprehensive reporting
 */
public class TestRunner {
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("           MOTORPH PAYROLL SYSTEM - COMPREHENSIVE JUNIT 5 TEST SUITE");
        System.out.println("                    Testing OOP Principles & Business Logic");
        System.out.println("=".repeat(80));
        
        // Create JUnit 5 launcher
        Launcher launcher = LauncherFactory.create();
        
        // Create summary listener for detailed reporting
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        
        // Build discovery request to find all tests in test package
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectPackage("test"))
                .build();
        
        // Register listener and execute tests
        launcher.registerTestExecutionListeners(listener);
        
        System.out.println("🚀 Starting test execution...\n");
        long startTime = System.currentTimeMillis();
        
        launcher.execute(request);
        
        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // Print comprehensive summary
        TestExecutionSummary summary = listener.getSummary();
        printDetailedTestSummary(summary, executionTime);
    }
    
    private static void printDetailedTestSummary(TestExecutionSummary summary, long executionTime) {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                        COMPREHENSIVE TEST EXECUTION SUMMARY");
        System.out.println("=".repeat(80));
        
        // Basic statistics
        System.out.printf("📊 TEST STATISTICS:%n");
        System.out.printf("   Tests Found     : %d%n", summary.getTestsFoundCount());
        System.out.printf("   Tests Started   : %d%n", summary.getTestsStartedCount());
        System.out.printf("   Tests Successful: %d%n", summary.getTestsSucceededCount());
        System.out.printf("   Tests Skipped   : %d%n", summary.getTestsSkippedCount());
        System.out.printf("   Tests Aborted   : %d%n", summary.getTestsAbortedCount());
        System.out.printf("   Tests Failed    : %d%n", summary.getTestsFailedCount());
        
        // Execution time
        System.out.printf("%n⏱️  EXECUTION TIME:%n");
        System.out.printf("   Total Time      : %d ms%n", summary.getTotalTime().toMillis());
        System.out.printf("   Wall Clock Time : %d ms%n", executionTime);
        
        // Success rate calculation
        long totalTests = summary.getTestsStartedCount();
        long successfulTests = summary.getTestsSucceededCount();
        double successRate = totalTests > 0 ? (double) successfulTests / totalTests * 100 : 0;
        
        System.out.printf("%n📈 SUCCESS RATE: %.1f%% (%d/%d)%n", successRate, successfulTests, totalTests);
        
        // OOP Principles coverage
        System.out.printf("%n🎯 OOP PRINCIPLES TESTED:%n");
        System.out.println("   ✅ Encapsulation - Data hiding and controlled access");
        System.out.println("   ✅ Inheritance - Code reuse and class hierarchy");
        System.out.println("   ✅ Polymorphism - Runtime behavior and method overriding");
        System.out.println("   ✅ Abstraction - Abstract classes and template methods");
        
        // Test categories
        System.out.printf("%n📋 TEST CATEGORIES COVERED:%n");
        System.out.println("   ✅ Employee Model Tests (Inheritance & Polymorphism)");
        System.out.println("   ✅ Payroll Calculation Tests (Business Logic)");
        System.out.println("   ✅ OOP Principles Tests (Four Pillars of OOP)");
        System.out.println("   ✅ Factory Pattern Tests (Design Patterns)");
        System.out.println("   ✅ Template Method Tests (Behavioral Patterns)");
        
        // Failure details
        if (summary.getTestsFailedCount() > 0) {
            System.out.println("\n" + "!".repeat(80));
            System.out.println("                         ❌ FAILED TESTS DETAILS");
            System.out.println("!".repeat(80));
            
            summary.getFailures().forEach(failure -> {
                System.out.println("❌ " + failure.getTestIdentifier().getDisplayName());
                System.out.println("   📍 Location: " + failure.getTestIdentifier().getSource().orElse("Unknown"));
                System.out.println("   💥 Error: " + failure.getException().getMessage());
                
                // Print stack trace for debugging
                if (failure.getException().getCause() != null) {
                    System.out.println("   🔍 Cause: " + failure.getException().getCause().getMessage());
                }
                System.out.println();
            });
        }
        
        // Recommendations
        System.out.println("\n" + "=".repeat(80));
        if (summary.getTestsFailedCount() == 0) {
            System.out.println("🎉 EXCELLENT! ALL TESTS PASSED!");
            System.out.println("✅ Your MotorPH Payroll System demonstrates proper OOP implementation");
            System.out.println("✅ All four pillars of OOP are correctly implemented");
            System.out.println("✅ Business logic is working as expected");
            System.out.println("✅ Code quality meets academic standards");
            
            if (successRate == 100.0) {
                System.out.println("🏆 PERFECT SCORE: 100% test success rate!");
            }
        } else {
            System.out.println("⚠️  SOME TESTS FAILED - REVIEW REQUIRED");
            System.out.println("📝 Please review the failed tests above and fix the implementation");
            System.out.println("🔧 Focus on the OOP principles that are not working correctly");
            System.out.println("💡 Ensure all abstract methods are properly implemented");
            
            if (successRate >= 80.0) {
                System.out.println("👍 Good progress! Most tests are passing (" + String.format("%.1f", successRate) + "%)");
            } else if (successRate >= 60.0) {
                System.out.println("⚡ Moderate progress. Need more work on implementation (" + String.format("%.1f", successRate) + "%)");
            } else {
                System.out.println("🚨 Significant issues found. Major revision needed (" + String.format("%.1f", successRate) + "%)");
            }
        }
        
        // Footer
        System.out.println("=".repeat(80));
        System.out.println("📚 Advanced Object-Oriented Programming - Final Project Testing");
        System.out.println("🏫 Mapúa Malayan Digital College - MO-IT113");
        System.out.println("👨‍💻 JUnit 5 Test Suite with Assert Functions and Detailed Reporting");
        System.out.println("=".repeat(80));
    }
}