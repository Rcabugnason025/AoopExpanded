/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

// Factory pattern for creating employees
public class EmployeeFactory {
    
    public static Employee createEmployee(String employeeType, int employeeId, 
                                        String firstName, String lastName, String position, 
                                        String department, double basicSalary) {
        switch (employeeType.toUpperCase()) {
            case "REGULAR":
                return new RegularEmployee(employeeId, firstName, lastName, position, department, basicSalary);
            case "CONTRACTUAL":
                return new ContractualEmployee(employeeId, firstName, lastName, position, department, basicSalary, "", "");
            default:
                throw new IllegalArgumentException("Unknown employee type: " + employeeType);
        }
    }
    
    // Polymorphic method demonstrating runtime behavior
    public static void printEmployeeDetails(Employee employee) {
        System.out.println("Employee Type: " + employee.getEmployeeType());
        System.out.println("Benefits Eligible: " + employee.isEligibleForBenefits());
        System.out.println("Net Pay (22 days, 0 OT): " + employee.calculateNetPay(22, 0));
    }
}
