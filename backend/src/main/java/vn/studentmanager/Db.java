package vn.studentmanager;

import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

@Component
public class Db {
  final JdbcTemplate jdbc;

  public Db(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<Map<String, Object>> list(String sql, Object... args) {
    return jdbc.queryForList(sql, args);
  }

  public Map<String, Object> one(String sql, Object... args) {
    var rows = list(sql, args);
    if (rows.isEmpty()) throw new Problem(404, "Không tìm thấy dữ liệu.");
    return rows.get(0);
  }

  public long count(String sql, Object... args) {
    return jdbc.queryForObject(sql, Long.class, args);
  }

  public int exec(String sql, Object... args) {
    return jdbc.update(sql, args);
  }

  public long insert(String sql, Object... args) {
    var keys = new GeneratedKeyHolder();
    jdbc.update(
        c -> {
          var p = c.prepareStatement(sql, new String[] {"id"});
          for (int i = 0; i < args.length; i++) p.setObject(i + 1, args[i]);
          return p;
        },
        keys);
    return Objects.requireNonNull(keys.getKey()).longValue();
  }

  static long id(Object x) {
    return ((Number) x).longValue();
  }

  static int num(Object x) {
    return ((Number) x).intValue();
  }

  static boolean bool(Object x) {
    return Boolean.TRUE.equals(x) || (x instanceof Number n && n.intValue() != 0);
  }
}
