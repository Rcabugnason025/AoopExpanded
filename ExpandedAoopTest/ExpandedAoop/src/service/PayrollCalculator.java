package service;

import dao.AttendanceDAO;
import dao.EmployeeDAO;
import dao.LeaveRequestDAO;
import dao.PositionDAO;
import model.Attendance;
import model.Employee;
import model.LeaveRequest;
import model.Position;
import model.GovernmentContribution;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Updated PayrollCalculator that matches the actual database schema and models
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

    public PayrollCalculator() {
        this.employeeDAO = new EmployeeDAO();
        this.attendanceDAO = new AttendanceDAO();
        this.leaveDAO = new LeaveRequestDAO();
        this.positionDAO = new PositionDAO();
    }

    /**
     * Calculate payroll for an employee within a specific period
     * Returns a PayrollData object with all calculated values
     */
    public PayrollData calculatePayroll(int employeeId, LocalDate periodStart, LocalDate periodEnd)
            throws PayrollCalculationException {

        try {
            validateInputs(employeeId, periodStart, periodEnd);

            // Get employee with position details
            Employee employee = employeeDAO.getEmployeeWithPositionDetails(employeeId);
            if (employee == null) {
                throw new PayrollCalculationException("Employee not found with ID: " + employeeId);
            }

            // Get position details
            Position position = positionDAO.getPositionById(employee.getPositionId());
            if (position == null) {
                throw new PayrollCalculationException("Position not found for employee: " + employeeId);
            }

            // Create payroll data object
            PayrollData payrollData = new PayrollData();
            payrollData.setEmployeeId(employeeId);
            payrollData.setPeriodStart(periodStart);
            payrollData.setPeriodEnd(periodEnd);
            payrollData.setMonthlyRate(position.getMonthlySalary());
            payrollData.setDailyRate(position.getDailyRate());

            // Calculate attendance-based earnings
            calculateAttendanceBasedEarnings(payrollData, employeeId, periodStart, periodEnd);

            // Set allowances from position
            payrollData.setRiceSubsidy(position.getRiceSubsidy());
            payrollData.setPhoneAllowance(position.getPhoneAllowance());
            payrollData.setClothingAllowance(position.getClothingAllowance());

            // Calculate time-based deductions
            calculateTimeBasedDeductions(payrollData, employeeId, periodStart, periodEnd);

            // Calculate government contributions
            calculateGovernmentContributions(payrollData, position.getMonthlySalary());

            // Calculate totals
            calculateTotals(payrollData);

            LOGGER.info(String.format("Payroll calculated for employee %d: Net Pay = %.2f",
                    employeeId, payrollData.getNetPay()));

            return payrollData;

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to calculate payroll for employee " + employeeId, e);
            throw new PayrollCalculationException("Failed to calculate payroll: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate attendance-based earnings
     */
    private void calculateAttendanceBasedEarnings(PayrollData payrollData, int employeeId,
                                                  LocalDate periodStart, LocalDate periodEnd) {

        List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeIdBetweenDates(
                employeeId, periodStart, periodEnd);

        int validDays = 0;
        double totalHours = 0.0;

        for (Attendance attendance : attendanceList) {
            if (attendance.getLogIn() != null) {
                validDays++;
                totalHours += attendance.getWorkHours();
            }
        }

        payrollData.setDaysWorked(validDays);
        payrollData.setBasicPay(validDays * payrollData.getDailyRate());
        payrollData.setTotalHours(totalHours);

        LOGGER.info(String.format("Employee %d worked %d days, %.2f hours",
                employeeId, validDays, totalHours));
    }

    /**
     * Calculate time-based deductions (late, undertime, unpaid leave)
     */
    private void calculateTimeBasedDeductions(PayrollData payrollData, int employeeId,
                                              LocalDate periodStart, LocalDate periodEnd) {

        List<Attendance> attendanceList = attendanceDAO.getAttendanceByEmployeeIdBetweenDates(
                employeeId, periodStart, periodEnd);

        double lateDeduction = calculateLateDeduction(attendanceList, payrollData.getDailyRate());
        double undertimeDeduction = calculateUndertimeDeduction(attendanceList, payrollData.getDailyRate());
        double unpaidLeaveDeduction = calculateUnpaidLeaveDeduction(employeeId, periodStart, periodEnd, payrollData.getDailyRate());

        payrollData.setLateDeduction(lateDeduction);
        payrollData.setUndertimeDeduction(undertimeDeduction);
        payrollData.setUnpaidLeaveDeduction(unpaidLeaveDeduction);
    }

    /**
     * Calculate government contributions using the model classes
     */
    private void calculateGovernmentContributions(PayrollData payrollData, double monthlySalary) {
        GovernmentContribution sss = GovernmentContribution.createSSS(payrollData.getEmployeeId(), monthlySalary);
        GovernmentContribution philhealth = GovernmentContribution.createPhilHealth(payrollData.getEmployeeId(), monthlySalary);
        GovernmentContribution pagibig = GovernmentContribution.createPagIBIG(payrollData.getEmployeeId(), monthlySalary);

        payrollData.setSss(sss.getAmount());
        payrollData.setPhilhealth(philhealth.getAmount());
        payrollData.setPagibig(pagibig.getAmount());

        // Simple tax calculation (should be more complex in real implementation)
        double tax = calculateIncomeTax(monthlySalary);
        payrollData.setTax(tax);
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
     * Simple income tax calculation
     */
    private double calculateIncomeTax(double monthlySalary) {
        double annualSalary = monthlySalary * 12;
        double annualTax = 0.0;

        if (annualSalary <= 250000) {
            annualTax = 0.0;
        } else if (annualSalary <= 400000) {
            annualTax = (annualSalary - 250000) * 0.15;
        } else if (annualSalary <= 800000) {
            annualTax = 22500 + (annualSalary - 400000) * 0.20;
        } else if (annualSalary <= 2000000) {
            annualTax = 102500 + (annualSalary - 800000) * 0.25;
        } else if (annualSalary <= 8000000) {
            annualTax = 402500 + (annualSalary - 2000000) * 0.30;
        } else {
            annualTax = 2202500 + (annualSalary - 8000000) * 0.35;
        }

        return annualTax / 12;
    }

    /**
     * Calculate final totals
     */
    private void calculateTotals(PayrollData payrollData) {
        // Calculate total allowances
        double totalAllowances = payrollData.getRiceSubsidy() +
                payrollData.getPhoneAllowance() +
                payrollData.getClothingAllowance();
        payrollData.setTotalAllowances(totalAllowances);

        // Calculate gross pay
        double grossPay = payrollData.getBasicPay() + totalAllowances;
        payrollData.setGrossPay(grossPay);

        // Calculate total deductions
        double totalDeductions = payrollData.getLateDeduction() +
                payrollData.getUndertimeDeduction() +
                payrollData.getUnpaidLeaveDeduction() +
                payrollData.getSss() +
                payrollData.getPhilhealth() +
                payrollData.getPagibig() +
                payrollData.getTax();
        payrollData.setTotalDeductions(totalDeductions);

        // Calculate net pay
        double netPay = grossPay - totalDeductions;
        payrollData.setNetPay(netPay);
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
     * PayrollData class to hold calculated payroll information
     */
    public static class PayrollData {
        private int employeeId;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private double monthlyRate;
        private double dailyRate;
        private int daysWorked;
        private double totalHours;
        private double basicPay;
        private double riceSubsidy;
        private double phoneAllowance;
        private double clothingAllowance;
        private double totalAllowances;
        private double grossPay;
        private double lateDeduction;
        private double undertimeDeduction;
        private double unpaidLeaveDeduction;
        private double sss;
        private double philhealth;
        private double pagibig;
        private double tax;
        private double totalDeductions;
        private double netPay;

        // Getters and setters
        public int getEmployeeId() { return employeeId; }
        public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

        public LocalDate getPeriodStart() { return periodStart; }
        public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

        public LocalDate getPeriodEnd() { return periodEnd; }
        public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

        public double getMonthlyRate() { return monthlyRate; }
        public void setMonthlyRate(double monthlyRate) { this.monthlyRate = monthlyRate; }

        public double getDailyRate() { return dailyRate; }
        public void setDailyRate(double dailyRate) { this.dailyRate = dailyRate; }

        public int getDaysWorked() { return daysWorked; }
        public void setDaysWorked(int daysWorked) { this.daysWorked = daysWorked; }

        public double getTotalHours() { return totalHours; }
        public void setTotalHours(double totalHours) { this.totalHours = totalHours; }

        public double getBasicPay() { return basicPay; }
        public void setBasicPay(double basicPay) { this.basicPay = basicPay; }

        public double getRiceSubsidy() { return riceSubsidy; }
        public void setRiceSubsidy(double riceSubsidy) { this.riceSubsidy = riceSubsidy; }

        public double getPhoneAllowance() { return phoneAllowance; }
        public void setPhoneAllowance(double phoneAllowance) { this.phoneAllowance = phoneAllowance; }

        public double getClothingAllowance() { return clothingAllowance; }
        public void setClothingAllowance(double clothingAllowance) { this.clothingAllowance = clothingAllowance; }

        public double getTotalAllowances() { return totalAllowances; }
        public void setTotalAllowances(double totalAllowances) { this.totalAllowances = totalAllowances; }

        public double getGrossPay() { return grossPay; }
        public void setGrossPay(double grossPay) { this.grossPay = grossPay; }

        public double getLateDeduction() { return lateDeduction; }
        public void setLateDeduction(double lateDeduction) { this.lateDeduction = lateDeduction; }

        public double getUndertimeDeduction() { return undertimeDeduction; }
        public void setUndertimeDeduction(double undertimeDeduction) { this.undertimeDeduction = undertimeDeduction; }

        public double getUnpaidLeaveDeduction() { return unpaidLeaveDeduction; }
        public void setUnpaidLeaveDeduction(double unpaidLeaveDeduction) { this.unpaidLeaveDeduction = unpaidLeaveDeduction; }

        public double getSss() { return sss; }
        public void setSss(double sss) { this.sss = sss; }

        public double getPhilhealth() { return philhealth; }
        public void setPhilhealth(double philhealth) { this.philhealth = philhealth; }

        public double getPagibig() { return pagibig; }
        public void setPagibig(double pagibig) { this.pagibig = pagibig; }

        public double getTax() { return tax; }
        public void setTax(double tax) { this.tax = tax; }

        public double getTotalDeductions() { return totalDeductions; }
        public void setTotalDeductions(double totalDeductions) { this.totalDeductions = totalDeductions; }

        public double getNetPay() { return netPay; }
        public void setNetPay(double netPay) { this.netPay = netPay; }
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