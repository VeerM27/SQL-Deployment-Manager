SQL Deployment Manager [SID - 2437213]
SQL Deployment Manager is a Spring Boot web application that validates, analyses, compares, and safely deploys SQL scripts into a MySQL environment. 
It provides a guided workflow consisting of SQL validation, schema comparison, row-impact assessment, backup creation, approval, deployment, and full history 
tracking.

!! Encounter any errors? Check the bottom part for common errors

1. System Requirements
    * Operating System
      -  macOS (Intel or Apple Silicon)
      -  Windows 10/11

    * Software Requirements
      -  Java 17 or later
      -  Maven 3.8+
      -  MySQL Server
      -  MySQL Workbench
      -  mysqldump available in PATH (used for database backups)
      -  Web browser (Chrome, Safari, Edge, Firefox)

2. Setup Instructions (macOS)
    Step 1: Install Homebrew (if not installed)
        /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
            Then on Apple Silicon macOS Run:
                echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
                eval "$(/opt/homebrew/bin/brew shellenv)"
            On Intel macOS Run:
                echo 'eval "$(/usr/local/bin/brew shellenv)"' >> ~/.zprofile
                eval "$(/usr/local/bin/brew shellenv)"

    Step 2: Install Java, Maven, and MySQL
        brew install openjdk@17
        brew install maven
        brew install mysql
    Add Java to your PATH (if prompted by Homebrew):
        export PATH="/usr/local/opt/openjdk@17/bin:$PATH"
    Step 3: Start MySQL
        brew services start mysql
    Step 4: Create the database:
        In MySQL Shell run:
        CREATE DATABASE sqldeploymentdb;
        (Optional) Create a dedicated user:
            CREATE USER 'sqlmanager'@'localhost' IDENTIFIED BY 'password123';
            GRANT ALL PRIVILEGES ON sqldeploymentdb.* TO 'sqlmanager'@'localhost';
            FLUSH PRIVILEGES;

        If you get errors in the above step, Import /database/college_schema.sql through MySQL Workbench [Server --> Data Import]

    Step 5: Configure the application
        Set environment variables for database credentials (REQUIRED):
            export DB_NAME=sqldeploymentdb
            export DB_USER=sqlmanager
            export DB_PASSWORD=password123

        Alternatively, create a .env file (recommended):
            Copy .env.example to .env and fill in your credentials

        Note: For security, credentials are NO LONGER stored in application.properties
    Step 6: Build and run the application
        mvn clean install
        mvn spring-boot:run
    Once running, open:
        http://localhost:8080
    
3. Setup Instructions (Windows)
    Step 1: Install Java 17
        Download and install from Oracle or Amazon Corretto.
        Verify installation:
            java -version
    Step 2: Install Maven
        Download: https://maven.apache.org/download.cgi
        Unzip and add the bin folder to your system PATH.
        Verify installation:
            mvn -version
    Step 3: Install MySQL Server 8.x
        Download from: https://dev.mysql.com/downloads/mysql/
        During installation, set a root password.
    Step 4: Create the database
        Open MySQL Shell or MySQL Workbench:
            CREATE DATABASE sqldeploymentdb;
            (Optional) Create a dedicated user:
                CREATE USER 'sqlmanager'@'localhost' IDENTIFIED BY 'password123';
                GRANT ALL PRIVILEGES ON sqldeploymentdb.* TO 'sqlmanager'@'localhost';
                FLUSH PRIVILEGES;
    Step 5: Configure the application
        Set environment variables for database credentials (REQUIRED):
            set DB_NAME=sqldeploymentdb
            set DB_USER=sqlmanager
            set DB_PASSWORD=password123

        Or add them to System Environment Variables for persistence.

        Note: For security, credentials are NO LONGER stored in application.properties
    Step 6: Build and run the application
            mvn clean install
            mvn spring-boot:run
        Open the app in your browser:
            http://localhost:8080

4. How to Use the Application
Once the web application is running, the user interface guides you through the full SQL deployment workflow:
* Development Page
   - Upload a .sql file or paste raw SQL text.
   - The system validates the SQL syntax and displays a preview.
   - Run schema comparison or SQL analysis from this page.
* Schema Comparison
   - Shows the structure of affected tables.
   - Displays expected schema changes for DDL or DML queries.
   - Shows a diff between the current schema and predicted post-deployment schema.
* SQL Analysis
   - Provides optimisation tips.
   - Highlights potential dangers (e.g., missing WHERE clauses).
   - Detects invalid SQL or unsupported operations.
8 Workflow Execution
   - The Deployment Workflow consists of four enforced steps:
   - Validate SQL – acknowledges analysis results
   - Backup – automatically generates a mysqldump backup
   - Approve – simulates administrator approval
   - Deploy – executes the SQL on the live MySQL database
   - All steps must be completed in order.
* History Page
   - Displays all deployments, backups, approvals, and executed SQL.
   - Provides full auditability of past actions.

* Backup Behaviour
   - Backups are created using mysqldump.
   - Backup path:
   - ~/sql-deployment-backups/
   - Each backup is timestamped.
   - Backup occurs before deployment.

NOTE:
- If mysqldump is not installed, the system will display a warning and skip the backup step safely.

5. Known Limitations
   - No automatic rollback UI (manual restore required).
   - Multi-environment deployment not implemented.
   - Authentication not implemented.
   - MySQL client tools required for backup functionality.

6. Troubleshooting
   - If Schema Comparison shows no output:
   - Execute at least one CREATE, INSERT, or UPDATE statement first.
   - Then recheck Schema Comparison.

   - This application has been developed and tested using standard, widely adopted technologies (Java, Spring Boot, Maven, and MySQL). While minor    
     environment-specific configuration adjustments (such as database credentials, port availability, or MySQL installation paths) may be required when running the application on a different machine, the system has been designed to provide clear error feedback and guided workflow progression.
   - All core functionality, execution steps, and expected outputs are documented in this report and the accompanying README. A technically competent user 
     following these instructions should be able to successfully run, evaluate, and validate the application’s behaviour, even in the presence of minor configuration differences.

7. Common Setup Issues and How to Resolve Them
   - Database Connection Errors
        Symptom: Application fails to start or reports database access errors.
        Cause: Incorrect MySQL username, password, or database name.
        Resolution: Update the database configuration in application.properties to match the local MySQL setup and ensure the target database exists.
   - MySQL or mysqldump Not Found
        Symptom: Backup step fails during workflow execution.
        Cause: MySQL client tools not installed or not available on the system PATH.
        Resolution: Install MySQL client tools and verify that mysqldump can be executed from the command line before running the application.
   - Port Already in Use
        Symptom: Application fails to start with a “port already in use” message.
        Cause: Another service is already running on the default Spring Boot port.
        Resolution: Stop the conflicting service or change the server port in application.properties.
   - SQL Script Execution Differences
        Symptom: SQL behaves differently than shown in screenshots.
        Cause: The local database schema or data differs from the example dataset.
        Resolution: This is expected behaviour. The application correctly reflects the current state of the connected database and reports schema or row-level impact accordingly.

End of README