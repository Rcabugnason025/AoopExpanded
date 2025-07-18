/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import model.Employee;
import dao.EmployeeDAO;
import service.SimplePayslipService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Date;
import java.util.Calendar;

public class PayslipGenerationPanel extends JPanel {
    private JTextField employeeIdField;
    private JTextField daysWorkedField;
    private JTextField overtimeHoursField;
    private JSpinner periodStartSpinner;
    private JSpinner periodEndSpinner;
    private JButton generateButton;
    private JButton previewButton;
    private JButton printButton;
    private JTextArea statusArea;
    
    private EmployeeDAO employeeDAO;
    private SimplePayslipService payslipService;
    
    public PayslipGenerationPanel() {
        this.employeeDAO = new EmployeeDAO();
        this.payslipService = new SimplePayslipService();
        initializeComponents();
        setupLayout();
        setupEventHandlers();
    }
    
    private void initializeComponents() {
        // Input fields
        employeeIdField = new JTextField(10);
        daysWorkedField = new JTextField(10);
        daysWorkedField.setText("22"); // Default
        overtimeHoursField = new JTextField(10);
        overtimeHoursField.setText("0"); // Default
        
        // Date pickers for period
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1); // Start of month
        Date startDate = cal.getTime();
        
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); // End of month
        Date endDate = cal.getTime();
        
        periodStartSpinner = new JSpinner(new SpinnerDateModel(startDate, null, null, Calendar.DAY_OF_MONTH));
        periodStartSpinner.setEditor(new JSpinner.DateEditor(periodStartSpinner, "MM/dd/yyyy"));
        
        periodEndSpinner = new JSpinner(new SpinnerDateModel(endDate, null, null, Calendar.DAY_OF_MONTH));
        periodEndSpinner.setEditor(new JSpinner.DateEditor(periodEndSpinner, "MM/dd/yyyy"));
        
        // Buttons
        generateButton = new JButton("Generate Payslip PDF");
        generateButton.setBackground(new Color(46, 204, 113));
        generateButton.setForeground(Color.WHITE);
        generateButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        previewButton = new JButton("Preview Payslip");
        previewButton.setBackground(new Color(52, 152, 219));
        previewButton.setForeground(Color.WHITE);
        previewButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        printButton = new JButton("Print Payslip");
        printButton.setBackground(new Color(155, 89, 182));
        printButton.setForeground(Color.WHITE);
        printButton.setFont(new Font("Arial", Font.BOLD, 12));
        
        // Status area
        statusArea = new JTextArea(8, 50);
        statusArea.setEditable(false);
        statusArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statusArea.setBackground(new Color(248, 249, 250));
        statusArea.setBorder(BorderFactory.createLoweredBevelBorder());
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(new TitledBorder("Payslip Generation"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
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
        
        // Period Start Date
        gbc.gridx = 0; gbc.gridy = 3;
        inputPanel.add(new JLabel("Period Start Date:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(periodStartSpinner, gbc);
        
        // Period End Date
        gbc.gridx = 0; gbc.gridy = 4;
        inputPanel.add(new JLabel("Period End Date:"), gbc);
        gbc.gridx = 1;
        inputPanel.add(periodEndSpinner, gbc);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(previewButton);
        buttonPanel.add(generateButton);
        buttonPanel.add(printButton);
        
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        inputPanel.add(buttonPanel, gbc);
        
        // Status panel
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(new TitledBorder("Generation Status"));
        statusPanel.add(new JScrollPane(statusArea), BorderLayout.CENTER);
        
        add(inputPanel, BorderLayout.NORTH);
        add(statusPanel, BorderLayout.CENTER);
    }
    
    private void setupEventHandlers() {
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generatePayslipPDF();
            }
        });
        
        previewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                previewPayslip();
            }
        });
        
        printButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                generateAndPrintPayslip();
            }
        });
    }
    
    private void generatePayslipPDF() {
        try {
            // Validate inputs
            if (!validateInputs()) return;
            
            // Get input values
            int employeeId = Integer.parseInt(employeeIdField.getText().trim());
            int daysWorked = Integer.parseInt(daysWorkedField.getText().trim());
            double overtimeHours = Double.parseDouble(overtimeHoursField.getText().trim());
            Date periodStart = (Date) periodStartSpinner.getValue();
            Date periodEnd = (Date) periodEndSpinner.getValue();
            
            updateStatus("Fetching employee information...");
            
            // Get employee
            Employee employee = employeeDAO.getEmployeeById(employeeId);
            if (employee == null) {
                updateStatus("❌ Employee not found with ID: " + employeeId);
                JOptionPane.showMessageDialog(this, "Employee not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            updateStatus("✓ Employee found: " + employee.getFullName());
            updateStatus("📊 Generating payslip PDF...");
            
            // Generate payslip
            java.time.LocalDate localStart = periodStart.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            java.time.LocalDate localEnd = periodEnd.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            
            File payslipFile = payslipService.generatePayslipToFile(employee.getEmployeeId(), localStart, localEnd, 
                System.getProperty("user.home") + "/MotorPH_Payslips/");
            
            updateStatus("✅ Payslip generated successfully!");
            updateStatus("📁 File saved to: " + payslipFile.getAbsolutePath());
            updateStatus("📄 File size: " + String.format("%.2f KB", payslipFile.length() / 1024.0));
            
            // Ask user if they want to open the file
            int option = JOptionPane.showConfirmDialog(this,
                "Payslip generated successfully!\n\nEmployee: " + employee.getFullName() + 
                "\nFile: " + payslipFile.getName() + "\n\nDo you want to open it now?",
                "Payslip Generated",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.INFORMATION_MESSAGE);
            
            if (option == JOptionPane.YES_OPTION) {
                Desktop.getDesktop().open(payslipFile);
            }
            
        } catch (NumberFormatException e) {
            updateStatus("❌ Invalid input format. Please check your entries.");
            JOptionPane.showMessageDialog(this, "Please enter valid numeric values.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            updateStatus("❌ Unexpected error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Unexpected error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void previewPayslip() {
        try {
            if (!validateInputs()) return;
            
            int employeeId = Integer.parseInt(employeeIdField.getText().trim());
            int daysWorked = Integer.parseInt(daysWorkedField.getText().trim());
            double overtimeHours = Double.parseDouble(overtimeHoursField.getText().trim());
            Date periodStart = (Date) periodStartSpinner.getValue();
            Date periodEnd = (Date) periodEndSpinner.getValue();
            
            Employee employee = employeeDAO.getEmployeeById(employeeId);
            if (employee == null) {
                JOptionPane.showMessageDialog(this, "Employee not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            updateStatus("🔍 Opening payslip preview for " + employee.getFullName() + "...");
            
            // Show preview
            java.time.LocalDate localStart = periodStart.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            java.time.LocalDate localEnd = periodEnd.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            
            String payslipText = payslipService.generatePayslipText(employee.getEmployeeId(), localStart, localEnd);
            
            // Show in dialog
            JDialog previewDialog = new JDialog();
            previewDialog.setTitle("Payslip Preview - " + employee.getFullName());
            previewDialog.setSize(800, 600);
            previewDialog.setLocationRelativeTo(this);
            
            JTextArea textArea = new JTextArea(payslipText);
            textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
            textArea.setEditable(false);
            
            previewDialog.add(new JScrollPane(textArea));
            previewDialog.setVisible(true);
            
            updateStatus("✓ Preview window opened successfully");
            
        } catch (Exception e) {
            updateStatus("❌ Preview error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to preview payslip: " + e.getMessage(), "Preview Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void generateAndPrintPayslip() {
        try {
            if (!validateInputs()) return;
            
            int employeeId = Integer.parseInt(employeeIdField.getText().trim());
            int daysWorked = Integer.parseInt(daysWorkedField.getText().trim());
            double overtimeHours = Double.parseDouble(overtimeHoursField.getText().trim());
            Date periodStart = (Date) periodStartSpinner.getValue();
            Date periodEnd = (Date) periodEndSpinner.getValue();
            
            Employee employee = employeeDAO.getEmployeeById(employeeId);
            if (employee == null) {
                JOptionPane.showMessageDialog(this, "Employee not found!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            updateStatus("🖨️ Generating payslip for printing...");
            
            // Generate PDF first
            java.time.LocalDate localStart = periodStart.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            java.time.LocalDate localEnd = periodEnd.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
            
            File payslipFile = payslipService.generatePayslipToFile(employee.getEmployeeId(), localStart, localEnd, 
                System.getProperty("user.home") + "/MotorPH_Payslips/");
            
            updateStatus("✓ PDF generated, opening print dialog...");
            
            // Open with default PDF viewer for printing
            Desktop.getDesktop().print(payslipFile);
            
            updateStatus("✅ Print dialog opened successfully");
            
        } catch (Exception e) {
            updateStatus("❌ Print error: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Failed to print payslip: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private boolean validateInputs() {
        // Validate Employee ID
        String employeeIdText = employeeIdField.getText().trim();
        if (employeeIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an Employee ID.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            employeeIdField.requestFocus();
            return false;
        }
        
        try {
            Integer.parseInt(employeeIdText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Employee ID must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            employeeIdField.requestFocus();
            return false;
        }
        
        // Validate Days Worked
        String daysWorkedText = daysWorkedField.getText().trim();
        if (daysWorkedText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter Days Worked.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            daysWorkedField.requestFocus();
            return false;
        }
        
        try {
            int daysWorked = Integer.parseInt(daysWorkedText);
            if (daysWorked < 0 || daysWorked > 31) {
                JOptionPane.showMessageDialog(this, "Days worked must be between 0 and 31.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                daysWorkedField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Days worked must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            daysWorkedField.requestFocus();
            return false;
        }
        
        // Validate Overtime Hours
        String overtimeText = overtimeHoursField.getText().trim();
        if (!overtimeText.isEmpty()) {
            try {
                double overtime = Double.parseDouble(overtimeText);
                if (overtime < 0) {
                    JOptionPane.showMessageDialog(this, "Overtime hours cannot be negative.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    overtimeHoursField.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Overtime hours must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                overtimeHoursField.requestFocus();
                return false;
            }
        }
        
        // Validate Date Range
        Date startDate = (Date) periodStartSpinner.getValue();
        Date endDate = (Date) periodEndSpinner.getValue();
        
        if (startDate.after(endDate)) {
            JOptionPane.showMessageDialog(this, "Period start date cannot be after end date.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    private void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = new java.text.SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
            statusArea.append("[" + timestamp + "] " + message + "\n");
            statusArea.setCaretPosition(statusArea.getDocument().getLength());
        });
    }
    
    public void clearForm() {
        employeeIdField.setText("");
        daysWorkedField.setText("22");
        overtimeHoursField.setText("0");
        statusArea.setText("");
        
        // Reset dates to current month
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        periodStartSpinner.setValue(cal.getTime());
        
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        periodEndSpinner.setValue(cal.getTime());
    }
}
