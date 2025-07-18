package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import dao.LeaveRequestDAO;
import dao.PositionDAO;
import dao.PayrollDAO;
import model.Attendance;
import model.Employee;
import model.LeaveRequest;
import model.Position;
import model.GovernmentContribution;
import model.Payroll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enhanced PayrollCalculator using stored procedures and views
 */
public class PayrollCalculator {
    private static final Logger LOGGER = Logger.getLogger(PayrollCalculator.class.getName());

    // Constants for payroll calculations
    private static final int STANDARD_WORKING_DAYS_PER_MONTH = 22;
    private static final int STANDARD_WORKING_HOURS_PER_DAY = 8;
    private static final double OVERTIME_RATE_MULTIPLIER = 1.25;
    private static final LocalTime STANDARD_LOGIN_TIME = LocalTime.of(8, 0);
    private static final LocalTime LATE_THRESHOLD_TIME = LocalTime.of(8, 15);
    private static final LocalTime STANDARD_LOGOUT_TIME = LocalTime.of(17, 0);

    // DAO instances
    private final EmployeeDAO employeeDAO;
    private final AttendanceDAO attendanceDAO;
    private final LeaveRequestDAO leaveDAO;
    private final PositionDAO positionDAO;
    private final PayrollDAO payrollDAO;

    public PayrollCalculator() {
        this.employeeDAO = new EmployeeDAO();
        this.attendanceDAO = new AttendanceDAO();
        this.leaveDAO = new LeaveRequestDAO();
        this.positionDAO = new PositionDAO();
        this.payrollDAO = new PayrollDAO();
    }

    /**
     * Calculate payroll using stored procedure for complex calculations
     */
    public Payroll calculatePayroll(int employeeId, LocalDate periodStart, LocalDate periodEnd)
            throws PayrollCalculationException {

        try {
            validateInputs(employeeId, periodStart, periodEnd);

            // Get employee with position details using view
            Employee employee = employeeDAO.getEmployeeWithPositionDetails(employeeId);
            if (employee == null) {
                throw new PayrollCalculationException("Employee not found with ID: " + employeeId);
            }

            // Use stored procedure for payslip data generation
            Map<String, Object> payslipData = payrollDAO.generatePayslipData(employeeId, periodStart, periodEnd);
            
            // Create payroll object from stored procedure results
            Payroll payroll = new Payroll();
            payroll.setEmployeeId(employeeId);
            payroll.setPeriodStart(java.sql.Date.valueOf(periodStart));
            payroll.setPeriodEnd(java.sql.Date.valueOf(periodEnd));
            
            // Set values from stored procedure
            payroll.setMonthlyRate((Double) payslipData.get("basic_salary"));
            payroll.setGrossPay((Double) payslipData.get("gross_income"));
            payroll.setSss((Double) payslipData.get("sss_contribution"));
            payroll.setPhilhealth((Double) payslipData.get("philhealth_contribution"));
            payroll.setPagibig((Double) payslipData.get("pagibig_contribution"));
            payroll.setTax((Double) payslipData.get("withholding_tax"));
            payroll.setTotalDeductions((Double) payslipData.get("total_deductions"));
            payroll.setNetPay((Double) payslipData.get("net_income"));

            // Calculate attendance-based data
            calculateAttendanceBasedEarnings(payroll, employeeId, periodStart, periodEnd);

            // Set allowances from employee (which comes from position via view)
            payroll.setRiceSubsidy(employee.getRiceSubsidy());
            payroll.setPhoneAllowance(employee.getPhoneAllowance());
            payroll.setClothingAllowance(employee.getClothingAllowance());

            // Calculate time-based deductions
            calculateTimeBasedDeductions(payroll, employeeId, periodStart, periodEnd);

            LOGGER.info(String.format("Payroll calculated for employee %d: Net Pay = %.2f",
                    employeeId, payroll.getNetPay()));

            return payroll;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to calculate payroll for employee " + employeeId, e);
            throw new PayrollCalculationException("Failed to calculate payroll: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate attendance-based earnings
     */
    private void calculateAttendanceBasedEarnings(Payroll payroll, int employeeId,
                                                  LocalDate periodStart, LocalDate periodEnd) {

        List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeIdBetweenDates(
                employeeId, periodStart, periodEnd);

        int validDays = 0;
        double totalHours = 0.0;
        double overtimeHours = 0.0;

        for (Attendance attendance : attendanceList) {
            if (attendance.getLogIn() != null) {
                validDays++;
                double workHours = attendance.getWorkHours();
                totalHours += workHours;
                
                // Calculate overtime (hours worked beyond 8 hours)
                if (workHours > 8) {
                    overtimeHours += (workHours - 8);
                }
            }
        }

        payroll.setDaysWorked(validDays);
        payroll.setOvertimeHours(overtimeHours);
        
        // Calculate basic pay and overtime pay
        double dailyRate = payroll.getMonthlyRate() / STANDARD_WORKING_DAYS_PER_MONTH;
        double basicPay = validDays * dailyRate;
        double overtimePay = (dailyRate / 8) * overtimeHours * OVERTIME_RATE_MULTIPLIER;
        
        payroll.setGrossEarnings(basicPay);
        payroll.setOvertimePay(overtimePay);

        LOGGER.info(String.format("Employee %d worked %d days, %.2f hours, %.2f overtime",
                employeeId, validDays, totalHours, overtimeHours));
    }

    /**
     * Calculate time-based deductions (late, undertime, unpaid leave)
     */
    private void calculateTimeBasedDeductions(Payroll payroll, int employeeId,
                                              LocalDate periodStart, LocalDate periodEnd) {

        List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeIdBetweenDates(
                employeeId, periodStart, periodEnd);

        double dailyRate = payroll.getMonthlyRate() / STANDARD_WORKING_DAYS_PER_MONTH;
        double lateDeduction = calculateLateDeduction(attendanceList, dailyRate);
        double undertimeDeduction = calculateUndertimeDeduction(attendanceList, dailyRate);
        double unpaidLeaveDeduction = calculateUnpaidLeaveDeduction(employeeId, periodStart, periodEnd, dailyRate);

        payroll.setLateDeduction(lateDeduction);
        payroll.setUndertimeDeduction(undertimeDeduction);
        payroll.setUnpaidLeaveDeduction(unpaidLeaveDeduction);
    }

    /**
     * Calculate late deduction
     */
    private double calculateLateDeduction(List<Attendance> attendanceList, double dailyRate) {
        double totalLateDeduction = 0.0;
        double hourlyRate = dailyRate / STANDARD_WORKING_HOURS_PER_DAY;

        for (Attendance attendance : attendanceList) {
            if (attendance.getLogIn() != null) {
                LocalTime loginTime = attendance.getLogIn().toLocalTime();
                if (loginTime.isAfter(LATE_THRESHOLD_TIME)) {
                    long minutesLate = ChronoUnit.MINUTES.between(STANDARD_LOGIN_TIME, loginTime);
                    double hoursLate = minutesLate / 60.0;
                    totalLateDeduction += hoursLate * hourlyRate;
                }
            }
        }

        return totalLateDeduction;
    }

    /**
     * Calculate undertime deduction
     */
    private double calculateUndertimeDeduction(List<Attendance> attendanceList, double dailyRate) {
        double totalUndertimeDeduction = 0.0;
        double hourlyRate = dailyRate / STANDARD_WORKING_HOURS_PER_DAY;

        for (Attendance attendance : attendanceList) {
            if (attendance.getLogOut() != null) {
                LocalTime logoutTime = attendance.getLogOut().toLocalTime();
                if (logoutTime.isBefore(STANDARD_LOGOUT_TIME)) {
                    long minutesShort = ChronoUnit.MINUTES.between(logoutTime, STANDARD_LOGOUT_TIME);
                    double hoursShort = minutesShort / 60.0;
                    totalUndertimeDeduction += hoursShort * hourlyRate;
                }
            }
        }

        return totalUndertimeDeduction;
    }

    /**
     * Calculate unpaid leave deduction
     */
    private double calculateUnpaidLeaveDeduction(int employeeId, LocalDate periodStart, LocalDate periodEnd, double dailyRate) {
        try {
            List<LeaveRequest> approvedLeaves = leaveDAO.getApprovedLeavesByEmployeeIdAndDateRange(
                    employeeId, periodStart, periodEnd);

            int unpaidLeaveDays = 0;
            for (LeaveRequest leave : approvedLeaves) {
                if ("Unpaid".equalsIgnoreCase(leave.getLeaveType())) {
                    unpaidLeaveDays += leave.getLeaveDays();
                }
            }

            return unpaidLeaveDays * dailyRate;
        } catch (Exception e) {
            LOGGER.warning("Error calculating unpaid leave deduction: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Validation methods
     */
    private void validateInputs(int employeeId, LocalDate periodStart, LocalDate periodEnd)
            throws PayrollCalculationException {
        if (employeeId <= 0) {
            throw new PayrollCalculationException("Invalid employee ID: " + employeeId);
        }
        if (periodStart == null || periodEnd == null) {
            throw new PayrollCalculationException("Period dates cannot be null");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new PayrollCalculationException("Period end cannot be before period start");
        }
    }

    /**
     * Custom exception for payroll calculation errors
     */
    public static class PayrollCalculationException extends Exception {
        public PayrollCalculationException(String message) {
            super(message);
        }

        public PayrollCalculationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}