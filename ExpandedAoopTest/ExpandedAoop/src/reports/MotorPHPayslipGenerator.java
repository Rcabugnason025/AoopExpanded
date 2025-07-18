/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package reports;

import model.PayslipData;
import model.Employee;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class MotorPHPayslipGenerator {
    private static final String PAYSLIP_TEMPLATE = "/reports/templates/motorph_payslip_template.jrxml";
    private static final String OUTPUT_PATH = System.getProperty("user.home") + "/MotorPH_Payslips/";
    
    public MotorPHPayslipGenerator() {
        // Create output directory
        File outputDir = new File(OUTPUT_PATH);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
    }
    
    public File generatePayslip(Employee employee, int daysWorked, double overtimeHours, 
                               Date periodStart, Date periodEnd) throws JRException {
        try {
            // Create payslip data
            PayslipData payslipData = new PayslipData(employee, daysWorked, overtimeHours);
            payslipData.setPeriodStartDate(periodStart);
            payslipData.setPeriodEndDate(periodEnd);
            
            // Load template
            InputStream templateStream = getClass().getResourceAsStream(PAYSLIP_TEMPLATE);
            if (templateStream == null) {
                throw new JRException("Payslip template not found: " + PAYSLIP_TEMPLATE);
            }
            
            // Compile report
            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
            
            // Create data source (single payslip)
            List<PayslipData> payslipList = Arrays.asList(payslipData);
            JRDataSource dataSource = new JRBeanCollectionDataSource(payslipList);
            
            // Set parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "EMPLOYEE PAYSLIP");
            parameters.put("GENERATED_DATE", new Date());
            
            // Add logo if available
            InputStream logoStream = getClass().getResourceAsStream("/images/motorph_logo.png");
            if (logoStream != null) {
                parameters.put("COMPANY_LOGO", logoStream);
            }
            
            // Fill report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            
            // Generate filename
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String filename = String.format("MotorPH_Payslip_%d_%s.pdf", employee.getEmployeeId(), timestamp);
            File outputFile = new File(OUTPUT_PATH + filename);
            
            // Export to PDF
            JasperExportManager.exportReportToPdfFile(jasperPrint, outputFile.getAbsolutePath());
            
            return outputFile;
            
        } catch (Exception e) {
            throw new JRException("Failed to generate payslip: " + e.getMessage(), e);
        }
    }
    
    public void previewPayslip(Employee employee, int daysWorked, double overtimeHours,
                              Date periodStart, Date periodEnd) throws JRException {
        try {
            // Create payslip data
            PayslipData payslipData = new PayslipData(employee, daysWorked, overtimeHours);
            payslipData.setPeriodStartDate(periodStart);
            payslipData.setPeriodEndDate(periodEnd);
            
            // Load and compile template
            InputStream templateStream = getClass().getResourceAsStream(PAYSLIP_TEMPLATE);
            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
            
            // Create data source
            List<PayslipData> payslipList = Arrays.asList(payslipData);
            JRDataSource dataSource = new JRBeanCollectionDataSource(payslipList);
            
            // Set parameters
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("REPORT_TITLE", "EMPLOYEE PAYSLIP");
            parameters.put("GENERATED_DATE", new Date());
            
            // Fill and preview
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            
            // Show preview window
            JasperViewer viewer = new JasperViewer(jasperPrint, false);
            viewer.setTitle("MotorPH Payslip Preview - " + employee.getFullName());
            viewer.setVisible(true);
            
        } catch (Exception e) {
            throw new JRException("Failed to preview payslip: " + e.getMessage(), e);
        }
    }
    
    public List<File> generateBulkPayslips(List<Employee> employees, int daysWorked, 
                                          double overtimeHours, Date periodStart, Date periodEnd) throws JRException {
        List<File> generatedFiles = new ArrayList<>();
        
        for (Employee employee : employees) {
            try {
                File payslipFile = generatePayslip(employee, daysWorked, overtimeHours, periodStart, periodEnd);
                generatedFiles.add(payslipFile);
            } catch (Exception e) {
                System.err.println("Failed to generate payslip for employee " + employee.getEmployeeId() + ": " + e.getMessage());
            }
        }
        
        return generatedFiles;
    }
}
