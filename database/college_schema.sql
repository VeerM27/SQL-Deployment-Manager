CREATE DATABASE IF NOT EXISTS college;
USE college;

DROP TABLE IF EXISTS students;

CREATE TABLE students (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL
);

INSERT INTO students (name, age) VALUES
('Ali', 20),
('Rahul', 22),
('Sara', 19),
('Priya', 21);
