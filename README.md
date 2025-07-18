@@ .. @@
 # MotorPH Payroll System

 ## 🚀 Advanced Object-Oriented Programming Project
 **Course:** MO-IT113 - Advanced Object-Oriented Programming  
 **Institution:** Mapúa Malayan Digital College  
 **Academic Year:** 2024-2025

 ## 👥 Development Team - Group 6 (Section A2101)
 - **Baguio, Gilliane Rose**
 - **Celocia, Jannine Claire**  
 - **Cabugnason, Rick**
 - **Decastillo, Pamela Loraine**
 - **Manalaysay, Rejoice**

 ## 📋 Project Overview

-The MotorPH Payroll System is a comprehensive Java-based desktop application designed to manage employee payroll, attendance, and HR operations for MotorPH company. The system has been enhanced from a CSV-based storage solution to a robust MySQL database-driven application with advanced features and improved scalability.
+The MotorPH Payroll System is a comprehensive Java-based desktop application designed to manage employee payroll, attendance, and HR operations for MotorPH company. The system demonstrates advanced Object-Oriented Programming principles including Inheritance, Abstraction, Polymorphism, and Encapsulation, with a fully normalized 3NF database design using Views and Stored Procedures.

+## 🎯 OOP Implementation Highlights
+
+### Four Pillars of OOP
+- **Encapsulation**: Private fields with controlled access through getters/setters with validation
+- **Inheritance**: Employee hierarchy with RegularEmployee and ContractualEmployee extending abstract Employee class
+- **Polymorphism**: Runtime method overriding and Factory pattern for employee creation
+- **Abstraction**: Abstract Employee class with template methods and abstract method contracts
+
+### Design Patterns
+- **Factory Pattern**: EmployeeFactory for creating different employee types
+- **Template Method Pattern**: calculateNetPay() method defining algorithm structure
+- **DAO Pattern**: Layered architecture with proper separation of concerns
+
 ### 🎯 Key Features

 #### Employee Management
-- **Complete Employee Records**: Personal information, employment status, position details
+- **Complete Employee Records**: Personal information, employment status, position details using OOP inheritance
 - **Role-Based Access Control**: Separate dashboards for employees and HR personnel
 - **Employee Search & Filtering**: Advanced search capabilities with multiple criteria
 - **Password Management**: Secure credential handling with change password functionality
+- **Polymorphic Employee Types**: Regular and Contractual employees with different behaviors

 #### Payroll Processing
-- **Automated Payroll Calculation**: Comprehensive salary computation including:
+- **Polymorphic Payroll Calculation**: Different calculation methods for different employee types:
   - Basic salary calculation based on days worked
   - Overtime pay with 125% rate multiplier (Regular employees only)
   - Government contributions (SSS, PhilHealth, Pag-IBIG)
   - Tax calculations using TRAIN law brackets
   - Allowances (Rice subsidy, Phone allowance, Clothing allowance)
   - Time-based deductions (Late, Undertime, Unpaid leave)
-- **Detailed Payslips**: Professional payslip generation with company branding
-- **Print & Export**: Print payslips or save for records
+- **JasperReports Integration**: Professional MotorPH template-based payslip generation
+- **Multiple Export Formats**: PDF, Excel, and Text exports using JasperReports

 #### Attendance Tracking
 - **Digital Time Logging**: Log in/log out time tracking
@@ .. @@
 #### Reporting & Analytics
 - **Monthly Payroll Reports**: Comprehensive payroll summaries
-- **Export Options**: CSV and HTML export formats
+- **JasperReports Integration**: Professional PDF reports using MotorPH templates
+- **Export Options**: PDF, Excel, CSV and HTML export formats
 - **Detailed Attendance Reports**: Attendance summary of all the employees
 - **Government Contributions Reports**: SSS, PhilHealth, and Pag-IBIG reports

+## 🗄️ Database Design (3NF Compliance)
+
+### Normalized Database Structure
+- **Third Normal Form (3NF)**: Eliminates transitive dependencies
+- **positions table**: Stores position-related data separately from employees
+- **Self-referencing foreign keys**: supervisor_id references employees.employee_id
+- **Views**: v_employee_details for simplified querying
+- **Stored Procedures**: sp_add_new_employee, sp_generate_payslip_data
+
+### Database Features
+- **Views for Simplified Queries**: Pre-joined employee data with position details
+- **Stored Procedures**: Centralized business logic for complex operations
+- **Referential Integrity**: Proper foreign key constraints and cascading deletes
+- **Indexing**: Performance optimization for frequently queried columns
+
 ## 🛠️ Installation & Setup

 ### Prerequisites
 - **Java Development Kit (JDK) 8 or higher**
 - **MySQL Server 8.0+**
 - **MySQL Workbench** (recommended)
 - **MySQL Connector/J** (JDBC Driver)
+- **JasperReports Library** (for PDF generation)

 ### Database Setup

 1. **Download the SQL Setup Script**
    ```
    File: aoopdatabase_payroll.sql
    Location: src/util/aoopdatabase_payroll.sql
    ```

 2. **Configure MySQL Connection**
    - **Host**: localhost
    - **Port**: 3306
    - **Username**: root
    - **Password**: admin
    - **Database**: aoopdatabase_payroll

 3. **Execute Setup Script**
    - Open MySQL Workbench
    - Connect to your MySQL server
    - Open and execute `aoopdatabase_payroll.sql`
-   - Verify successful creation of database and tables
+   - Verify successful creation of database, tables, views, and stored procedures

 4. **Verify Installation**
    ```sql
    USE aoopdatabase_payroll;
    SELECT COUNT(*) FROM employees; -- Should return 34
    SELECT COUNT(*) FROM credentials; -- Should return 34
    SELECT COUNT(*) FROM attendance; -- Should return 34+
+   SHOW TABLES; -- Should show positions, employees, credentials, attendance, leave_requests
+   SHOW PROCEDURE STATUS WHERE Db = 'aoopdatabase_payroll'; -- Should show stored procedures
    ```

 ### Application Setup

 1. **Clone/Download Project**
    ```bash
    git clone <repository-url>
    cd motorph-payroll-system
    ```

-2. **Add MySQL Connector**
+2. **Add Required Libraries**
    - Download `mysql-connector-java.jar`
+   - Download JasperReports JAR files
    - Add to project classpath
    - Ensure it's included in build path

 3. **Configure Database Connection**
    - Update `src/util/DBConnection.java` if needed
    - Default configuration:
      ```java
      HOST = "localhost"
      PORT = "3306"
      DATABASE_NAME = "aoopdatabase_payroll"
      USER = "root"
      PASSWORD = "admin"
      ```

 4. **Compile and Run**
    ```bash
    # Compile
-   javac -cp ".:mysql-connector-java.jar" src/**/*.java
+   javac -cp ".:mysql-connector-java.jar:jasperreports-*.jar" src/**/*.java
    
    # Run
-   java -cp ".:mysql-connector-java.jar:src" ui.MainApplication
+   java -cp ".:mysql-connector-java.jar:jasperreports-*.jar:src" ui.MainApplication
    ```

+## 🧪 Testing Framework
+
+### JUnit 5 Test Suite
+- **Comprehensive Unit Tests**: Testing all OOP principles and business logic
+- **Assert Functions**: Proper use of JUnit assertions for validation
+- **Test Categories**:
+  - Employee Model Tests (Inheritance & Polymorphism)
+  - Payroll Calculation Tests (Business Logic)
+  - OOP Principles Tests (Four Pillars of OOP)
+  - Factory Pattern Tests (Design Patterns)
+
+### Running Tests
+```bash
+# Run all tests
+java -cp ".:junit-*.jar:src" test.TestRunner
+
+# View detailed test results with success rates and OOP principle coverage
+```
+
 ## 🔐 Default Login Credentials

 The system comes with pre-configured test accounts:

 ### Employee Accounts
 - **Employee IDs**: 10001 to 10034
 - **Default Password**: `password1234`

 ### Sample HR/Management Accounts
 - **Employee ID**: 10001 (CEO)
 - **Employee ID**: 10002 (COO)  
 - **Employee ID**: 10003 (CFO)
 - **Employee ID**: 10006 (HR Manager)
 - **Password**: `password1234`

+## 📊 Technical Architecture
+
+### OOP Design Patterns Used
+1. **Abstract Factory Pattern**: Employee creation with different types
+2. **Template Method Pattern**: Payroll calculation algorithm
+3. **Data Access Object (DAO) Pattern**: Database abstraction layer
+4. **Model-View-Controller (MVC)**: Separation of concerns
+
+### Database Integration
+- **3NF Normalized Design**: Proper relational database structure
+- **Views**: Simplified data access for complex joins
+- **Stored Procedures**: Centralized business logic
+- **Connection Pooling**: Efficient database resource management
+
+### Report Generation
+- **JasperReports Integration**: Professional PDF generation
+- **MotorPH Template**: Company-branded payslip design
+- **Multiple Export Formats**: PDF, Excel, CSV, HTML
 
 ## 🚀 Future Enhancements

 ### Planned Features
 - **Enhanced Reporting System**: 
   - Employee performance dashboards
   - Custom report builder
 - **Web-based Interface**: Browser-accessible application
 - **Biometric Integration**: Fingerprint/face recognition for attendance
 - **Mobile Application**: Mobile app for employee self-service
 - **Advanced Reporting**: Business intelligence dashboards
 - **API Integration**: External system integrations
 - **Cloud Deployment**: Cloud-based hosting options

+## 🎓 Academic Compliance
+
+This project demonstrates mastery of:
+- ✅ **Object-Oriented Programming**: All four pillars implemented
+- ✅ **Database Design**: 3NF normalization with views and stored procedures  
+- ✅ **Design Patterns**: Factory, Template Method, DAO patterns
+- ✅ **Unit Testing**: Comprehensive JUnit 5 test suite with assertions
+- ✅ **Report Generation**: JasperReports with MotorPH template
+- ✅ **GUI Development**: Swing-based user interface with proper usability
+- ✅ **Layered Architecture**: Proper separation of concerns with packages

 ## 📄 License

 Academic use only. Developed by Group 6, Mapúa Malayan Digital College.

 **© 2025 MotorPH Payroll System**