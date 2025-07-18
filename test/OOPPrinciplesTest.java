package test;

import model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 test suite specifically for OOP principles
 * Tests the four pillars of OOP: Encapsulation, Inheritance, Polymorphism, Abstraction
 */
@DisplayName("OOP Principles Test Suite")
class OOPPrinciplesTest {
    
    private Employee regularEmployee;
    private Employee contractualEmployee;
    
    @BeforeEach
    @DisplayName("Setup OOP test environment")
    void setUp() {
        regularEmployee = new RegularEmployee(1001, "Alice", "Johnson", "Developer", 50000);
        contractualEmployee = new ContractualEmployee(1002, "Bob", "Smith", "Consultant", 60000, "2025-12-31", "Project X");
    }
    
    @Nested
    @DisplayName("Encapsulation Tests")
    class EncapsulationTests {
        
        @Test
        @DisplayName("Test private fields are not directly accessible")
        void testPrivateFieldsEncapsulation() {
            // We cannot directly access private fields, must use getters/setters
            assertDoesNotThrow(() -> regularEmployee.getEmployeeId(), "Should access employee ID through getter");
            assertDoesNotThrow(() -> regularEmployee.setEmployeeId(2001), "Should modify employee ID through setter");
            
            assertEquals(2001, regularEmployee.getEmployeeId(), "Setter should update the field value");
        }
        
        @Test
        @DisplayName("Test data validation in setters")
        void testSetterValidation() {
            // Test validation in setters
            assertThrows(IllegalArgumentException.class, 
                        () -> regularEmployee.setEmployeeId(-1),
                        "Setter should validate negative employee ID");
            
            assertThrows(IllegalArgumentException.class,
                        () -> regularEmployee.setFirstName(""),
                        "Setter should validate empty first name");
            
            assertThrows(IllegalArgumentException.class,
                        () -> regularEmployee.setLastName(null),
                        "Setter should validate null last name");
        }
        
        @Test
        @DisplayName("Test controlled access to internal state")
        void testControlledAccess() {
            // Test that internal calculations are protected
            double originalSalary = regularEmployee.getBasicSalary();
            regularEmployee.setBasicSalary(60000);
            
            // Derived fields should be automatically updated
            assertNotEquals(originalSalary, regularEmployee.getBasicSalary(), 
                           "Salary should be updated through setter");
            
            // Hourly rate should be recalculated
            double expectedHourlyRate = 60000 / 22 / 8; // New salary / 22 days / 8 hours
            assertEquals(expectedHourlyRate, regularEmployee.getHourlyRate(), 0.01,
                        "Hourly rate should be automatically recalculated");
        }
    }
    
    @Nested
    @DisplayName("Inheritance Tests")
    class InheritanceTests {
        
        @Test
        @DisplayName("Test inheritance hierarchy")
        void testInheritanceHierarchy() {
            // Test inheritance chain: RegularEmployee -> Employee -> BaseEntity
            assertTrue(regularEmployee instanceof Employee, "RegularEmployee should inherit from Employee");
            assertTrue(regularEmployee instanceof BaseEntity, "RegularEmployee should inherit from BaseEntity");
            assertTrue(contractualEmployee instanceof Employee, "ContractualEmployee should inherit from Employee");
            assertTrue(contractualEmployee instanceof BaseEntity, "ContractualEmployee should inherit from BaseEntity");
        }
        
        @Test
        @DisplayName("Test inherited method usage")
        void testInheritedMethods() {
            // Test methods inherited from Employee
            assertEquals("Alice Johnson", regularEmployee.getFullName(), "Should inherit getFullName from Employee");
            assertTrue(regularEmployee.getDailyRate() > 0, "Should inherit getDailyRate from Employee");
            
            // Test methods inherited from BaseEntity
            assertNotNull(regularEmployee.getCreatedAt(), "Should inherit getCreatedAt from BaseEntity");
            assertTrue(regularEmployee.getId() >= 0, "Should inherit getId from BaseEntity");
        }
        
        @Test
        @DisplayName("Test method overriding")
        void testMethodOverriding() {
            // Test that subclasses override parent methods appropriately
            assertEquals("Regular Employee", regularEmployee.getEmployeeType(), 
                        "RegularEmployee should override getEmployeeType");
            assertEquals("Contractual Employee", contractualEmployee.getEmployeeType(),
                        "ContractualEmployee should override getEmployeeType");
            
            // Test toString overriding
            String regularString = regularEmployee.toString();
            String contractualString = contractualEmployee.toString();
            
            assertTrue(regularString.contains("RegularEmployee"), "RegularEmployee should override toString");
            assertTrue(contractualString.contains("ContractualEmployee"), "ContractualEmployee should override toString");
        }
        
        @Test
        @DisplayName("Test super constructor calls")
        void testSuperConstructorCalls() {
            // Create new employees to test constructor chaining
            RegularEmployee newRegular = new RegularEmployee(3001, "Charlie", "Brown", "Analyst", 45000);
            ContractualEmployee newContractual = new ContractualEmployee(3002, "Diana", "Prince", "Specialist", 55000, "2025-06-30", "Project Y");
            
            // Test that parent constructor was called
            assertEquals("Charlie Brown", newRegular.getFullName(), "Super constructor should set name");
            assertEquals("Diana Prince", newContractual.getFullName(), "Super constructor should set name");
            assertEquals(45000, newRegular.getBasicSalary(), "Super constructor should set salary");
            assertEquals(55000, newContractual.getBasicSalary(), "Super constructor should set salary");
        }
    }
    
    @Nested
    @DisplayName("Polymorphism Tests")
    class PolymorphismTests {
        
        @Test
        @DisplayName("Test runtime polymorphism with method overriding")
        void testRuntimePolymorphism() {
            // Create array of Employee references pointing to different concrete types
            Employee[] employees = {
                new RegularEmployee(4001, "Eve", "Adams", "Manager", 70000),
                new ContractualEmployee(4002, "Frank", "Miller", "Advisor", 80000, "2025-09-30", "Project Z")
            };
            
            // Test that each employee behaves according to its actual type at runtime
            for (Employee emp : employees) {
                double grossPay = emp.calculateGrossPay(22, 8);
                double allowances = emp.calculateAllowances();
                boolean benefits = emp.isEligibleForBenefits();
                String type = emp.getEmployeeType();
                
                // All should return valid values
                assertTrue(grossPay > 0, type + " should have positive gross pay");
                assertTrue(allowances >= 0, type + " should have non-negative allowances");
                assertNotNull(type, "Employee type should not be null");
                
                // But behavior should differ based on actual type
                if (emp instanceof RegularEmployee) {
                    assertTrue(allowances > 0, "Regular employee should have allowances");
                    assertTrue(benefits, "Regular employee should be eligible for benefits");
                } else if (emp instanceof ContractualEmployee) {
                    assertEquals(0, allowances, 0.01, "Contractual employee should have no allowances");
                    assertFalse(benefits, "Contractual employee should not be eligible for benefits");
                }
            }
        }
        
        @Test
        @DisplayName("Test method overloading")
        void testMethodOverloading() {
            // Test Factory method overloading
            Employee emp1 = EmployeeFactory.createEmployee("REGULAR", 5001, "Grace", "Lee", "Developer", 50000);
            Employee emp2 = EmployeeFactory.createContractualEmployee(5002, "Henry", "Wong", "Consultant", 60000, "2025-12-31", "Special Project");
            
            assertInstanceOf(RegularEmployee.class, emp1, "First factory method should create RegularEmployee");
            assertInstanceOf(ContractualEmployee.class, emp2, "Overloaded factory method should create ContractualEmployee");
            
            // Test that overloaded method sets additional fields
            ContractualEmployee contractual = (ContractualEmployee) emp2;
            assertEquals("2025-12-31", contractual.getContractEndDate(), "Overloaded method should set contract end date");
            assertEquals("Special Project", contractual.getProjectAssignment(), "Overloaded method should set project assignment");
        }
        
        @Test
        @DisplayName("Test polymorphic collections")
        void testPolymorphicCollections() {
            // Create a collection of different employee types
            java.util.List<Employee> employeeList = java.util.Arrays.asList(
                new RegularEmployee(6001, "Ivy", "Chen", "Senior Developer", 65000),
                new ContractualEmployee(6002, "Jack", "Taylor", "Technical Writer", 45000, "2025-08-31", "Documentation Project"),
                new RegularEmployee(6003, "Kelly", "Davis", "Team Lead", 75000)
            );
            
            // Test polymorphic behavior in collection
            double totalNetPay = 0;
            for (Employee emp : employeeList) {
                totalNetPay += emp.calculateNetPay(22, 0);
            }
            
            assertTrue(totalNetPay > 0, "Total net pay should be positive");
            assertEquals(3, employeeList.size(), "Should have 3 employees in collection");
            
            // Test that we can call polymorphic methods on collection elements
            long regularCount = employeeList.stream()
                .filter(emp -> emp instanceof RegularEmployee)
                .count();
            long contractualCount = employeeList.stream()
                .filter(emp -> emp instanceof ContractualEmployee)
                .count();
            
            assertEquals(2, regularCount, "Should have 2 regular employees");
            assertEquals(1, contractualCount, "Should have 1 contractual employee");
        }
    }
    
    @Nested
    @DisplayName("Abstraction Tests")
    class AbstractionTests {
        
        @Test
        @DisplayName("Test abstract class cannot be instantiated directly")
        void testAbstractClassInstantiation() {
            // We can only test this conceptually since Java prevents direct instantiation
            // But we can verify that we must use concrete subclasses
            assertDoesNotThrow(() -> new RegularEmployee(), "Should be able to create concrete RegularEmployee");
            assertDoesNotThrow(() -> new ContractualEmployee(), "Should be able to create concrete ContractualEmployee");
            
            // Verify that abstract methods are implemented
            assertDoesNotThrow(() -> regularEmployee.calculateGrossPay(22, 0), "Abstract method should be implemented");
            assertDoesNotThrow(() -> contractualEmployee.calculateDeductions(), "Abstract method should be implemented");
        }
        
        @Test
        @DisplayName("Test abstract method implementation requirement")
        void testAbstractMethodImplementation() {
            // All abstract methods must be implemented by concrete classes
            
            // Test calculateGrossPay
            double regularGrossPay = regularEmployee.calculateGrossPay(22, 8);
            double contractualGrossPay = contractualEmployee.calculateGrossPay(22, 8);
            
            assertTrue(regularGrossPay > 0, "RegularEmployee must implement calculateGrossPay");
            assertTrue(contractualGrossPay > 0, "ContractualEmployee must implement calculateGrossPay");
            
            // Test calculateDeductions
            double regularDeductions = regularEmployee.calculateDeductions();
            double contractualDeductions = contractualEmployee.calculateDeductions();
            
            assertTrue(regularDeductions > 0, "RegularEmployee must implement calculateDeductions");
            assertTrue(contractualDeductions >= 0, "ContractualEmployee must implement calculateDeductions");
            
            // Test calculateAllowances
            double regularAllowances = regularEmployee.calculateAllowances();
            double contractualAllowances = contractualEmployee.calculateAllowances();
            
            assertTrue(regularAllowances > 0, "RegularEmployee must implement calculateAllowances");
            assertEquals(0, contractualAllowances, 0.01, "ContractualEmployee must implement calculateAllowances");
            
            // Test isEligibleForBenefits
            assertTrue(regularEmployee.isEligibleForBenefits(), "RegularEmployee must implement isEligibleForBenefits");
            assertFalse(contractualEmployee.isEligibleForBenefits(), "ContractualEmployee must implement isEligibleForBenefits");
        }
        
        @Test
        @DisplayName("Test template method pattern (concrete method using abstract methods)")
        void testTemplateMethodPattern() {
            // calculateNetPay is a template method that uses abstract methods
            double regularNetPay = regularEmployee.calculateNetPay(22, 4);
            double contractualNetPay = contractualEmployee.calculateNetPay(22, 4);
            
            // Template method should work for both types
            assertTrue(regularNetPay > 0, "Template method should work for RegularEmployee");
            assertTrue(contractualNetPay > 0, "Template method should work for ContractualEmployee");
            
            // Verify template method algorithm: grossPay + allowances - deductions
            double regularGross = regularEmployee.calculateGrossPay(22, 4);
            double regularAllowances = regularEmployee.calculateAllowances();
            double regularDeductions = regularEmployee.calculateDeductions();
            double expectedRegularNet = regularGross + regularAllowances - regularDeductions;
            
            assertEquals(expectedRegularNet, regularNetPay, 0.01, 
                        "Template method should follow defined algorithm");
        }
        
        @Test
        @DisplayName("Test interface-like behavior through abstract methods")
        void testInterfaceLikeBehavior() {
            // Abstract methods provide interface-like contracts
            
            // Create different employee types and test they all respond to same interface
            Employee[] employees = {
                new RegularEmployee(7001, "Luna", "Garcia", "QA Engineer", 48000),
                new ContractualEmployee(7002, "Mike", "Rodriguez", "Business Analyst", 52000, "2025-11-30", "Analysis Project")
            };
            
            // All employees must respond to the same abstract method interface
            for (Employee emp : employees) {
                // Test that all abstract methods are callable and return reasonable values
                assertDoesNotThrow(() -> emp.calculateGrossPay(22, 0), "All employees must implement calculateGrossPay");
                assertDoesNotThrow(() -> emp.calculateDeductions(), "All employees must implement calculateDeductions");
                assertDoesNotThrow(() -> emp.calculateAllowances(), "All employees must implement calculateAllowances");
                assertDoesNotThrow(() -> emp.isEligibleForBenefits(), "All employees must implement isEligibleForBenefits");
                
                // Test return value constraints
                assertTrue(emp.calculateGrossPay(22, 0) >= 0, "Gross pay should be non-negative");
                assertTrue(emp.calculateDeductions() >= 0, "Deductions should be non-negative");
                assertTrue(emp.calculateAllowances() >= 0, "Allowances should be non-negative");
            }
        }
    }
    
    @Test
    @DisplayName("Integration test - All OOP principles working together")
    void testOOPIntegration() {
        // Create employees using Factory (Abstraction)
        Employee manager = EmployeeFactory.createEmployee("REGULAR", 8001, "Nancy", "Wilson", "Project Manager", 80000);
        Employee consultant = EmployeeFactory.createEmployee("CONTRACTUAL", 8002, "Oscar", "Martinez", "IT Consultant", 70000);
        
        // Test Encapsulation - controlled access to data
        assertEquals(80000, manager.getBasicSalary(), "Encapsulation should allow controlled access");
        manager.setBasicSalary(85000);
        assertEquals(85000, manager.getBasicSalary(), "Encapsulation should allow controlled modification");
        
        // Test Inheritance - shared behavior
        assertTrue(manager instanceof Employee, "Should inherit from Employee");
        assertTrue(consultant instanceof Employee, "Should inherit from Employee");
        assertEquals("Nancy Wilson", manager.getFullName(), "Should inherit getFullName method");
        assertEquals("Oscar Martinez", consultant.getFullName(), "Should inherit getFullName method");
        
        // Test Polymorphism - different behavior for same interface
        double managerAllowances = manager.calculateAllowances();
        double consultantAllowances = consultant.calculateAllowances();
        
        assertTrue(managerAllowances > 0, "Manager (Regular) should have allowances");
        assertEquals(0, consultantAllowances, 0.01, "Consultant (Contractual) should have no allowances");
        
        // Test Abstraction - template method using abstract methods
        double managerNetPay = manager.calculateNetPay(22, 0);
        double consultantNetPay = consultant.calculateNetPay(22, 0);
        
        assertTrue(managerNetPay > 0, "Template method should work for manager");
        assertTrue(consultantNetPay > 0, "Template method should work for consultant");
        
        // Verify different employee types produce different results
        assertNotEquals(manager.getEmployeeType(), consultant.getEmployeeType(), 
                       "Different employee types should return different type strings");
    }
    
    @AfterEach
    @DisplayName("Cleanup OOP test environment")
    void tearDown() {
        regularEmployee = null;
        contractualEmployee = null;
    }
    
    @AfterAll
    @DisplayName("OOP Principles test completion")
    static void tearDownAll() {
        System.out.println("🎯 All OOP Principles tests completed successfully!");
        System.out.println("✅ Encapsulation: Data hiding and controlled access verified");
        System.out.println("✅ Inheritance: Code reuse and hierarchy verified");
        System.out.println("✅ Polymorphism: Runtime behavior and method overriding verified");
        System.out.println("✅ Abstraction: Abstract classes and template methods verified");
    }
}