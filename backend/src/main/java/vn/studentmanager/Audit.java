package vn.studentmanager;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
class Audit {
  private final Db db;

  Audit(Db db) {
    this.db = db;
  }

  void log(String action, String detail) {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    db.exec(
        "INSERT INTO audit_logs(actor,action,detail) VALUES(?,?,?)",
        auth == null ? "SYSTEM" : auth.getName(),
        action,
        detail.length() > 1500 ? detail.substring(0, 1500) : detail);
  }
}
