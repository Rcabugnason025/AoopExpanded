/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import model.*;
import dao.EmployeeDAO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.text.DecimalFormat;

public class PayrollCalculationPanel extends JPanel {
    private JTextField employeeIdField;
    private JTextField daysWorkedField;
    private JTextField overtimeHoursField;
    private JTextArea resultArea;
    private JButton calculateButton;
    private JButton clearButton;
    private EmployeeDAO employeeDAO;
    private DecimalFormat currencyFormat;
    
    public PayrollCalculationPanel() {
        this.employeeDAO = new EmployeeDAO();
        this.currencyFormat = new DecimalFormat("₱#,##0.00");
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Input fields with validation
        employeeIdField = new JTextField(10);
        daysWorkedField = new JTextField(10);
        overtimeHoursField = new JTextField(10);
        
        // Buttons with better styling
        calculateButton = new JButton("Calculate Payroll");
        calculateButton.setBackground(new Color(52, 152, 219));
        calculateButton.setForeground(Color.WHITE);
        calculateButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        clearButton = new JButton("Clear");
        clearButton.setBackground(new Color(149, 165, 166));
        clearButton.setForeground(Color.WHITE);
        
        // Result area with better formatting
        resultArea = new JTextArea(15, 40);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        resultArea.setBackground(new Color(248, 249, 250));
        resultArea.setBorder(BorderFactory.createLoweredBevelBorder());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Input panel with better organization
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new TitledBorder("Payroll Calculation Input"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Employee ID
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Employee ID:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(employeeIdField, gbc);
        
        // Days Worked
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Days Worked:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(daysWorkedField, gbc);
        
        // Overtime Hours
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Overtime Hours:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(overtimeHoursField, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(calculateButton);
        buttonPanel.add(clearButton);
        
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(buttonPanel, gbc);
        
        // Result panel
        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.setBorder(new TitledBorder("Payroll Calculation Results"));
        resultPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        
        add(inputPanel, BorderLayout.NORTH);
        add(resultPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        calculateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performPayrollCalculation();
            }
        });
        
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearAllFields();
            }
        });
        
        // Input validation on focus lost
        employeeIdField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                validateEmployeeId();
            }
        });
        
        daysWorkedField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                validateDaysWorked();
            }
        });
        
        overtimeHoursField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                validateOvertimeHours();
            }
        });
    }
    
    private void performPayrollCalculation() {
        try {
            // Validate inputs
            if (!validateAllInputs()) {
                return;
            }
            
            // Get input values
            int employeeId = Integer.parseInt(employeeIdField.getText().trim());
            int daysWorked = Integer.parseInt(daysWorkedField.getText().trim());
            double overtimeHours = Double.parseDouble(overtimeHoursField.getText().trim());
            
            // Get employee from database
            Employee employee = employeeDAO.getEmployeeById(employeeId);
            if (employee == null) {
                showErrorMessage("Employee not found with ID: " + employeeId);
                return;
            }
            
            // Show loading indicator
            calculateButton.setEnabled(false);
            calculateButton.setText("Calculating...");
            
            // Perform calculation using polymorphism
            double grossPay = employee.calculateGrossPay(daysWorked, overtimeHours);
            double deductions = employee.calculateDeductions();
            double allowances = employee.calculateAllowances();
            double netPay = employee.calculateNetPay(daysWorked, overtimeHours);
            
            // Display detailed results
            displayPayrollResults(employee, daysWorked, overtimeHours, 
                                grossPay, deductions, allowances, netPay);
            
        } catch (NumberFormatException e) {
            showErrorMessage("Please enter valid numeric values.");
        } catch (SQLException e) {
            showErrorMessage("Database error: " + e.getMessage());
        } catch (Exception e) {
            showErrorMessage("Unexpected error: " + e.getMessage());
        } finally {
            // Reset button
            calculateButton.setEnabled(true);
            calculateButton.setText("Calculate Payroll");
        }
    }
    
    private boolean validateAllInputs() {
        return validateEmployeeId() && validateDaysWorked() && validateOvertimeHours();
    }
    
    private boolean validateEmployeeId() {
        String text = employeeIdField.getText().trim();
        if (text.isEmpty()) {
            highlightError(employeeIdField, "Employee ID is required");
            return false;
        }
        try {
            int id = Integer.parseInt(text);
            if (id <= 0) {
                highlightError(employeeIdField, "Employee ID must be positive");
                return false;
            }
            clearError(employeeIdField);
            return true;
        } catch (NumberFormatException e) {
            highlightError(employeeIdField, "Employee ID must be a number");
            return false;
        }
    }
    
    private boolean validateDaysWorked() {
        String text = daysWorkedField.getText().trim();
        if (text.isEmpty()) {
            highlightError(daysWorkedField, "Days worked is required");
            return false;
        }
        try {
            int days = Integer.parseInt(text);
            if (days < 0 || days > 31) {
                highlightError(daysWorkedField, "Days worked must be between 0 and 31");
                return false;
            }
            clearError(daysWorkedField);
            return true;
        } catch (NumberFormatException e) {
            highlightError(daysWorkedField, "Days worked must be a number");
            return false;
        }
    }
    
    private boolean validateOvertimeHours() {
        String text = overtimeHoursField.getText().trim();
        if (text.isEmpty()) {
            overtimeHoursField.setText("0"); // Default to 0
            return true;
        }
        try {
            double hours = Double.parseDouble(text);
            if (hours < 0 || hours > 100) {
                highlightError(overtimeHoursField, "Overtime hours must be between 0 and 100");
                return false;
            }
            clearError(overtimeHoursField);
            return true;
        } catch (NumberFormatException e) {
            highlightError(overtimeHoursField, "Overtime hours must be a number");
            return false;
        }
    }
    
    private void highlightError(JTextField field, String message) {
        field.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        field.setToolTipText(message);
    }
    
    private void clearError(JTextField field) {
        field.setBorder(UIManager.getBorder("TextField.border"));
        field.setToolTipText(null);
    }
    
    private void displayPayrollResults(Employee employee, int daysWorked, double overtimeHours,
                                     double grossPay, double deductions, double allowances, double netPay) {
        StringBuilder result = new StringBuilder();
        result.append("═══════════════════════════════════════════════════════════\n");
        result.append("                  MOTORPH PAYROLL CALCULATION\n");
        result.append("═══════════════════════════════════════════════════════════\n\n");
        
        result.append("EMPLOYEE INFORMATION:\n");
        result.append("  Employee ID      : ").append(employee.getEmployeeId()).append("\n");
        result.append("  Name            : ").append(employee.getFullName()).append("\n");
        result.append("  Employee Type   : ").append(employee.getEmployeeType()).append("\n");
        result.append("  Position        : ").append(employee.getPosition()).append("\n");
        result.append("  Department      : ").append(employee.getDepartment()).append("\n");
        result.append("  Basic Salary    : ").append(currencyFormat.format(employee.getBasicSalary())).append("\n\n");
        
        result.append("CALCULATION DETAILS:\n");
        result.append("  Days Worked     : ").append(daysWorked).append(" days\n");
        result.append("  Overtime Hours  : ").append(String.format("%.2f", overtimeHours)).append(" hours\n");
        result.append("  Daily Rate      : ").append(currencyFormat.format(employee.getBasicSalary() / 22)).append("\n\n");
        
        result.append("PAY BREAKDOWN:\n");
        result.append("  Gross Pay       : ").append(currencyFormat.format(grossPay)).append("\n");
        result.append("  Allowances      : ").append(currencyFormat.format(allowances)).append("\n");
        result.append("  Total Deductions: ").append(currencyFormat.format(deductions)).append("\n");
        result.append("  ").append("─".repeat(40)).append("\n");
        result.append("  NET PAY         : ").append(currencyFormat.format(netPay)).append("\n\n");
        
        result.append("BENEFITS ELIGIBILITY:\n");
        result.append("  Benefits Eligible: ").append(employee.isEligibleForBenefits() ? "YES" : "NO").append("\n\n");
        
        result.append("═══════════════════════════════════════════════════════════\n");
        result.append("Calculation completed on: ").append(new java.util.Date()).append("\n");
        result.append("═══════════════════════════════════════════════════════════");
        
        resultArea.setText(result.toString());
        resultArea.setCaretPosition(0); // Scroll to top
    }
    
    private void clearAllFields() {
        employeeIdField.setText("");
        daysWorkedField.setText("");
        overtimeHoursField.setText("");
        resultArea.setText("");
        clearError(employeeIdField);
        clearError(daysWorkedField);
        clearError(overtimeHoursField);
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccessMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
