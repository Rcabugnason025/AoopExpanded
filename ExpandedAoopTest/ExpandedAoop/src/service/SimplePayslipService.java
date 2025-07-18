package service;

import dao.EmployeeDAO;
import model.Employee;
import service.PayrollCalculator.PayrollData;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Simple Payslip Service without JasperReports dependency
 * Generates text-based payslips that can be printed or saved
 */
public class SimplePayslipService {
    private static final Logger LOGGER = Logger.getLogger(SimplePayslipService.class.getName());
    
    private final EmployeeDAO employeeDAO;
    private final PayrollCalculator payrollCalculator;
    
    public SimplePayslipService() {
        this.employeeDAO = new EmployeeDAO();
        this.payrollCalculator = new PayrollCalculator();
    }
    
    /**
     * Generate payslip as formatted text
     */
    public String generatePayslipText(int employeeId, java.time.LocalDate periodStart, 
                                     java.time.LocalDate periodEnd) throws Exception {
        
        // Get employee information
        Employee employee = employeeDAO.getEmployeeWithPositionDetails(employeeId);
        if (employee == null) {
            throw new Exception("Employee not found with ID: " + employeeId);
        }
        
        // Calculate payroll data
        PayrollData payrollData = payrollCalculator.calculatePayroll(employeeId, periodStart, periodEnd);
        
        return formatPayslip(employee, payrollData);
    }
    
    /**
     * Generate and save payslip to file
     */
    public File generatePayslipToFile(int employeeId, java.time.LocalDate periodStart,
                                     java.time.LocalDate periodEnd, String outputDir) throws Exception {
        
        String payslipText = generatePayslipText(employeeId, periodStart, periodEnd);
        
        // Get employee for filename
        Employee employee = employeeDAO.getEmployeeById(employeeId);
        String employeeName = employee != null ? employee.getLastName() : "Unknown";
        
        // Create filename
        String filename = String.format("Payslip_%s_%d_%s.txt",
                employeeName.replaceAll("\\s+", ""),
                employeeId,
                periodStart.format(DateTimeFormatter.ofPattern("yyyy_MM")));
        
        // Ensure output directory exists
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            boolean created = outputDirectory.mkdirs();
            if (!created) {
                throw new Exception("Failed to create output directory: " + outputDir);
            }
        }
        
        // Write file
        File outputFile = new File(outputDirectory, filename);
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(payslipText);
        }
        
        LOGGER.info(String.format("Payslip saved to: %s", outputFile.getAbsolutePath()));
        return outputFile;
    }
    
    /**
     * Format payslip as professional text document
     */
    private String formatPayslip(Employee employee, PayrollData payrollData) {
        StringBuilder sb = new StringBuilder();
        
        // Generate payslip number
        String payslipNo = String.format("PS-%d-%s",
                employee.getEmployeeId(),
                payrollData.getPeriodEnd().format(DateTimeFormatter.ofPattern("yyyy-MM")));
        
        String periodStart = payrollData.getPeriodStart().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        String periodEnd = payrollData.getPeriodEnd().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        
        // Header
        sb.append("================================================================================\n");
        sb.append("                                   MotorPH\n");
        sb.append("           7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City\n");
        sb.append("           Phone: (028) 911-5071 / (028) 911-5072 / (028) 911-5073\n");
        sb.append("                         Email: corporate@motorph.com\n");
        sb.append("================================================================================\n");
        sb.append("                              EMPLOYEE PAYSLIP\n");
        sb.append("================================================================================\n\n");
        
        // Payslip Information
        sb.append("PAYSLIP NO: ").append(payslipNo).append("\n");
        sb.append("EMPLOYEE ID: ").append(employee.getEmployeeId()).append("\n");
        sb.append("EMPLOYEE NAME: ").append(employee.getFullName()).append("\n");
        sb.append("POSITION: ").append(employee.getPosition()).append("\n");
        sb.append("DEPARTMENT: ").append(employee.getDepartment()).append("\n");
        sb.append("PERIOD: ").append(periodStart).append(" to ").append(periodEnd).append("\n\n");
        
        // Earnings
        sb.append("EARNINGS:\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("%-30s %20s\n", "Monthly Rate", formatCurrency(payrollData.getMonthlyRate())));
        sb.append(String.format("%-30s %20s\n", "Daily Rate", formatCurrency(payrollData.getDailyRate())));
        sb.append(String.format("%-30s %20d\n", "Days Worked", payrollData.getDaysWorked()));
        sb.append(String.format("%-30s %20s\n", "Basic Pay", formatCurrency(payrollData.getBasicPay())));
        sb.append("                                                    ____________\n");
        sb.append(String.format("%-30s %20s\n", "GROSS INCOME", formatCurrency(payrollData.getBasicPay())));
        sb.append("\n");
        
        // Benefits
        sb.append("BENEFITS:\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("%-30s %20s\n", "Rice Subsidy", formatCurrency(payrollData.getRiceSubsidy())));
        sb.append(String.format("%-30s %20s\n", "Phone Allowance", formatCurrency(payrollData.getPhoneAllowance())));
        sb.append(String.format("%-30s %20s\n", "Clothing Allowance", formatCurrency(payrollData.getClothingAllowance())));
        sb.append("                                                    ____________\n");
        sb.append(String.format("%-30s %20s\n", "TOTAL BENEFITS", formatCurrency(payrollData.getTotalAllowances())));
        sb.append("\n");
        
        // Deductions
        sb.append("DEDUCTIONS:\n");
        sb.append("--------------------------------------------------------------------------------\n");
        sb.append(String.format("%-30s %20s\n", "Social Security System", formatCurrency(payrollData.getSss())));
        sb.append(String.format("%-30s %20s\n", "PhilHealth", formatCurrency(payrollData.getPhilhealth())));
        sb.append(String.format("%-30s %20s\n", "Pag-IBIG", formatCurrency(payrollData.getPagibig())));
        sb.append(String.format("%-30s %20s\n", "Withholding Tax", formatCurrency(payrollData.getTax())));
        sb.append("                                                    ____________\n");
        sb.append(String.format("%-30s %20s\n", "TOTAL DEDUCTIONS", formatCurrency(payrollData.getTotalDeductions())));
        sb.append("\n");
        
        // Summary
        sb.append("SUMMARY:\n");
        sb.append("================================================================================\n");
        sb.append(String.format("%-30s %20s\n", "Gross Income", formatCurrency(payrollData.getBasicPay())));
        sb.append(String.format("%-30s %20s\n", "Benefits", formatCurrency(payrollData.getTotalAllowances())));
        sb.append(String.format("%-30s %20s\n", "Deductions", formatCurrency(payrollData.getTotalDeductions())));
        sb.append("                                                    ____________\n");
        sb.append(String.format("%-30s %20s\n", "TAKE HOME PAY", formatCurrency(payrollData.getNetPay())));
        sb.append("================================================================================\n\n");
        
        // Footer
        sb.append("This payslip is computer-generated and does not require signature.\n");
        sb.append("Generated on: ").append(java.time.LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy 'at' HH:mm:ss"))).append("\n");
        sb.append("================================================================================\n");
        
        return sb.toString();
    }
    
    private String formatCurrency(double amount) {
        return String.format("₱%,.2f", amount);
    }
}