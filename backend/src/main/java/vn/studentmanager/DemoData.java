package vn.studentmanager;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "app.seed", havingValue = "true")
class DemoData implements ApplicationRunner {
  final Db db;
  final Accounts accounts;

  DemoData(Db d, Accounts a) {
    db = d;
    accounts = a;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (db.count("SELECT COUNT(*) FROM users") > 0) return;
    long admin = accounts.create("admin", "Admin@2026Demo", "ADMIN", false);
    long teacher = accounts.create("gv001", "Teacher@2026Demo", "TEACHER", false);
    long student = accounts.create("sv001", "Student@2026Demo", "STUDENT", false);
    long dept =
        db.insert("INSERT INTO departments(code,name) VALUES('CNTT','Công nghệ thông tin')");
    long dept2 = db.insert("INSERT INTO departments(code,name) VALUES('KT','Kinh tế')");
    long major =
        db.insert(
            "INSERT INTO majors(code,name,department_id) VALUES('KTPM','Kỹ thuật phần mềm',?)",
            dept);
    long cls =
        db.insert(
            "INSERT INTO classes(code,name,major_id) VALUES('K18KTPM01','K18 Kỹ thuật phần mềm"
                + " 01',?)",
            major);
    long tid =
        db.insert(
            "INSERT INTO teachers(code,full_name,email,phone,department_id,user_id)"
                + " VALUES('GV001','Nguyễn Minh Anh','minhanh@example.com','0901234567',?,?)",
            dept,
            teacher);
    long sid =
        db.insert(
            "INSERT INTO"
                + " students(code,full_name,email,phone,birth_date,gender,class_id,status,user_id)"
                + " VALUES('SV001','Nguyễn Văn"
                + " An','vanan@example.com','0912345678','2005-04-15','MALE',?,'STUDYING',?)",
            cls,
            student);
    db.exec(
        "INSERT INTO students(code,full_name,email,birth_date,gender,class_id,status)"
            + " VALUES('SV002','Trần Thu"
            + " Bình','thubinh@example.com','2005-08-20','FEMALE',?,'STUDYING')",
        cls);
    db.exec(
        "INSERT INTO students(code,full_name,email,birth_date,gender,class_id,status)"
            + " VALUES('SV003','Lê Hải Nam','hainam@example.com','2005-11-07','MALE',?,'STUDYING')",
        cls);
    long subject =
        db.insert("INSERT INTO subjects(code,name,credits) VALUES('IT101','Lập trình Java',3)");
    long subject2 =
        db.insert("INSERT INTO subjects(code,name,credits) VALUES('IT102','Cơ sở dữ liệu',3)");
    long subject3 =
        db.insert(
            "INSERT INTO subjects(code,name,credits) VALUES('IT103','Phân tích và thiết kế hệ"
                + " thống',3)");
    long term =
        db.insert(
            "INSERT INTO semesters(code,name,starts_on,ends_on) VALUES('2026-HK1','Học kỳ 1 ·"
                + " 2026–2027','2026-08-15','2027-01-15')");
    long old =
        db.insert(
            "INSERT INTO semesters(code,name,starts_on,ends_on) VALUES('2025-HK2','Học kỳ 2 ·"
                + " 2025–2026','2026-01-15','2026-06-15')");
    long section =
        db.insert(
            "INSERT INTO"
                + " sections(code,subject_id,semester_id,teacher_id,capacity,schedule_text,room,status)"
                + " VALUES('JAVA-2601',?,?,?,40,'Thứ 2 · Tiết 1–3','A1-302','OPEN')",
            subject,
            term,
            tid);
    db.exec(
        "INSERT INTO"
            + " sections(code,subject_id,semester_id,teacher_id,capacity,schedule_text,room,status)"
            + " VALUES('CSDL-2601',?,?,?,35,'Thứ 4 · Tiết 4–6','A1-405','OPEN')",
        subject2,
        term,
        tid);
    long done =
        db.insert(
            "INSERT INTO"
                + " sections(code,subject_id,semester_id,teacher_id,capacity,schedule_text,room,status,grade_locked)"
                + " VALUES('PTTK-2502',?,?,?,40,'Thứ 6 · Tiết 1–3','B2-201','COMPLETED',TRUE)",
            subject3,
            old,
            tid);
    db.exec("INSERT INTO enrollments(student_id,section_id) VALUES(?,?)", sid, section);
    long e = db.insert("INSERT INTO enrollments(student_id,section_id) VALUES(?,?)", sid, done);
    db.exec(
        "INSERT INTO scores(enrollment_id,tx1,tx2,final_score,total,letter,points)"
            + " VALUES(?,9,8,8.5,8.5,'A',4)",
        e);
    db.exec(
        "INSERT INTO audit_logs(actor,action,detail) VALUES('SYSTEM','DEMO_DATA','Khởi tạo dữ liệu"
            + " minh họa cho ba vai trò')");
  }
}
