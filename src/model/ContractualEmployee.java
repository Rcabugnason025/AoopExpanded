package model;

// Another concrete class showing POLYMORPHISM and INHERITANCE
public class ContractualEmployee extends Employee {
    private String contractEndDate;
    private String projectAssignment;
    private static final double CONTRACTUAL_RICE_SUBSIDY = 0.0;
    private static final double CONTRACTUAL_PHONE_ALLOWANCE = 0.0;
    private static final double CONTRACTUAL_CLOTHING_ALLOWANCE = 0.0;
    
    public ContractualEmployee() {
        super();
        this.riceSubsidy = CONTRACTUAL_RICE_SUBSIDY;
        this.phoneAllowance = CONTRACTUAL_PHONE_ALLOWANCE;
        this.clothingAllowance = CONTRACTUAL_CLOTHING_ALLOWANCE;
    }
    
    public ContractualEmployee(int employeeId, String firstName, String lastName,
                              String position, double basicSalary,
                              String contractEndDate, String projectAssignment) {
        super(employeeId, firstName, lastName, position, basicSalary);
        this.contractEndDate = contractEndDate;
        this.projectAssignment = projectAssignment;
        this.riceSubsidy = CONTRACTUAL_RICE_SUBSIDY;
        this.phoneAllowance = CONTRACTUAL_PHONE_ALLOWANCE;
        this.clothingAllowance = CONTRACTUAL_CLOTHING_ALLOWANCE;
    }
    
    // Different implementation for contractual employees - POLYMORPHISM
    @Override
    public double calculateGrossPay(int daysWorked, double overtimeHours) {
        // Contractual employees get different calculation - no overtime pay
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
    
    // Private helper methods - ENCAPSULATION
    private double calculateSSS() {
        return Math.min(basicSalary * 0.045, 1125.0);
    }
    
    private double calculatePhilHealth() {
        return Math.min(basicSalary * 0.025, 1800.0);
    }
    
    // Getters and setters for contractual-specific fields
    public String getContractEndDate() { return contractEndDate; }
    public void setContractEndDate(String contractEndDate) { 
        this.contractEndDate = contractEndDate; 
        touch();
    }
    
    public String getProjectAssignment() { return projectAssignment; }
    public void setProjectAssignment(String projectAssignment) { 
        this.projectAssignment = projectAssignment; 
        touch();
    }
    
    @Override
    public String toString() {
        return "ContractualEmployee{" +
                "employeeId=" + employeeId +
                ", name='" + getFullName() + '\'' +
                ", position='" + position + '\'' +
                ", contractEndDate='" + contractEndDate + '\'' +
                ", projectAssignment='" + projectAssignment + '\'' +
                '}';
    }
}