package dao;

import util.DBConnection;
import model.Payroll;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * DAO for payroll operations using stored procedures and views
 */
public class PayrollDAO {
    private static final Logger LOGGER = Logger.getLogger(PayrollDAO.class.getName());

    /**
     * Generate payslip data using stored procedure
     */
    public Map<String, Object> generatePayslipData(int employeeId, LocalDate startDate, LocalDate endDate) {
        Map<String, Object> payslipData = new HashMap<>();
        String sql = "CALL sp_generate_payslip_data(?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    payslipData.put("employee_id", rs.getInt("employee_id"));
                    payslipData.put("full_name", rs.getString("full_name"));
                    payslipData.put("position_title", rs.getString("position_title"));
                    payslipData.put("pay_period_start", rs.getDate("pay_period_start"));
                    payslipData.put("pay_period_end", rs.getDate("pay_period_end"));
                    payslipData.put("basic_salary", rs.getDouble("basic_salary"));
                    payslipData.put("gross_income", rs.getDouble("gross_income"));
                    payslipData.put("sss_contribution", rs.getDouble("sss_contribution"));
                    payslipData.put("philhealth_contribution", rs.getDouble("philhealth_contribution"));
                    payslipData.put("pagibig_contribution", rs.getDouble("pagibig_contribution"));
                    payslipData.put("withholding_tax", rs.getDouble("withholding_tax"));
                    payslipData.put("total_deductions", rs.getDouble("total_deductions"));
                    payslipData.put("net_income", rs.getDouble("net_income"));
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error generating payslip data for employee: " + employeeId, ex);
            throw new RuntimeException("Failed to generate payslip data", ex);
        }

        return payslipData;
    }

    /**
     * Save payroll record
     */
    public boolean savePayroll(Payroll payroll) {
        if (payroll == null) {
            throw new IllegalArgumentException("Payroll cannot be null");
        }

        String sql = "INSERT INTO payroll_records (employee_id, period_start, period_end, " +
                "monthly_rate, days_worked, overtime_hours, gross_pay, total_deductions, net_pay, " +
                "rice_subsidy, phone_allowance, clothing_allowance, sss, philhealth, pagibig, tax) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, payroll.getEmployeeId());
            stmt.setDate(2, payroll.getPeriodStart());
            stmt.setDate(3, payroll.getPeriodEnd());
            stmt.setDouble(4, payroll.getMonthlyRate());
            stmt.setInt(5, payroll.getDaysWorked());
            stmt.setDouble(6, payroll.getOvertimeHours());
            stmt.setDouble(7, payroll.getGrossPay());
            stmt.setDouble(8, payroll.getTotalDeductions());
            stmt.setDouble(9, payroll.getNetPay());
            stmt.setDouble(10, payroll.getRiceSubsidy());
            stmt.setDouble(11, payroll.getPhoneAllowance());
            stmt.setDouble(12, payroll.getClothingAllowance());
            stmt.setDouble(13, payroll.getSss());
            stmt.setDouble(14, payroll.getPhilhealth());
            stmt.setDouble(15, payroll.getPagibig());
            stmt.setDouble(16, payroll.getTax());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        payroll.setPayrollId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error saving payroll record", ex);
            throw new RuntimeException("Failed to save payroll record", ex);
        }

        return false;
    }

    /**
     * Get payroll records for employee
     */
    public List<Payroll> getPayrollByEmployee(int employeeId) {
        List<Payroll> payrolls = new ArrayList<>();
        String sql = "SELECT * FROM payroll_records WHERE employee_id = ? ORDER BY period_end DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Payroll payroll = mapResultSetToPayroll(rs);
                    payrolls.add(payroll);
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error retrieving payroll for employee: " + employeeId, ex);
            throw new RuntimeException("Failed to retrieve payroll records", ex);
        }

        return payrolls;
    }

    private Payroll mapResultSetToPayroll(ResultSet rs) throws SQLException {
        Payroll payroll = new Payroll();
        payroll.setPayrollId(rs.getInt("id"));
        payroll.setEmployeeId(rs.getInt("employee_id"));
        payroll.setPeriodStart(rs.getDate("period_start"));
        payroll.setPeriodEnd(rs.getDate("period_end"));
        payroll.setMonthlyRate(rs.getDouble("monthly_rate"));
        payroll.setDaysWorked(rs.getInt("days_worked"));
        payroll.setOvertimeHours(rs.getDouble("overtime_hours"));
        payroll.setGrossPay(rs.getDouble("gross_pay"));
        payroll.setTotalDeductions(rs.getDouble("total_deductions"));
        payroll.setNetPay(rs.getDouble("net_pay"));
        payroll.setRiceSubsidy(rs.getDouble("rice_subsidy"));
        payroll.setPhoneAllowance(rs.getDouble("phone_allowance"));
        payroll.setClothingAllowance(rs.getDouble("clothing_allowance"));
        payroll.setSss(rs.getDouble("sss"));
        payroll.setPhilhealth(rs.getDouble("philhealth"));
        payroll.setPagibig(rs.getDouble("pagibig"));
        payroll.setTax(rs.getDouble("tax"));
        return payroll;
    }
}