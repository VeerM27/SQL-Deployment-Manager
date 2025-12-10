SQL Deployment Manager - SID:2437213
======================

QUICK START (Marker Setup � 2 Minutes)

1. Install:
   - Java JDK 17+
   - MySQL Server
2. Create the database:
   mysql -u root -p < database/college_schema.sql
3. Set environment variables:
   In VSCode's terminal:
   macOS / Linux:
   export DB_USER=root
   export DB_PASS=your_password_here
   Windows (PowerShell):
   setx DB_USER root
   setx DB_PASS your_password_here

   In IntelliJ:
   Open Run --> Edit Configurations
   Select your spring boot run configuration
   Find Environment Variables
   Set : DB_USER=root;DB_PASS=your_mysql_password_here
   Apply, then Run.

4. Run the application:
   ./mvn spring-boot:run

5. Open in browser:
   http://localhost:8080

--------------------------------------------------

1. System Overview

The SQL Deployment Manager is a web-based Java Spring Boot application designed to manage the controlled execution of SQL scripts on a MySQL database. The system provides SQL validation, staged workflow approval, full database backup, deployment execution, schema impact analysis, row-level impact tracking, and history logging.

2. System Requirements

- Java JDK 17 or higher
- Apache Maven (optional � Maven Wrapper included)
- MySQL Server
- Web Browser (Chrome, Edge, or Firefox)
- macOS / Windows / Linux

3. How to Run the Application

1. Open the project in IntelliJ IDEA or VS Code.
2. Ensure MySQL is running.
3. Make sure the database credentials are set using environment variables in 'application.properties'.
4. Confirm the database using:

   mysql -u root -p < database/college_schema.sql

5. Run:
   ./mvnw spring-boot:run

6. Navigate to:
   http://localhost:8080

IMPORTANT:
Do NOT edit application.properties inside the "target" directory. Only edit:
src/main/resources/application.properties

--------------------------------------------------

4. How to Use the System

Step 1 � SQL Input  
Navigate to the Development page and paste an SQL script or upload a .sql file.

Step 2 � SQL Validation  
Navigate to the Analysis page to validate SQL and detect query type.

Step 3 � Backup  
Navigate to the Workflow page and select Backup to generate a full database dump.

Step 4 � Approval  
Press the Approve button to authorize deployment.

Step 5 � Deployment  
Press Deploy to execute the SQL on the MySQL college database.

Step 6 � Impact Review  
Navigate to Schema Comparison to review:
- Schema-level changes
- Row-level impact

Step 7 � History  
Navigate to the History page for full audit logging.

--------------------------------------------------

5. Backup Behaviour

- Backups are created using mysqldump.
- Backup path:
  ~/sql-deployment-backups/
- Each backup is timestamped.
- Backup occurs before deployment.

NOTE:
If mysqldump is not installed, the system will display a warning and skip the backup step safely.

--------------------------------------------------

6. Known Limitations

- No automatic rollback UI (manual restore required).
- Multi-environment deployment not implemented.
- Authentication not implemented.
- MySQL client tools required for backup functionality.

--------------------------------------------------

7. Troubleshooting

If Schema Comparison shows no output:
- Execute at least one CREATE, INSERT, or UPDATE statement first.
- Then recheck Schema Comparison.

--------------------------------------------------

End of README
