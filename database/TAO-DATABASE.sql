-- Chay bang tai khoan quan tri MySQL trong Workbench.
-- Co so du lieu moi, khong dung database cua du an cu.
CREATE DATABASE IF NOT EXISTS student_manager_v2
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Thay CHOOSE_A_NEW_PASSWORD truoc khi chay hai lenh sau.
CREATE USER IF NOT EXISTS 'student_v2'@'localhost' IDENTIFIED BY 'CHOOSE_A_NEW_PASSWORD';
GRANT ALL PRIVILEGES ON student_manager_v2.* TO 'student_v2'@'localhost';
-- Khong can import cac bang: Flyway tu tao bang khi backend khoi dong.
-- CREATE USER IF NOT EXISTS khong doi mat khau tai khoan da ton tai.
