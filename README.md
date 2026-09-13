# Student Skills and Project Management System

A complete BTech DBMS mini project built with Java Swing, Java, MySQL, and JDBC. The app includes student registration, secure login, skill tracking, project and task management, teams, achievements, certifications, admin analytics, and mentor/admin messaging.

## Tech Stack

- Frontend: Java Swing only
- Backend logic: Java only
- Database: MySQL
- Connectivity: JDBC
- Editor: VS Code

## Project Structure

```text
StudentSkillsProjectManagement/
|-- src/
|-- lib/
|   `-- mysql-connector-j.jar
|-- images/
|-- sql/
|   `-- database.sql
`-- .vscode/
```

## Database Setup

1. Open MySQL Workbench or MySQL CLI.
2. Run `sql/database.sql`.
3. Create a `db.properties` file in the project folder and add your own MySQL username and password.
4. Download MySQL Connector/J and place the jar in `lib/mysql-connector-j.jar`.

Default local MySQL settings:

```properties
db.user=
db.password=
```
> **Note:** `db.properties` is not included in this repository because it contains database credentials. Each user should create their own `db.properties` file using their local MySQL credentials.

If your MySQL root account has a password, enter it after `db.password=`.

Example:

```properties
db.user=root
db.password=your_mysql_password
```

If the app cannot connect, it opens a database setup window where you can enter the same MySQL username and password used in MySQL Workbench. If the database does not exist, click `Create Database` to import `sql/database.sql` automatically.

The Java app automatically creates a default admin login if the tables exist:

- Username: `admin`
- Password: `admin123`

## VS Code Setup

1. Install the VS Code Extension Pack for Java.
2. Open the `StudentSkillsProjectManagement` folder in VS Code.
3. Put `mysql-connector-j.jar` inside `lib`.
4. Run `Main.java` from VS Code, or use the included launch configuration.

## Compile and Run Manually

```powershell
javac -d out src/*.java
java -cp "out;lib/mysql-connector-j.jar" Main
```

## Demo Flow

1. Import the SQL script.
2. Start the Java app.
3. Login as `admin/admin123` to view analytics and approve projects.
4. Register a new student from the login screen.
5. Login with the student's USN and password.
6. Add skills, create projects, create teams, add tasks, mark tasks complete, add achievements/certifications, and send admin messages.

## DBMS Requirements Covered

- Primary keys and foreign keys
- SQL joins in dashboards and reports
- Trigger to update project completion percentage when task status changes
- Stored procedure `GetProjectsByStudent(student_id)`
- CRUD operations across students, skills, projects, teams, tasks, achievements, certifications, and messages

## Important Note

The sample student login row in `database.sql` contains a placeholder password hash because the app uses secure PBKDF2 hashes generated in Java. Use the registration form to create working student logins, or login as the auto-created admin.
