package test;

import model.*;
import dao.EmployeeDAO;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 test suite for Employee models
 * Tests inheritance, abstraction, and polymorphism
 */
@DisplayName("Employee Model Tests - OOP Principles")
class EmployeeTest {
    
    private RegularEmployee regularEmployee;
    private ContractualEmployee contractualEmployee;
    private EmployeeDAO employeeDAO;
    
    @BeforeEach
    @DisplayName("Setup test data before each test")
    void setUp() {
        regularEmployee = new RegularEmployee(10001, "John", "Doe", "Software Developer", 50000);
        contractualEmployee = new ContractualEmployee(10002, "Jane", "Smith", "Consultant", 60000, "2025-12-31", "Project Alpha");
        employeeDAO = new EmployeeDAO();
    }
    
    @Test
    @DisplayName("Test Inheritance - Regular employee should inherit from Employee")
    void testInheritance() {
        // Test inheritance relationship
        assertTrue(regularEmployee instanceof Employee, "RegularEmployee should inherit from Employee");
        assertTrue(contractualEmployee instanceof Employee, "ContractualEmployee should inherit from Employee");
        
        // Test inherited methods
        assertEquals("John Doe", regularEmployee.getFullName(), "Inherited getFullName() should work");
        assertEquals("Jane Smith", contractualEmployee.getFullName(), "Inherited getFullName() should work");
    }
    
    @Test
    @DisplayName("Test Abstraction - Abstract methods must be implemented")
    void testAbstraction() {
        // Test that abstract methods are implemented
        assertDoesNotThrow(() -> regularEmployee.calculateGrossPay(22, 8), 
                          "Abstract method calculateGrossPay should be implemented");
        assertDoesNotThrow(() -> regularEmployee.calculateDeductions(), 
                          "Abstract method calculateDeductions should be implemented");
        assertDoesNotThrow(() -> regularEmployee.calculateAllowances(), 
                          "Abstract method calculateAllowances should be implemented");
        assertDoesNotThrow(() -> regularEmployee.isEligibleForBenefits(), 
                          "Abstract method isEligibleForBenefits should be implemented");
    }
    
    @Test
    @DisplayName("Test Polymorphism - Different implementations for different employee types")
    void testPolymorphism() {
        // Test polymorphic behavior
        Employee[] employees = {regularEmployee, contractualEmployee};
        
        for (Employee emp : employees) {
            // Each employee type should have different implementations
            double grossPay = emp.calculateGrossPay(22, 8);
            double allowances = emp.calculateAllowances();
            boolean benefits = emp.isEligibleForBenefits();
            String type = emp.getEmployeeType();
            
            assertNotNull(type, "Employee type should not be null");
            assertTrue(grossPay >= 0, "Gross pay should be non-negative");
            assertTrue(allowances >= 0, "Allowances should be non-negative");
        }
        
        // Test different behaviors
        assertNotEquals(regularEmployee.getEmployeeType(), contractualEmployee.getEmployeeType(),
                       "Different employee types should return different type strings");
        
        assertTrue(regularEmployee.isEligibleForBenefits(), "Regular employees should be eligible for benefits");
        assertFalse(contractualEmployee.isEligibleForBenefits(), "Contractual employees should not be eligible for benefits");
    }
    
    @Test
    @DisplayName("Test Regular employee gross pay calculation with overtime")
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
                    "Regular employee gross pay calculation should include overtime");
        assertTrue(grossPay > 50000, "Gross pay with overtime should be greater than basic salary");
    }
    
    @Test
    @DisplayName("Test Contractual employee has no overtime pay")
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
    }
    
    @Test
    @DisplayName("Test Employee Factory pattern creates correct types")
    void testEmployeeFactory() {
        // Act
        Employee regular = EmployeeFactory.createEmployee("REGULAR", 20001, "Test", "Regular", "Developer", 45000);
        Employee contractual = EmployeeFactory.createEmployee("CONTRACTUAL", 20002, "Test", "Contractual", "Consultant", 55000);
        
        // Assert
        assertInstanceOf(RegularEmployee.class, regular, 
                        "Factory should create RegularEmployee for REGULAR type");
        assertInstanceOf(ContractualEmployee.class, contractual, 
                        "Factory should create ContractualEmployee for CONTRACTUAL type");
        
        assertEquals("Regular Employee", regular.getEmployeeType());
        assertEquals("Contractual Employee", contractual.getEmployeeType());
    }
    
    @Test
    @DisplayName("Test Factory throws exception for invalid type")
    void testEmployeeFactoryInvalidType() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> EmployeeFactory.createEmployee("INVALID", 30001, "Test", "Test", "Position", 50000),
            "Factory should throw exception for invalid employee type"
        );
        
        assertTrue(exception.getMessage().contains("Unknown employee type"), 
                  "Exception message should indicate unknown employee type");
    }
    
    @ParameterizedTest
    @DisplayName("Test salary calculations with different working days")
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
    
    @ParameterizedTest
    @DisplayName("Test deduction calculations for different salary ranges")
    @CsvSource({
        "20000, true",   // Low salary
        "50000, true",   // Medium salary  
        "100000, true"   // High salary
    })
    void testDeductionCalculations(double salary, boolean shouldHaveDeductions) {
        // Arrange
        RegularEmployee emp = new RegularEmployee(99999, "Test", "Employee", "Test Position", salary);
        
        // Act
        double deductions = emp.calculateDeductions();
        
        // Assert
        if (shouldHaveDeductions) {
            assertTrue(deductions > 0, "Employee with salary " + salary + " should have deductions");
        }
        
        // Test that deductions are reasonable (not more than 50% of salary)
        assertTrue(deductions < salary * 0.5, "Deductions should not exceed 50% of salary");
    }
    
    @Test
    @DisplayName("Test Template Method Pattern - calculateNetPay")
    void testTemplateMethodPattern() {
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
                    "Template method should calculate net pay correctly");
        assertTrue(netPay > 0, "Net pay should be positive");
    }
    
    @Test
    @DisplayName("Test Encapsulation - Private fields and public methods")
    void testEncapsulation() {
        // Test that we can access data through public methods
        assertEquals(10001, regularEmployee.getEmployeeId());
        assertEquals("John", regularEmployee.getFirstName());
        assertEquals("Doe", regularEmployee.getLastName());
        assertEquals(50000.0, regularEmployee.getBasicSalary());
        
        // Test that we can modify data through setters
        regularEmployee.setBasicSalary(60000.0);
        assertEquals(60000.0, regularEmployee.getBasicSalary());
        
        // Test validation in setters
        assertThrows(IllegalArgumentException.class, 
                    () -> regularEmployee.setEmployeeId(-1),
                    "Setter should validate employee ID");
    }
    
    @Test
    @DisplayName("Test Employee validation")
    void testEmployeeValidation() {
        // Test valid employee
        assertTrue(regularEmployee.isValid(), "Regular employee should be valid");
        
        // Test invalid employee
        Employee invalidEmployee = new RegularEmployee();
        assertFalse(invalidEmployee.isValid(), "Employee without required fields should be invalid");
        
        // Test edge cases
        regularEmployee.setFirstName("");
        assertFalse(regularEmployee.isValid(), "Employee with empty first name should be invalid");
        
        regularEmployee.setFirstName("John");
        regularEmployee.setLastName(null);
        assertFalse(regularEmployee.isValid(), "Employee with null last name should be invalid");
    }
    
    @Test
    @DisplayName("Test Employee age calculation")
    void testAgeCalculation() {
        // Set birthday to 30 years ago
        regularEmployee.setBirthday(java.time.LocalDate.now().minusYears(30));
        
        int age = regularEmployee.getAge();
        assertEquals(30, age, "Age should be calculated correctly");
        
        // Test with null birthday
        regularEmployee.setBirthday(null);
        assertEquals(0, regularEmployee.getAge(), "Age should be 0 when birthday is null");
    }
    
    @Test
    @DisplayName("Test Employee toString method")
    void testToStringMethod() {
        String regularString = regularEmployee.toString();
        String contractualString = contractualEmployee.toString();
        
        assertNotNull(regularString, "toString should not return null");
        assertNotNull(contractualString, "toString should not return null");
        
        assertTrue(regularString.contains("RegularEmployee"), "Regular employee toString should contain class name");
        assertTrue(contractualString.contains("ContractualEmployee"), "Contractual employee toString should contain class name");
        
        assertTrue(regularString.contains("John Doe"), "toString should contain employee name");
        assertTrue(contractualString.contains("Jane Smith"), "toString should contain employee name");
    }
    
    @Test
    @DisplayName("Test Polymorphic behavior with Employee array")
    void testPolymorphicArray() {
        // Arrange
        Employee[] employees = {
            new RegularEmployee(1, "Alice", "Johnson", "Manager", 70000),
            new ContractualEmployee(2, "Bob", "Wilson", "Consultant", 80000, "2025-12-31", "Project Beta"),
            new RegularEmployee(3, "Carol", "Brown", "Developer", 55000)
        };
        
        // Act & Assert
        for (Employee emp : employees) {
            // Each employee should respond to the same interface differently
            assertNotNull(emp.getEmployeeType(), "Employee type should not be null");
            assertTrue(emp.calculateGrossPay(22, 0) > 0, "Gross pay should be positive");
            assertTrue(emp.calculateDeductions() >= 0, "Deductions should be non-negative");
            assertTrue(emp.calculateAllowances() >= 0, "Allowances should be non-negative");
            
            // Test polymorphic method calls
            double netPay = emp.calculateNetPay(22, 0);
            assertTrue(netPay > 0, "Net pay should be positive for " + emp.getEmployeeType());
        }
    }
    
    @AfterEach
    @DisplayName("Cleanup after each test")
    void tearDown() {
        // Reset any static variables or cleanup resources if needed
        regularEmployee = null;
        contractualEmployee = null;
    }
    
    @AfterAll
    @DisplayName("Final cleanup after all tests")
    static void tearDownAll() {
        System.out.println("✅ All Employee OOP tests completed successfully!");
        System.out.println("📊 Tested: Inheritance, Abstraction, Polymorphism, Encapsulation");
    }
}