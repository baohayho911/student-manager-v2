package vn.studentmanager;

import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class Accounts {
  final Db db;
  final PasswordEncoder encoder;
  final Audit audit;

  Accounts(Db db, PasswordEncoder encoder, Audit audit) {
    this.db = db;
    this.encoder = encoder;
    this.audit = audit;
  }

  long create(String username, String password, String role, boolean change) {
    Problem.require(username.matches("[a-z0-9._-]{2,50}"), "Tên đăng nhập không hợp lệ.");
    return db.insert(
        "INSERT INTO users(username,password_hash,role,must_change) VALUES(?,?,?,?)",
        username,
        encoder.encode(password),
        role,
        change);
  }

  List<Map<String, Object>> list() {
    return db.list(
        "SELECT"
            + " u.id,u.username,u.role,u.enabled,u.must_change,COALESCE(s.full_name,t.full_name,'Quản"
            + " trị viên') AS full_name FROM users u LEFT JOIN students s ON s.user_id=u.id LEFT"
            + " JOIN teachers t ON t.user_id=u.id ORDER BY u.id DESC");
  }

  @Transactional
  public void change(long userId, Map<String, Object> b) {
    var u = db.one("SELECT * FROM users WHERE id=? FOR UPDATE", userId);
    Problem.require(b.get("oldPassword") instanceof String, "Nhập mật khẩu hiện tại.");
    String old = (String) b.get("oldPassword");
    Problem.require(
        encoder.matches(old, (String) u.get("password_hash")), "Mật khẩu hiện tại không đúng.");
    String password = Input.password(b, "newPassword");
    Problem.require(
        !encoder.matches(password, (String) u.get("password_hash")),
        "Mật khẩu mới phải khác mật khẩu cũ.");
    db.exec(
        "UPDATE users SET password_hash=?,must_change=FALSE,auth_version=auth_version+1 WHERE id=?",
        encoder.encode(password),
        userId);
    audit.log("CHANGE_PASSWORD", "Tài khoản #" + userId);
  }

  @Transactional
  public void reset(long id, Map<String, Object> b) {
    db.one("SELECT id FROM users WHERE id=? FOR UPDATE", id);
    db.exec(
        "UPDATE users SET password_hash=?,must_change=TRUE,auth_version=auth_version+1 WHERE id=?",
        encoder.encode(Input.password(b, "newPassword")),
        id);
    audit.log("RESET_PASSWORD", "Tài khoản #" + id);
  }

  @Transactional
  public void enabled(long id, boolean enabled) {
    var u = db.one("SELECT * FROM users WHERE id=? FOR UPDATE", id);
    Problem.require(
        !u.get("role").equals("ADMIN"), "Không khóa tài khoản quản trị trong phiên bản này.");
    db.exec("UPDATE users SET enabled=?,auth_version=auth_version+1 WHERE id=?", enabled, id);
    audit.log(enabled ? "UNLOCK_ACCOUNT" : "LOCK_ACCOUNT", "Tài khoản #" + id);
  }

  @Transactional
  public void delete(long id) {
    var u = db.one("SELECT * FROM users WHERE id=? FOR UPDATE", id);
    Problem.require(!u.get("role").equals("ADMIN"), "Không xóa tài khoản quản trị.");
    db.exec("UPDATE students SET user_id=NULL WHERE user_id=?", id);
    db.exec("UPDATE teachers SET user_id=NULL WHERE user_id=?", id);
    db.exec("DELETE FROM users WHERE id=?", id);
    audit.log("DELETE_ACCOUNT", "Tài khoản " + u.get("username") + "; giữ hồ sơ và điểm");
  }

  @Transactional
  public void provision(Map<String, Object> b) {
    String role = Input.option(b, "role", "STUDENT", "TEACHER");
    String table = role.equals("STUDENT") ? "students" : "teachers";
    long id = Input.number(b, "profile_id", 1, Long.MAX_VALUE);
    var p = db.one("SELECT * FROM " + table + " WHERE id=? FOR UPDATE", id);
    Problem.require(p.get("user_id") == null, "Hồ sơ đã có tài khoản.");
    long uid =
        create(
            p.get("code").toString().toLowerCase(Locale.ROOT),
            Input.password(b, "newPassword"),
            role,
            true);
    db.exec("UPDATE " + table + " SET user_id=? WHERE id=?", uid, id);
    audit.log("CREATE_ACCOUNT", "Cấp tài khoản cho " + role + " #" + id);
  }
}
