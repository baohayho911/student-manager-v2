package vn.studentmanager;

import java.util.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
class Api {
  final Db db;
  final Catalogs catalogs;
  final Academic academic;
  final Accounts accounts;

  Api(Db db, Catalogs c, Academic a, Accounts ac) {
    this.db = db;
    catalogs = c;
    academic = a;
    accounts = ac;
  }

  @GetMapping("/auth/csrf")
  Map<String, String> csrf(CsrfToken t) {
    return Map.of("token", t.getToken(), "headerName", t.getHeaderName());
  }

  @GetMapping("/auth/me")
  Map<String, Object> me(@AuthenticationPrincipal LoginUser u) {
    var m =
        new LinkedHashMap<>(
            db.one("SELECT id,username,role,must_change FROM users WHERE id=?", u.id()));
    if (!u.role().equals("ADMIN")) {
      String table = u.role().equals("STUDENT") ? "students" : "teachers";
      m.put("profile", db.one("SELECT * FROM " + table + " WHERE user_id=?", u.id()));
    }
    return m;
  }

  @PostMapping("/auth/password")
  void password(@AuthenticationPrincipal LoginUser u, @RequestBody Map<String, Object> b) {
    accounts.change(u.id(), b);
  }

  @GetMapping("/admin/catalog/{kind}")
  Object catalog(@PathVariable String kind) {
    return catalogs.list(kind);
  }

  @PostMapping("/admin/catalog/{kind}")
  Object add(@PathVariable String kind, @RequestBody Map<String, Object> b) {
    return Map.of("id", catalogs.save(kind, null, b));
  }

  @PutMapping("/admin/catalog/{kind}/{id}")
  Object edit(
      @PathVariable String kind, @PathVariable long id, @RequestBody Map<String, Object> b) {
    return Map.of("id", catalogs.save(kind, id, b));
  }

  @DeleteMapping("/admin/catalog/{kind}/{id}")
  void delete(@PathVariable String kind, @PathVariable long id) {
    catalogs.delete(kind, id);
  }

  @PostMapping("/admin/import/students")
  void importStudents(@RequestBody Map<String, Object> b) {
    catalogs.importStudents(b);
  }

  @GetMapping("/admin/accounts")
  Object accounts() {
    return accounts.list();
  }

  @PostMapping("/admin/accounts")
  void provision(@RequestBody Map<String, Object> b) {
    accounts.provision(b);
  }

  @PutMapping("/admin/accounts/{id}/password")
  void reset(@PathVariable long id, @RequestBody Map<String, Object> b) {
    accounts.reset(id, b);
  }

  @PutMapping("/admin/accounts/{id}/enabled")
  void enabled(@PathVariable long id, @RequestBody Map<String, Object> b) {
    Problem.require(b.get("enabled") instanceof Boolean, "Trạng thái không hợp lệ.");
    accounts.enabled(id, (Boolean) b.get("enabled"));
  }

  @DeleteMapping("/admin/accounts/{id}")
  void deleteAccount(@PathVariable long id) {
    accounts.delete(id);
  }

  @GetMapping("/admin/audit")
  Object audit() {
    return db.list("SELECT * FROM audit_logs ORDER BY id DESC LIMIT 500");
  }

  @GetMapping("/sections")
  Object sections(@AuthenticationPrincipal LoginUser u) {
    return academic.sections(u);
  }

  @PostMapping("/admin/sections")
  Object addSection(@RequestBody Map<String, Object> b) {
    return Map.of("id", academic.saveSection(null, b));
  }

  @PutMapping("/admin/sections/{id}")
  Object editSection(@PathVariable long id, @RequestBody Map<String, Object> b) {
    return Map.of("id", academic.saveSection(id, b));
  }

  @DeleteMapping("/admin/sections/{id}")
  void deleteSection(@PathVariable long id) {
    academic.deleteSection(id);
  }

  @PostMapping("/sections/{id}/enroll")
  void enroll(
      @AuthenticationPrincipal LoginUser u,
      @PathVariable long id,
      @RequestBody Map<String, Object> b) {
    academic.enroll(u, id, b);
  }

  @DeleteMapping("/enrollments/{id}")
  void cancel(@AuthenticationPrincipal LoginUser u, @PathVariable long id) {
    academic.cancel(u, id);
  }

  @GetMapping("/sections/{id}/roster")
  Object roster(@AuthenticationPrincipal LoginUser u, @PathVariable long id) {
    return academic.roster(u, id);
  }

  @PutMapping("/scores/{id}")
  void score(
      @AuthenticationPrincipal LoginUser u,
      @PathVariable long id,
      @RequestBody Map<String, Object> b) {
    academic.score(u, id, b);
  }

  @PostMapping("/sections/{id}/lock")
  void lock(
      @AuthenticationPrincipal LoginUser u,
      @PathVariable long id,
      @RequestBody Map<String, Object> b) {
    Problem.require(b.get("locked") instanceof Boolean, "Trạng thái chốt không hợp lệ.");
    academic.lock(u, id, (Boolean) b.get("locked"), Input.text(b, "reason", 300, false));
  }

  @GetMapping("/transcript")
  Object transcript(
      @AuthenticationPrincipal LoginUser u, @RequestParam(required = false) Long studentId) {
    return academic.transcript(u, studentId);
  }

  @GetMapping("/dashboard")
  Object dashboard(
      @AuthenticationPrincipal LoginUser u, @RequestParam(required = false) Long semesterId) {
    var m = new LinkedHashMap<String, Object>();
    var sections = academic.sections(u);
    if (semesterId != null)
      sections = sections.stream().filter(s -> Db.id(s.get("semester_id")) == semesterId).toList();
    m.put("sections", sections);
    if (u.role().equals("ADMIN")) {
      m.put("students", db.count("SELECT COUNT(*) FROM students"));
      m.put("teachers", db.count("SELECT COUNT(*) FROM teachers"));
      m.put("subjects", db.count("SELECT COUNT(*) FROM subjects"));
      m.put("accounts", db.count("SELECT COUNT(*) FROM users"));
      m.put("recent", db.list("SELECT * FROM audit_logs ORDER BY id DESC LIMIT 6"));
    } else if (u.role().equals("STUDENT")) m.put("transcript", academic.transcript(u, null));
    return m;
  }
}
