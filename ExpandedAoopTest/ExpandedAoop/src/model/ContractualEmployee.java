/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

// Another concrete class showing POLYMORPHISM
public class ContractualEmployee extends Employee {
    private String contractEndDate;
    private String projectAssignment;
    
    public ContractualEmployee(int employeeId, String firstName, String lastName,
                              String position, String department, double basicSalary,
                              String contractEndDate, String projectAssignment) {
        super(employeeId, firstName, lastName, position, department, basicSalary);
        this.contractEndDate = contractEndDate;
        this.projectAssignment = projectAssignment;
    }
    
    @Override
    public double calculateGrossPay(int daysWorked, double overtimeHours) {
        // Contractual employees get different calculation
        double dailyRate = basicSalary / 22;
        return dailyRate * daysWorked; // No overtime for contractuals
    }
    
    @Override
    public double calculateDeductions() {
        // Minimal deductions for contractuals
        double sss = calculateSSS();
        double philHealth = calculatePhilHealth();
        return sss + philHealth; // No Pag-IBIG, no withholding tax
    }
    
    @Override
    public double calculateAllowances() {
        return 0; // Contractuals don't get allowances
    }
    
    @Override
    public boolean isEligibleForBenefits() {
        return false; // Limited benefits for contractuals
    }
    
    @Override
    public String getEmployeeType() {
        return "Contractual Employee";
    }
    
    private double calculateSSS() {
        return Math.min(basicSalary * 0.045, 1125.0);
    }
    
    private double calculatePhilHealth() {
        return Math.min(basicSalary * 0.025, 1800.0);
    }
    
    // Getters and setters
    public String getContractEndDate() { return contractEndDate; }
    public void setContractEndDate(String contractEndDate) { this.contractEndDate = contractEndDate; }
    
    public String getProjectAssignment() { return projectAssignment; }
    public void setProjectAssignment(String projectAssignment) { this.projectAssignment = projectAssignment; }
}