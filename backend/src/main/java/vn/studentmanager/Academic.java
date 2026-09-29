package vn.studentmanager;

import java.math.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class Academic {
  final Db db;
  final Audit audit;

  Academic(Db db, Audit audit) {
    this.db = db;
    this.audit = audit;
  }

  static void admin(LoginUser u) {
    if (!u.role().equals("ADMIN"))
      throw new Problem(403, "Chỉ quản trị viên được thực hiện thao tác này.");
  }

  long profile(LoginUser u, String table) {
    return Db.id(db.one("SELECT id FROM " + table + " WHERE user_id=?", u.id()).get("id"));
  }

  Map<String, Object> section(long id, boolean lock) {
    return db.one("SELECT * FROM sections WHERE id=?" + (lock ? " FOR UPDATE" : ""), id);
  }

  void teaching(LoginUser u, Map<String, Object> s) {
    if (u.role().equals("ADMIN")) return;
    if (!u.role().equals("TEACHER") || Db.id(s.get("teacher_id")) != profile(u, "teachers"))
      throw new Problem(403, "Bạn không phụ trách lớp học phần này.");
  }

  List<Map<String, Object>> sections(LoginUser u) {
    String sql =
        "SELECT c.*,s.name AS subject_name,s.credits,t.full_name AS teacher_name,m.name AS"
            + " semester_name,(SELECT COUNT(*) FROM enrollments e WHERE e.section_id=c.id AND"
            + " e.status='ACTIVE') AS enrolled FROM sections c JOIN subjects s ON c.subject_id=s.id"
            + " JOIN teachers t ON c.teacher_id=t.id JOIN semesters m ON c.semester_id=m.id";
    if (u.role().equals("TEACHER"))
      return db.list(sql + " WHERE c.teacher_id=? ORDER BY c.id DESC", profile(u, "teachers"));
    if (u.role().equals("STUDENT"))
      return db.list(
          sql
              + " WHERE c.status='OPEN' OR c.id IN(SELECT section_id FROM enrollments WHERE"
              + " student_id=?) ORDER BY c.id DESC",
          profile(u, "students"));
    return db.list(sql + " ORDER BY c.id DESC");
  }

  @Transactional
  public long saveSection(Long id, Map<String, Object> b) {
    var old = id == null ? null : section(id, true);
    String code = Input.code(b);
    long subject = Input.number(b, "subject_id", 1, Long.MAX_VALUE),
        semester = Input.number(b, "semester_id", 1, Long.MAX_VALUE),
        teacher = Input.number(b, "teacher_id", 1, Long.MAX_VALUE),
        capacity = Input.number(b, "capacity", 1, 1000);
    db.one("SELECT id FROM subjects WHERE id=?", subject);
    db.one("SELECT id FROM semesters WHERE id=?", semester);
    db.one("SELECT id FROM teachers WHERE id=?", teacher);
    int w1 = (int) Input.number(b, "weight1", 0, 100),
        w2 = (int) Input.number(b, "weight2", 0, 100),
        wf = (int) Input.number(b, "weight_final", 0, 100);
    Problem.require(w1 + w2 + wf == 100, "Tổng tỷ trọng điểm phải bằng 100%.");
    String status = Input.option(b, "status", "OPEN", "CLOSED", "COMPLETED");
    if (old != null) {
      Problem.require(
          capacity
              >= db.count(
                  "SELECT COUNT(*) FROM enrollments WHERE section_id=? AND status='ACTIVE'", id),
          "Sĩ số tối đa không được thấp hơn số sinh viên đang đăng ký.");
      if (db.count("SELECT COUNT(*) FROM enrollments WHERE section_id=?", id) > 0)
        Problem.require(
            Db.id(old.get("subject_id")) == subject && Db.id(old.get("semester_id")) == semester,
            "Không đổi môn hoặc học kỳ khi lớp đã có lịch sử đăng ký.");
      if (db.count(
              "SELECT COUNT(*) FROM scores g JOIN enrollments e ON g.enrollment_id=e.id WHERE"
                  + " e.section_id=?",
              id)
          > 0)
        Problem.require(
            Db.num(old.get("weight1")) == w1
                && Db.num(old.get("weight2")) == w2
                && Db.num(old.get("weight_final")) == wf,
            "Không đổi tỷ trọng khi đã có điểm.");
      Problem.require(
          !Db.bool(old.get("grade_locked")) || !status.equals("OPEN"),
          "Không mở đăng ký khi lớp đã chốt điểm.");
    }
    Object[] args = {
      code,
      subject,
      semester,
      teacher,
      capacity,
      Input.text(b, "schedule_text", 200, false),
      Input.text(b, "room", 50, false),
      status,
      w1,
      w2,
      wf
    };
    if (id == null)
      id =
          db.insert(
              "INSERT INTO"
                  + " sections(code,subject_id,semester_id,teacher_id,capacity,schedule_text,room,status,weight1,weight2,weight_final)"
                  + " VALUES(?,?,?,?,?,?,?,?,?,?,?)",
              args);
    else {
      var vals = new ArrayList<>(Arrays.asList(args));
      vals.add(id);
      db.exec(
          "UPDATE sections SET"
              + " code=?,subject_id=?,semester_id=?,teacher_id=?,capacity=?,schedule_text=?,room=?,status=?,weight1=?,weight2=?,weight_final=?"
              + " WHERE id=?",
          vals.toArray());
    }
    audit.log("SAVE_SECTION", "Lớp " + code);
    return id;
  }

  @Transactional
  public void deleteSection(long id) {
    var s = section(id, true);
    if (db.count("SELECT COUNT(*) FROM enrollments WHERE section_id=?", id) > 0)
      throw new Problem(409, "Lớp đã có lịch sử đăng ký, không thể xóa.");
    db.exec("DELETE FROM sections WHERE id=?", id);
    audit.log("DELETE_SECTION", s.get("code").toString());
  }

  @Transactional
  public void enroll(LoginUser u, long sectionId, Map<String, Object> b) {
    long student;
    if (u.role().equals("STUDENT")) student = profile(u, "students");
    else {
      admin(u);
      student = Input.number(b, "student_id", 1, Long.MAX_VALUE);
    }
    var st = db.one("SELECT * FROM students WHERE id=? FOR UPDATE", student);
    var s = section(sectionId, true);
    Problem.require(
        st.get("status").equals("STUDYING"), "Chỉ sinh viên đang học được đăng ký học phần.");
    Problem.require(
        s.get("status").equals("OPEN") && !Db.bool(s.get("grade_locked")), "Lớp không mở đăng ký.");
    Problem.require(
        db.count(
                "SELECT COUNT(*) FROM enrollments WHERE section_id=? AND status='ACTIVE'",
                sectionId)
            < Db.num(s.get("capacity")),
        "Lớp đã đủ sĩ số.");
    Problem.require(
        db.count(
                "SELECT COUNT(*) FROM enrollments e JOIN sections c ON e.section_id=c.id WHERE"
                    + " e.student_id=? AND e.status='ACTIVE' AND c.subject_id=? AND"
                    + " c.semester_id=?",
                student,
                s.get("subject_id"),
                s.get("semester_id"))
            == 0,
        "Bạn đã đăng ký môn này trong học kỳ này.");
    var prior =
        db.list(
            "SELECT id FROM enrollments WHERE student_id=? AND section_id=?", student, sectionId);
    if (prior.isEmpty())
      db.exec("INSERT INTO enrollments(student_id,section_id) VALUES(?,?)", student, sectionId);
    else
      db.exec(
          "UPDATE enrollments SET status='ACTIVE',registered_at=CURRENT_TIMESTAMP WHERE id=?",
          prior.get(0).get("id"));
    audit.log("ENROLL", "Sinh viên #" + student + "; lớp " + s.get("code"));
  }

  @Transactional
  public void cancel(LoginUser u, long id) {
    var e = db.one("SELECT * FROM enrollments WHERE id=?", id);
    long student = Db.id(e.get("student_id"));
    if (u.role().equals("STUDENT"))
      Problem.require(student == profile(u, "students"), "Không được hủy đăng ký của người khác.");
    else admin(u);
    db.one("SELECT id FROM students WHERE id=? FOR UPDATE", student);
    var s = section(Db.id(e.get("section_id")), true);
    e = db.one("SELECT * FROM enrollments WHERE id=?", id);
    Problem.require(e.get("status").equals("ACTIVE"), "Đăng ký đã hủy.");
    Problem.require(
        s.get("status").equals("OPEN") && !Db.bool(s.get("grade_locked")),
        "Chỉ hủy khi lớp đang mở đăng ký và chưa chốt điểm.");
    Problem.require(
        db.count("SELECT COUNT(*) FROM scores WHERE enrollment_id=?", id) == 0,
        "Đăng ký đã có điểm, không được hủy.");
    db.exec("UPDATE enrollments SET status='CANCELLED' WHERE id=?", id);
    audit.log("CANCEL_ENROLLMENT", "Đăng ký #" + id);
  }

  List<Map<String, Object>> roster(LoginUser u, long id) {
    teaching(u, section(id, false));
    return db.list(
        "SELECT"
            + " e.id,e.student_id,e.status,s.code,s.full_name,s.email,g.tx1,g.tx2,g.final_score,g.total,g.letter"
            + " FROM enrollments e JOIN students s ON s.id=e.student_id LEFT JOIN scores g ON"
            + " g.enrollment_id=e.id WHERE e.section_id=? ORDER BY s.code",
        id);
  }

  @Transactional
  public void score(LoginUser u, long enrollment, Map<String, Object> b) {
    var e = db.one("SELECT * FROM enrollments WHERE id=?", enrollment);
    var s = section(Db.id(e.get("section_id")), true);
    teaching(u, s);
    e = db.one("SELECT * FROM enrollments WHERE id=?", enrollment);
    Problem.require(e.get("status").equals("ACTIVE"), "Không nhập điểm cho đăng ký đã hủy.");
    Problem.require(
        !Db.bool(s.get("grade_locked")), "Điểm đã chốt. ADMIN cần mở lại với lý do trước khi sửa.");
    BigDecimal a = Input.score(b, "tx1"),
        c = Input.score(b, "tx2"),
        f = Input.score(b, "final_score");
    BigDecimal total =
        a.multiply(BigDecimal.valueOf(Db.num(s.get("weight1"))))
            .add(c.multiply(BigDecimal.valueOf(Db.num(s.get("weight2")))))
            .add(f.multiply(BigDecimal.valueOf(Db.num(s.get("weight_final")))))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    double n = total.doubleValue();
    String letter = n >= 8.5 ? "A" : n >= 7 ? "B" : n >= 5.5 ? "C" : n >= 4 ? "D" : "F";
    int points =
        switch (letter) {
          case "A" -> 4;
          case "B" -> 3;
          case "C" -> 2;
          case "D" -> 1;
          default -> 0;
        };
    var prev = db.list("SELECT * FROM scores WHERE enrollment_id=?", enrollment);
    String reason = Input.text(b, "reason", 300, !prev.isEmpty());
    if (prev.isEmpty())
      db.exec(
          "INSERT INTO scores(enrollment_id,tx1,tx2,final_score,total,letter,points)"
              + " VALUES(?,?,?,?,?,?,?)",
          enrollment,
          a,
          c,
          f,
          total,
          letter,
          points);
    else
      db.exec(
          "UPDATE scores SET tx1=?,tx2=?,final_score=?,total=?,letter=?,points=? WHERE"
              + " enrollment_id=?",
          a,
          c,
          f,
          total,
          letter,
          points,
          enrollment);
    audit.log(
        "SAVE_SCORE",
        "Đăng ký #"
            + enrollment
            + "; cũ: "
            + (prev.isEmpty() ? "chưa có" : prev.get(0))
            + "; mới: "
            + a
            + ", "
            + c
            + ", "
            + f
            + " => "
            + total
            + "; lý do: "
            + reason);
  }

  @Transactional
  public void lock(LoginUser u, long id, boolean lock, String reason) {
    var s = section(id, true);
    teaching(u, s);
    if (!lock) {
      admin(u);
      Problem.require(reason != null && !reason.isBlank(), "Cần ghi lý do mở lại điểm.");
      Problem.require(reason.length() <= 300, "Lý do tối đa 300 ký tự.");
    }
    if (lock) {
      Problem.require(
          !s.get("status").equals("OPEN"), "Đóng đăng ký học phần trước khi chốt điểm.");
      Problem.require(
          db.count(
                  "SELECT COUNT(*) FROM enrollments e LEFT JOIN scores g ON g.enrollment_id=e.id"
                      + " WHERE e.section_id=? AND e.status='ACTIVE' AND g.enrollment_id IS NULL",
                  id)
              == 0,
          "Cần nhập đủ điểm cho tất cả sinh viên đang đăng ký.");
    }
    db.exec("UPDATE sections SET grade_locked=? WHERE id=?", lock, id);
    audit.log(
        lock ? "LOCK_GRADES" : "REOPEN_GRADES",
        "Lớp " + s.get("code") + "; " + (reason == null ? "" : reason));
  }

  List<Map<String, Object>> results(LoginUser u, Long selected) {
    long student;
    if (u.role().equals("STUDENT")) student = profile(u, "students");
    else {
      admin(u);
      Problem.require(selected != null, "Chọn sinh viên.");
      student = selected;
    }
    return db.list(
        "SELECT e.id,e.section_id,e.status,c.code AS section_code,c.subject_id,c.semester_id,m.name"
            + " AS semester_name,m.starts_on,s.name AS subject_name,s.code AS"
            + " subject_code,s.credits,c.grade_locked,c.status AS section_status,CASE WHEN"
            + " c.grade_locked=TRUE THEN g.tx1 ELSE NULL END AS tx1,CASE WHEN c.grade_locked=TRUE"
            + " THEN g.tx2 ELSE NULL END AS tx2,CASE WHEN c.grade_locked=TRUE THEN g.final_score"
            + " ELSE NULL END AS final_score,CASE WHEN c.grade_locked=TRUE THEN g.total ELSE NULL"
            + " END AS total,CASE WHEN c.grade_locked=TRUE THEN g.letter ELSE NULL END AS"
            + " letter,CASE WHEN c.grade_locked=TRUE THEN g.points ELSE NULL END AS points FROM"
            + " enrollments e JOIN sections c ON c.id=e.section_id JOIN subjects s ON"
            + " s.id=c.subject_id JOIN semesters m ON m.id=c.semester_id LEFT JOIN scores g ON"
            + " g.enrollment_id=e.id WHERE e.student_id=? ORDER BY m.starts_on DESC,e.id DESC",
        student);
  }

  Map<String, Object> transcript(LoginUser u, Long student) {
    var rows = results(u, student);
    var best = new LinkedHashMap<Long, Map<String, Object>>();
    var terms = new LinkedHashMap<Long, List<Map<String, Object>>>();
    for (var r : rows)
      if (r.get("points") != null && r.get("status").equals("ACTIVE")) {
        long subject = Db.id(r.get("subject_id")), term = Db.id(r.get("semester_id"));
        terms.computeIfAbsent(term, k -> new ArrayList<>()).add(r);
        var old = best.get(subject);
        if (old == null
            || new BigDecimal(r.get("total").toString())
                    .compareTo(new BigDecimal(old.get("total").toString()))
                > 0) best.put(subject, r);
      }
    var summary = new LinkedHashMap<String, Object>(aggregate(new ArrayList<>(best.values())));
    summary.put("rows", rows);
    var termSummaries = new ArrayList<Map<String, Object>>();
    terms.forEach(
        (id, list) -> {
          var m = new LinkedHashMap<String, Object>(aggregate(list));
          m.put("semester_id", id);
          m.put("name", list.get(0).get("semester_name"));
          termSummaries.add(m);
        });
    summary.put("semesters", termSummaries);
    return summary;
  }

  Map<String, Object> aggregate(List<Map<String, Object>> rows) {
    BigDecimal sum = BigDecimal.ZERO;
    int credits = 0, earned = 0;
    for (var r : rows) {
      int c = Db.num(r.get("credits"));
      BigDecimal p = new BigDecimal(r.get("points").toString());
      credits += c;
      if (p.signum() > 0) earned += c;
      sum = sum.add(p.multiply(BigDecimal.valueOf(c)));
    }
    Map<String, Object> m = new LinkedHashMap<>();
    m.put(
        "gpa",
        credits == 0 ? null : sum.divide(BigDecimal.valueOf(credits), 2, RoundingMode.HALF_UP));
    m.put("graded_credits", credits);
    m.put("earned_credits", earned);
    return m;
  }
}
