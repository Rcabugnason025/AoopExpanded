/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package reports;

import model.Employee;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

import javax.swing.*;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class JasperReportGenerator {
    private static final String REPORTS_PATH = "/reports/templates/";
    private static final String OUTPUT_PATH = System.getProperty("user.home") + "/MotorPH_Reports/";
    
    public JasperReportGenerator() {
        // Create output directory if it doesn't exist
        File outputDir = new File(OUTPUT_PATH);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }
    
    public File generatePayrollReport(List<PayrollReportData> payrollData, String periodName) 
            throws JRException {
        try {
            // Load MotorPH payroll template
            InputStream reportTemplate = getClass().getResourceAsStream(REPORTS_PATH + "motorph_payroll_template.jrxml");
            if (reportTemplate == null) {
                throw new JRException("MotorPH payroll template not found. Please ensure motorph_payroll_template.jrxml exists in " + REPORTS_PATH);
            }
            
            // Compile the report
            JasperReport jasperReport = JasperCompileManager.compileReport(reportTemplate);
            
            // Create data source
            JRDataSource dataSource = new JRBeanCollectionDataSource(payrollData);
            
            // Set report parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("COMPANY_NAME", "MotorPH Corporation");
            parameters.put("COMPANY_ADDRESS", "123 Automotive Street, Makati City, Philippines");
            parameters.put("REPORT_TITLE", "Employee Payroll Report");
            parameters.put("PERIOD_NAME", periodName);
            parameters.put("GENERATED_DATE", new Date());
            parameters.put("GENERATED_BY", "HR Department");
            
            // Calculate totals
            double totalGrossPay = payrollData.stream().mapToDouble(PayrollReportData::getGrossPay).sum();
            double totalDeductions = payrollData.stream().mapToDouble(PayrollReportData::getTotalDeductions).sum();
            double totalNetPay = payrollData.stream().mapToDouble(PayrollReportData::getNetPay).sum();
            
            parameters.put("TOTAL_GROSS_PAY", totalGrossPay);
            parameters.put("TOTAL_DEDUCTIONS", totalDeductions);
            parameters.put("TOTAL_NET_PAY", totalNetPay);
            parameters.put("EMPLOYEE_COUNT", payrollData.size());
            
            // Add company logo
            InputStream logoStream = getClass().getResourceAsStream("/images/motorph_logo.png");
            if (logoStream != null) {
                parameters.put("COMPANY_LOGO", logoStream);
            }
            
            // Fill the report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            
            // Generate output filename
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = String.format("MotorPH_PayrollReport_%s_%s.pdf", 
                                          periodName.replaceAll(" ", "_"), timestamp);
            
            File outputFile = new File(OUTPUT_PATH + fileName);
            
            // Export to PDF
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputFile.getAbsolutePath());
            
            return outputFile;
            
        } catch (Exception e) {
            throw new JRException("Failed to generate payroll report: " + e.getMessage(), e);
        }
    }
    
    public File generateEmployeeReport(Employee employee, List<PayrollReportData> employeePayrollHistory) 
            throws JRException {
        try {
            // Load MotorPH employee template
            InputStream reportTemplate = getClass().getResourceAsStream(REPORTS_PATH + "motorph_employee_template.jrxml");
            if (reportTemplate == null) {
                throw new JRException("MotorPH employee template not found");
            }
            
            JasperReport jasperReport = JasperCompileManager.compileReport(reportTemplate);
            JRDataSource dataSource = new JRBeanCollectionDataSource(employeePayrollHistory);
            
            // Set parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("COMPANY_NAME", "MotorPH Corporation");
            parameters.put("EMPLOYEE_ID", employee.getEmployeeId());
            parameters.put("EMPLOYEE_NAME", employee.getFullName());
            parameters.put("POSITION", employee.getPosition());
            parameters.put("DEPARTMENT", employee.getDepartment());
            parameters.put("BASIC_SALARY", employee.getBasicSalary());
            parameters.put("GENERATED_DATE", new Date());
            
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String fileName = String.format("MotorPH_EmployeeReport_%d_%s.pdf", 
                                          employee.getEmployeeId(), timestamp);
            
            File outputFile = new File(OUTPUT_PATH + fileName);
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputFile.getAbsolutePath());
            
            return outputFile;
            
        } catch (Exception e) {
            throw new JRException("Failed to generate employee report: " + e.getMessage(), e);
        }
    }
    
    public void previewReport(JasperPrint jasperPrint) {
        // Show report in viewer window
        JasperViewer viewer = new JasperViewer(jasperPrint, false);
        viewer.setTitle("MotorPH Report Preview");
        viewer.setVisible(true);
    }
}

// Data transfer object for reports
class PayrollReportData {
    private int employeeId;
    private String employeeName;
    private String position;
    private String department;
    private String employeeType;
    private int daysWorked;
    private double overtimeHours;
    private double grossPay;
    private double allowances;
    private double totalDeductions;
    private double netPay;
    private Date processedDate;
    
    // Constructor
    public PayrollReportData(Employee employee, int daysWorked, double overtimeHours) {
        this.employeeId = employee.getEmployeeId();
        this.employeeName = employee.getFullName();
        this.position = employee.getPosition();
        this.department = employee.getDepartment();
        this.employeeType = employee.getEmployeeType();
        this.daysWorked = daysWorked;
        this.overtimeHours = overtimeHours;
        this.grossPay = employee.calculateGrossPay(daysWorked, overtimeHours);
        this.allowances = employee.calculateAllowances();
        this.totalDeductions = employee.calculateDeductions();
        this.netPay = employee.calculateNetPay(daysWorked, overtimeHours);
        this.processedDate = new Date();
    }
    
    // Getters for JasperReports
    public int getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getPosition() { return position; }
    public String getDepartment() { return department; }
    public String getEmployeeType() { return employeeType; }
    public int getDaysWorked() { return daysWorked; }
    public double getOvertimeHours() { return overtimeHours; }
    public double getGrossPay() { return grossPay; }
    public double getAllowances() { return allowances; }
    public double getTotalDeductions() { return totalDeductions; }
    public double getNetPay() { return netPay; }
    public Date getProcessedDate() { return processedDate; }
}
