package vn.studentmanager;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

public class Problem extends RuntimeException {
  final int status;

  public Problem(int status, String message) {
    super(message);
    this.status = status;
  }

  static void require(boolean ok, String msg) {
    if (!ok) throw new Problem(400, msg);
  }
}

@RestControllerAdvice
class ErrorAdvice {
  @ExceptionHandler(Problem.class)
  ResponseEntity<?> problem(Problem e) {
    return ResponseEntity.status(e.status).body(Map.of("message", e.getMessage()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> conflict(DataIntegrityViolationException e) {
    return ResponseEntity.status(409)
        .body(
            Map.of(
                "message",
                "Mã/tài khoản bị trùng hoặc dữ liệu đang được sử dụng. Hãy kiểm tra các liên kết"
                    + " trước khi xóa."));
  }

  @ExceptionHandler({
    IllegalArgumentException.class,
    MethodArgumentTypeMismatchException.class,
    HttpMessageNotReadableException.class
  })
  ResponseEntity<?> invalid(Exception e) {
    return ResponseEntity.badRequest()
        .body(
            Map.of(
                "message", "Dữ liệu không hợp lệ. Kiểm tra kiểu số, ngày và các trường bắt buộc."));
  }
}
