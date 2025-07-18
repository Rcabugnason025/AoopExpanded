/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package test;

import model.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Model Tests")
class EmployeeTest {
    
    private RegularEmployee regularEmployee;
    private ContractualEmployee contractualEmployee;
    
    @BeforeEach
    @DisplayName("Setup test data")
    void setUp() {
        regularEmployee = new RegularEmployee(10001, "John", "Doe", "Developer", "IT", 50000);
        contractualEmployee = new ContractualEmployee(10002, "Jane", "Smith", "Consultant", "IT", 60000, "2025-12-31", "Project Alpha");
    }
    
    @Test
    @DisplayName("Regular employee should calculate gross pay correctly")
    void testRegularEmployeeGrossPayCalculation() {
        // Arrange
        int daysWorked = 22;
        double overtimeHours = 8.0;
        
        // Act
        double grossPay = regularEmployee.calculateGrossPay(daysWorked, overtimeHours);
        
        // Assert
        double expectedDailyRate = 50000.0 / 22; // ₱2,272.73
        double expectedRegularPay = expectedDailyRate * 22; // ₱50,000
        double expectedOvertimePay = (expectedDailyRate / 8) * 8.0 * 1.25; // ₱2,840.91
        double expectedGrossPay = expectedRegularPay + expectedOvertimePay;
        
        assertEquals(expectedGrossPay, grossPay, 0.01, 
                    "Regular employee gross pay calculation should be correct");
        assertTrue(grossPay > 50000, "Gross pay with overtime should be greater than basic salary");
    }
    
    @Test
    @DisplayName("Contractual employee should not receive overtime pay")
    void testContractualEmployeeNoOvertimePay() {
        // Arrange
        int daysWorked = 22;
        double overtimeHours = 10.0; // Should be ignored
        
        // Act
        double grossPay = contractualEmployee.calculateGrossPay(daysWorked, overtimeHours);
        
        // Assert
        double expectedDailyRate = 60000.0 / 22;
        double expectedGrossPay = expectedDailyRate * 22; // No overtime
        
        assertEquals(expectedGrossPay, grossPay, 0.01, 
                    "Contractual employee should not receive overtime pay");
        assertEquals(60000.0, grossPay, 0.01, 
                    "Contractual gross pay should equal basic salary for full month");
    }
    
    @Test
    @DisplayName("Regular employee should be eligible for benefits")
    void testRegularEmployeeBenefitsEligibility() {
        // Act & Assert
        assertTrue(regularEmployee.isEligibleForBenefits(), 
                  "Regular employees should be eligible for benefits");
    }
    
    @Test
    @DisplayName("Contractual employee should not be eligible for benefits")
    void testContractualEmployeeBenefitsEligibility() {
        // Act & Assert
        assertFalse(contractualEmployee.isEligibleForBenefits(), 
                   "Contractual employees should not be eligible for benefits");
    }
    
    @Test
    @DisplayName("Regular employee should receive allowances")
    void testRegularEmployeeAllowances() {
        // Act
        double allowances = regularEmployee.calculateAllowances();
        
        // Assert
        double expectedAllowances = 1500.0 + 2000.0 + 1000.0; // Rice + Phone + Clothing
        assertEquals(expectedAllowances, allowances, 0.01, 
                    "Regular employee should receive all allowances");
        assertTrue(allowances > 0, "Allowances should be positive");
    }
    
    @Test
    @DisplayName("Contractual employee should not receive allowances")
    void testContractualEmployeeAllowances() {
        // Act
        double allowances = contractualEmployee.calculateAllowances();
        
        // Assert
        assertEquals(0.0, allowances, 0.01, 
                    "Contractual employee should not receive allowances");
    }
    
    @Test
    @DisplayName("Employee deductions should be calculated correctly")
    void testEmployeeDeductionsCalculation() {
        // Act
        double regularDeductions = regularEmployee.calculateDeductions();
        double contractualDeductions = contractualEmployee.calculateDeductions();
        
        // Assert
        assertTrue(regularDeductions > 0, "Regular employee should have deductions");
        assertTrue(contractualDeductions > 0, "Contractual employee should have some deductions");
        assertTrue(regularDeductions > contractualDeductions, 
                  "Regular employee should have more deductions than contractual");
    }
    
    @Test
    @DisplayName("Net pay calculation should use template method pattern")
    void testNetPayTemplateMethod() {
        // Arrange
        int daysWorked = 22;
        double overtimeHours = 4.0;
        
        // Act
        double netPay = regularEmployee.calculateNetPay(daysWorked, overtimeHours);
        double grossPay = regularEmployee.calculateGrossPay(daysWorked, overtimeHours);
        double allowances = regularEmployee.calculateAllowances();
        double deductions = regularEmployee.calculateDeductions();
        
        // Assert
        double expectedNetPay = grossPay + allowances - deductions;
        assertEquals(expectedNetPay, netPay, 0.01, 
                    "Net pay should equal gross pay plus allowances minus deductions");
        assertTrue(netPay > 0, "Net pay should be positive");
    }
    
    @Test
    @DisplayName("Employee type should be polymorphic")
    void testEmployeeTypePolymorphism() {
        // Act & Assert
        assertEquals("Regular Employee", regularEmployee.getEmployeeType(), 
                    "Regular employee type should be correct");
        assertEquals("Contractual Employee", contractualEmployee.getEmployeeType(), 
                    "Contractual employee type should be correct");
        
        // Test polymorphism
        Employee[] employees = {regularEmployee, contractualEmployee};
        for (Employee emp : employees) {
            assertNotNull(emp.getEmployeeType(), "Employee type should not be null");
            assertFalse(emp.getEmployeeType().isEmpty(), "Employee type should not be empty");
        }
    }
    
    @Test
    @DisplayName("Employee factory should create correct employee types")
    void testEmployeeFactory() {
        // Act
        Employee regular = EmployeeFactory.createEmployee("REGULAR", 20001, "Test", "Regular", "Developer", "IT", 45000);
        Employee contractual = EmployeeFactory.createEmployee("CONTRACTUAL", 20002, "Test", "Contractual", "Consultant", "IT", 55000);
        
        // Assert
        assertInstanceOf(RegularEmployee.class, regular, 
                        "Factory should create RegularEmployee for REGULAR type");
        assertInstanceOf(ContractualEmployee.class, contractual, 
                        "Factory should create ContractualEmployee for CONTRACTUAL type");
        
        assertEquals("Regular Employee", regular.getEmployeeType());
        assertEquals("Contractual Employee", contractual.getEmployeeType());
    }
    
    @Test
    @DisplayName("Employee factory should throw exception for invalid type")
    void testEmployeeFactoryInvalidType() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> EmployeeFactory.createEmployee("INVALID", 30001, "Test", "Test", "Position", "Dept", 50000),
            "Factory should throw exception for invalid employee type"
        );
        
        assertTrue(exception.getMessage().contains("Unknown employee type"), 
                  "Exception message should indicate unknown employee type");
    }
    
    @Test
    @DisplayName("Employee full name should be concatenated correctly")
    void testEmployeeFullName() {
        // Act & Assert
        assertEquals("John Doe", regularEmployee.getFullName(), 
                    "Full name should be first name + space + last name");
        assertEquals("Jane Smith", contractualEmployee.getFullName(), 
                    "Full name should be first name + space + last name");
    }
    
    @Test
    @DisplayName("Employee properties should be settable and gettable")
    void testEmployeeEncapsulation() {
        // Arrange
        String newPosition = "Senior Developer";
        String newDepartment = "Engineering";
        double newSalary = 75000.0;
        
        // Act
        regularEmployee.setPosition(newPosition);
        regularEmployee.setDepartment(newDepartment);
        regularEmployee.setBasicSalary(newSalary);
        
        // Assert
        assertEquals(newPosition, regularEmployee.getPosition(), 
                    "Position should be updatable");
        assertEquals(newDepartment, regularEmployee.getDepartment(), 
                    "Department should be updatable");
        assertEquals(newSalary, regularEmployee.getBasicSalary(), 0.01, 
                    "Basic salary should be updatable");
    }
    
    @ParameterizedTest
    @DisplayName("Salary calculations should work for different inputs")
    @ValueSource(ints = {15, 20, 22, 25})
    void testSalaryCalculationWithDifferentDays(int daysWorked) {
        // Act
        double grossPay = regularEmployee.calculateGrossPay(daysWorked, 0);
        
        // Assert
        assertTrue(grossPay > 0, "Gross pay should be positive for " + daysWorked + " days");
        
        double expectedDailyRate = regularEmployee.getBasicSalary() / 22;
        double expectedGrossPay = expectedDailyRate * daysWorked;
        assertEquals(expectedGrossPay, grossPay, 0.01, 
                    "Gross pay should be calculated correctly for " + daysWorked + " days");
    }
    
    @AfterEach
    @DisplayName("Cleanup after each test")
    void tearDown() {
        // Reset any static variables or cleanup resources if needed
        System.gc(); // Suggest garbage collection
    }
    
    @AfterAll
    @DisplayName("Final cleanup after all tests")
    static void tearDownAll() {
        System.out.println("All Employee tests completed successfully!");
    }
}
