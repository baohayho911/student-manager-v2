package vn.studentmanager;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:tests;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
      "app.seed=true"
    })
@AutoConfigureMockMvc
class ApplicationTest {
  @Autowired MockMvc mvc;
  @Autowired Db db;
  @Autowired DemoData seed;
  @Autowired Academic academic;
  @Autowired Accounts accounts;
  @Autowired Catalogs catalogs;

  @BeforeEach
  void reset() {
    for (String t :
        List.of(
            "scores",
            "enrollments",
            "sections",
            "students",
            "teachers",
            "classes",
            "majors",
            "departments",
            "subjects",
            "semesters",
            "audit_logs",
            "users")) db.exec("DELETE FROM " + t);
    seed.run(new DefaultApplicationArguments(new String[] {}));
  }

  long id(String table, String code) {
    return Db.id(
        db.one(
                "SELECT id FROM "
                    + table
                    + " WHERE "
                    + (table.equals("users") ? "username" : "code")
                    + "=?",
                code)
            .get("id"));
  }

  LoginUser user(String username) {
    var u = db.one("SELECT * FROM users WHERE username=?", username);
    return new LoginUser(
        Db.id(u.get("id")),
        username,
        (String) u.get("password_hash"),
        (String) u.get("role"),
        true,
        Db.num(u.get("auth_version")));
  }

  MockHttpSession login(String name, String pass) throws Exception {
    return (MockHttpSession)
        mvc.perform(
                post("/api/auth/login")
                    .with(csrf())
                    .param("username", name)
                    .param("password", pass))
            .andExpect(status().isOk())
            .andReturn()
            .getRequest()
            .getSession(false);
  }

  Map<String, Object> student(String code) {
    return new LinkedHashMap<>(
        Map.of(
            "code",
            code,
            "full_name",
            "Sinh viên kiểm thử",
            "class_id",
            id("classes", "K18KTPM01"),
            "status",
            "STUDYING",
            "gender",
            "OTHER",
            "initialPassword",
            "Example@2026Test",
            "email",
            "test@example.com"));
  }

  @Test
  void anonymousCannotReadData() throws Exception {
    mvc.perform(get("/api/admin/accounts")).andExpect(status().isUnauthorized());
  }

  @Test
  void invalidPasswordFails() throws Exception {
    mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .param("username", "admin")
                .param("password", "wrong"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void loginUsesCsrfAndStudentCannotAdmin() throws Exception {
    mvc.perform(
            post("/api/auth/login").param("username", "admin").param("password", "Admin@2026Demo"))
        .andExpect(status().isForbidden());
    var s = login("sv001", "Student@2026Demo");
    mvc.perform(get("/api/admin/catalog/students").session(s)).andExpect(status().isForbidden());
    mvc.perform(get("/api/auth/me").session(s)).andExpect(jsonPath("$.role").value("STUDENT"));
  }

  @Test
  void deletionRemovesLinkedAccountAtomically() {
    long st = catalogs.save("students", null, student("TESTDEL"));
    long uid = Db.id(db.one("SELECT user_id FROM students WHERE id=?", st).get("user_id"));
    catalogs.delete("students", st);
    assertEquals(0, db.count("SELECT COUNT(*) FROM users WHERE id=?", uid));
    assertEquals(0, db.count("SELECT COUNT(*) FROM students WHERE id=?", st));
  }

  @Test
  void studentWithHistoryCannotBeDeleted() {
    long st = id("students", "SV001");
    assertThrows(Problem.class, () -> catalogs.delete("students", st));
    assertEquals(1, db.count("SELECT COUNT(*) FROM students WHERE id=?", st));
    assertEquals(1, db.count("SELECT COUNT(*) FROM users WHERE username='sv001'"));
  }

  @Test
  void deletingAccountPreservesProfileAndScoresAndRevokesSession() throws Exception {
    var s = login("sv001", "Student@2026Demo");
    accounts.delete(id("users", "sv001"));
    assertNull(db.one("SELECT user_id FROM students WHERE code='SV001'").get("user_id"));
    assertEquals(1, db.count("SELECT COUNT(*) FROM scores"));
    mvc.perform(get("/api/auth/me").session(s)).andExpect(status().isUnauthorized());
  }

  @Test
  void accountLockRevokesAlreadyLoggedInSession() throws Exception {
    var s = login("sv001", "Student@2026Demo");
    accounts.enabled(id("users", "sv001"), false);
    mvc.perform(get("/api/auth/me").session(s)).andExpect(status().isUnauthorized());
    mvc.perform(
            post("/api/auth/login")
                .with(csrf())
                .param("username", "sv001")
                .param("password", "Student@2026Demo"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void resetRequiresPasswordChangeAndRevokesSession() throws Exception {
    var s = login("sv001", "Student@2026Demo");
    accounts.reset(id("users", "sv001"), Map.of("newPassword", "NewPassword@2026"));
    mvc.perform(get("/api/auth/me").session(s)).andExpect(status().isUnauthorized());
    var next = login("sv001", "NewPassword@2026");
    mvc.perform(get("/api/sections").session(next)).andExpect(status().isForbidden());
    mvc.perform(
            post("/api/auth/password")
                .session(next)
                .with(csrf())
                .contentType("application/json")
                .content(
                    "{\"oldPassword\":\"NewPassword@2026\",\"newPassword\":\"FinalPassword@2026\"}"))
        .andExpect(status().isOk());
    var finalSession = login("sv001", "FinalPassword@2026");
    mvc.perform(get("/api/sections").session(finalSession)).andExpect(status().isOk());
  }

  @Test
  void duplicateEnrollmentAndCancelledRegistration() {
    long section = id("sections", "CSDL-2601");
    academic.enroll(user("sv001"), section, Map.of());
    assertThrows(Problem.class, () -> academic.enroll(user("sv001"), section, Map.of()));
    long e = Db.id(db.one("SELECT id FROM enrollments WHERE section_id=?", section).get("id"));
    academic.cancel(user("sv001"), e);
    academic.enroll(user("sv001"), section, Map.of());
    assertEquals(1, db.count("SELECT COUNT(*) FROM enrollments WHERE section_id=?", section));
  }

  @Test
  void studentCannotCancelOthersRegistration() {
    long section = id("sections", "CSDL-2601");
    academic.enroll(user("admin"), section, Map.of("student_id", id("students", "SV002")));
    long e = Db.id(db.one("SELECT id FROM enrollments WHERE section_id=?", section).get("id"));
    assertThrows(Problem.class, () -> academic.cancel(user("sv001"), e));
  }

  @Test
  void scoresHiddenUntilLockedAndLockedScoreCannotChange() {
    long section = id("sections", "JAVA-2601");
    long e = Db.id(db.one("SELECT id FROM enrollments WHERE section_id=?", section).get("id"));
    academic.score(user("gv001"), e, Map.of("tx1", 9, "tx2", 8, "final_score", 8.5));
    assertNull(
        academic.results(user("sv001"), null).stream()
            .filter(r -> Db.id(r.get("id")) == e)
            .findFirst()
            .get()
            .get("total"));
    assertThrows(Problem.class, () -> academic.lock(user("gv001"), section, true, ""));
    db.exec("UPDATE sections SET status='CLOSED' WHERE id=?", section);
    academic.lock(user("gv001"), section, true, "");
    assertEquals(
        new BigDecimal("8.50"),
        db.one("SELECT total FROM scores WHERE enrollment_id=?", e).get("total"));
    assertEquals(new BigDecimal("4.00"), academic.transcript(user("sv001"), null).get("gpa"));
    assertThrows(
        Problem.class,
        () ->
            academic.score(
                user("gv001"), e, Map.of("tx1", 5, "tx2", 5, "final_score", 5, "reason", "sửa")));
    assertThrows(Problem.class, () -> academic.lock(user("gv001"), section, false, "sửa"));
    academic.lock(user("admin"), section, false, "Đối chiếu bài thi");
    academic.score(
        user("gv001"),
        e,
        Map.of("tx1", 5, "tx2", 5, "final_score", 5, "reason", "Đối chiếu bài thi"));
    assertTrue(db.count("SELECT COUNT(*) FROM audit_logs WHERE action='SAVE_SCORE'") >= 2);
  }

  @Test
  void cannotCancelAfterAnyScoreEntered() {
    long section = id("sections", "JAVA-2601"),
        e = Db.id(db.one("SELECT id FROM enrollments WHERE section_id=?", section).get("id"));
    academic.score(user("gv001"), e, Map.of("tx1", 5, "tx2", 6, "final_score", 7));
    assertThrows(Problem.class, () -> academic.cancel(user("sv001"), e));
  }

  @Test
  void studentCannotReadRosterOrWriteScore() {
    long section = id("sections", "JAVA-2601"),
        e = Db.id(db.one("SELECT id FROM enrollments WHERE section_id=?", section).get("id"));
    assertThrows(Problem.class, () -> academic.roster(user("sv001"), section));
    assertThrows(
        Problem.class,
        () -> academic.score(user("sv001"), e, Map.of("tx1", 10, "tx2", 10, "final_score", 10)));
  }

  @Test
  void scoresOutsideRangeRejected() {
    long e =
        Db.id(
            db.one(
                    "SELECT e.id FROM enrollments e JOIN sections s ON s.id=e.section_id WHERE"
                        + " s.code='JAVA-2601'")
                .get("id"));
    assertThrows(
        Problem.class,
        () -> academic.score(user("gv001"), e, Map.of("tx1", 11, "tx2", 5, "final_score", 5)));
    assertThrows(
        Problem.class,
        () -> academic.score(user("gv001"), e, Map.of("tx1", 8.555, "tx2", 5, "final_score", 5)));
  }

  @Test
  void importRollsBackEntireBatch() {
    long n = db.count("SELECT COUNT(*) FROM students"), u = db.count("SELECT COUNT(*) FROM users");
    assertThrows(
        Exception.class,
        () ->
            catalogs.importStudents(
                Map.of(
                    "rows",
                    List.of(student("IMPORTED"), student("SV001")),
                    "initialPassword",
                    "Imported@2026")));
    assertEquals(n, db.count("SELECT COUNT(*) FROM students"));
    assertEquals(u, db.count("SELECT COUNT(*) FROM users"));
  }

  @Test
  void onlyOneStudentCanTakeLastSeat() throws Exception {
    long section = id("sections", "CSDL-2601");
    db.exec("UPDATE sections SET capacity=1 WHERE id=?", section);
    var pool = Executors.newFixedThreadPool(2);
    var gate = new CountDownLatch(1);
    LoginUser admin = user("admin");
    List<Future<Boolean>> outcomes = new ArrayList<>();
    for (String code : List.of("SV002", "SV003")) {
      long st = id("students", code);
      outcomes.add(
          pool.submit(
              () -> {
                gate.await();
                try {
                  academic.enroll(admin, section, Map.of("student_id", st));
                  return true;
                } catch (Problem e) {
                  return false;
                }
              }));
    }
    gate.countDown();
    int successes = 0;
    for (var f : outcomes) if (f.get(10, TimeUnit.SECONDS)) successes++;
    pool.shutdownNow();
    assertEquals(1, successes);
    assertEquals(
        1,
        db.count(
            "SELECT COUNT(*) FROM enrollments WHERE section_id=? AND status='ACTIVE'", section));
  }

  @Test
  void retakeUsesHighestPublishedTotalOnce() {
    long student = id("students", "SV001"),
        subject = id("subjects", "IT103"),
        term = id("semesters", "2026-HK1"),
        teacher = id("teachers", "GV001");
    long section =
        db.insert(
            "INSERT INTO"
                + " sections(code,subject_id,semester_id,teacher_id,capacity,status,grade_locked)"
                + " VALUES('RETAKE',?,?,?,40,'COMPLETED',TRUE)",
            subject,
            term,
            teacher);
    long enrollment =
        db.insert("INSERT INTO enrollments(student_id,section_id) VALUES(?,?)", student, section);
    db.exec(
        "INSERT INTO scores(enrollment_id,tx1,tx2,final_score,total,letter,points)"
            + " VALUES(?,6,6,6,6,'C',2)",
        enrollment);
    var transcript = academic.transcript(user("sv001"), null);
    assertEquals(3, transcript.get("earned_credits"));
    assertEquals(new BigDecimal("4.00"), transcript.get("gpa"));
  }

  @Test
  void teacherCannotSeeAnotherTeachersRoster() {
    long uid = accounts.create("otherteacher", "Other@2026Teacher", "TEACHER", false);
    db.exec(
        "INSERT INTO teachers(code,full_name,department_id,user_id) VALUES('GVOTHER','Other',?,?)",
        id("departments", "CNTT"),
        uid);
    assertThrows(
        Problem.class, () -> academic.roster(user("otherteacher"), id("sections", "JAVA-2601")));
  }

  @Test
  void oldPasswordWhitespaceIsPreserved() throws Exception {
    long uid = id("users", "sv001");
    accounts.reset(uid, Map.of("newPassword", "  Secret@2026  "));
    accounts.change(
        uid, Map.of("oldPassword", "  Secret@2026  ", "newPassword", "Changed@2026Pass"));
    login("sv001", "Changed@2026Pass");
  }
}
