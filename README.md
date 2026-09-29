# UniDesk — Ứng dụng web quản lý sinh viên

Bộ mã nguồn mới, độc lập với dự án cũ. Backend Spring Boot, frontend React; giao diện sáng, menu bên trái, bảng quản lý và ba vai trò theo hướng của PDF tham khảo. Dùng logo HaUI theo yêu cầu cho bản đồ án; tên ứng dụng vẫn là UniDesk.

**Cách nhanh nhất:** giải nén toàn bộ ZIP, chạy `CHAY-UNG-DUNG.cmd`, đợi dòng `Started Application`, rồi mở **http://localhost:8081**. Cần Java 17 trở lên; khuyến nghị dùng JDK 21 cho môi trường phát triển. Bản JAR đã kèm giao diện: không cần npm, Maven hay MySQL cho lần chạy thử này.

Đọc `HUONG-DAN-WINDOWS.html` bằng Chrome để xem hướng dẫn có định dạng, hoặc `docs/HUONG-DAN.md` để xem đầy đủ lệnh chạy, MySQL và cách sửa mã nguồn.

## Tài khoản mẫu

| Vai trò | Tên đăng nhập | Mật khẩu ban đầu |
|---|---|---|
| ADMIN | admin | Admin@2026Demo |
| Giảng viên | gv001 | Teacher@2026Demo |
| Sinh viên | sv001 | Student@2026Demo |

Đây là dữ liệu thử. Tài khoản mẫu được tạo một lần khi database còn trống. Đổi mật khẩu không làm mất dữ liệu. Các tài khoản do ADMIN tạo hoặc reset sẽ bắt buộc đổi mật khẩu trước khi dùng hệ thống.

## Các tệp chính

- `backend/`: mã Java, cấu hình, migration SQL, kiểm thử tự động và Maven Wrapper.
- `frontend/`: mã React, CSS, cấu hình Vite và package-lock.json.
- `release/student-manager-2.0.0.jar`: bản đã build, chứa cả giao diện và backend.
- `database/TAO-DATABASE.sql`: tạo database và user riêng cho MySQL.
- `CHAY-UNG-DUNG.cmd`: chạy ngay với database H2 lưu trên ổ đĩa.
- `CHAY-MYSQL.ps1`: chạy bản JAR với MySQL và hỏi mật khẩu tại terminal.
- `BUILD-LAI.cmd`: build lại frontend, chạy kiểm thử backend và đóng gói JAR mới.
- `docs/CHUC-NANG-VA-QUY-TAC.md`: phạm vi chức năng và quy tắc nghiệp vụ.
- `docs/KIEM-THU.md`: kết quả kiểm tra và kịch bản demo.
- `docs/API.md`: danh sách API.

Không ghi đè thư mục dự án cũ. Cơ sở dữ liệu cũ không được tự động chuyển sang cấu trúc này.

## Bản cập nhật HaUI + Render

Logo HaUI và cỡ chữ lớn hơn đã được build vào bản chạy sẵn. Xem **HUONG-DAN-RENDER.html** để cập nhật dữ liệu cũ, sửa giao diện và triển khai Render + Aiven MySQL từng bước. Mã nguồn cấu hình: `Dockerfile`, `render.yaml`, `deploy/start-render.sh`, `backend/src/main/resources/application-render.properties`.
