SQL Deployment Manager [SID - 2437213]
SQL Deployment Manager is a Spring Boot web application that validates, analyses, compares, and safely deploys SQL scripts into a MySQL environment. 
It provides a guided workflow consisting of SQL validation, schema comparison, row-impact assessment, backup creation, approval, deployment, and full history 
tracking.

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
        Edit src/main/resources/application.properties:
            spring.datasource.url=jdbc:mysql://localhost:3306/sqldeploymentdb
            spring.datasource.username=sqlmanager
            spring.datasource.password=password123
            spring.jpa.hibernate.ddl-auto=update

            backup.mysqldump.path=/usr/local/mysql/bin/mysqldump   # adjust if different
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
        Edit src/main/resources/application.properties:
        spring.datasource.url=jdbc:mysql://localhost:3306/sqldeploymentdb
        spring.datasource.username=sqlmanager
        spring.datasource.password=password123
        spring.jpa.hibernate.ddl-auto=update

        backup.mysqldump.path=C:/Program Files/MySQL/MySQL Server 8.0/bin/mysqldump.exe
        Make sure the mysqldump path matches your installation.
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

End of README