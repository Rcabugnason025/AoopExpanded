/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import com.motorph.model.PayslipData;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import model.PayslipData;

@Service
public class PayslipService {
    
    private static final String TEMPLATE_PATH = "/reports/templates/motorph_payslip_template.jrxml";
    
    /**
     * Generate PDF payslip for a single employee
     */
    public byte[] generatePayslipPDF(PayslipData payslipData) throws JRException {
        try {
            // Load the template
            InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_PATH);
            if (templateStream == null) {
                throw new JRException("Template not found: " + TEMPLATE_PATH);
            }
            
            // Compile the template
            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
            
            // Create data source
            List<PayslipData> dataList = Arrays.asList(payslipData);
            JRDataSource dataSource = new JRBeanCollectionDataSource(dataList);
            
            // Set parameters (if any)
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("COMPANY_LOGO", getClass().getResourceAsStream("/images/company-logo.png"));
            
            // Fill the report
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            
            // Export to PDF
            return JasperExportManager.exportReportToPdf(jasperPrint);
            
        } catch (Exception e) {
            throw new JRException("Error generating payslip PDF", e);
        }
    }
    
    /**
     * Generate payslip for multiple employees
     */
    public byte[] generateBatchPayslipPDF(List<PayslipData> payslipDataList) throws JRException {
        try {
            InputStream templateStream = getClass().getResourceAsStream(TEMPLATE_PATH);
            JasperReport jasperReport = JasperCompileManager.compileReport(templateStream);
            
            JRDataSource dataSource = new JRBeanCollectionDataSource(payslipDataList);
            
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("COMPANY_LOGO", getClass().getResourceAsStream("/images/company-logo.png"));
            
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);
            
            return JasperExportManager.exportReportToPdf(jasperPrint);
            
        } catch (Exception e) {
            throw new JRException("Error generating batch payslip PDF", e);
        }
    }
    
    /**
     * Create sample payslip data for testing
     */
    public PayslipData createSamplePayslipData() {
        PayslipData payslip = new PayslipData();
        
        payslip.setPayslipNo("PS-2024-001");
        payslip.setEmployeeId(1001);
        payslip.setEmployeeName("Juan Dela Cruz");
        payslip.setPeriodStartDate(java.time.LocalDate.of(2024, 1, 1));
        payslip.setPeriodEndDate(java.time.LocalDate.of(2024, 1, 15));
        payslip.setPosition("Software Developer");
        payslip.setDepartment("IT");
        
        // Earnings
        payslip.setMonthlyRate(50000.0);
        payslip.setDailyRate(2380.95);
        payslip.setDaysWorked(22);
        payslip.setOvertime(5000.0);
        payslip.setGrossIncome(55000.0);
        
        // Benefits
        payslip.setRiceSubsidy(1500.0);
        payslip.setPhoneAllowance(1000.0);
        payslip.setClothingAllowance(500.0);
        payslip.setTotalBenefits(3000.0);
        
        // Deductions
        payslip.setSocialSecuritySystem(2200.0);
        payslip.setPhilHealth(1375.0);
        payslip.setPagIbig(200.0);
        payslip.setWithholdingTax(5500.0);
        payslip.setTotalDeductions(9275.0);
        
        // Take home pay
        payslip.setTakeHomePay(48725.0);
        
        return payslip;
    }
}
