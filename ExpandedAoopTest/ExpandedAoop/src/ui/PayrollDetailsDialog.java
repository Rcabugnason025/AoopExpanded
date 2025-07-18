package ui;

import model.Employee;
import model.Payroll;
import service.SimplePayslipService;

import javax.swing.*;
import java.awt.*;
import java.awt.print.PrinterException;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PayrollDetailsDialog extends JDialog {
    private Employee employee;
    private Payroll payroll;
    private SimplePayslipService payslipService;
    private JTextArea payslipTextArea;
    private JLabel statusLabel;

    public PayrollDetailsDialog(Frame parent, Employee employee, Payroll payroll) {
        super(parent, "Payroll Details - " + employee.getFullName(), true);
        this.employee = employee;
        this.payroll = payroll;

        // Initialize payslip service
        try {
            this.payslipService = new SimplePayslipService();
            System.out.println("✅ Payslip service initialized successfully");
        } catch (Exception e) {
            System.err.println("⚠️ Payslip service initialization failed: " + e.getMessage());
            this.payslipService = null;
        }

        initializeComponents();
        setupLayout();
        generatePayslip();

        setSize(700, 800);
        setLocationRelativeTo(parent);
        setResizable(true);
        setMinimumSize(new Dimension(650, 700));
    }

    private void initializeComponents() {
        payslipTextArea = new JTextArea();
        payslipTextArea.setEditable(false);
        payslipTextArea.setFont(new Font("Courier New", Font.PLAIN, 12));
        payslipTextArea.setBackground(Color.WHITE);
        payslipTextArea.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        statusLabel.setForeground(Color.GRAY);
    }

    private void setupLayout() {
        setLayout(new BorderLayout());

        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Company header
        JPanel companyPanel = createCompanyHeader();

        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(companyPanel, BorderLayout.NORTH);

        // Payslip text
        JScrollPane scrollPane = new JScrollPane(payslipTextArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        add(contentPanel, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);

        // Status Panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.SOUTH);

        // Add button panel above status
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(25, 25, 112));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel titleLabel = new JLabel("Employee Payslip - MotorPH Payroll System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);

        // Add service status indicator
        JLabel serviceStatus = new JLabel("🟢 Payslip Service Ready");
        serviceStatus.setFont(new Font("Arial", Font.PLAIN, 12));
        serviceStatus.setForeground(Color.GREEN);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(serviceStatus, BorderLayout.EAST);

        return headerPanel;
    }

    private JPanel createCompanyHeader() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Company Name
        JLabel companyName = new JLabel("MotorPH", JLabel.CENTER);
        companyName.setFont(new Font("Arial", Font.BOLD, 24));
        companyName.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Address
        JLabel address = new JLabel("7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City", JLabel.CENTER);
        address.setFont(new Font("Arial", Font.PLAIN, 12));
        address.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Phone
        JLabel phone = new JLabel("Phone: (028) 911-5071 / (028) 911-5072 / (028) 911-5073", JLabel.CENTER);
        phone.setFont(new Font("Arial", Font.PLAIN, 12));
        phone.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Email
        JLabel email = new JLabel("Email: corporate@motorph.com", JLabel.CENTER);
        email.setFont(new Font("Arial", Font.PLAIN, 12));
        email.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Payslip Title
        JLabel payslipTitle = new JLabel("EMPLOYEE PAYSLIP", JLabel.CENTER);
        payslipTitle.setFont(new Font("Arial", Font.BOLD, 18));
        payslipTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(companyName);
        panel.add(Box.createVerticalStrut(5));
        panel.add(address);
        panel.add(phone);
        panel.add(email);
        panel.add(Box.createVerticalStrut(15));
        panel.add(payslipTitle);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));

        // Create buttons with enhanced functionality
        JButton printButton = createStyledButton("🖨️ Print", new Color(70, 130, 180));
        JButton textButton = createStyledButton("📝 Save Text", new Color(255, 140, 0));
        JButton closeButton = createStyledButton("❌ Close", new Color(108, 117, 125));

        // Add action listeners
        printButton.addActionListener(e -> printPayslip());
        textButton.addActionListener(e -> saveAsText());
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(printButton);
        buttonPanel.add(textButton);
        buttonPanel.add(closeButton);

        return buttonPanel;
    }

    private JButton createStyledButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(130, 35));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        return button;
    }

    private void generatePayslip() {
        setStatus("Generating payslip...");

        StringBuilder sb = new StringBuilder();

        // Generate payslip number and dates
        String payslipNo = String.format("%d-%s",
                employee.getEmployeeId(),
                payroll.getEndDateAsLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

        String periodStart = payroll.getStartDateAsLocalDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
        String periodEnd = payroll.getEndDateAsLocalDate().format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));

        // Company Header (for printing/text preview)
        sb.append("\n");
        sb.append("                                MotorPH\n");
        sb.append("           7 Jupiter Avenue cor. F. Sandoval Jr., Bagong Nayon, Quezon City\n");
        sb.append("           Phone: (028) 911-5071 / (028) 911-5072 / (028) 911-5073\n");
        sb.append("                         Email: corporate@motorph.com\n\n");
        sb.append("                            EMPLOYEE PAYSLIP\n");
        sb.append("\n");
        sb.append("================================================\n\n");

        // Payslip Details
        sb.append("PAYSLIP NO: ").append(payslipNo).append("\n\n");

        // Employee Information Section
        sb.append("EMPLOYEE INFORMATION:\n");
        sb.append("================================================\n");
        sb.append("Employee ID         : ").append(employee.getEmployeeId()).append("\n");
        sb.append("Name                : ").append(employee.getLastName()).append(", ").append(employee.getFirstName()).append("\n");
        sb.append("Position            : ").append(employee.getPosition()).append("\n");
        sb.append("Department          : ").append(employee.getPosition()).append("\n");
        sb.append("Employment Status   : ").append(employee.getStatus()).append("\n\n");

        // Pay Period Information
        sb.append("PAY PERIOD INFORMATION:\n");
        sb.append("================================================\n");
        sb.append("Pay Period          : ").append(periodStart).append(" to ").append(periodEnd).append("\n");
        sb.append("Days Worked         : ").append(payroll.getDaysWorked()).append("\n");
        sb.append("Monthly Rate        : ").append(formatCurrency(payroll.getMonthlyRate())).append("\n");
        sb.append("Daily Rate          : ").append(formatCurrency(payroll.getDailyRate())).append("\n\n");

        // Earnings Section
        sb.append("EARNINGS:\n");
        sb.append("================================================\n");
        sb.append("Basic Pay           : ").append(formatCurrency(payroll.getGrossEarnings())).append("\n");
        sb.append("Overtime Pay        : ").append(formatCurrency(payroll.getOvertimePay())).append("\n");
        sb.append("Rice Subsidy        : ").append(formatCurrency(payroll.getRiceSubsidy())).append("\n");
        sb.append("Phone Allowance     : ").append(formatCurrency(payroll.getPhoneAllowance())).append("\n");
        sb.append("Clothing Allowance  : ").append(formatCurrency(payroll.getClothingAllowance())).append("\n");
        sb.append("                      ").append("____________").append("\n");
        sb.append("GROSS PAY           : ").append(formatCurrency(payroll.getGrossPay())).append("\n\n");

        // Deductions Section
        sb.append("DEDUCTIONS:\n");
        sb.append("================================================\n");
        sb.append("Social Security System : ").append(formatCurrency(payroll.getSss())).append("\n");
        sb.append("Philhealth            : ").append(formatCurrency(payroll.getPhilhealth())).append("\n");
        sb.append("Pag-Ibig               : ").append(formatCurrency(payroll.getPagibig())).append("\n");
        sb.append("Withholding Tax        : ").append(formatCurrency(payroll.getTax())).append("\n");
        if (payroll.getLateDeduction() > 0) {
            sb.append("Late Deduction         : ").append(formatCurrency(payroll.getLateDeduction())).append("\n");
        }
        if (payroll.getUndertimeDeduction() > 0) {
            sb.append("Undertime Deduction    : ").append(formatCurrency(payroll.getUndertimeDeduction())).append("\n");
        }
        if (payroll.getUnpaidLeaveDeduction() > 0) {
            sb.append("Unpaid Leave           : ").append(formatCurrency(payroll.getUnpaidLeaveDeduction())).append("\n");
        }
        sb.append("                         ").append("____________").append("\n");
        sb.append("TOTAL DEDUCTIONS       : ").append(formatCurrency(payroll.getTotalDeductions())).append("\n\n");

        // Summary Section
        sb.append("SUMMARY:\n");
        sb.append("================================================\n");
        sb.append("Gross Pay              : ").append(formatCurrency(payroll.getGrossPay())).append("\n");
        sb.append("Total Deductions       : ").append(formatCurrency(payroll.getTotalDeductions())).append("\n");
        sb.append("                         ").append("____________").append("\n");
        sb.append("TAKE HOME PAY          : ").append(formatCurrency(payroll.getNetPay())).append("\n\n");

        // Footer
        sb.append("================================================\n");
        sb.append("This payslip is computer-generated and does not require signature.\n");
        sb.append("Please keep this document for your records.\n\n");
        sb.append("Generated on: ").append(java.time.LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' HH:mm:ss"))).append("\n");

        payslipTextArea.setText(sb.toString());
        payslipTextArea.setCaretPosition(0); // Scroll to top

        setStatus("Payslip ready - Print and save available");
    }

    private String formatCurrency(double amount) {
        return String.format("₱%,.2f", amount);
    }

    private void printPayslip() {
        setStatus("Preparing to print...");
        try {
            boolean doPrint = payslipTextArea.print();
            if (doPrint) {
                showSuccess("Payslip printed successfully!");
            } else {
                setStatus("Print cancelled by user");
            }
        } catch (PrinterException e) {
            showError("Error printing payslip: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Enhanced text save functionality (existing functionality improved)
     */
    private void saveAsText() {
        setStatus("Saving as text file...");

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Payslip as Text");

        String filename = String.format("Payslip_%s_%s.txt",
                employee.getLastName().replaceAll("\\s+", ""),
                payroll.getPeriodStart().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy_MM")));

        fileChooser.setSelectedFile(new File(filename));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            try {
                File fileToSave = fileChooser.getSelectedFile();

                // Ensure .txt extension
                if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                    fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
                }

                // Write payslip content to file
                java.nio.file.Files.write(fileToSave.toPath(),
                        payslipTextArea.getText().getBytes(java.nio.charset.StandardCharsets.UTF_8));

                showSuccess("Text payslip saved successfully to:\n" + fileToSave.getAbsolutePath());

            } catch (Exception e) {
                showError("Error saving text file: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            setStatus("Text save cancelled");
        }
    }

    // Status and message helper methods
    private void setStatus(String message) {
        statusLabel.setText(message);
        statusLabel.repaint();
    }

    private void showSuccess(String message) {
        setStatus("Success: " + message);
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        setStatus("Error: " + message);
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String message) {
        setStatus("Warning: " + message);
        JOptionPane.showMessageDialog(this, message, "Warning", JOptionPane.WARNING_MESSAGE);
    }

}