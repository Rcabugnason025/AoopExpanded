/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.text.NumberFormat;
import java.util.Locale;

public class PayslipData {
    private String payslipNo;
    private Integer employeeId;
    private String employeeName;
    private LocalDate periodStartDate;
    private LocalDate periodEndDate;
    private String position;
    private String department;
    private Double monthlyRate;
    private Double dailyRate;
    private Integer daysWorked;
    private Double overtime;
    private Double grossIncome;
    private Double riceSubsidy;
    private Double phoneAllowance;
    private Double clothingAllowance;
    private Double totalBenefits;
    private Double socialSecuritySystem;
    private Double philHealth;
    private Double pagIbig;
    private Double withholdingTax;
    private Double totalDeductions;
    private Double takeHomePay;
    
    // Company information
    private String companyName = "MotorPH";
    private String companyAddress = "123 Main Street, Manila, Philippines";
    private String companyPhone = "Tel: (02) 8123-4567";
    private String companyEmail = "info@motorph.com";
    
    private static final NumberFormat CURRENCY_FORMAT = 
        NumberFormat.getCurrencyInstance(new Locale("en", "PH"));
    private static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("MMM dd, yyyy");
    
    // Constructors
    public PayslipData() {}
    
    // Getters and setters for all fields
    public String getPayslipNo() { return payslipNo; }
    public void setPayslipNo(String payslipNo) { this.payslipNo = payslipNo; }
    
    public Integer getEmployeeId() { return employeeId; }
    public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }
    
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    
    public LocalDate getPeriodStartDate() { return periodStartDate; }
    public void setPeriodStartDate(LocalDate periodStartDate) { this.periodStartDate = periodStartDate; }
    
    public LocalDate getPeriodEndDate() { return periodEndDate; }
    public void setPeriodEndDate(LocalDate periodEndDate) { this.periodEndDate = periodEndDate; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public Double getMonthlyRate() { return monthlyRate; }
    public void setMonthlyRate(Double monthlyRate) { this.monthlyRate = monthlyRate; }
    
    public Double getDailyRate() { return dailyRate; }
    public void setDailyRate(Double dailyRate) { this.dailyRate = dailyRate; }
    
    public Integer getDaysWorked() { return daysWorked; }
    public void setDaysWorked(Integer daysWorked) { this.daysWorked = daysWorked; }
    
    public Double getOvertime() { return overtime; }
    public void setOvertime(Double overtime) { this.overtime = overtime; }
    
    public Double getGrossIncome() { return grossIncome; }
    public void setGrossIncome(Double grossIncome) { this.grossIncome = grossIncome; }
    
    public Double getRiceSubsidy() { return riceSubsidy; }
    public void setRiceSubsidy(Double riceSubsidy) { this.riceSubsidy = riceSubsidy; }
    
    public Double getPhoneAllowance() { return phoneAllowance; }
    public void setPhoneAllowance(Double phoneAllowance) { this.phoneAllowance = phoneAllowance; }
    
    public Double getClothingAllowance() { return clothingAllowance; }
    public void setClothingAllowance(Double clothingAllowance) { this.clothingAllowance = clothingAllowance; }
    
    public Double getTotalBenefits() { return totalBenefits; }
    public void setTotalBenefits(Double totalBenefits) { this.totalBenefits = totalBenefits; }
    
    public Double getSocialSecuritySystem() { return socialSecuritySystem; }
    public void setSocialSecuritySystem(Double socialSecuritySystem) { this.socialSecuritySystem = socialSecuritySystem; }
    
    public Double getPhilHealth() { return philHealth; }
    public void setPhilHealth(Double philHealth) { this.philHealth = philHealth; }
    
    public Double getPagIbig() { return pagIbig; }
    public void setPagIbig(Double pagIbig) { this.pagIbig = pagIbig; }
    
    public Double getWithholdingTax() { return withholdingTax; }
    public void setWithholdingTax(Double withholdingTax) { this.withholdingTax = withholdingTax; }
    
    public Double getTotalDeductions() { return totalDeductions; }
    public void setTotalDeductions(Double totalDeductions) { this.totalDeductions = totalDeductions; }
    
    public Double getTakeHomePay() { return takeHomePay; }
    public void setTakeHomePay(Double takeHomePay) { this.takeHomePay = takeHomePay; }
    
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    
    public String getCompanyAddress() { return companyAddress; }
    public void setCompanyAddress(String companyAddress) { this.companyAddress = companyAddress; }
    
    public String getCompanyPhone() { return companyPhone; }
    public void setCompanyPhone(String companyPhone) { this.companyPhone = companyPhone; }
    
    public String getCompanyEmail() { return companyEmail; }
    public void setCompanyEmail(String companyEmail) { this.companyEmail = companyEmail; }
    
    // ==================== FORMATTED GETTERS FOR JASPER ====================
    // These methods return formatted strings that JasperReports will use
    
    public String getFormattedPeriodStartDate() {
        return periodStartDate != null ? periodStartDate.format(DATE_FORMAT) : "";
    }
    
    public String getFormattedPeriodEndDate() {
        return periodEndDate != null ? periodEndDate.format(DATE_FORMAT) : "";
    }
    
    public String getFormattedMonthlyRate() {
        return monthlyRate != null ? CURRENCY_FORMAT.format(monthlyRate) : "₱0.00";
    }
    
    public String getFormattedDailyRate() {
        return dailyRate != null ? CURRENCY_FORMAT.format(dailyRate) : "₱0.00";
    }
    
    public String getFormattedGrossIncome() {
        return grossIncome != null ? CURRENCY_FORMAT.format(grossIncome) : "₱0.00";
    }
    
    public String getFormattedRiceSubsidy() {
        return riceSubsidy != null ? CURRENCY_FORMAT.format(riceSubsidy) : "₱0.00";
    }
    
    public String getFormattedPhoneAllowance() {
        return phoneAllowance != null ? CURRENCY_FORMAT.format(phoneAllowance) : "₱0.00";
    }
    
    public String getFormattedClothingAllowance() {
        return clothingAllowance != null ? CURRENCY_FORMAT.format(clothingAllowance) : "₱0.00";
    }
    
    public String getFormattedTotalBenefits() {
        return totalBenefits != null ? CURRENCY_FORMAT.format(totalBenefits) : "₱0.00";
    }
    
    public String getFormattedSocialSecuritySystem() {
        return socialSecuritySystem != null ? CURRENCY_FORMAT.format(socialSecuritySystem) : "₱0.00";
    }
    
    public String getFormattedPhilHealth() {
        return philHealth != null ? CURRENCY_FORMAT.format(philHealth) : "₱0.00";
    }
    
    public String getFormattedPagIbig() {
        return pagIbig != null ? CURRENCY_FORMAT.format(pagIbig) : "₱0.00";
    }
    
    public String getFormattedWithholdingTax() {
        return withholdingTax != null ? CURRENCY_FORMAT.format(withholdingTax) : "₱0.00";
    }
    
    public String getFormattedTotalDeductions() {
        return totalDeductions != null ? CURRENCY_FORMAT.format(totalDeductions) : "₱0.00";
    }
    
    public String getFormattedTakeHomePay() {
        return takeHomePay != null ? CURRENCY_FORMAT.format(takeHomePay) : "₱0.00";
    }
}

