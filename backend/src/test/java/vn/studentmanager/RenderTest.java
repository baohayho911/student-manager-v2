package vn.studentmanager;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    properties = {
      "spring.datasource.url=jdbc:h2:mem:render-tests;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "app.bootstrap.username=deployadmin",
      "app.bootstrap.password=OnlyForTest@2026"
    })
@ActiveProfiles("render")
@AutoConfigureMockMvc
class RenderTest {
  @Autowired Db db;
  @Autowired Accounts accounts;
  @Autowired org.springframework.security.crypto.password.PasswordEncoder encoder;
  @Autowired MockMvc mvc;

  @Test
  void createsOnlyPrivateAdminAndDoesNotResetOnRestart() {
    assertEquals(1, db.count("SELECT COUNT(*) FROM users"));
    assertEquals(0, db.count("SELECT COUNT(*) FROM students"));
    var admin = db.one("SELECT * FROM users WHERE username='deployadmin'");
    assertEquals("ADMIN", admin.get("role"));
    assertTrue(Db.bool(admin.get("must_change")));
    assertTrue(encoder.matches("OnlyForTest@2026", (String) admin.get("password_hash")));
    new BootstrapAdmin(db, accounts, "anotheradmin", "DifferentSecret@2026")
        .run(new DefaultApplicationArguments(new String[] {}));
    assertEquals(1, db.count("SELECT COUNT(*) FROM users"));
    assertEquals(
        admin.get("password_hash"),
        db.one("SELECT * FROM users WHERE username='deployadmin'").get("password_hash"));
  }

  @Test
  void healthIsPublicButDataRequiresAuthentication() throws Exception {
    mvc.perform(get("/api/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"));
    mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
  }
}
