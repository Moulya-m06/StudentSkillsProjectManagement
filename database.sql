DROP DATABASE IF EXISTS student_skills_pm;
CREATE DATABASE student_skills_pm;
USE student_skills_pm;

CREATE TABLE LOGIN (
    user_id VARCHAR(20) PRIMARY KEY,
    username VARCHAR(60) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role ENUM('STUDENT','ADMIN') NOT NULL,
    status ENUM('ACTIVE','BLOCKED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ADMIN (
    admin_id VARCHAR(20) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(15),
    FOREIGN KEY (admin_id) REFERENCES LOGIN(user_id) ON DELETE CASCADE
);

CREATE TABLE STUDENT (
    student_id VARCHAR(20) PRIMARY KEY,
    usn VARCHAR(30) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(15),
    email VARCHAR(100) NOT NULL UNIQUE,
    department VARCHAR(60),
    year INT CHECK (year BETWEEN 1 AND 4),
    address TEXT,
    profile_photo VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE STUDENT_DETAILS (
    detail_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL UNIQUE,
    summary TEXT,
    cgpa DECIMAL(4,2) DEFAULT 0,
    career_goal VARCHAR(255),
    FOREIGN KEY (student_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE
);

CREATE TABLE SKILLS (
    skill_id INT AUTO_INCREMENT PRIMARY KEY,
    skill_name VARCHAR(80) NOT NULL UNIQUE,
    category ENUM('Technical','Soft Skill','Domain') DEFAULT 'Technical'
);

CREATE TABLE STUDENT_SKILLS (
    student_skill_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    skill_id INT NOT NULL,
    skill_level ENUM('Beginner','Intermediate','Advanced','Expert') DEFAULT 'Beginner',
    years_experience DECIMAL(4,1) DEFAULT 0,
    UNIQUE(student_id, skill_id),
    FOREIGN KEY (student_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE,
    FOREIGN KEY (skill_id) REFERENCES SKILLS(skill_id) ON DELETE CASCADE
);

CREATE TABLE PROJECT (
    project_id INT AUTO_INCREMENT PRIMARY KEY,
    project_name VARCHAR(120) NOT NULL,
    domain VARCHAR(80),
    technologies_used VARCHAR(255),
    team_size INT DEFAULT 1,
    deadline DATE,
    status ENUM('PLANNING','IN_PROGRESS','COMPLETED','ON_HOLD') DEFAULT 'PLANNING',
    description TEXT,
    completion_percentage INT DEFAULT 0,
    approval_status ENUM('PENDING','APPROVED','REJECTED') DEFAULT 'PENDING',
    created_by VARCHAR(20) NOT NULL,
    team_id INT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES STUDENT(student_id) ON DELETE CASCADE
);

CREATE TABLE TEAM (
    team_id INT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(100) NOT NULL,
    project_id INT NOT NULL,
    created_by VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES PROJECT(project_id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES STUDENT(student_id) ON DELETE CASCADE
);

ALTER TABLE PROJECT
ADD CONSTRAINT fk_project_team
FOREIGN KEY (team_id) REFERENCES TEAM(team_id) ON DELETE SET NULL;

CREATE TABLE TEAM_MEMBERS (
    team_id INT NOT NULL,
    student_id VARCHAR(20) NOT NULL,
    role VARCHAR(60) DEFAULT 'Member',
    contribution VARCHAR(255),
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (team_id, student_id),
    FOREIGN KEY (team_id) REFERENCES TEAM(team_id) ON DELETE CASCADE,
    FOREIGN KEY (student_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE
);

CREATE TABLE PROJECT_TASKS (
    task_id INT AUTO_INCREMENT PRIMARY KEY,
    project_id INT NOT NULL,
    task_title VARCHAR(120) NOT NULL,
    assigned_to VARCHAR(20),
    due_date DATE,
    status ENUM('TODO','IN_PROGRESS','COMPLETED') DEFAULT 'TODO',
    completed_on DATE,
    FOREIGN KEY (project_id) REFERENCES PROJECT(project_id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_to) REFERENCES STUDENT(student_id) ON DELETE SET NULL
);

CREATE TABLE CERTIFICATIONS (
    certification_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    title VARCHAR(120) NOT NULL,
    issuer VARCHAR(120),
    issue_date DATE,
    FOREIGN KEY (student_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE
);

CREATE TABLE ACHIEVEMENTS (
    achievement_id INT AUTO_INCREMENT PRIMARY KEY,
    student_id VARCHAR(20) NOT NULL,
    title VARCHAR(120) NOT NULL,
    description TEXT,
    achievement_date DATE,
    FOREIGN KEY (student_id) REFERENCES STUDENT(student_id) ON DELETE CASCADE
);

CREATE TABLE MESSAGES (
    message_id INT AUTO_INCREMENT PRIMARY KEY,
    sender_id VARCHAR(20) NOT NULL,
    receiver_id VARCHAR(20) NOT NULL,
    subject VARCHAR(120),
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DELIMITER $$

CREATE TRIGGER trg_task_insert_progress
AFTER INSERT ON PROJECT_TASKS
FOR EACH ROW
BEGIN
    UPDATE PROJECT
    SET completion_percentage = (
        SELECT ROUND(100 * SUM(status = 'COMPLETED') / COUNT(*))
        FROM PROJECT_TASKS
        WHERE project_id = NEW.project_id
    ),
    status = CASE
        WHEN (SELECT COUNT(*) FROM PROJECT_TASKS WHERE project_id = NEW.project_id) > 0
         AND (SELECT COUNT(*) FROM PROJECT_TASKS WHERE project_id = NEW.project_id AND status <> 'COMPLETED') = 0
        THEN 'COMPLETED'
        ELSE status
    END
    WHERE project_id = NEW.project_id;
END$$

CREATE TRIGGER trg_task_update_progress
AFTER UPDATE ON PROJECT_TASKS
FOR EACH ROW
BEGIN
    UPDATE PROJECT
    SET completion_percentage = (
        SELECT ROUND(100 * SUM(status = 'COMPLETED') / COUNT(*))
        FROM PROJECT_TASKS
        WHERE project_id = NEW.project_id
    ),
    status = CASE
        WHEN (SELECT COUNT(*) FROM PROJECT_TASKS WHERE project_id = NEW.project_id) > 0
         AND (SELECT COUNT(*) FROM PROJECT_TASKS WHERE project_id = NEW.project_id AND status <> 'COMPLETED') = 0
        THEN 'COMPLETED'
        WHEN status = 'COMPLETED' THEN 'IN_PROGRESS'
        ELSE status
    END
    WHERE project_id = NEW.project_id;
END$$

CREATE PROCEDURE GetProjectsByStudent(IN in_student_id VARCHAR(20))
BEGIN
    SELECT DISTINCT p.project_id, p.project_name, p.domain, p.technologies_used,
           p.deadline, p.status, p.completion_percentage, p.approval_status
    FROM PROJECT p
    LEFT JOIN TEAM t ON p.team_id = t.team_id
    LEFT JOIN TEAM_MEMBERS tm ON t.team_id = tm.team_id
    WHERE p.created_by = in_student_id OR tm.student_id = in_student_id
    ORDER BY p.deadline;
END$$

DELIMITER ;

INSERT INTO LOGIN(user_id, username, password_hash, role, status)
VALUES ('S1001','1RV23CS001','PBKDF2$replace_with_app_generated_hash','STUDENT','ACTIVE');

INSERT INTO STUDENT(student_id, usn, full_name, phone, email, department, year, address)
VALUES ('S1001','1RV23CS001','Aarav Sharma','9876543210','aarav@example.com','Computer Science',3,'Bengaluru');

INSERT INTO STUDENT_DETAILS(student_id, summary, cgpa, career_goal)
VALUES ('S1001','Java and DBMS enthusiast',8.70,'Become a full-stack Java engineer');

INSERT INTO SKILLS(skill_name, category) VALUES
('Java','Technical'), ('Python','Technical'), ('Web Development','Technical'),
('UI/UX Design','Domain'), ('AI/ML','Domain'), ('Data Science','Domain'),
('Cloud Computing','Technical'), ('DBMS','Technical'), ('Cyber Security','Domain'),
('Communication','Soft Skill'), ('Leadership','Soft Skill');

INSERT INTO STUDENT_SKILLS(student_id, skill_id, skill_level, years_experience)
VALUES ('S1001',1,'Advanced',2.0), ('S1001',8,'Intermediate',1.5);

INSERT INTO PROJECT(project_name, domain, technologies_used, team_size, deadline, status, description, completion_percentage, approval_status, created_by)
VALUES ('Student Skills and Project Management System','DBMS','Java Swing, MySQL, JDBC',4,'2026-06-30','IN_PROGRESS','Mini project to manage skills, teams, and projects',0,'APPROVED','S1001');

INSERT INTO TEAM(team_name, project_id, created_by) VALUES ('DBMS Innovators',1,'S1001');
UPDATE PROJECT SET team_id = 1 WHERE project_id = 1;
INSERT INTO TEAM_MEMBERS(team_id, student_id, role, contribution) VALUES (1,'S1001','Leader','Database design and Java UI');
INSERT INTO PROJECT_TASKS(project_id, task_title, assigned_to, due_date, status) VALUES
(1,'Design ER schema','S1001','2026-06-05','COMPLETED'),
(1,'Build Swing dashboard','S1001','2026-06-15','IN_PROGRESS');
INSERT INTO CERTIFICATIONS(student_id, title, issuer, issue_date) VALUES ('S1001','Java Programming','Oracle Academy','2026-01-12');
INSERT INTO ACHIEVEMENTS(student_id, title, description, achievement_date) VALUES ('S1001','DBMS Mini Project Finalist','Selected for department demo','2026-05-10');
