package vn.studentmanager;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Creates only the first administrator; never resets an existing account. */
@Component
@Profile("render")
class BootstrapAdmin implements ApplicationRunner {
  private final Db db;
  private final Accounts accounts;
  private final String username;
  private final String password;

  BootstrapAdmin(
      Db db,
      Accounts accounts,
      @Value("${app.bootstrap.username}") String username,
      @Value("${app.bootstrap.password}") String password) {
    this.db = db;
    this.accounts = accounts;
    this.username = username;
    this.password = password;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (db.count("SELECT COUNT(*) FROM users") > 0) return;
    if (!username.matches("[a-z0-9._-]{2,50}"))
      throw new IllegalStateException(
          "ADMIN_USERNAME must contain 2-50 lowercase letters, digits, dots, underscores or"
              + " hyphens.");
    try {
      Input.password(Map.of("password", password), "password");
    } catch (Problem e) {
      throw new IllegalStateException(
          "Set ADMIN_PASSWORD to a private password of at least 12 characters (maximum 72 UTF-8"
              + " bytes) before first deployment.");
    }
    accounts.create(username, password, "ADMIN", true);
  }
}
