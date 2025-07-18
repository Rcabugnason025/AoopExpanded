package model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

// Abstract base class demonstrating ABSTRACTION
public abstract class Employee {
    // Protected fields for inheritance
    protected int employeeId;
    protected String firstName;
    protected String lastName;
    protected String position;
    protected String department;
    protected double basicSalary;
    protected Date dateHired;
    protected String status;
    
    // Constructor
    public Employee(int employeeId, String firstName, String lastName, String position, String department, double basicSalary) {
        this.employeeId = employeeId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.position = position;
        this.department = department;
        this.basicSalary = basicSalary;
        this.dateHired = new Date();
        this.status = "ACTIVE";
    }
    
    // Abstract methods - MUST be implemented by subclasses
    public abstract double calculateGrossPay(int daysWorked, double overtimeHours);
    public abstract double calculateDeductions();
    public abstract double calculateAllowances();
    public abstract boolean isEligibleForBenefits();
    
    // Template method pattern - defines algorithm structure
    public final double calculateNetPay(int daysWorked, double overtimeHours) {
        double grossPay = calculateGrossPay(daysWorked, overtimeHours);
        double deductions = calculateDeductions();
        double allowances = calculateAllowances();
        return grossPay + allowances - deductions;
    }
    
    // Polymorphic method - can be overridden
    public String getEmployeeType() {
        return "General Employee";
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    // Getters and setters with proper encapsulation
    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }
    
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    
    public double getBasicSalary() { return basicSalary; }
    public void setBasicSalary(double basicSalary) { this.basicSalary = basicSalary; }
    
    public Date getDateHired() { return dateHired; }
    public void setDateHired(Date dateHired) { this.dateHired = dateHired; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

	public int getId() {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public String getImmediateSupervisor() {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public Object getBirthday() {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public void setImmediateSupervisor(String string) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public void setPagibigNumber(String string) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public void setTinNumber(String string) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public void setPhilhealthNumber(String string) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public void setSssNumber(String string) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public void setPhoneNumber(String string) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public void setAddress(String string) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public void setBirthday(LocalDate toLocalDate) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public void setId(int aInt) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public void setUpdatedAt(LocalDateTime toLocalDateTime) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public void setCreatedAt(LocalDateTime toLocalDateTime) {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public Object getAddress() {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public Object getPhoneNumber() {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public Object getSssNumber() {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public Object getPhilhealthNumber() {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public Object getTinNumber() {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}

	public Object getPagibigNumber() {
		throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
	}
}