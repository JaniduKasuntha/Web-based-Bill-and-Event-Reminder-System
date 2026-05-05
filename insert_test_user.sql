-- SQL Script to create a test user
-- Database: university_reminder

USE university_reminder;

-- 1. Insert a test university
INSERT IGNORE INTO university (id, name) VALUES (1, 'Test University');

-- 2. Insert a test user
-- Username: admin
-- Password: password123 (Since you have NoOpPasswordEncoder, it's stored in plain text)
-- Role: ADMIN
INSERT IGNORE INTO users (id, username, password, email, university_id) 
VALUES (1, 'admin', 'password123', 'admin@example.com', 1);

-- 3. Insert roles
INSERT IGNORE INTO user_roles (user_id, roles) VALUES (1, 'ROLE_ADMIN');
