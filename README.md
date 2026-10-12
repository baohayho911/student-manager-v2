# Xây dựng ứng dụng web quản lý sinh viên

Ứng dụng hỗ trợ quản lý hồ sơ sinh viên, giảng viên, danh mục đào tạo, lớp học phần, đăng ký học phần và kết quả học tập. Hệ thống có ba vai trò: **Quản trị viên**, **Giảng viên** và **Sinh viên**, với quyền truy cập được kiểm tra tại backend.

Dự án phục vụ đề tài **“Xây dựng ứng dụng web quản lý sinh viên”**. Giao diện sử dụng logo HaUI và hình ảnh khuôn viên trường theo thiết kế của đề tài. Đây là sản phẩm đồ án, không phải cổng thông tin chính thức của Đại học Công nghiệp Hà Nội.

## 1. Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Giao diện | React 19, JavaScript, HTML, CSS |
| Công cụ phát triển và build frontend | Vite 6, npm |
| Backend | Java, Spring Boot 3.5, Spring Web |
| Xác thực và phân quyền | Spring Security, phiên đăng nhập bằng cookie, CSRF, băm mật khẩu BCrypt |
| Truy cập cơ sở dữ liệu | Spring JDBC |
| Cơ sở dữ liệu | H2 cho chế độ demo; MySQL cho cấu hình MySQL và Render |
| Quản lý phiên bản cơ sở dữ liệu | Flyway |
| Build và kiểm thử backend | Maven Wrapper, Spring Boot Test, Spring Security Test |
| Đóng gói và triển khai | Docker, cấu hình triển khai Render |

Backend cấu hình tương thích Java 17; khuyến nghị **JDK 21** khi phát triển và build. Dockerfile sử dụng Java 21. Frontend yêu cầu **Node.js từ 20.19.0 trở lên**; có thể sử dụng Node.js 22 như Dockerfile của dự án.

## 2. Chức năng theo vai trò

### Chức năng chung

- Đăng nhập, đăng xuất và đổi mật khẩu.
- Xem thông tin tài khoản và hồ sơ cá nhân tương ứng.
- Xem trang tổng quan theo vai trò.
- Bắt buộc đổi mật khẩu đối với tài khoản vừa được cấp hoặc đặt lại mật khẩu.

Sinh viên và giảng viên chưa tự chỉnh sửa hồ sơ cá nhân; việc cập nhật hồ sơ do quản trị viên thực hiện.

### Quản trị viên

- **Quản lý sinh viên:** thêm, sửa, xóa có kiểm tra ràng buộc; tìm kiếm; nhập danh sách CSV và xuất danh sách.
- **Quản lý giảng viên:** quản lý hồ sơ, thông tin liên hệ và khoa công tác.
- **Quản lý danh mục đào tạo:** khoa, ngành đào tạo, lớp hành chính, môn học và học kỳ.
- **Quản lý lớp học phần:** mở, sửa, xóa lớp; phân công giảng viên; thiết lập sĩ số, lịch học, phòng học, trạng thái và trọng số điểm.
- **Quản lý đăng ký học phần:** xem danh sách lớp; đăng ký bổ sung hoặc hủy đăng ký cho sinh viên khi đủ điều kiện.
- **Quản lý điểm học tập:** nhập, sửa, chốt, công bố và mở lại bảng điểm; xuất bảng điểm lớp.
- **Tra cứu kết quả học tập:** xem bảng điểm, GPA và tín chỉ của từng sinh viên.
- **Quản lý tài khoản:** cấp tài khoản cho hồ sơ sinh viên, giảng viên; khóa, mở khóa, đặt lại mật khẩu và xóa tài khoản theo quyền cho phép.
- **Xem nhật ký thao tác:** tra cứu và xuất 500 bản ghi gần nhất.

### Giảng viên

- Tra cứu các lớp học phần được phân công và danh sách sinh viên trong lớp.
- Nhập, sửa điểm khi bảng điểm chưa chốt.
- Chốt và công bố điểm khi lớp đáp ứng điều kiện.
- Xuất bảng điểm của lớp phụ trách.

Giảng viên không được tạo lớp học phần, thay đổi đăng ký của sinh viên hoặc mở lại bảng điểm đã chốt.

### Sinh viên

- Tra cứu lớp học phần đang mở và các lớp đã đăng ký.
- Đăng ký, hủy đăng ký học phần khi đáp ứng điều kiện.
- Xem kết quả đã công bố, GPA học kỳ, GPA tích lũy và tín chỉ đạt.
- Xuất bảng điểm cá nhân dưới dạng CSV.

### Phạm vi biểu đồ use case tổng quan

Các nhóm chức năng trên là cơ sở xây dựng mục **2.2.1. Biểu đồ use case tổng quan** của báo cáo. Thao tác thêm, sửa, xóa, tìm kiếm, lọc và nhập/xuất CSV được mô tả trong chức năng tương ứng, không bắt buộc tách thành use case riêng ở mức tổng quan.

Ứng dụng có thống kê trên trang **Tổng quan** và xuất CSV tại các bảng dữ liệu; chưa có phân hệ **Thống kê và báo cáo** độc lập. Use case **Quản lý điểm học tập** thuộc cả quản trị viên và giảng viên, với phạm vi quyền khác nhau.

## 3. Mô hình dữ liệu

Phần mô hình hóa dữ liệu nghiệp vụ của báo cáo gồm **11 thực thể chính**:

| Thực thể trong báo cáo | Bảng trong mã nguồn | Nội dung quản lý |
|---|---|---|
| Tài khoản | `users` | Thông tin đăng nhập, vai trò, trạng thái và phiên bản xác thực |
| Khoa | `departments` | Đơn vị quản lý ngành và giảng viên |
| Ngành đào tạo | `majors` | Ngành đào tạo thuộc khoa |
| Lớp hành chính | `classes` | Lớp hành chính thuộc ngành |
| Sinh viên | `students` | Hồ sơ, lớp hành chính và trạng thái học tập |
| Giảng viên | `teachers` | Hồ sơ, khoa công tác và tài khoản liên kết |
| Môn học | `subjects` | Mã môn, tên môn và số tín chỉ |
| Học kỳ | `semesters` | Thông tin và thời gian học kỳ |
| Lớp học phần | `sections` | Môn học được mở theo học kỳ, giảng viên và cấu hình điểm |
| Đăng ký học phần | `enrollments` | Liên kết sinh viên với lớp học phần |
| Điểm học tập | `scores` | Điểm thành phần và kết quả của một đăng ký học phần |

Các quan hệ chính:

- Khoa quản lý nhiều ngành đào tạo và giảng viên.
- Ngành đào tạo có nhiều lớp hành chính; mỗi lớp hành chính có nhiều sinh viên.
- Mỗi lớp học phần thuộc một môn học, một học kỳ và một giảng viên phụ trách.
- Sinh viên và lớp học phần có quan hệ nhiều–nhiều thông qua đăng ký học phần.
- Mỗi đăng ký có thể chưa có điểm hoặc có một bản ghi điểm.
- Hồ sơ sinh viên, giảng viên có thể tồn tại khi chưa có hoặc đã bị xóa tài khoản đăng nhập.

**Phân biệt mô hình logic và triển khai:** báo cáo dùng các tên tiếng Việt như `maSinhVien`, `maLopHocPhan`; cơ sở dữ liệu hiện tại chủ yếu dùng khóa chính số `id`, mã nghiệp vụ `code` có ràng buộc duy nhất và khóa ngoại tham chiếu `id`. Bảng `scores` sử dụng `enrollment_id` vừa là khóa chính, vừa là khóa ngoại. Khi trình bày mô hình vật lý hoặc từ điển dữ liệu triển khai, cần đối chiếu các tệp migration trong `backend/src/main/resources/db/migration/`.

Ngoài 11 bảng nghiệp vụ, ứng dụng có bảng **`audit_logs`** phục vụ lưu vết thao tác. Vì vậy, cơ sở dữ liệu thực tế có **12 bảng ứng dụng**; nhật ký vẫn là một chức năng của quản trị viên. Nếu biểu đồ trong báo cáo chỉ trình bày 11 thực thể nghiệp vụ, cần ghi rõ phạm vi đó và mô tả nhật ký là dữ liệu hỗ trợ kỹ thuật.

## 4. Quy tắc nghiệp vụ chính

- Sinh viên phải đang học; lớp phải mở đăng ký, chưa chốt điểm và còn chỗ để đăng ký.
- Không đăng ký hai lớp của cùng một môn trong cùng học kỳ.
- Mỗi cặp sinh viên–lớp học phần có một bản ghi đăng ký; hủy và đăng ký lại được xử lý bằng trạng thái.
- Chỉ hủy đăng ký khi lớp còn mở, chưa chốt và đăng ký chưa có bản ghi điểm.
- Điểm thường xuyên 1, thường xuyên 2 và cuối kỳ nằm trong khoảng 0–10; tổng trọng số bằng 100%.
- Chỉ chốt điểm khi lớp đã đóng đăng ký và mọi sinh viên đang đăng ký đã có đủ điểm.
- Sinh viên chỉ xem được điểm đã công bố. Chỉ quản trị viên được mở lại bảng điểm và phải ghi lý do.
- Sửa điểm đã có phải ghi lý do; thay đổi được lưu nhật ký.
- Xóa tài khoản đăng nhập giữ lại hồ sơ, đăng ký học phần và điểm. Xóa hồ sơ là thao tác riêng và chịu ràng buộc dữ liệu.
- GPA tích lũy chọn lần học có điểm tổng kết cao nhất của mỗi môn; điểm và tín chỉ chỉ được tính theo các đăng ký còn hiệu lực, đã chốt điểm.

Quy tắc quy đổi điểm của dự án: A từ 8,5; B từ 7,0; C từ 5,5; D từ 4,0; còn lại là F. Điểm hệ 4 tương ứng là 4, 3, 2, 1 và 0. Đây là quy tắc minh họa của đề tài, không khẳng định áp dụng đầy đủ quy chế đào tạo của HaUI.

## 5. Chạy ứng dụng trên Windows

### Lần đầu lấy mã nguồn từ GitHub

1. Cài **JDK 21** và **Node.js 22**, bảo đảm có thể chạy `java -version` và `node -v` trong terminal.
2. Clone repository hoặc chọn **Code → Download ZIP** rồi giải nén.
3. Mở thư mục dự án, chạy **`BUILD-LAI.cmd`**. Lần đầu cần kết nối Internet để tải các thư viện.
4. Đợi build thành công. Script tạo file `release/student-manager-2.0.0.jar` chứa cả backend và giao diện.
5. Chạy **`CHAY-UNG-DUNG.cmd`**.
6. Khi xuất hiện thông báo `Started Application`, mở **http://localhost:8081**.

Repository chứa mã nguồn; không nên giả định file JAR đã có sẵn sau khi tải GitHub. Nếu đã có bản JAR hợp lệ và chưa sửa mã nguồn, những lần sau chỉ cần chạy `CHAY-UNG-DUNG.cmd`.

Script chạy ứng dụng bằng profile `demo`, sử dụng H2 lưu tại `backend/data/`; không cần cài MySQL cho chế độ này. Giữ cửa sổ chạy ứng dụng mở. Nhấn **Ctrl+C** để dừng.

### Tài khoản mẫu của chế độ demo

| Vai trò | Tên đăng nhập | Mật khẩu ban đầu |
|---|---|---|
| Quản trị viên | `admin` | `Admin@2026Demo` |
| Giảng viên | `gv001` | `Teacher@2026Demo` |
| Sinh viên | `sv001` | `Student@2026Demo` |

Các tài khoản trên được tạo khi chức năng nạp dữ liệu mẫu bật và bảng tài khoản đang trống. Chúng không thay thế mật khẩu đã đổi và không phải thông tin đăng nhập của bản triển khai thực tế. Profile `render` tắt dữ liệu mẫu.

### Sau khi sửa mã nguồn

Dừng ứng dụng, chạy lại **`BUILD-LAI.cmd`**, sau đó chạy **`CHAY-UNG-DUNG.cmd`** và tải lại trình duyệt. Chỉ chạy lại file CMD khởi động sẽ không tự build những thay đổi trong React hoặc Java.

## 6. Phát triển với frontend và backend riêng

Mở terminal thứ nhất tại thư mục `backend`:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend mặc định chạy tại `http://127.0.0.1:8080` với profile `demo`.

Mở terminal thứ hai tại thư mục `frontend`:

```powershell
npm ci
npm run dev
```

Truy cập **http://127.0.0.1:5173**. Vite chuyển tiếp các yêu cầu `/api` tới backend cổng 8080. Dùng cùng địa chỉ `127.0.0.1` khi phát triển theo cấu hình này.

Trên Linux/macOS, dùng `./mvnw` thay cho `mvnw.cmd`. Có thể build toàn bộ bằng `sh build.sh` tại thư mục gốc dự án.

## 7. Sử dụng MySQL và triển khai Render

### MySQL trên máy tính

- Chuẩn bị MySQL và tham khảo [script tạo cơ sở dữ liệu](database/TAO-DATABASE.sql).
- Build ứng dụng trước, sau đó chạy `CHAY-MYSQL.ps1` bằng PowerShell.
- Script yêu cầu tài khoản, mật khẩu MySQL và hỏi có nạp dữ liệu mẫu khi database trống hay không.
- Cấu hình mặc định của script kết nối database `student_manager_v2` tại `localhost:3306`; điều chỉnh cấu hình nếu môi trường khác.

### Render với MySQL bên ngoài

Dự án có sẵn `Dockerfile`, `render.yaml`, profile `render` và script khởi động để triển khai backend cùng giao diện trong một dịch vụ Docker.

| Biến môi trường | Ý nghĩa |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Đặt thành `render` |
| `DB_HOST` | Máy chủ MySQL |
| `DB_PORT` | Cổng MySQL của nhà cung cấp |
| `DB_NAME` | Tên database |
| `DB_USERNAME` | Tài khoản MySQL |
| `DB_PASSWORD` | Mật khẩu MySQL |
| `ADMIN_USERNAME` | Tên tài khoản quản trị khởi tạo |
| `ADMIN_PASSWORD` | Mật khẩu riêng để khởi tạo quản trị viên |

Cấu hình hiện tại yêu cầu **Secret File `mysql-ca.pem`** chứa chứng chỉ CA của dịch vụ MySQL. Script khởi động tạo truststore để xác minh kết nối TLS. Tài khoản quản trị chỉ được tạo khi bảng `users` trống; thay đổi `ADMIN_PASSWORD` không tự đặt lại mật khẩu tài khoản đã tồn tại.

Xem [hướng dẫn triển khai Render](docs/TRIEN-KHAI-RENDER.md) và [hướng dẫn HTML](HUONG-DAN-RENDER.html) để thiết lập theo cấu hình dự án. Không đưa mật khẩu triển khai hoặc tệp cấu hình chứa bí mật lên repository.

## 8. Cấu trúc mã nguồn

| Đường dẫn | Nội dung |
|---|---|
| `frontend/src/App.jsx` | Màn hình và luồng thao tác theo vai trò |
| `frontend/src/components.jsx` | Thành phần giao diện dùng chung |
| `frontend/src/style.css` | Kiểu dáng giao diện |
| `frontend/public/` | Logo và ảnh nền |
| `backend/src/main/java/vn/studentmanager/` | API, xử lý nghiệp vụ và bảo mật |
| `backend/src/main/resources/` | Cấu hình môi trường và migration dữ liệu |
| `backend/src/test/` | Kiểm thử backend |
| `database/` | Script hỗ trợ thiết lập MySQL |
| `deploy/` | Script khởi động trên Render |
| `docs/` | Tài liệu chức năng, API, kiểm thử và triển khai |
| `BUILD-LAI.cmd`, `build.sh` | Build frontend, kiểm thử backend và tạo JAR |
| `CHAY-UNG-DUNG.cmd` | Chạy bản JAR ở chế độ demo trên Windows |

## 9. Kiểm tra và kiểm thử

Kiểm tra bản build frontend, chạy tại thư mục `frontend`:

```powershell
npm ci
npm run build
```

Chạy kiểm thử và đóng gói backend, tại thư mục `backend`:

```powershell
.\mvnw.cmd clean verify
```

Để tạo bản chạy có giao diện mới nhất, dùng `BUILD-LAI.cmd` hoặc `sh build.sh`; chạy Maven riêng không tự build React. Kết quả kiểm thử cần được xác nhận trên phiên bản mã nguồn và môi trường đang sử dụng.

## 10. Giới hạn hiện tại

- Lịch học được lưu dạng mô tả, chưa tự kiểm tra trùng lịch, phòng hoặc giảng viên.
- Chưa quản lý môn tiên quyết, chương trình đào tạo theo khóa và hạn mức tín chỉ.
- Chưa có điểm danh, học phí, thanh toán, phúc khảo hoặc xét tốt nghiệp tự động.
- Chưa có đặt lại mật khẩu qua email; người dùng liên hệ quản trị viên.
- Nhập danh sách sinh viên bằng CSV, chưa đọc trực tiếp file XLSX.
- Chưa có phân hệ báo cáo độc lập; số liệu được xem tại Tổng quan, bảng điểm và các bảng quản lý.
- Tìm kiếm và phân trang chủ yếu thực hiện phía trình duyệt, phù hợp phạm vi dữ liệu của đồ án.

Phạm vi chức năng và mô hình trong báo cáo cần phản ánh các giới hạn này, tránh mô tả những chức năng chưa được triển khai.
