@@ .. @@
 import model.Employee;
 import model.Payroll;
 import service.JasperPayslipService;
 import service.JasperPayslipService.ExportFormat;
+import service.JasperPayslipService.JasperReportException;
 
 import javax.swing.*;
 import java.awt.*;
@@ .. @@
     /**
      * NEW: Save payslip as PDF using JasperReports with MotorPH template
      */
     private void saveAsPDF() {
         if (!jasperReportsAvailable) {
-            showError("JasperReports not available. Please check your classpath and ensure JasperReports JAR files are included.");
+            showError("JasperReports not available. Please ensure MotorPH template and JasperReports JAR files are included.");
             return;
         }
 
-        setStatus("Generating PDF...");
+        setStatus("Generating PDF using MotorPH template...");
 
         try {
             JFileChooser fileChooser = new JFileChooser();
-            fileChooser.setDialogTitle("Save Payslip as PDF");
+            fileChooser.setDialogTitle("Save MotorPH Payslip as PDF");
 
-            String filename = String.format("Payslip_%s_%s.pdf",
+            String filename = String.format("MotorPH_Payslip_%s_%s.pdf",
                     employee.getLastName().replaceAll("\\s+", ""),
                     payroll.getStartDateAsLocalDate().format(DateTimeFormatter.ofPattern("yyyy_MM")));
 
@@ -286,7 +287,7 @@
                     fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
                 }
 
-                // Generate PDF using JasperReports
+                // Generate PDF using JasperReports with MotorPH template
                 byte[] pdfData = jasperService.generatePayslipReport(employee, payroll, ExportFormat.PDF);
 
                 // Save to file
@@ -294,7 +295,7 @@
                     fos.write(pdfData);
                 }
 
-                showSuccess("PDF payslip saved successfully to:\n" + fileToSave.getAbsolutePath());
+                showSuccess("MotorPH PDF payslip saved successfully to:\n" + fileToSave.getAbsolutePath());
 
                 // Ask if user wants to open the PDF
                 int openFile = JOptionPane.showConfirmDialog(this,
@@ .. @@
             } else {
                 setStatus("PDF save cancelled");
             }
 
-        } catch (Exception e) {
-            showError("Error generating PDF: " + e.getMessage());
+        } catch (JasperReportException e) {
+            showError("Error generating MotorPH PDF: " + e.getMessage());
+            e.printStackTrace();
+        } catch (Exception e) {
+            showError("Error generating MotorPH PDF: " + e.getMessage());
             e.printStackTrace();
         }
     }
@@ .. @@
     /**
      * NEW: Save payslip as Excel using JasperReports with MotorPH template
      */
     private void saveAsExcel() {
         if (!jasperReportsAvailable) {
-            showError("JasperReports not available. Please check your classpath and ensure JasperReports JAR files are included.");
+            showError("JasperReports not available. Please ensure MotorPH template and JasperReports JAR files are included.");
             return;
         }
 
-        setStatus("Generating Excel file...");
+        setStatus("Generating Excel file using MotorPH template...");
 
         try {
             JFileChooser fileChooser = new JFileChooser();
-            fileChooser.setDialogTitle("Save Payslip as Excel");
+            fileChooser.setDialogTitle("Save MotorPH Payslip as Excel");
 
-            String filename = String.format("Payslip_%s_%s.xlsx",
+            String filename = String.format("MotorPH_Payslip_%s_%s.xlsx",
                     employee.getLastName().replaceAll("\\s+", ""),
                     payroll.getStartDateAsLocalDate().format(DateTimeFormatter.ofPattern("yyyy_MM")));
 
@@ -334,7 +335,7 @@
                     fileToSave = new File(fileToSave.getAbsolutePath() + ".xlsx");
                 }
 
-                // Generate Excel using JasperReports
+                // Generate Excel using JasperReports with MotorPH template
                 byte[] excelData = jasperService.generatePayslipReport(employee, payroll, ExportFormat.EXCEL);
 
                 // Save to file
@@ -342,7 +343,7 @@
                     fos.write(excelData);
                 }
 
-                showSuccess("Excel payslip saved successfully to:\n" + fileToSave.getAbsolutePath());
+                showSuccess("MotorPH Excel payslip saved successfully to:\n" + fileToSave.getAbsolutePath());
 
                 // Ask if user wants to open the Excel file
                 int openFile = JOptionPane.showConfirmDialog(this,
@@ .. @@
             } else {
                 setStatus("Excel save cancelled");
             }
 
-        } catch (Exception e) {
-            showError("Error generating Excel file: " + e.getMessage());
+        } catch (JasperReportException e) {
+            showError("Error generating MotorPH Excel file: " + e.getMessage());
+            e.printStackTrace();
+        } catch (Exception e) {
+            showError("Error generating MotorPH Excel file: " + e.getMessage());
             e.printStackTrace();
         }
     }
@@ .. @@
         JFileChooser fileChooser = new JFileChooser();
-        fileChooser.setDialogTitle("Save Payslip as Text");
+        fileChooser.setDialogTitle("Save MotorPH Payslip as Text");
 
-        String filename = String.format("Payslip_%s_%s.txt",
+        String filename = String.format("MotorPH_Payslip_%s_%s.txt",
                 employee.getLastName().replaceAll("\\s+", ""),
                 payroll.getStartDateAsLocalDate().format(DateTimeFormatter.ofPattern("yyyy_MM")));
 
@@ -380,7 +381,7 @@
                 java.nio.file.Files.write(fileToSave.toPath(),
                         payslipTextArea.getText().getBytes(java.nio.charset.StandardCharsets.UTF_8));
 
-                showSuccess("Text payslip saved successfully to:\n" + fileToSave.getAbsolutePath());
+                showSuccess("MotorPH text payslip saved successfully to:\n" + fileToSave.getAbsolutePath());
 
             } catch (Exception e) {
                 showError("Error saving text file: " + e.getMessage());
@@ .. @@
     public void showVersionInfo() {
         StringBuilder info = new StringBuilder();
-        info.append("PayrollDetailsDialog Enhanced Version\n");
+        info.append("MotorPH PayrollDetailsDialog Enhanced Version\n");
         info.append("=====================================\n");
+        info.append("MotorPH Template: ✅ Integrated\n");
         info.append("JasperReports Available: ").append(jasperReportsAvailable ? "✅ Yes" : "❌ No").append("\n");
         info.append("PDF Export: ").append(jasperReportsAvailable ? "✅ Available" : "❌ Unavailable").append("\n");
         info.append("Excel Export: ").append(jasperReportsAvailable ? "✅ Available" : "❌ Unavailable").append("\n");
@@ .. @@
         if (!jasperReportsAvailable) {
             info.append("\n⚠️ To enable PDF/Excel export:\n");
             info.append("1. Add JasperReports JAR files to classpath\n");
-            info.append("2. Ensure motorph_payslip.jrxml is in resources/reports/\n");
+            info.append("2. Ensure motorph_payslip.jrxml MotorPH template is in resources/\n");
             info.append("3. Restart the application\n");
         }
 
-        JOptionPane.showMessageDialog(this, info.toString(), "Version Information",
+        JOptionPane.showMessageDialog(this, info.toString(), "MotorPH Version Information",
                 JOptionPane.INFORMATION_MESSAGE);
     }
 }