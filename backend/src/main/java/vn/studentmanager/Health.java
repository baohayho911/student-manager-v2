package vn.studentmanager;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class Health {
  private final Db db;

  Health(Db db) {
    this.db = db;
  }

  @GetMapping("/api/health")
  ResponseEntity<Map<String, String>> health() {
    try {
      db.count("SELECT 1");
      return ResponseEntity.ok(Map.of("status", "UP"));
    } catch (org.springframework.dao.DataAccessException e) {
      return ResponseEntity.status(503).body(Map.of("status", "DOWN"));
    }
  }
}
