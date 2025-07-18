/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import model.Employee;
import dao.EmployeeDAO;
import reports.JasperReportGenerator;
import reports.PayrollReportData;
import net.sf.jasperreports.engine.JRException;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReportsPanel extends JPanel {
    private JButton generatePayrollReportButton;
    private JButton generateEmployeeReportButton;
    private JTextField periodNameField;
    private JTextField employeeIdField;
    private JTextArea statusArea;
    private final EmployeeDAO employeeDAO;
    private final JasperReportGenerator reportGenerator;
    
    public ReportsPanel() {
        this.employeeDAO = new EmployeeDAO();
        this.reportGenerator = new JasperReportGenerator();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        generatePayrollReportButton = new JButton("Generate Payroll Report");
        generatePayrollReportButton.setBackground(new Color(46, 204, 113));
        generatePayrollReportButton.setForeground(Color.WHITE);
        generatePayrollReportButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        generateEmployeeReportButton = new JButton("Generate Employee Report");
        generateEmployeeReportButton.setBackground(new Color(52, 152, 219));
        generateEmployeeReportButton.setForeground(Color.WHITE);
        generateEmployeeReportButton.setFont(new Font("Arial", Font.BOLD, 14));
        
        periodNameField = new JTextField(15);
        periodNameField.setText("January 2025"); // Default value
        
        employeeIdField = new JTextField(15);
        
        statusArea = new JTextArea(10, 40);
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statusArea.setBackground(new Color(248, 249, 250));
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Control panel
        JPanel controlPanel = new JPanel(new GridBagLayout());
        controlPanel.setBorder(new TitledBorder("Report Generation"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Payroll report section
        gbc.gridx = 0; gbc.gridy = 0;
        controlPanel.add(new JLabel("Period Name:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(periodNameField, gbc);
        gbc.gridx = 2;
        controlPanel.add(generatePayrollReportButton, gbc);
        
        // Employee report section
        gbc.gridx = 0; gbc.gridy = 1;
        controlPanel.add(new JLabel("Employee ID:"), gbc);
        gbc.gridx = 1;
        controlPanel.add(employeeIdField, gbc);
        gbc.gridx = 2;
        controlPanel.add(generateEmployeeReportButton, gbc);
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new TitledBorder("Report Generation Status"));
        statusPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        
        add(controlPanel, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        generatePayrollReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePayrollReport();
            }
        });
        
        generateEmployeeReportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateEmployeeReport();
            }
        });
    }
    
    private void generatePayrollReport() {
        try {
            String periodName = periodNameField.getText().trim();
            if (periodName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a period name.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            updateStatus("Generating payroll report for period: " + periodName);
            
            // Get all active employees
            List<Employee> employees = employeeDAO.getAllActiveEmployees();
            if (employees.isEmpty()) {
                updateStatus("No active employees found.");
                return;
            }
            
            // Create payroll data for all employees
            List<PayrollReportData> payrollData = new ArrayList<>();
            for (Employee employee : employees) {
                // For demo, assume 22 days worked and 0 overtime
                PayrollReportData data = new PayrollReportData(employee, 22, 0);
                payrollData.add(data);
            }
            
            updateStatus("Processing " + employees.size() + " employees...");
            
            // Generate report
            File reportFile = reportGenerator.generatePayrollReport(payrollData, periodName);
            
            updateStatus("✓ Payroll report generated successfully!");
            updateStatus("  File saved to: " + reportFile.getAbsolutePath());
            updateStatus("  Total employees: " + employees.size());
            updateStatus("  Report size: " + String.format("%.2f KB", reportFile.length() / 1024.0));
            
            // Ask user if they want to open the report
            int option = JOptionPane.showConfirmDialog(this, 
                "Report generated successfully!\nDo you want to open it now?", 
                "Report Generated", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.INFORMATION_MESSAGE);
            
            if (option == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(reportFile);
            }
            
        } catch (JRException e) {
            updateStatus("✗ Error generating report: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to generate report: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            updateStatus("✗ Unexpected error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Unexpected error: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generateEmployeeReport() {
        try {
            String employeeIdText = employeeIdField.getText().trim();
            if (employeeIdText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an employee ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            int employeeId = Integer.parseInt(employeeIdText);
            updateStatus("Generating employee report for ID: " + employeeId);
            
            // Get employee details
            Employee employee = employeeDAO.getEmployeeById(employeeId);
            if (employee == null) {
                updateStatus("✗ Employee not found with ID: " + employeeId);
                JOptionPane.showMessageDialog(this, "Employee not found with ID: " + employeeId, 
                                            "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create sample payroll history (in real app, get from database)
            List<PayrollReportData> payrollHistory = new ArrayList<>();
            for (int i = 1; i <= 6; i++) { // Last 6 months
                PayrollReportData data = new PayrollReportData(employee, 22, 0);
                payrollHistory.add(data);
            }
            
            updateStatus("Processing employee: " + employee.getFullName());
            
            // Generate report
            File reportFile = reportGenerator.generateEmployeeReport(employee, payrollHistory);
            
            updateStatus("✓ Employee report generated successfully!");
            updateStatus("  Employee: " + employee.getFullName());
            updateStatus("  Position: " + employee.getPosition());
            updateStatus("  File saved to: " + reportFile.getAbsolutePath());
            
            // Ask user if they want to open the report
            int option = JOptionPane.showConfirmDialog(this, 
                "Employee report generated successfully!\nDo you want to open it now?", 
                "Report Generated", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.INFORMATION_MESSAGE);
            
            if (option == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(reportFile);
            }
            
        } catch (NumberFormatException e) {
            updateStatus("✗ Invalid employee ID format");
            JOptionPane.showMessageDialog(this, "Please enter a valid employee ID number.", 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        } catch (JRException e) {
            updateStatus("✗ Error generating report: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to generate report: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            updateStatus("✗ Unexpected error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Unexpected error: " + e.getMessage(), 
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            statusArea.append("[" + new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date()) + "] " + message + "\n");
            statusArea.setCaretPosition(statusArea.getDocument().getLength());
        });
    }
}