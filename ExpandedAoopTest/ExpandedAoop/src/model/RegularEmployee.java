/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

// Concrete class demonstrating INHERITANCE
public class RegularEmployee extends Employee {
    private double riceSubsidy;
    private double phoneAllowance;
    private double clothingAllowance;
    
    public RegularEmployee(int employeeId, String firstName, String lastName,
                          String position, String department, double basicSalary) {
        super(employeeId, firstName, lastName, position, department, basicSalary);
        this.riceSubsidy = 1500.0;
        this.phoneAllowance = 2000.0;
        this.clothingAllowance = 1000.0;
    }
    
    @Override
    public double calculateGrossPay(int daysWorked, double overtimeHours) {
        double dailyRate = basicSalary / 22; // Assuming 22 working days per month
        double regularPay = dailyRate * daysWorked;
        double overtimePay = (dailyRate / 8) * overtimeHours * 1.25; // 125% rate
        return regularPay + overtimePay;
    }
    
    @Override
    public double calculateDeductions() {
        double sss = calculateSSS();
        double philHealth = calculatePhilHealth();
        double pagIbig = calculatePagIBIG();
        double withholdingTax = calculateWithholdingTax();
        return sss + philHealth + pagIbig + withholdingTax;
    }
    
    @Override
    public double calculateAllowances() {
        return riceSubsidy + phoneAllowance + clothingAllowance;
    }
    
    @Override
    public boolean isEligibleForBenefits() {
        return true; // Regular employees get all benefits
    }
    
    @Override
    public String getEmployeeType() {
        return "Regular Employee";
    }
    
    // Private helper methods for calculations
    private double calculateSSS() {
        if (basicSalary <= 3250) return 135.0;
        if (basicSalary <= 25000) return basicSalary * 0.045;
        return 1125.0; // Maximum SSS
    }
    
    private double calculatePhilHealth() {
        return Math.min(basicSalary * 0.025, 1800.0); // 2.5% of salary, max 1800
    }
    
    private double calculatePagIBIG() {
        if (basicSalary <= 1500) return basicSalary * 0.01;
        return Math.min(basicSalary * 0.02, 100.0); // 2% of salary, max 100
    }
    
    private double calculateWithholdingTax() {
        // Philippine TRAIN law tax calculation
        double annualSalary = basicSalary * 12;
        if (annualSalary <= 250000) return 0;
        if (annualSalary <= 400000) return (annualSalary - 250000) * 0.15 / 12;
        if (annualSalary <= 800000) return (22500 + (annualSalary - 400000) * 0.20) / 12;
        // Add more brackets as needed
        return 0; // Simplified for demo
    }
    
    // Getters and setters for allowances
    public double getRiceSubsidy() { return riceSubsidy; }
    public void setRiceSubsidy(double riceSubsidy) { this.riceSubsidy = riceSubsidy; }
    
    public double getPhoneAllowance() { return phoneAllowance; }
    public void setPhoneAllowance(double phoneAllowance) { this.phoneAllowance = phoneAllowance; }
    
    public double getClothingAllowance() { return clothingAllowance; }
    public void setClothingAllowance(double clothingAllowance) { this.clothingAllowance = clothingAllowance; }
}