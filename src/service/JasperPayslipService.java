package service;

import dao.EmployeeDAO;
import dao.PayrollDAO;
import model.Employee;
import service.PayrollCalculator.PayrollCalculationException;
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
 * Enhanced JasperReports Service using MotorPH template and stored procedures
 */
public class JasperPayslipService {
    private static final Logger LOGGER = Logger.getLogger(JasperPayslipService.class.getName());

    // Template paths - Using MotorPH template as required
    private static final String PAYSLIP_TEMPLATE = "/motorph_payslip.jrxml";
    private static final String COMPILED_PAYSLIP = "/reports/motorph_payslip.jasper";
    private static final String COMPANY_LOGO = "/images/motorph_logo.png";

    // Services
    private final EmployeeDAO employeeDAO;
    private final PayrollDAO payrollDAO;
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
        this.payrollDAO = new PayrollDAO();
        this.payrollCalculator = new PayrollCalculator();
        validateEnvironment();
        LOGGER.info("JasperPayslipService initialized successfully with MotorPH template");
    }

    /**
     * Generate payslip report using MotorPH template and stored procedures
     */
    public byte[] generatePayslipReport(int employeeId, java.time.LocalDate periodStart,
                                        java.time.LocalDate periodEnd, ExportFormat format)
            throws JasperReportException {

        try {
            LOGGER.info(String.format("Generating %s payslip for employee %d from %s to %s using MotorPH template",
                    format.name(), employeeId, periodStart, periodEnd));

            // Get employee information using view
            Employee employee = employeeDAO.getEmployeeWithPositionDetails(employeeId);
            if (employee == null) {
                throw new JasperReportException("Employee not found with ID: " + employeeId);
            }

            // Get payslip data using stored procedure
            Map<String, Object> payslipData = payrollDAO.generatePayslipData(employeeId, periodStart, periodEnd);

            // Create JasperReports data source using MotorPH format
            PayslipData motorPHPayslipData = createMotorPHPayslipData(employee, payslipData, periodStart, periodEnd);
            List<PayslipData> dataList = Arrays.asList(motorPHPayslipData);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

            // Prepare parameters for MotorPH template
            Map<String, Object> parameters = createMotorPHReportParameters();

            // Compile and fill report using MotorPH template
            JasperReport jasperReport = getCompiledMotorPHReport();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            // Export based on format
            return exportReport(jasperPrint, format);

        } catch (PayrollCalculationException e) {
            LOGGER.log(Level.SEVERE, "Payroll calculation failed", e);
            throw new JasperReportException("Failed to calculate payroll: " + e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating payslip report", e);
            throw new JasperReportException("Failed to generate payslip report: " + e.getMessage(), e);
        }
    }

    /**
     * Generate payslip and save to file using MotorPH template
     */
    public File generatePayslipToFile(int employeeId, java.time.LocalDate periodStart,
                                      java.time.LocalDate periodEnd, ExportFormat format, String outputDir)
            throws JasperReportException {

        try {
            byte[] reportData = generatePayslipReport(employeeId, periodStart, periodEnd, format);

            // Get employee for filename
            Employee employee = employeeDAO.getEmployeeById(employeeId);
            String employeeName = employee != null ? employee.getLastName() : "Unknown";

            // Create filename with MotorPH prefix
            String filename = String.format("MotorPH_Payslip_%s_%d_%s.%s",
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

            LOGGER.info(String.format("MotorPH payslip saved to: %s", outputFile.getAbsolutePath()));
            return outputFile;

        } catch (IOException e) {
            throw new JasperReportException("Failed to save payslip to file: " + e.getMessage(), e);
        }
    }

    /**
     * Create payslip data object matching the MotorPH JRXML template exactly
     */
    private PayslipData createMotorPHPayslipData(Employee employee, Map<String, Object> storedProcData, 
                                                 java.time.LocalDate periodStart, java.time.LocalDate periodEnd) {
        PayslipData data = new PayslipData();

        // Employee information
        data.setEmployeeId(employee.getEmployeeId());
        data.setEmployeeName(employee.getFullName());
        data.setPosition(employee.getPosition() != null ? employee.getPosition() : "N/A");
        data.setDepartment(getDepartmentFromPosition(employee.getPosition()));

        // MotorPH payslip identification
        data.setPayslipNo(String.format("MP-%d-%s",
                employee.getEmployeeId(),
                periodEnd.format(DateTimeFormatter.ofPattern("yyyy-MM"))));

        // Period information
        data.setPeriodStart(Date.valueOf(periodStart));
        data.setPeriodEnd(Date.valueOf(periodEnd));

        // Salary information from stored procedure
        data.setMonthlyRate(BigDecimal.valueOf((Double) storedProcData.get("basic_salary")));
        data.setDailyRate(BigDecimal.valueOf((Double) storedProcData.get("basic_salary") / 22));
        data.setDaysWorked(22); // Default for stored procedure

        // Benefits/Allowances
        data.setRiceSubsidy(BigDecimal.valueOf(employee.getRiceSubsidy()));
        data.setPhoneAllowance(BigDecimal.valueOf(employee.getPhoneAllowance()));
        data.setClothingAllowance(BigDecimal.valueOf(employee.getClothingAllowance()));
        data.setTotalBenefits(BigDecimal.valueOf(employee.getTotalAllowances()));

        // Deductions from stored procedure
        data.setSss(BigDecimal.valueOf((Double) storedProcData.get("sss_contribution")));
        data.setPhilhealth(BigDecimal.valueOf((Double) storedProcData.get("philhealth_contribution")));
        data.setPagibig(BigDecimal.valueOf((Double) storedProcData.get("pagibig_contribution")));
        data.setTax(BigDecimal.valueOf((Double) storedProcData.get("withholding_tax")));
        data.setTotalDeductions(BigDecimal.valueOf((Double) storedProcData.get("total_deductions")));

        // Pay amounts from stored procedure
        data.setGrossPay(BigDecimal.valueOf((Double) storedProcData.get("gross_income")));
        data.setNetPay(BigDecimal.valueOf((Double) storedProcData.get("net_income")));

        return data;
    }

    /**
     * Get department from position with MotorPH specific mappings
     */
    private String getDepartmentFromPosition(String position) {
        if (position == null) return "General";

        String pos = position.toLowerCase();
        if (pos.contains("ceo") || pos.contains("chief executive")) return "Executive";
        if (pos.contains("coo") || pos.contains("chief operating")) return "Operations";
        if (pos.contains("cfo") || pos.contains("chief finance")) return "Finance";
        if (pos.contains("cmo") || pos.contains("chief marketing")) return "Marketing";
        if (pos.contains("hr") || pos.contains("human resource")) return "Human Resources";
        if (pos.contains("accounting") || pos.contains("payroll")) return "Accounting";
        if (pos.contains("it") || pos.contains("operations")) return "IT Operations";
        if (pos.contains("sales") || pos.contains("marketing")) return "Sales & Marketing";
        if (pos.contains("supply") || pos.contains("logistics")) return "Supply Chain";
        if (pos.contains("customer") || pos.contains("service")) return "Customer Service";

        return "General";
    }

    /**
     * Create report parameters for MotorPH template
     */
    private Map<String, Object> createMotorPHReportParameters() {
        Map<String, Object> parameters = new HashMap<>();

        // MotorPH company information
        parameters.put("COMPANY_NAME", "MotorPH");
        parameters.put("COMPANY_ADDRESS", "7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City");
        parameters.put("COMPANY_PHONE", "Phone: (028) 911-5071 / (028) 911-5072 / (028) 911-5073");
        parameters.put("COMPANY_EMAIL", "Email: corporate@motorph.com");

        // Handle MotorPH logo loading
        InputStream logoStream = getClass().getResourceAsStream(COMPANY_LOGO);
        if (logoStream != null) {
            parameters.put("COMPANY_LOGO", logoStream);
        } else {
            LOGGER.warning("MotorPH logo not found, report will generate without logo");
            parameters.put("COMPANY_LOGO", null);
        }

        parameters.put("REPORT_TITLE", "EMPLOYEE PAYSLIP");
        parameters.put("GENERATED_BY", "MotorPH Payroll System");
        parameters.put("GENERATION_DATE", new java.util.Date());

        return parameters;
    }

    /**
     * Validate environment and MotorPH template resources
     */
    private void validateEnvironment() throws JasperReportException {
        InputStream templateStream = getClass().getResourceAsStream(PAYSLIP_TEMPLATE);
        if (templateStream == null) {
            throw new JasperReportException(
                    "MotorPH template not found: " + PAYSLIP_TEMPLATE +
                            "\nPlease ensure motorph_payslip.jrxml is in src/main/resources/"
            );
        }
        try {
            templateStream.close();
        } catch (IOException e) {
            LOGGER.warning("Failed to close template stream: " + e.getMessage());
        }

        LOGGER.info("MotorPH JasperReports environment validation completed");
    }

    /**
     * Get compiled MotorPH JasperReport
     */
    private JasperReport getCompiledMotorPHReport() throws JRException {
        // Try to load pre-compiled MotorPH report first
        InputStream compiledStream = getClass().getResourceAsStream(COMPILED_PAYSLIP);

        if (compiledStream != null) {
            try {
                LOGGER.info("Loading pre-compiled MotorPH report: " + COMPILED_PAYSLIP);
                return (JasperReport) JRLoader.loadObject(compiledStream);
            } catch (JRException e) {
                LOGGER.warning("Failed to load pre-compiled MotorPH report, will compile from JRXML: " + e.getMessage());
            } finally {
                try {
                    compiledStream.close();
                } catch (IOException e) {
                    LOGGER.warning("Failed to close compiled report stream: " + e.getMessage());
                }
            }
        }

        // Compile from MotorPH JRXML
        InputStream jrxmlStream = getClass().getResourceAsStream(PAYSLIP_TEMPLATE);
        if (jrxmlStream == null) {
            throw new JRException("MotorPH report template not found: " + PAYSLIP_TEMPLATE);
        }

        try {
            LOGGER.info("Compiling MotorPH JRXML template: " + PAYSLIP_TEMPLATE);
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
                pdfConfig.setMetadataTitle("MotorPH Employee Payslip");
                pdfConfig.setMetadataSubject("MotorPH Payroll Document");
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
     * Generate payslip using Employee polymorphism and MotorPH template
     */
    public byte[] generatePayslipReport(Employee employee, model.Payroll payroll, ExportFormat format)
            throws JasperReportException {

        try {
            LOGGER.info(String.format("Generating %s payslip for %s (%s) using MotorPH template",
                    format.name(), employee.getFullName(), employee.getEmployeeType()));

            // Create MotorPH payslip data using polymorphic employee methods
            PayslipData motorPHData = createMotorPHPayslipDataFromPayroll(employee, payroll);
            List<PayslipData> dataList = Arrays.asList(motorPHData);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(dataList);

            // Prepare MotorPH parameters
            Map<String, Object> parameters = createMotorPHReportParameters();
            parameters.put("EMPLOYEE_NAME", employee.getFullName());
            parameters.put("EMPLOYEE_ID", employee.getEmployeeId());
            parameters.put("EMPLOYEE_TYPE", employee.getEmployeeType());

            // Compile and fill MotorPH report
            JasperReport jasperReport = getCompiledMotorPHReport();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            return exportReport(jasperPrint, format);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error generating MotorPH payslip report", e);
            throw new JasperReportException("Failed to generate MotorPH payslip report: " + e.getMessage(), e);
        }
    }

    /**
     * Create MotorPH payslip data from Payroll object using polymorphic employee methods
     */
    private PayslipData createMotorPHPayslipDataFromPayroll(Employee employee, model.Payroll payroll) {
        PayslipData data = new PayslipData();

        // Employee information using polymorphic methods
        data.setEmployeeId(employee.getEmployeeId());
        data.setEmployeeName(employee.getFullName());
        data.setPosition(employee.getPosition());
        data.setDepartment(getDepartmentFromPosition(employee.getPosition()));

        // MotorPH payslip identification
        data.setPayslipNo(String.format("MP-%d-%s",
                employee.getEmployeeId(),
                payroll.getPeriodEnd().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM"))));

        // Period information
        data.setPeriodStart(payroll.getPeriodStart());
        data.setPeriodEnd(payroll.getPeriodEnd());

        // Salary information
        data.setMonthlyRate(BigDecimal.valueOf(payroll.getMonthlyRate()));
        data.setDailyRate(BigDecimal.valueOf(payroll.getMonthlyRate() / 22));
        data.setDaysWorked(payroll.getDaysWorked());

        // Benefits/Allowances using polymorphic employee methods
        data.setRiceSubsidy(BigDecimal.valueOf(payroll.getRiceSubsidy()));
        data.setPhoneAllowance(BigDecimal.valueOf(payroll.getPhoneAllowance()));
        data.setClothingAllowance(BigDecimal.valueOf(payroll.getClothingAllowance()));
        data.setTotalBenefits(BigDecimal.valueOf(employee.calculateAllowances()));

        // Deductions
        data.setSss(BigDecimal.valueOf(payroll.getSss()));
        data.setPhilhealth(BigDecimal.valueOf(payroll.getPhilhealth()));
        data.setPagibig(BigDecimal.valueOf(payroll.getPagibig()));
        data.setTax(BigDecimal.valueOf(payroll.getTax()));
        data.setTotalDeductions(BigDecimal.valueOf(payroll.getTotalDeductions()));

        // Pay amounts
        data.setGrossPay(BigDecimal.valueOf(payroll.getGrossPay()));
        data.setNetPay(BigDecimal.valueOf(payroll.getNetPay()));

        return data;
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
     * Data Transfer Object for MotorPH JasperReports - matches JRXML template exactly
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

        // Getters and setters for MotorPH template
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