package vn.studentmanager;

import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class Catalogs {
  final Db db;
  final Accounts accounts;
  final Audit audit;
  static final Set<String> KINDS =
      Set.of("departments", "majors", "classes", "students", "teachers", "subjects", "semesters");

  Catalogs(Db db, Accounts a, Audit audit) {
    this.db = db;
    this.accounts = a;
    this.audit = audit;
  }

  String table(String kind) {
    if (!KINDS.contains(kind)) throw new Problem(404, "Danh mục không tồn tại.");
    return kind;
  }

  List<Map<String, Object>> list(String kind) {
    return db.list("SELECT * FROM " + table(kind) + " ORDER BY id DESC");
  }

  @Transactional
  public long save(String kind, Long id, Map<String, Object> b) {
    String t = table(kind);
    Map<String, Object> old =
        id == null ? null : db.one("SELECT * FROM " + t + " WHERE id=? FOR UPDATE", id);
    Map<String, Object> v = new LinkedHashMap<>();
    v.put("code", Input.code(b));
    boolean profile = kind.equals("students") || kind.equals("teachers");
    v.put(profile ? "full_name" : "name", Input.text(b, profile ? "full_name" : "name", 150, true));
    switch (kind) {
      case "majors" -> v.put("department_id", ref(b, "department_id", "departments"));
      case "classes" -> v.put("major_id", ref(b, "major_id", "majors"));
      case "subjects" -> {
        v.put("credits", Input.number(b, "credits", 1, 30));
        if (id != null
            && db.count(
                    "SELECT COUNT(*) FROM enrollments e JOIN sections c ON c.id=e.section_id WHERE"
                        + " c.subject_id=?",
                    id)
                > 0)
          Problem.require(
              Db.num(old.get("credits")) == ((Number) v.get("credits")).intValue(),
              "Không đổi tín chỉ khi môn đã có đăng ký học. Hãy tạo mã môn mới cho chương trình"
                  + " mới.");
      }
      case "semesters" -> {
        String start = Input.date(b, "starts_on", true), end = Input.date(b, "ends_on", true);
        Problem.require(
            !LocalDate.parse(end).isBefore(LocalDate.parse(start)),
            "Ngày kết thúc phải từ ngày bắt đầu trở đi.");
        v.put("starts_on", start);
        v.put("ends_on", end);
      }
      case "students" -> {
        v.put("class_id", ref(b, "class_id", "classes"));
        v.put("status", Input.option(b, "status", "STUDYING", "PAUSED", "LEFT", "GRADUATED"));
        v.put("gender", Input.option(b, "gender", "MALE", "FEMALE", "OTHER"));
        String birth = Input.date(b, "birth_date", false);
        Problem.require(
            birth == null || !LocalDate.parse(birth).isAfter(LocalDate.now()),
            "Ngày sinh không được ở tương lai.");
        v.put("birth_date", birth);
      }
      case "teachers" -> v.put("department_id", ref(b, "department_id", "departments"));
    }
    if (profile) {
      v.put("email", Input.email(b));
      String phone = Input.text(b, "phone", 20, false);
      Problem.require(
          phone.isEmpty() || phone.matches("[+0-9 ()-]{7,20}"), "Số điện thoại không hợp lệ.");
      v.put("phone", phone);
    }
    if (id == null) {
      if (profile) {
        String username = v.get("code").toString().toLowerCase(Locale.ROOT);
        long uid =
            accounts.create(
                username,
                Input.password(b, "initialPassword"),
                kind.equals("students") ? "STUDENT" : "TEACHER",
                true);
        v.put("user_id", uid);
      }
      String marks = String.join(",", Collections.nCopies(v.size(), "?"));
      id =
          db.insert(
              "INSERT INTO " + t + "(" + String.join(",", v.keySet()) + ") VALUES(" + marks + ")",
              v.values().toArray());
    } else {
      String fields = String.join(",", v.keySet().stream().map(k -> k + "=?").toList());
      var values = new ArrayList<>(v.values());
      values.add(id);
      db.exec("UPDATE " + t + " SET " + fields + " WHERE id=?", values.toArray());
    }
    audit.log("SAVE_" + kind.toUpperCase(Locale.ROOT), "Hồ sơ #" + id + "; mã " + v.get("code"));
    return id;
  }

  @Transactional
  public void importStudents(Map<String, Object> body) {
    Object source = body.get("rows");
    Problem.require(source instanceof List<?>, "Danh sách nhập không hợp lệ.");
    var rows = (List<?>) source;
    Problem.require(!rows.isEmpty() && rows.size() <= 200, "Nhập từ 1 đến 200 sinh viên mỗi lần.");
    String password = Input.password(body, "initialPassword");
    for (int i = 0; i < rows.size(); i++) {
      Problem.require(rows.get(i) instanceof Map<?, ?>, "Dòng dữ liệu không hợp lệ.");
      Map<String, Object> b = new LinkedHashMap<>();
      ((Map<?, ?>) rows.get(i)).forEach((k, v) -> b.put(String.valueOf(k), v));
      b.put("initialPassword", password);
      try {
        save("students", null, b);
      } catch (Problem e) {
        throw new Problem(e.status, "Dòng " + (i + 2) + ": " + e.getMessage());
      }
    }
    audit.log("IMPORT_STUDENTS", "Nhập " + rows.size() + " sinh viên");
  }

  long ref(Map<String, Object> b, String field, String t) {
    long id = Input.number(b, field, 1, Long.MAX_VALUE);
    db.one("SELECT id FROM " + t + " WHERE id=?", id);
    return id;
  }

  @Transactional
  public void delete(String kind, long id) {
    String t = table(kind);
    var row = db.one("SELECT * FROM " + t + " WHERE id=? FOR UPDATE", id);
    if (kind.equals("students")
        && db.count("SELECT COUNT(*) FROM enrollments WHERE student_id=?", id) > 0)
      throw new Problem(
          409,
          "Sinh viên đã có lịch sử đăng ký học phần. Hãy khóa tài khoản hoặc chuyển trạng thái học"
              + " tập; không xóa hồ sơ.");
    if (kind.equals("teachers")
        && db.count("SELECT COUNT(*) FROM sections WHERE teacher_id=?", id) > 0)
      throw new Problem(
          409, "Giảng viên đang được phân công lớp học phần. Hãy chuyển phân công trước khi xóa.");
    db.exec("DELETE FROM " + t + " WHERE id=?", id);
    if (row.get("user_id") != null) db.exec("DELETE FROM users WHERE id=?", row.get("user_id"));
    audit.log(
        "DELETE_" + kind.toUpperCase(Locale.ROOT),
        "Mã " + row.get("code") + "; xóa tài khoản liên kết nếu có");
  }
}
