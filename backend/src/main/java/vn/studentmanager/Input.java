package vn.studentmanager;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.*;

class Input {
  static String text(Map<String, Object> b, String k, int max, boolean required) {
    Object v = b.get(k);
    if (v != null && !(v instanceof String))
      throw new Problem(400, "Trường " + k + " phải là văn bản.");
    String s = v == null ? "" : v.toString().trim();
    Problem.require(!required || !s.isEmpty(), "Thiếu thông tin: " + k);
    Problem.require(s.length() <= max, "Thông tin quá dài: " + k);
    return s;
  }

  static String code(Map<String, Object> b) {
    String s = text(b, "code", 30, true).toUpperCase(Locale.ROOT);
    Problem.require(
        s.matches("[A-Z0-9._-]{2,30}"),
        "Mã gồm 2–30 chữ không dấu, số, dấu chấm, gạch dưới hoặc gạch ngang.");
    return s;
  }

  static long number(Map<String, Object> b, String k, long min, long max) {
    try {
      long n = new BigDecimal(String.valueOf(b.get(k))).longValueExact();
      Problem.require(n >= min && n <= max, "Giá trị ngoài phạm vi: " + k);
      return n;
    } catch (NumberFormatException | ArithmeticException e) {
      throw new Problem(400, "Giá trị số không hợp lệ: " + k);
    }
  }

  static String option(Map<String, Object> b, String k, String... options) {
    String s = text(b, k, 30, true);
    Problem.require(Arrays.asList(options).contains(s), "Lựa chọn không hợp lệ: " + k);
    return s;
  }

  static String date(Map<String, Object> b, String k, boolean required) {
    String s = text(b, k, 10, required);
    if (s.isEmpty()) return null;
    LocalDate.parse(s);
    return s;
  }

  static BigDecimal score(Map<String, Object> b, String k) {
    try {
      BigDecimal n = new BigDecimal(String.valueOf(b.get(k)));
      Problem.require(
          n.signum() >= 0 && n.compareTo(BigDecimal.TEN) <= 0 && n.scale() <= 2,
          "Điểm phải từ 0 đến 10, tối đa 2 chữ số thập phân.");
      return n;
    } catch (NumberFormatException e) {
      throw new Problem(400, "Nhập đầy đủ ba điểm thành phần.");
    }
  }

  static String password(Map<String, Object> b, String k) {
    Object value = b.get(k);
    Problem.require(value instanceof String, "Nhập mật khẩu.");
    String s = (String) value;
    Problem.require(
        !s.isBlank() && s.length() >= 12 && s.getBytes(StandardCharsets.UTF_8).length <= 72,
        "Mật khẩu cần ít nhất 12 ký tự và tối đa 72 byte UTF-8.");
    return s;
  }

  static String email(Map<String, Object> b) {
    String s = text(b, "email", 150, false);
    Problem.require(
        s.isEmpty() || s.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"), "Email không hợp lệ.");
    return s.isEmpty() ? null : s;
  }
}
