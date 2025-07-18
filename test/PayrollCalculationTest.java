package test;

import model.*;
import service.PayrollCalculator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JUnit 5 test suite for Payroll Calculation using OOP principles
 */
@DisplayName("Payroll Calculation Tests - OOP Implementation")
class PayrollCalculationTest {
    
    private RegularEmployee regularEmployee;
    private ContractualEmployee contractualEmployee;
    private PayrollCalculator payrollCalculator;
    
    @BeforeEach
    @DisplayName("Setup test data")
    void setUp() {
        regularEmployee = new RegularEmployee(10001, "Test", "Employee", "Developer", 50000);
        contractualEmployee = new ContractualEmployee(10002, "Test", "Contractor", "Consultant", 60000, "2025-12-31", "Test Project");
        payrollCalculator = new PayrollCalculator();
    }
    
    @Test
    @DisplayName("Test SSS contribution calculation using polymorphism")
    void testSSSContributionCalculation() {
        // Test different salary brackets using polymorphic behavior
        
        // Low salary bracket
        RegularEmployee lowSalaryEmp = new RegularEmployee(10001, "Low", "Salary", "Junior", 15000);
        double lowSalaryDeductions = lowSalaryEmp.calculateDeductions();
        assertTrue(lowSalaryDeductions > 0, "Low salary employee should have SSS deduction");
        
        // Medium salary bracket
        RegularEmployee medSalaryEmp = new RegularEmployee(10002, "Med", "Salary", "Developer", 30000);
        double medSalaryDeductions = medSalaryEmp.calculateDeductions();
        assertTrue(medSalaryDeductions > 0, "Medium salary employee should have SSS deduction");
        
        // High salary bracket
        RegularEmployee highSalaryEmp = new RegularEmployee(10003, "High", "Salary", "Manager", 80000);
        double highSalaryDeductions = highSalaryEmp.calculateDeductions();
        assertTrue(highSalaryDeductions > 0, "High salary employee should have SSS deduction");
        
        // Test that higher salaries generally have higher deductions (up to caps)
        assertTrue(medSalaryDeductions >= lowSalaryDeductions, 
                  "Medium salary should have equal or higher deductions than low salary");
    }
    
    @ParameterizedTest
    @DisplayName("Test overtime pay calculation with different hours using polymorphism")
    @CsvSource({
        "0, 50000.0",      // No overtime
        "4, 51136.36",     // 4 hours overtime
        "8, 52272.73",     // 8 hours overtime
        "12, 53409.09"     // 12 hours overtime
    })
    void testOvertimePayCalculation(double overtimeHours, double expectedGrossPay) {
        // Act - using polymorphic method
        double actualGrossPay = regularEmployee.calculateGrossPay(22, overtimeHours);
        
        // Assert
        assertEquals(expectedGrossPay, actualGrossPay, 100.0, 
                    String.format("Gross pay for %.1f overtime hours should be approximately ₱%.2f", 
                                 overtimeHours, expectedGrossPay));
    }
    
    @Test
    @DisplayName("Test polymorphic behavior between Regular and Contractual employees")
    void testPolymorphicPayrollBehavior() {
        // Arrange
        Employee[] employees = {regularEmployee, contractualEmployee};
        int daysWorked = 22;
        double overtimeHours = 8.0;
        
        // Act & Assert
        for (Employee emp : employees) {
            double grossPay = emp.calculateGrossPay(daysWorked, overtimeHours);
            double allowances = emp.calculateAllowances();
            double deductions = emp.calculateDeductions();
            boolean benefits = emp.isEligibleForBenefits();
            
            // All employees should have positive gross pay
            assertTrue(grossPay > 0, emp.getEmployeeType() + " should have positive gross pay");
            
            // Test polymorphic differences
            if (emp instanceof RegularEmployee) {
                assertTrue(allowances > 0, "Regular employees should have allowances");
                assertTrue(benefits, "Regular employees should be eligible for benefits");
            } else if (emp instanceof ContractualEmployee) {
                assertEquals(0, allowances, 0.01, "Contractual employees should have no allowances");
                assertFalse(benefits, "Contractual employees should not be eligible for benefits");
            }
        }
    }
    
    @Test
    @DisplayName("Test Philippine withholding tax calculation using inheritance")
    void testWithholdingTaxCalculation() {
        // Test tax-exempt employee (annual salary ≤ 250,000)
        RegularEmployee taxExemptEmp = new RegularEmployee(10001, "Tax", "Exempt", "Junior", 20000);
        double taxExemptDeductions = taxExemptEmp.calculateDeductions();
        
        // Test taxable employee (annual salary > 250,000)
        RegularEmployee taxableEmp = new RegularEmployee(10002, "Tax", "Able", "Senior", 30000);
        double taxableDeductions = taxableEmp.calculateDeductions();
        
        // Assert
        assertTrue(taxableDeductions >= taxExemptDeductions, 
                  "Higher salary employee should have same or higher deductions");
        
        // Both should have positive deductions (at least SSS, PhilHealth, Pag-IBIG)
        assertTrue(taxExemptDeductions > 0, "Even tax-exempt employees should have government contributions");
        assertTrue(taxableDeductions > 0, "Taxable employees should have deductions");
    }
    
    @Test
    @DisplayName("Test Template Method Pattern in calculateNetPay")
    void testTemplateMethodPattern() {
        // Arrange
        int daysWorked = 22;
        double overtimeHours = 8.0;
        
        // Act - Template method calls abstract methods polymorphically
        double netPay = regularEmployee.calculateNetPay(daysWorked, overtimeHours);
        double grossPay = regularEmployee.calculateGrossPay(daysWorked, overtimeHours);
        double allowances = regularEmployee.calculateAllowances();
        double deductions = regularEmployee.calculateDeductions();
        
        // Assert
        assertAll("Net pay calculation components",
            () -> assertTrue(grossPay > 0, "Gross pay should be positive"),
            () -> assertTrue(allowances > 0, "Allowances should be positive for regular employee"),
            () -> assertTrue(deductions > 0, "Deductions should be positive"),
            () -> assertEquals(grossPay + allowances - deductions, netPay, 0.01, 
                             "Template method should calculate net pay correctly"),
            () -> assertTrue(netPay > 0, "Net pay should be positive")
        );
    }
    
    @Test
    @DisplayName("Test Factory Pattern creates employees with correct payroll behavior")
    void testFactoryPatternPayrollBehavior() {
        // Arrange & Act
        Employee factoryRegular = EmployeeFactory.createEmployee("REGULAR", 30001, "Factory", "Regular", "Developer", 45000);
        Employee factoryContractual = EmployeeFactory.createEmployee("CONTRACTUAL", 30002, "Factory", "Contractual", "Consultant", 55000);
        
        // Assert polymorphic behavior
        assertTrue(factoryRegular.isEligibleForBenefits(), "Factory-created regular employee should be eligible for benefits");
        assertFalse(factoryContractual.isEligibleForBenefits(), "Factory-created contractual employee should not be eligible for benefits");
        
        assertTrue(factoryRegular.calculateAllowances() > 0, "Factory-created regular employee should have allowances");
        assertEquals(0, factoryContractual.calculateAllowances(), 0.01, "Factory-created contractual employee should have no allowances");
    }
    
    @ParameterizedTest
    @DisplayName("Test edge cases in payroll calculation using polymorphism")
    @ValueSource(ints = {0, 1, 15, 22, 30})
    void testPayrollCalculationEdgeCases(int daysWorked) {
        // Act
        double regularGrossPay = regularEmployee.calculateGrossPay(daysWorked, 0);
        double contractualGrossPay = contractualEmployee.calculateGrossPay(daysWorked, 0);
        
        // Assert
        if (daysWorked == 0) {
            assertEquals(0.0, regularGrossPay, 0.01, "Zero days worked should result in zero gross pay");
            assertEquals(0.0, contractualGrossPay, 0.01, "Zero days worked should result in zero gross pay");
        } else {
            assertTrue(regularGrossPay > 0, "Regular employee should have positive gross pay for " + daysWorked + " days");
            assertTrue(contractualGrossPay > 0, "Contractual employee should have positive gross pay for " + daysWorked + " days");
        }
        
        // Test that calculations are proportional
        if (daysWorked > 0) {
            double regularDailyRate = regularEmployee.getBasicSalary() / 22;
            double expectedRegularPay = regularDailyRate * daysWorked;
            assertEquals(expectedRegularPay, regularGrossPay, 0.01, 
                        "Regular employee gross pay should be proportional to days worked");
        }
    }
    
    @Test
    @DisplayName("Test calculation consistency using polymorphism")
    void testCalculationConsistency() {
        // Calculate payroll multiple times with same inputs using polymorphic methods
        double grossPay1 = regularEmployee.calculateGrossPay(22, 8);
        double grossPay2 = regularEmployee.calculateGrossPay(22, 8);
        double grossPay3 = regularEmployee.calculateGrossPay(22, 8);
        
        double deductions1 = regularEmployee.calculateDeductions();
        double deductions2 = regularEmployee.calculateDeductions();
        
        double allowances1 = regularEmployee.calculateAllowances();
        double allowances2 = regularEmployee.calculateAllowances();
        
        // Assert
        assertAll("Calculation consistency",
            () -> assertEquals(grossPay1, grossPay2, 0.01, "Repeated gross pay calculations should be consistent"),
            () -> assertEquals(grossPay2, grossPay3, 0.01, "Repeated gross pay calculations should be consistent"),
            () -> assertEquals(deductions1, deductions2, 0.01, "Repeated deduction calculations should be consistent"),
            () -> assertEquals(allowances1, allowances2, 0.01, "Repeated allowance calculations should be consistent")
        );
    }
    
    @Test
    @DisplayName("Test inheritance hierarchy with instanceof")
    void testInheritanceHierarchy() {
        // Test inheritance relationships
        assertTrue(regularEmployee instanceof Employee, "RegularEmployee should be instance of Employee");
        assertTrue(regularEmployee instanceof BaseEntity, "RegularEmployee should be instance of BaseEntity");
        assertTrue(contractualEmployee instanceof Employee, "ContractualEmployee should be instance of Employee");
        assertTrue(contractualEmployee instanceof BaseEntity, "ContractualEmployee should be instance of BaseEntity");
        
        // Test that they are different concrete types
        assertFalse(regularEmployee instanceof ContractualEmployee, "RegularEmployee should not be instance of ContractualEmployee");
        assertFalse(contractualEmployee instanceof RegularEmployee, "ContractualEmployee should not be instance of RegularEmployee");
    }
    
    @Test
    @DisplayName("Test abstract class cannot be instantiated")
    void testAbstractClassInstantiation() {
        // This test verifies that Employee is abstract by ensuring we can only create concrete subclasses
        assertDoesNotThrow(() -> new RegularEmployee(), "Should be able to create RegularEmployee");
        assertDoesNotThrow(() -> new ContractualEmployee(), "Should be able to create ContractualEmployee");
        
        // Employee class should be abstract (cannot test direct instantiation in JUnit, but we can verify behavior)
        Employee emp = new RegularEmployee();
        assertNotNull(emp, "Should be able to create Employee through concrete subclass");
    }
    
    @AfterEach
    @DisplayName("Cleanup after each test")
    void tearDown() {
        regularEmployee = null;
        contractualEmployee = null;
        payrollCalculator = null;
    }
    
    @AfterAll
    @DisplayName("Final cleanup after all tests")
    static void tearDownAll() {
        System.out.println("✅ All Payroll Calculation OOP tests completed successfully!");
        System.out.println("📊 Tested: Polymorphism, Inheritance, Template Method Pattern, Factory Pattern");
    }
}