/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Payroll Calculation Tests")
class PayrollCalculationTest {
    
    private RegularEmployee testEmployee;
    
    @BeforeEach
    void setUp() {
        testEmployee = new RegularEmployee(10001, "Test", "Employee", "Developer", "IT", 50000);
    }
    
    @Test
    @DisplayName("Should calculate SSS contribution correctly")
    void testSSSContributionCalculation() {
        // Test different salary brackets
        
        // Low salary bracket
        RegularEmployee lowSalaryEmp = new RegularEmployee(10001, "Low", "Salary", "Junior", "IT", 3000);
        assertTrue(lowSalaryEmp.calculateDeductions() > 0, "Low salary employee should have SSS deduction");
        
        // Medium salary bracket
        RegularEmployee medSalaryEmp = new RegularEmployee(10002, "Med", "Salary", "Developer", "IT", 15000);
        assertTrue(medSalaryEmp.calculateDeductions() > 0, "Medium salary employee should have SSS deduction");
        
        // High salary bracket
        RegularEmployee highSalaryEmp = new RegularEmployee(10003, "High", "Salary", "Manager", "IT", 30000);
        assertTrue(highSalaryEmp.calculateDeductions() > 0, "High salary employee should have SSS deduction");
    }
    
    @ParameterizedTest
    @DisplayName("Should calculate overtime pay with different hours")
    @CsvSource({
        "0, 50000.0",      // No overtime
        "4, 51136.36",     // 4 hours overtime
        "8, 52272.73",     // 8 hours overtime
        "12, 53409.09"     // 12 hours overtime
    })
    void testOvertimePayCalculation(double overtimeHours, double expectedGrossPay) {
        // Act
        double actualGrossPay = testEmployee.calculateGrossPay(22, overtimeHours);
        
        // Assert
        assertEquals(expectedGrossPay, actualGrossPay, 1.0, 
                    String.format("Gross pay for %.1f overtime hours should be approximately ₱%.2f", 
                                 overtimeHours, expectedGrossPay));
    }
    
    @Test
    @DisplayName("Should calculate Philippine withholding tax correctly")
    void testWithholdingTaxCalculation() {
        // Test tax-exempt employee (annual salary ≤ 250,000)
        RegularEmployee taxExemptEmp = new RegularEmployee(10001, "Tax", "Exempt", "Junior", "IT", 20000);
        double taxExemptDeductions = taxExemptEmp.calculateDeductions();
        
        // Test taxable employee (annual salary > 250,000)
        RegularEmployee taxableEmp = new RegularEmployee(10002, "Tax", "Able", "Senior", "IT", 25000);
        double taxableDeductions = taxableEmp.calculateDeductions();
        
        // Assert
        assertTrue(taxableDeductions >= taxExemptDeductions, 
                  "Higher salary employee should have same or higher deductions");
    }
    
    @Test
    @DisplayName("Should handle edge cases in payroll calculation")
    void testPayrollCalculationEdgeCases() {
        // Test zero days worked
        double zeroDaysGrossPay = testEmployee.calculateGrossPay(0, 0);
        assertEquals(0.0, zeroDaysGrossPay, 0.01, "Zero days worked should result in zero gross pay");
        
        // Test maximum days in month
        double maxDaysGrossPay = testEmployee.calculateGrossPay(31, 0);
        assertTrue(maxDaysGrossPay > testEmployee.getBasicSalary(), 
                  "Working more than standard days should result in higher gross pay");
        
        // Test negative overtime (should be handled gracefully)
        assertDoesNotThrow(() -> testEmployee.calculateGrossPay(22, -1), 
                          "Negative overtime should not throw exception");
    }
    
    @Test
    @DisplayName("Should maintain calculation consistency")
    void testCalculationConsistency() {
        // Calculate payroll multiple times with same inputs
        double grossPay1 = testEmployee.calculateGrossPay(22, 8);
        double grossPay2 = testEmployee.calculateGrossPay(22, 8);
        double grossPay3 = testEmployee.calculateGrossPay(22, 8);
        
        // Assert
        assertEquals(grossPay1, grossPay2, 0.01, "Repeated calculations should be consistent");
        assertEquals(grossPay2, grossPay3, 0.01, "Repeated calculations should be consistent");
        assertEquals(grossPay1, grossPay3, 0.01, "Repeated calculations should be consistent");
    }
    
    @Test
    @DisplayName("Should calculate net pay correctly with all components")
    void testCompleteNetPayCalculation() {
        // Arrange
        int daysWorked = 22;
        double overtimeHours = 8.0;
        
        // Act
        double grossPay = testEmployee.calculateGrossPay(daysWorked, overtimeHours);
        double allowances = testEmployee.calculateAllowances();
        double deductions = testEmployee.calculateDeductions();
        double netPay = testEmployee.calculateNetPay(daysWorked, overtimeHours);
        
        // Assert
        assertAll("Net pay calculation components",
            () -> assertTrue(grossPay > 0, "Gross pay should be positive"),
            () -> assertTrue(allowances > 0, "Allowances should be positive for regular employee"),
            () -> assertTrue(deductions > 0, "Deductions should be positive"),
            () -> assertEquals(grossPay + allowances - deductions, netPay, 0.01, 
                             "Net pay should equal gross pay plus allowances minus deductions"),
            () -> assertTrue(netPay > 0, "Net pay should be positive")
        );
    }
}