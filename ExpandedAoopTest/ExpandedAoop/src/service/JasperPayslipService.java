package service;

import dao.EmployeeDAO;
import model.Employee;
import service.PayrollCalculator.PayrollData;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;

import java.io.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.sql.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Updated JasperReports Service for MotorPH Payroll System
 * Now properly integrated with the updated PayrollCalculator and database schema
 */
public class JasperPayslipService {
    private static final Logger LOGGER = Logger.getLogger(JasperPayslipService.class.getName());

    // Template paths
    private static final String PAYSLIP_TEMPLATE = "/motorph_payslip.jrxml";
    private static final String COMPILED_PAYSLIP = "/reports/motorph_payslip.jasper";
    private static final String COMPANY_LOGO = "/images/motorph_logo.png";

    // Services
    private final EmployeeDAO employeeDAO;
    private final PayrollCalculator payrollCalculator;

    // Export formats
    public enum ExportFormat {
        PDF("pdf", "application/pdf"),
        EXCEL("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        private final String extension;
        private final String mimeType;

        ExportFormat(String extension, String mimeType) {
            this.extension = extension;
            this.mimeType = mimeType;
        }

        public String getExtension() { return extension; }
        public String getMimeType() { return mimeType; }
    }

    /**
     * Constructor with dependency injection
     */
    public JasperPayslipService() throws JasperReportException {
        this.employeeDAO = new EmployeeDAO();
        this.payrollCalculator = new PayrollCalculator();
        validateEnvironment();
        LOGGER.info("JasperPayslipService initialized successfully");
    }

    /**
     * Generate payslip report using PayrollCalculator
     */
    public byte[] generatePayslipReport(int employeeId, java.time.LocalDate periodStart,
                                        java.time.LocalDate periodEnd, ExportFormat format)
            throws JasperReportException {

        try {
            LOGGER.info(String.format("Generating %s payslip for employee %d from %s to %s",
                    format.name(), employeeId, periodStart, periodEnd));

            // Get employee information
            Employee employee = employeeDAO.getEmployeeWithPositionDetails(employeeId);
            if (employee == null) {
                throw new JasperReportException("Employee not found with ID: " + employeeId);
            }

            // Calculate payroll data
            PayrollData payrollData = payrollCalculator.calculatePayroll(employeeId, periodStart, periodEnd);

            // Create JasperReports data source
            PayslipData payslipData = createPayslipData(employee, payrollData);
            List<PayslipData> dataList = Arrays.asList(payslipData);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

            // Prepare parameters
            Map<String, Object> parameters = createReportParameters(employee, payrollData);

            // Compile and fill report
            JasperReport jasperReport = getCompiledReport();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Export based on format
            return exportReport(jasperPrint, format);

        } catch (PayrollCalculator.PayrollCalculationException e) {
            LOGGER.log(Level.SEVERE, "Payroll calculation failed", e);
            throw new JasperReportException("Failed to calculate payroll: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating payslip report", e);
            throw new JasperReportException("Failed to generate payslip report: " + e.getMessage(), e);
        }
    }

    /**
     * Generate payslip and save to file
     */
    public File generatePayslipToFile(int employeeId, java.time.LocalDate periodStart,
                                      java.time.LocalDate periodEnd, ExportFormat format, String outputDir)
            throws JasperReportException {

        try {
            byte[] reportData = generatePayslipReport(employeeId, periodStart, periodEnd, format);

            // Get employee for filename
            Employee employee = employeeDAO.getEmployeeById(employeeId);
            String employeeName = employee != null ? employee.getLastName() : "Unknown";

            // Create filename
            String filename = String.format("Payslip_%s_%d_%s.%s",
                    employeeName.replaceAll("\\s+", ""),
                    employeeId,
                    periodStart.format(DateTimeFormatter.ofPattern("yyyy_MM")),
                    format.getExtension());

            // Ensure output directory exists
            File outputDirectory = new File(outputDir);
            if (!outputDirectory.exists()) {
                boolean created = outputDirectory.mkdirs();
                if (!created) {
                    throw new JasperReportException("Failed to create output directory: " + outputDir);
                }
            }

            // Write file
            File outputFile = new File(outputDirectory, filename);
            try (FileOutputStream fos = new FileOutputStream(outputFile)) {
                fos.write(reportData);
            }

            LOGGER.info(String.format("Payslip saved to: %s", outputFile.getAbsolutePath()));
            return outputFile;

        } catch (IOException e) {
            throw new JasperReportException("Failed to save payslip to file: " + e.getMessage(), e);
        }
    }

    /**
     * Create payslip data object matching the JRXML template
     */
    private PayslipData createPayslipData(Employee employee, PayrollData payrollData) {
        PayslipData data = new PayslipData();

        // Employee information
        data.setEmployeeId(employee.getId());
        data.setEmployeeName(employee.getFullName());
        data.setPosition(employee.getPosition() != null ? employee.getPosition() : "N/A");
        data.setDepartment(getDepartmentFromPosition(employee.getPosition()));

        // Payslip identification
        data.setPayslipNo(String.format("PS-%d-%s",
                employee.getId(),
                payrollData.getPeriodEnd().format(DateTimeFormatter.ofPattern("yyyy-MM"))));

        // Period information
        data.setPeriodStart(Date.valueOf(payrollData.getPeriodStart()));
        data.setPeriodEnd(Date.valueOf(payrollData.getPeriodEnd()));

        // Salary information
        data.setMonthlyRate(BigDecimal.valueOf(payrollData.getMonthlyRate()));
        data.setDailyRate(BigDecimal.valueOf(payrollData.getDailyRate()));
        data.setDaysWorked(payrollData.getDaysWorked());

        // Benefits/Allowances
        data.setRiceSubsidy(BigDecimal.valueOf(payrollData.getRiceSubsidy()));
        data.setPhoneAllowance(BigDecimal.valueOf(payrollData.getPhoneAllowance()));
        data.setClothingAllowance(BigDecimal.valueOf(payrollData.getClothingAllowance()));
        data.setTotalBenefits(BigDecimal.valueOf(payrollData.getTotalAllowances()));

        // Deductions
        data.setSss(BigDecimal.valueOf(payrollData.getSss()));
        data.setPhilhealth(BigDecimal.valueOf(payrollData.getPhilhealth()));
        data.setPagibig(BigDecimal.valueOf(payrollData.getPagibig()));
        data.setTax(BigDecimal.valueOf(payrollData.getTax()));
        data.setTotalDeductions(BigDecimal.valueOf(payrollData.getTotalDeductions()));

        // Pay amounts
        data.setGrossPay(BigDecimal.valueOf(payrollData.getGrossPay()));
        data.setNetPay(BigDecimal.valueOf(payrollData.getNetPay()));

        return data;
    }

    /**
     * Get department from position with fallback
     */
    private String getDepartmentFromPosition(String position) {
        if (position == null) return "General";

        String pos = position.toLowerCase();
        if (pos.contains("hr") || pos.contains("human resource")) return "Human Resources";
        if (pos.contains("accounting") || pos.contains("payroll")) return "Accounting";
        if (pos.contains("marketing") || pos.contains("sales")) return "Marketing";
        if (pos.contains("it") || pos.contains("operations")) return "IT Operations";
        if (pos.contains("ceo") || pos.contains("executive")) return "Executive";
        if (pos.contains("chief")) return "Executive";

        return "General";
    }

    /**
     * Create report parameters
     */
    private Map<String, Object> createReportParameters(Employee employee, PayrollData payrollData) {
        Map<String, Object> parameters = new HashMap<>();

        // Handle logo loading
        InputStream logoStream = getClass().getResourceAsStream(COMPANY_LOGO);
        if (logoStream != null) {
            parameters.put("COMPANY_LOGO", logoStream);
        } else {
            LOGGER.warning("Company logo not found, report will generate without logo");
            parameters.put("COMPANY_LOGO", null);
        }

        parameters.put("REPORT_TITLE", "EMPLOYEE PAYSLIP");
        parameters.put("GENERATED_BY", "MotorPH Payroll System");
        parameters.put("EMPLOYEE_NAME", employee.getFullName());
        parameters.put("EMPLOYEE_ID", employee.getId());
        parameters.put("GENERATION_DATE", new java.util.Date());

        return parameters;
    }

    /**
     * Validate environment and resources
     */
    private void validateEnvironment() throws JasperReportException {
        InputStream templateStream = getClass().getResourceAsStream(PAYSLIP_TEMPLATE);
        if (templateStream == null) {
            throw new JasperReportException(
                    "Template not found: " + PAYSLIP_TEMPLATE +
                            "\nPlease ensure motorph_payslip.jrxml is in src/main/resources/"
            );
        }
        try {
            templateStream.close();
        } catch (IOException e) {
            LOGGER.warning("Failed to close template stream: " + e.getMessage());
        }

        LOGGER.info("JasperReports environment validation completed");
    }

    /**
     * Get compiled JasperReport
     */
    private JasperReport getCompiledReport() throws JRException {
        // Try to load pre-compiled report first
        InputStream compiledStream = getClass().getResourceAsStream(COMPILED_PAYSLIP);

        if (compiledStream != null) {
            try {
                LOGGER.info("Loading pre-compiled report: " + COMPILED_PAYSLIP);
                return (JasperReport) JRLoader.loadObject(compiledStream);
            } catch (JRException e) {
                LOGGER.warning("Failed to load pre-compiled report, will compile from JRXML: " + e.getMessage());
            } finally {
                try {
                    compiledStream.close();
                } catch (IOException e) {
                    LOGGER.warning("Failed to close compiled report stream: " + e.getMessage());
                }
            }
        }

        // Compile from JRXML
        InputStream jrxmlStream = getClass().getResourceAsStream(PAYSLIP_TEMPLATE);
        if (jrxmlStream == null) {
            throw new JRException("Report template not found: " + PAYSLIP_TEMPLATE);
        }

        try {
            LOGGER.info("Compiling JRXML template: " + PAYSLIP_TEMPLATE);
            return JasperCompileManager.compileReport(jrxmlStream);
        } finally {
            try {
                jrxmlStream.close();
            } catch (IOException e) {
                LOGGER.warning("Failed to close JRXML stream: " + e.getMessage());
            }
        }
    }

    /**
     * Export report based on format
     */
    private byte[] exportReport(JasperPrint jasperPrint, ExportFormat format) throws JRException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        switch (format) {
            case PDF:
                JRPdfExporter pdfExporter = new JRPdfExporter();
                pdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

                SimplePdfExporterConfiguration pdfConfig = new SimplePdfExporterConfiguration();
                pdfConfig.setMetadataAuthor("MotorPH Payroll System");
                pdfConfig.setMetadataTitle("Employee Payslip");
                pdfConfig.setMetadataSubject("Payroll Document");
                pdfExporter.setConfiguration(pdfConfig);

                pdfExporter.exportReport();
                break;

            case EXCEL:
                JRXlsxExporter xlsxExporter = new JRXlsxExporter();
                xlsxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));

                SimpleXlsxReportConfiguration xlsxConfig = new SimpleXlsxReportConfiguration();
                xlsxConfig.setOnePagePerSheet(true);
                xlsxConfig.setDetectCellType(true);
                xlsxConfig.setCollapseRowSpan(false);
                xlsxExporter.setConfiguration(xlsxConfig);

                xlsxExporter.exportReport();
                break;

            default:
                throw new JRException("Unsupported export format: " + format);
        }

        return outputStream.toByteArray();
    }

    /**
     * Check if JasperReports is available
     */
    public static boolean isJasperReportsAvailable() {
        try {
            Class.forName("net.sf.jasperreports.engine.JasperReport");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * Data Transfer Object for JasperReports - matches JRXML template exactly
     */
    public static class PayslipData {
        private Integer employeeId;
        private String employeeName;
        private String position;
        private String department;
        private String payslipNo;
        private Date periodStart;
        private Date periodEnd;
        private BigDecimal monthlyRate;
        private BigDecimal dailyRate;
        private Integer daysWorked;
        private BigDecimal riceSubsidy;
        private BigDecimal phoneAllowance;
        private BigDecimal clothingAllowance;
        private BigDecimal totalBenefits;
        private BigDecimal sss;
        private BigDecimal philhealth;
        private BigDecimal pagibig;
        private BigDecimal tax;
        private BigDecimal totalDeductions;
        private BigDecimal grossPay;
        private BigDecimal netPay;

        // Getters and setters
        public Integer getEmployeeId() { return employeeId; }
        public void setEmployeeId(Integer employeeId) { this.employeeId = employeeId; }

        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }

        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }

        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }

        public String getPayslipNo() { return payslipNo; }
        public void setPayslipNo(String payslipNo) { this.payslipNo = payslipNo; }

        public Date getPeriodStart() { return periodStart; }
        public void setPeriodStart(Date periodStart) { this.periodStart = periodStart; }

        public Date getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(Date periodEnd) { this.periodEnd = periodEnd; }

        public BigDecimal getMonthlyRate() { return monthlyRate; }
        public void setMonthlyRate(BigDecimal monthlyRate) { this.monthlyRate = monthlyRate; }

        public BigDecimal getDailyRate() { return dailyRate; }
        public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }

        public Integer getDaysWorked() { return daysWorked; }
        public void setDaysWorked(Integer daysWorked) { this.daysWorked = daysWorked; }

        public BigDecimal getRiceSubsidy() { return riceSubsidy; }
        public void setRiceSubsidy(BigDecimal riceSubsidy) { this.riceSubsidy = riceSubsidy; }

        public BigDecimal getPhoneAllowance() { return phoneAllowance; }
        public void setPhoneAllowance(BigDecimal phoneAllowance) { this.phoneAllowance = phoneAllowance; }

        public BigDecimal getClothingAllowance() { return clothingAllowance; }
        public void setClothingAllowance(BigDecimal clothingAllowance) { this.clothingAllowance = clothingAllowance; }

        public BigDecimal getTotalBenefits() { return totalBenefits; }
        public void setTotalBenefits(BigDecimal totalBenefits) { this.totalBenefits = totalBenefits; }

        public BigDecimal getSss() { return sss; }
        public void setSss(BigDecimal sss) { this.sss = sss; }

        public BigDecimal getPhilhealth() { return philhealth; }
        public void setPhilhealth(BigDecimal philhealth) { this.philhealth = philhealth; }

        public BigDecimal getPagibig() { return pagibig; }
        public void setPagibig(BigDecimal pagibig) { this.pagibig = pagibig; }

        public BigDecimal getTax() { return tax; }
        public void setTax(BigDecimal tax) { this.tax = tax; }

        public BigDecimal getTotalDeductions() { return totalDeductions; }
        public void setTotalDeductions(BigDecimal totalDeductions) { this.totalDeductions = totalDeductions; }

        public BigDecimal getGrossPay() { return grossPay; }
        public void setGrossPay(BigDecimal grossPay) { this.grossPay = grossPay; }

        public BigDecimal getNetPay() { return netPay; }
        public void setNetPay(BigDecimal netPay) { this.netPay = netPay; }
    }

    /**
     * Custom exception for JasperReports operations
     */
    public static class JasperReportException extends Exception {
        public JasperReportException(String message) {
            super(message);
        }

        public JasperReportException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}