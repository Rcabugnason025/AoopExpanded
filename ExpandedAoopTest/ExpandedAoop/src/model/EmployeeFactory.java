package model;

// Factory pattern for creating employees - DESIGN PATTERN
public class EmployeeFactory {
    
    // Factory method demonstrating POLYMORPHISM
    public static Employee createEmployee(String employeeType, int employeeId, 
                                        String firstName, String lastName, String position, 
                                        double basicSalary) {
        switch (employeeType.toUpperCase()) {
            case "REGULAR":
                return new RegularEmployee(employeeId, firstName, lastName, position, basicSalary);
            case "CONTRACTUAL":
                return new ContractualEmployee(employeeId, firstName, lastName, position, basicSalary, "", "");
            default:
                throw new IllegalArgumentException("Unknown employee type: " + employeeType);
        }
    }
    
    // Overloaded factory method for contractual employees
    public static Employee createContractualEmployee(int employeeId, String firstName, String lastName, 
                                                   String position, double basicSalary, 
                                                   String contractEndDate, String projectAssignment) {
        return new ContractualEmployee(employeeId, firstName, lastName, position, basicSalary, 
                                     contractEndDate, projectAssignment);
    }
    
    // Polymorphic method demonstrating runtime behavior
    public static void printEmployeeDetails(Employee employee) {
        System.out.println("Employee Type: " + employee.getEmployeeType());
        System.out.println("Benefits Eligible: " + employee.isEligibleForBenefits());
        System.out.println("Net Pay (22 days, 0 OT): " + employee.calculateNetPay(22, 0));
        System.out.println("Allowances: " + employee.calculateAllowances());
        System.out.println("Deductions: " + employee.calculateDeductions());
    }
    
    // Method to determine employee type from status
    public static String determineEmployeeType(String status) {
        if (status == null) return "REGULAR";
        
        switch (status.toUpperCase()) {
            case "CONTRACTUAL":
            case "CONTRACT":
                return "CONTRACTUAL";
            case "REGULAR":
            case "PROBATIONARY":
            default:
                return "REGULAR";
        }
    }
}