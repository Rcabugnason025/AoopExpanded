package dao;

import util.DBConnection;
import model.Employee;
import model.RegularEmployee;
import model.ContractualEmployee;
import model.EmployeeFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeDAO {
    private static final Logger LOGGER = Logger.getLogger(EmployeeDAO.class.getName());

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM v_employee_details ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Employee e = mapResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching all employees", ex);
            throw new RuntimeException("Failed to fetch employees", ex);
        }

        return employees;
    }

    public Employee getEmployeeById(int employeeId) {
        String query = "SELECT * FROM v_employee_details WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToEmployee(rs);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching employee with ID: " + employeeId, ex);
            throw new RuntimeException("Failed to fetch employee", ex);
        }

        return null;
    }

    public Employee getEmployeeWithPositionDetails(int employeeId) {
        return getEmployeeById(employeeId); // Using view already includes position details
    }

    public boolean insertEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }

        // Validate required fields
        if (employee.getEmployeeId() <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        if (employee.getFirstName() == null || employee.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (employee.getLastName() == null || employee.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }

        // Check for duplicate employee ID
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT 1 FROM employees WHERE employee_id = ?")) {

            checkStmt.setInt(1, employee.getEmployeeId());
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    throw new IllegalArgumentException("Employee ID " + employee.getEmployeeId() + " already exists");
                }
            }
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error checking for duplicate employee ID: " + employee.getEmployeeId(), ex);
            throw new RuntimeException("Error checking for duplicate employee ID: " + ex.getMessage(), ex);
        }

        // Use stored procedure for safe insertion
        String sql = "CALL sp_add_new_employee(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employee.getEmployeeId());
            stmt.setString(2, employee.getLastName() != null ? employee.getLastName().trim() : null);
            stmt.setString(3, employee.getFirstName() != null ? employee.getFirstName().trim() : null);
            stmt.setDate(4, employee.getBirthday() != null ? java.sql.Date.valueOf(employee.getBirthday()) : null);
            stmt.setString(5, employee.getAddress() != null ? employee.getAddress().trim() : null);
            stmt.setString(6, employee.getPhoneNumber() != null ? employee.getPhoneNumber().trim() : null);
            stmt.setString(7, employee.getSssNumber() != null ? employee.getSssNumber().trim() : null);
            stmt.setString(8, employee.getPhilhealthNumber() != null ? employee.getPhilhealthNumber().trim() : null);
            stmt.setString(9, employee.getTinNumber() != null ? employee.getTinNumber().trim() : null);
            stmt.setString(10, employee.getPagibigNumber() != null ? employee.getPagibigNumber().trim() : null);
            stmt.setString(11, employee.getStatus() != null ? employee.getStatus().trim() : "Regular");
            stmt.setInt(12, getPositionId(employee.getPosition()));
            stmt.setObject(13, getSupervisorId(employee.getImmediateSupervisor()), java.sql.Types.INTEGER);
            stmt.setString(14, "password1234"); // Default password

            stmt.execute();

            LOGGER.info("Successfully inserted employee: " + employee.getEmployeeId() + " - " + employee.getFullName());
            return true;

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error inserting employee: " + employee.getEmployeeId(), ex);
            throw new RuntimeException("Failed to insert employee: " + ex.getMessage(), ex);
        }
    }

    public boolean updateEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee cannot be null");
        }
        if (employee.getEmployeeId() <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        String sql = "UPDATE employees SET last_name=?, first_name=?, birthday=?, address=?, " +
                "phone_number=?, sss_number=?, philhealth_number=?, tin_number=?, " +
                "pagibig_number=?, status=?, position_id=?, supervisor_id=? WHERE employee_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, employee.getLastName() != null ? employee.getLastName().trim() : null);
            stmt.setString(2, employee.getFirstName() != null ? employee.getFirstName().trim() : null);
            stmt.setDate(3, employee.getBirthday() != null ? java.sql.Date.valueOf(employee.getBirthday()) : null);
            stmt.setString(4, employee.getAddress() != null ? employee.getAddress().trim() : null);
            stmt.setString(5, employee.getPhoneNumber() != null ? employee.getPhoneNumber().trim() : null);
            stmt.setString(6, employee.getSssNumber() != null ? employee.getSssNumber().trim() : null);
            stmt.setString(7, employee.getPhilhealthNumber() != null ? employee.getPhilhealthNumber().trim() : null);
            stmt.setString(8, employee.getTinNumber() != null ? employee.getTinNumber().trim() : null);
            stmt.setString(9, employee.getPagibigNumber() != null ? employee.getPagibigNumber().trim() : null);
            stmt.setString(10, employee.getStatus() != null ? employee.getStatus().trim() : "Regular");
            stmt.setInt(11, getPositionId(employee.getPosition()));
            stmt.setObject(12, getSupervisorId(employee.getImmediateSupervisor()), java.sql.Types.INTEGER);
            stmt.setInt(13, employee.getEmployeeId());

            int result = stmt.executeUpdate();

            if (result > 0) {
                LOGGER.info("Successfully updated employee: " + employee.getEmployeeId() + " - " + employee.getFullName());
                return true;
            } else {
                LOGGER.warning("No employee found with ID: " + employee.getEmployeeId() + " for update");
                return false;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error updating employee with ID: " + employee.getEmployeeId(), ex);
            throw new RuntimeException("Failed to update employee: " + ex.getMessage(), ex);
        }
    }

    public boolean deleteEmployee(int employeeId) {
        if (employeeId <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }

        String sql = "DELETE FROM employees WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            int result = stmt.executeUpdate();

            if (result > 0) {
                LOGGER.info("Successfully deleted employee with ID: " + employeeId);
                return true;
            } else {
                LOGGER.warning("No employee found with ID: " + employeeId + " for deletion");
                return false;
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error deleting employee with ID: " + employeeId, ex);
            throw new RuntimeException("Failed to delete employee: " + ex.getMessage(), ex);
        }
    }

    public List<Employee> getEmployeesByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM v_employee_details WHERE status = ? ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status.trim());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee e = mapResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error fetching employees by status: " + status, ex);
            throw new RuntimeException("Failed to fetch employees by status", ex);
        }

        return employees;
    }

    public List<Employee> searchEmployees(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllEmployees();
        }

        List<Employee> employees = new ArrayList<>();
        String query = "SELECT * FROM v_employee_details WHERE " +
                "employee_id LIKE ? OR " +
                "CONCAT(first_name, ' ', last_name) LIKE ? OR " +
                "CONCAT(last_name, ', ', first_name) LIKE ? OR " +
                "position_title LIKE ? " +
                "ORDER BY last_name, first_name";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            String searchPattern = "%" + searchTerm.trim() + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            stmt.setString(4, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Employee e = mapResultSetToEmployee(rs);
                employees.add(e);
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error searching employees with term: " + searchTerm, ex);
            throw new RuntimeException("Failed to search employees", ex);
        }

        return employees;
    }

    public List<Employee> getAllActiveEmployees() {
        return getEmployeesByStatus("Regular");
    }

    // Enhanced mapResultSetToEmployee using POLYMORPHISM
    private Employee mapResultSetToEmployee(ResultSet rs) throws SQLException {
        String status = rs.getString("status");
        String employeeType = EmployeeFactory.determineEmployeeType(status);
        
        // Create appropriate employee type using FACTORY PATTERN
        Employee e = EmployeeFactory.createEmployee(employeeType, 
                rs.getInt("employee_id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("position_title"),
                rs.getDouble("basic_salary"));

        // Set additional properties
        e.setId(rs.getInt("employee_id"));
        
        java.sql.Date birthday = rs.getDate("birthday");
        if (birthday != null) {
            e.setBirthday(birthday.toLocalDate());
        }

        e.setAddress(rs.getString("address"));
        e.setPhoneNumber(rs.getString("phone_number"));
        e.setSssNumber(rs.getString("sss_number"));
        e.setPhilhealthNumber(rs.getString("philhealth_number"));
        e.setTinNumber(rs.getString("tin_number"));
        e.setPagibigNumber(rs.getString("pagibig_number"));
        e.setStatus(rs.getString("status"));
        e.setImmediateSupervisor(rs.getString("supervisor_name"));

        // Set allowances from position
        e.setRiceSubsidy(rs.getDouble("rice_subsidy"));
        e.setPhoneAllowance(rs.getDouble("phone_allowance"));
        e.setClothingAllowance(rs.getDouble("clothing_allowance"));
        e.setGrossSemiMonthlyRate(rs.getDouble("gross_semi_monthly_rate"));
        e.setHourlyRate(rs.getDouble("hourly_rate"));
        e.setPositionId(rs.getInt("supervisor_id"));

        return e;
    }

    // Helper methods to convert between position names and IDs
    private int getPositionId(String positionName) {
        if (positionName == null) return 1; // Default position

        String query = "SELECT position_id FROM positions WHERE position_title = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, positionName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("position_id");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error getting position ID for: " + positionName, ex);
        }

        return 1; // Default position ID if not found
    }

    private Integer getSupervisorId(String supervisorName) {
        if (supervisorName == null || supervisorName.trim().isEmpty()) {
            return null;
        }

        String query = "SELECT employee_id FROM employees WHERE CONCAT(last_name, ', ', first_name) = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, supervisorName.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("employee_id");
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.WARNING, "Error getting supervisor ID for: " + supervisorName, ex);
        }

        return null;
    }

    public boolean employeeExists(int employeeId) {
        if (employeeId <= 0) {
            return false;
        }

        String query = "SELECT 1 FROM employees WHERE employee_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employeeId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error checking if employee exists: " + employeeId, ex);
            return false;
        }
    }

    public int getEmployeeCountByStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }

        String query = "SELECT COUNT(*) FROM employees WHERE status = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }

        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, "Error getting employee count by status: " + status, ex);
            throw new RuntimeException("Failed to get employee count", ex);
        }

        return 0;
    }
}