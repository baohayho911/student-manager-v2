# API và tổ chức mã nguồn

Base URL khi chạy bản JAR: http://localhost:8081/api. Khi phát triển: frontend5173 proxy backend8080.

## Xác thực

GET `/auth/csrf` lấy token và tên header. POST `/auth/login` dùng form-urlencoded gồm username/password và header CSRF. Sau đăng nhập lấy CSRF mới. Các yêu cầu thay đổi dữ liệu gửi CSRF và cookie phiên; không dùng JWT. GET `/auth/me`, POST `/auth/logout`, POST `/auth/password` (oldPassword, newPassword).

## API theo nghiệp vụ

| API | Phương thức | Quyền |
|---|---|---|
| `/admin/catalog/{kind}` | GET, POST | ADMIN |
| `/admin/catalog/{kind}/{id}` | PUT, DELETE | ADMIN |
| `/admin/import/students` | POST | ADMIN |
| `/admin/accounts` | GET, POST | ADMIN |
| `/admin/accounts/{id}` | DELETE | ADMIN |
| `/admin/accounts/{id}/enabled` | PUT | ADMIN |
| `/admin/accounts/{id}/password` | PUT | ADMIN |
| `/admin/audit` | GET | ADMIN |
| `/sections` | GET | Danh sách giới hạn theo vai trò |
| `/admin/sections` | POST | ADMIN |
| `/admin/sections/{id}` | PUT, DELETE | ADMIN |
| `/sections/{id}/enroll` | POST | STUDENT của mình, hoặc ADMIN gửi student_id |
| `/enrollments/{id}` | DELETE | STUDENT của mình hoặc ADMIN |
| `/sections/{id}/roster` | GET | ADMIN hoặc giảng viên phụ trách |
| `/scores/{enrollmentId}` | PUT | ADMIN hoặc giảng viên phụ trách |
| `/sections/{id}/lock` | POST | Giảng viên phụ trách/ADMIN chốt; chỉ ADMIN mở lại |
| `/transcript` | GET | STUDENT của mình; ADMIN thêm ?studentId=... |
| `/dashboard` | GET | Theo vai trò; tùy chọn ?semesterId=... |

kind: departments, majors, classes, students, teachers, subjects, semesters.

Các tên trường theo snake_case trong migration và frontend/config.js. Lỗi trả JSON `message`, mã 400 dữ liệu sai, 401 hết/chưa có phiên, 403 thiếu quyền/CSRF/đổi mật khẩu bắt buộc, 404 không tìm thấy, 409 liên kết/trùng dữ liệu.

## Mã nguồn

- Security.java: cấu hình Spring Security, CSRF, form login và kiểm tra phiên còn hiệu lực qua auth_version.
- Api.java: định tuyến REST, nhận dữ liệu từ client.
- Catalogs.java: danh mục/hồ sơ/CSV và quy tắc xóa.
- Accounts.java: mật khẩu, cấp/khóa/xóa tài khoản.
- Academic.java: lớp học phần, đăng ký, điểm, chốt điểm, bảng điểm/GPA.
- Db.java: thao tác SQL tham số với JdbcTemplate.
- Input.java: kiểm tra dữ liệu đầu vào; không ghép dữ liệu người dùng vào SQL.
- Audit.java: ghi nhật ký trong giao dịch nghiệp vụ.
- DemoData.java: tạo dữ liệu thử khi bật app.seed và bảng users trống.
- frontend/src/config.js: cấu hình các trường biểu mẫu.
- frontend/src/components.jsx: bảng, form, cửa sổ xác nhận, thông báo.
- frontend/src/App.jsx: màn hình theo vai trò.
- frontend/src/style.css: giao diện responsive.

Các câu SQL ghép tên bảng chỉ dùng danh sách cho phép trong mã; giá trị đầu vào luôn truyền qua placeholder. Khi mở rộng cần giữ nguyên nguyên tắc này.
