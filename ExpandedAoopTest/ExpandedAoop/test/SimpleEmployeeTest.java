package test;

import model.*;

/**
 * Simple test class without JUnit dependencies
 * Tests OOP principles manually with clear output
 */
public class SimpleEmployeeTest {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(80));
        System.out.println("           MOTORPH PAYROLL SYSTEM - OOP PRINCIPLES TEST");
        System.out.println("=".repeat(80));
        
        runAllTests();
    }
    
    public static void runAllTests() {
        int totalTests = 0;
        int passedTests = 0;
        
        System.out.println("🧪 Starting OOP Principles Tests...\n");
        
        // Test 1: Inheritance
        totalTests++;
        if (testInheritance()) {
            passedTests++;
            System.out.println("✅ PASS: Inheritance Test");
        } else {
            System.out.println("❌ FAIL: Inheritance Test");
        }
        
        // Test 2: Polymorphism
        totalTests++;
        if (testPolymorphism()) {
            passedTests++;
            System.out.println("✅ PASS: Polymorphism Test");
        } else {
            System.out.println("❌ FAIL: Polymorphism Test");
        }
        
        // Test 3: Abstraction
        totalTests++;
        if (testAbstraction()) {
            passedTests++;
            System.out.println("✅ PASS: Abstraction Test");
        } else {
            System.out.println("❌ FAIL: Abstraction Test");
        }
        
        // Test 4: Encapsulation
        totalTests++;
        if (testEncapsulation()) {
            passedTests++;
            System.out.println("✅ PASS: Encapsulation Test");
        } else {
            System.out.println("❌ FAIL: Encapsulation Test");
        }
        
        // Test 5: Factory Pattern
        totalTests++;
        if (testFactoryPattern()) {
            passedTests++;
            System.out.println("✅ PASS: Factory Pattern Test");
        } else {
            System.out.println("❌ FAIL: Factory Pattern Test");
        }
        
        // Test 6: Payroll Calculations
        totalTests++;
        if (testPayrollCalculations()) {
            passedTests++;
            System.out.println("✅ PASS: Payroll Calculations Test");
        } else {
            System.out.println("❌ FAIL: Payroll Calculations Test");
        }
        
        // Print summary
        System.out.println("\n" + "=".repeat(80));
        System.out.println("                        TEST EXECUTION SUMMARY");
        System.out.println("=".repeat(80));
        System.out.printf("Tests Run: %d%n", totalTests);
        System.out.printf("Tests Passed: %d%n", passedTests);
        System.out.printf("Tests Failed: %d%n", (totalTests - passedTests));
        System.out.printf("Success Rate: %.1f%%%n", ((double) passedTests / totalTests) * 100);
        
        if (passedTests == totalTests) {
            System.out.println("\n🎉 ALL TESTS PASSED! Your OOP implementation is excellent!");
            System.out.println("✅ Inheritance: Properly implemented");
            System.out.println("✅ Polymorphism: Different behaviors for different types");
            System.out.println("✅ Abstraction: Abstract class with abstract methods");
            System.out.println("✅ Encapsulation: Private fields with controlled access");
            System.out.println("✅ Factory Pattern: Proper object creation");
            System.out.println("✅ Business Logic: Payroll calculations working correctly");
        } else {
            System.out.println("\n⚠️ Some tests failed. Please review the implementation.");
        }
        System.out.println("=".repeat(80));
    }
    
    private static boolean testInheritance() {
        try {
            RegularEmployee regular = new RegularEmployee(1001, "John", "Doe", "Developer", 50000);
            ContractualEmployee contractual = new ContractualEmployee(1002, "Jane", "Smith", "Consultant", 60000, "2025-12-31", "Project A");
            
            // Test inheritance relationship
            boolean test1 = regular instanceof Employee;
            boolean test2 = contractual instanceof Employee;
            boolean test3 = regular instanceof BaseEntity;
            boolean test4 = contractual instanceof BaseEntity;
            
            // Test inherited methods
            boolean test5 = "John Doe".equals(regular.getFullName());
            boolean test6 = "Jane Smith".equals(contractual.getFullName());
            
            return test1 && test2 && test3 && test4 && test5 && test6;
        } catch (Exception e) {
            System.out.println("   Error in inheritance test: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean testPolymorphism() {
        try {
            Employee regular = new RegularEmployee(2001, "Alice", "Johnson", "Manager", 70000);
            Employee contractual = new ContractualEmployee(2002, "Bob", "Wilson", "Consultant", 80000, "2025-12-31", "Project B");
            
            // Test different behaviors for same interface
            double regularGrossPay = regular.calculateGrossPay(22, 8);
            double contractualGrossPay = contractual.calculateGrossPay(22, 8);
            
            double regularAllowances = regular.calculateAllowances();
            double contractualAllowances = contractual.calculateAllowances();
            
            boolean regularBenefits = regular.isEligibleForBenefits();
            boolean contractualBenefits = contractual.isEligibleForBenefits();
            
            // Test that behaviors are different
            boolean test1 = regularGrossPay > 70000; // Should include overtime
            boolean test2 = contractualGrossPay <= 80000; // No overtime for contractual
            boolean test3 = regularAllowances > 0; // Regular gets allowances
            boolean test4 = contractualAllowances == 0; // Contractual gets no allowances
            boolean test5 = regularBenefits; // Regular eligible for benefits
            boolean test6 = !contractualBenefits; // Contractual not eligible
            
            // Test different employee types
            boolean test7 = !regular.getEmployeeType().equals(contractual.getEmployeeType());
            
            return test1 && test2 && test3 && test4 && test5 && test6 && test7;
        } catch (Exception e) {
            System.out.println("   Error in polymorphism test: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean testAbstraction() {
        try {
            RegularEmployee regular = new RegularEmployee(3001, "Charlie", "Brown", "Analyst", 45000);
            
            // Test that abstract methods are implemented
            double grossPay = regular.calculateGrossPay(22, 0);
            double deductions = regular.calculateDeductions();
            double allowances = regular.calculateAllowances();
            boolean benefits = regular.isEligibleForBenefits();
            
            // Test template method pattern
            double netPay = regular.calculateNetPay(22, 0);
            double expectedNetPay = grossPay + allowances - deductions;
            
            boolean test1 = grossPay > 0;
            boolean test2 = deductions > 0;
            boolean test3 = allowances > 0;
            boolean test4 = benefits;
            boolean test5 = Math.abs(netPay - expectedNetPay) < 0.01; // Template method works correctly
            
            return test1 && test2 && test3 && test4 && test5;
        } catch (Exception e) {
            System.out.println("   Error in abstraction test: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean testEncapsulation() {
        try {
            RegularEmployee employee = new RegularEmployee(4001, "Diana", "Prince", "Specialist", 55000);
            
            // Test getter/setter access
            int originalId = employee.getEmployeeId();
            employee.setEmployeeId(4002);
            boolean test1 = employee.getEmployeeId() == 4002;
            
            // Test validation in setters
            boolean test2 = false;
            try {
                employee.setEmployeeId(-1); // Should throw exception
            } catch (IllegalArgumentException e) {
                test2 = true; // Exception caught, validation works
            }
            
            // Test that private fields are not directly accessible (conceptual test)
            boolean test3 = employee.getFirstName().equals("Diana");
            employee.setFirstName("Wonder");
            boolean test4 = employee.getFirstName().equals("Wonder");
            
            // Test validation for required fields
            boolean test5 = false;
            try {
                employee.setFirstName(""); // Should throw exception
            } catch (IllegalArgumentException e) {
                test5 = true;
            }
            
            return test1 && test2 && test3 && test4 && test5;
        } catch (Exception e) {
            System.out.println("   Error in encapsulation test: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean testFactoryPattern() {
        try {
            Employee regular = EmployeeFactory.createEmployee("REGULAR", 5001, "Eve", "Adams", "Developer", 50000);
            Employee contractual = EmployeeFactory.createEmployee("CONTRACTUAL", 5002, "Frank", "Miller", "Consultant", 60000);
            
            boolean test1 = regular instanceof RegularEmployee;
            boolean test2 = contractual instanceof ContractualEmployee;
            boolean test3 = "Regular Employee".equals(regular.getEmployeeType());
            boolean test4 = "Contractual Employee".equals(contractual.getEmployeeType());
            
            // Test invalid type throws exception
            boolean test5 = false;
            try {
                EmployeeFactory.createEmployee("INVALID", 5003, "Test", "Test", "Position", 50000);
            } catch (IllegalArgumentException e) {
                test5 = true;
            }
            
            return test1 && test2 && test3 && test4 && test5;
        } catch (Exception e) {
            System.out.println("   Error in factory pattern test: " + e.getMessage());
            return false;
        }
    }
    
    private static boolean testPayrollCalculations() {
        try {
            RegularEmployee regular = new RegularEmployee(6001, "Grace", "Lee", "Senior Developer", 60000);
            ContractualEmployee contractual = new ContractualEmployee(6002, "Henry", "Wong", "Consultant", 70000, "2025-12-31", "Project C");
            
            // Test regular employee calculations
            double regularGrossPay = regular.calculateGrossPay(22, 8); // With overtime
            double regularGrossPayNoOT = regular.calculateGrossPay(22, 0); // No overtime
            boolean test1 = regularGrossPay > regularGrossPayNoOT; // Overtime should increase pay
            
            // Test contractual employee calculations
            double contractualGrossPay = contractual.calculateGrossPay(22, 8); // Overtime ignored
            double contractualGrossPayNoOT = contractual.calculateGrossPay(22, 0);
            boolean test2 = Math.abs(contractualGrossPay - contractualGrossPayNoOT) < 0.01; // No overtime difference
            
            // Test deductions
            double regularDeductions = regular.calculateDeductions();
            double contractualDeductions = contractual.calculateDeductions();
            boolean test3 = regularDeductions > contractualDeductions; // Regular has more deductions
            
            // Test allowances
            double regularAllowances = regular.calculateAllowances();
            double contractualAllowances = contractual.calculateAllowances();
            boolean test4 = regularAllowances > 0 && contractualAllowances == 0;
            
            // Test net pay calculation
            double netPay = regular.calculateNetPay(22, 0);
            boolean test5 = netPay > 0;
            
            return test1 && test2 && test3 && test4 && test5;
        } catch (Exception e) {
            System.out.println("   Error in payroll calculations test: " + e.getMessage());
            return false;
        }
    }
}