package model;

// Concrete class demonstrating INHERITANCE and POLYMORPHISM
public class RegularEmployee extends Employee {
    private static final double DEFAULT_RICE_SUBSIDY = 1500.0;
    private static final double DEFAULT_PHONE_ALLOWANCE = 2000.0;
    private static final double DEFAULT_CLOTHING_ALLOWANCE = 1000.0;
    
    // Constructor using super() - INHERITANCE
    public RegularEmployee() {
        super();
        this.riceSubsidy = DEFAULT_RICE_SUBSIDY;
        this.phoneAllowance = DEFAULT_PHONE_ALLOWANCE;
        this.clothingAllowance = DEFAULT_CLOTHING_ALLOWANCE;
    }
    
    public RegularEmployee(int employeeId, String firstName, String lastName, String position, double basicSalary) {
        super(employeeId, firstName, lastName, position, basicSalary);
        this.riceSubsidy = DEFAULT_RICE_SUBSIDY;
        this.phoneAllowance = DEFAULT_PHONE_ALLOWANCE;
        this.clothingAllowance = DEFAULT_CLOTHING_ALLOWANCE;
    }
    
    // Implementing abstract methods - POLYMORPHISM
    @Override
    public double calculateGrossPay(int daysWorked, double overtimeHours) {
        double dailyRate = basicSalary / 22; // 22 working days per month
        double regularPay = dailyRate * daysWorked;
        double overtimePay = (dailyRate / 8) * overtimeHours * 1.25; // 125% rate for overtime
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
    
    // Private helper methods for calculations - ENCAPSULATION
    private double calculateSSS() {
        if (basicSalary <= 3250) return 135.0;
        if (basicSalary <= 25000) return basicSalary * 0.045;
        return 1125.0; // Maximum SSS contribution
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
        if (annualSalary <= 2000000) return (102500 + (annualSalary - 800000) * 0.25) / 12;
        if (annualSalary <= 8000000) return (402500 + (annualSalary - 2000000) * 0.30) / 12;
        return (2202500 + (annualSalary - 8000000) * 0.35) / 12;
    }
    
    // Method overriding - POLYMORPHISM
    @Override
    public String toString() {
        return "RegularEmployee{" +
                "employeeId=" + employeeId +
                ", name='" + getFullName() + '\'' +
                ", position='" + position + '\'' +
                ", basicSalary=" + basicSalary +
                ", totalAllowances=" + calculateAllowances() +
                '}';
    }
}