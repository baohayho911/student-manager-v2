# Cập nhật HaUI và triển khai Render

Bản cập nhật ngày 29/09/2026. Gói này đã có logo HaUI, chữ lớn hơn và cấu hình Render + MySQL bên ngoài. UniDesk vẫn là tên ứng dụng đồ án.

## 1. Dùng bản mới trên Windows

1. Tắt ứng dụng cũ bằng Ctrl+C tại cửa sổ đang chạy. Không chạy đồng thời hai bản.
2. Sao lưu thư mục ứng dụng cũ, đặc biệt **backend/data**. Đây là dữ liệu H2 của cách chạy mặc định bằng CHAY-UNG-DUNG.cmd.
3. Giải nén ZIP mới ra **thư mục mới**, không chạy trực tiếp trong ZIP.
4. Nếu muốn giữ dữ liệu đã nhập: khi cả hai bản đều đã tắt, sao chép toàn bộ **backend/data** từ bản cũ sang đúng **backend/data** của bản mới, trước khi chạy bản mới. Không lấy thư mục target thay cho data. Nếu dùng MySQL từ trước, giữ cấu hình MySQL và sao lưu database MySQL thay vì chép H2.
5. Mở **CHAY-UNG-DUNG.cmd**, truy cập **http://localhost:8081**. Bản JAR đã build sẵn nên không cần BUILD-LAI.cmd nếu chỉ sử dụng.
6. Nếu trình duyệt còn hình cũ, nhấn Ctrl+F5. Tài khoản và dữ liệu cũ được giữ khi chép đúng data. Với database mới tại máy, tài khoản demo vẫn dùng như hướng dẫn Windows.

Không thay đổi migration V1 hoặc xóa database để cập nhật giao diện.

## 2. Muốn tự sửa logo và cỡ chữ sau này

- Ảnh logo: **frontend/public/haui-logo.png**. Thay bằng ảnh cùng tên; nên dùng ảnh vuông, rõ nét. App.jsx dùng ảnh này ở trang đăng nhập và menu; index.html dùng làm favicon.
- Cỡ chữ: **frontend/src/style.css**. Bản này tăng chữ gốc từ 14px lên 16px, chữ bảng/nhãn từ 12px lên 14px và chữ nhỏ tối thiểu 12px. Các font-size riêng trong CSS vẫn quyết định cỡ chữ từng vùng; chỉ sửa :root không thay đổi mọi vùng.
- Logo được hiển thị 46×46px qua **img.brand-mark**, object-fit: contain giữ tỉ lệ.
- Sau khi sửa mã nguồn: cài Node.js và Java/Maven theo hướng dẫn Windows, tắt ứng dụng, chạy **BUILD-LAI.cmd**, rồi **CHAY-UNG-DUNG.cmd**. Trên Render, commit/push lên GitHub rồi deploy lại.
- Nguồn logo: https://www.haui.edu.vn/dnn/web/haui/assets/images/logo.png ; trang nhận diện HaUI: https://www.haui.edu.vn/vn/page/tttt/detail/61517 .

## 3. Hiểu đúng cách triển khai

Render chạy cả giao diện React đã build và backend Spring Boot trong **một Docker Web Service**. Không tạo Static Site riêng. MySQL đặt trên Aiven. Không cần bật máy cá nhân, không cần chạy file .cmd hoặc mở tunnel sau khi deploy xong.

**Render Free không chạy liên tục không ngủ:** sau 15 phút không có truy cập, dịch vụ có thể ngủ; lần mở lại thường mất khoảng một phút. File ghi vào ổ đĩa Render sẽ mất khi restart/redeploy/ngủ. Vì thế không dùng H2 file trên Render. Nếu cần phục vụ liên tục, chọn instance trả phí phù hợp; gói miễn phí phù hợp trình diễn đồ án.

Aiven hiện có MySQL Free 1 GB dung lượng, không có hạn sử dụng cố định; vẫn có giới hạn và có thể bị tắt khi không hoạt động. Chọn đúng **Free**, không nhầm gói trial hoặc gói trả phí. Chính sách có thể thay đổi, kiểm tra màn hình giá trước khi tạo dịch vụ.

**Dữ liệu trên máy không tự chuyển lên mạng.** Hướng dẫn dưới đây tạo database online mới, ban đầu chỉ có một quản trị viên. Không đưa backend/data hoặc mật khẩu lên GitHub. Muốn mang toàn bộ dữ liệu H2 hiện có lên MySQL cần một bước chuyển dữ liệu riêng; chức năng CSV sinh viên không phải bản sao lưu toàn hệ thống.

## 4. Tạo MySQL trên Aiven

1. Mở https://console.aiven.io/ và tạo/đăng nhập tài khoản.
2. Tạo project nếu được yêu cầu, chọn tạo dịch vụ **MySQL** và gói **Free**. Chọn vùng gần Render nếu gói cho phép.
3. Đợi trạng thái **Running**. Trong trang **Overview / Connection information**, ghi lại: **Host**, **Port**, **Database name**, **User**, **Password**. Dùng đúng port được cấp; không mặc định cho rằng đó là 3306.
4. Database thường tên **defaultdb**, user thường **avnadmin**; lấy giá trị thực trên tài khoản của bạn.
5. Tải **CA certificate** trong thông tin kết nối. Mở file .pem bằng Notepad và giữ toàn bộ nội dung từ BEGIN CERTIFICATE đến END CERTIFICATE. Đây là chứng chỉ CA, không phải private key.
6. Nếu đã giới hạn IP truy cập database, cho phép các dải outbound IP của Web Service Render (xem trang service Render → Connect/Outbound). Nếu chưa giới hạn, dùng mặc định để kết nối lần đầu rồi thiết lập theo nhu cầu.

## 5. Đưa mã nguồn lên GitHub

Cách dễ trên Windows: dùng https://desktop.github.com/ .

1. Đăng nhập GitHub Desktop. Chọn **File → New repository**; đặt tên ví dụ **unidesk-haui**, tạo repository mới ở thư mục riêng.
2. Mở thư mục repository vừa tạo trong Explorer. Chép **nội dung bên trong student-manager-v2** vào đó: ở gốc phải thấy **Dockerfile**, **render.yaml**, **frontend**, **backend**, **deploy**. Không lồng thêm một tầng student-manager-v2.
3. Giữ file **.gitignore** đi kèm. Không đưa data, .env, certificate, node_modules, target hay release JAR lên GitHub. Gitignore đã loại chúng; kiểm tra danh sách Changes trước khi commit.
4. Trong GitHub Desktop nhập Summary, ví dụ `HaUI UI and Render deployment`; chọn **Commit to main**, rồi **Publish repository**. Có thể giữ repository **Private** và cấp Render quyền đọc repository đó.
5. Kiểm tra trên GitHub có Dockerfile ở gốc và có deploy/start-render.sh. Không tải file ZIP nguyên khối lên repository.

## 6. Tạo Web Service trên Render

1. Đăng nhập https://dashboard.render.com/ . Chọn **New → Web Service**, kết nối GitHub và chọn repository vừa tạo.
2. Đặt Name tùy ý, ví dụ **unidesk-haui**. Chọn branch đã push (thường main).
3. **Language/Runtime: Docker**. **Root Directory: để trống** nếu Dockerfile nằm ở gốc. **Dockerfile Path: ./Dockerfile**. Chọn region gần database và Instance Type **Free** để thử.
4. Không nhập lệnh npm start hay java vào Build/Start Command: Dockerfile và entrypoint đã lo phần này. Nếu có Docker Command, để trống.
5. Mở **Environment Variables**, thêm chính xác bảng sau. Nhập giá trị thô, không bọc dấu nháy; không nhập mật khẩu vào mã nguồn.

| Key | Value |
| --- | --- |
| SPRING_PROFILES_ACTIVE | render |
| DB_HOST | Host của Aiven, chỉ hostname, không có https:// hay mysql:// |
| DB_PORT | Port đúng từ Aiven |
| DB_NAME | Database của Aiven, thường defaultdb |
| DB_USERNAME | User MySQL từ Aiven |
| DB_PASSWORD | Password MySQL từ Aiven |
| ADMIN_USERNAME | admin hoặc tên tự đặt: 2–50 chữ thường/số/dấu chấm/gạch dưới/gạch ngang |
| ADMIN_PASSWORD | Mật khẩu quản trị do bạn tự tạo, ít nhất 12 ký tự, tối đa 72 byte UTF-8 |

6. Trong mục **Secret Files** (có thể nằm trong Advanced/Environment), chọn Add Secret File. **Filename: mysql-ca.pem**. **Contents: toàn bộ chứng chỉ CA của Aiven**. Lưu lại. Docker sẽ đọc file ở **/etc/secrets/mysql-ca.pem** và tạo truststore tự động. Nếu giao diện chưa cho thêm trước lần deploy đầu, tạo service, bổ sung Secret File ngay trong Environment, rồi chọn Save, rebuild and deploy.
7. Trong Advanced/Settings đặt **Health Check Path: /api/health**. Render tự cấp PORT; ứng dụng nhận PORT và lắng nghe trên 0.0.0.0, không cần đặt SERVER_PORT.
8. Chọn **Deploy Web Service**. Lần đầu cần tải dependency và build Java/React, có thể mất vài phút. Theo dõi Logs đến khi **Live**.
9. Mở URL **https://<tên-dịch-vụ>.onrender.com** mà Render cấp. Kiểm tra `/api/health` trả `{"status":"UP"}`.
10. Đăng nhập bằng ADMIN_USERNAME và ADMIN_PASSWORD đã đặt. Ứng dụng yêu cầu đổi mật khẩu trước khi dùng. Không dùng Admin@2026Demo trên database online mới.

**Tài khoản khởi tạo chỉ tạo khi bảng users trống.** Deploy lại hoặc thay biến ADMIN_PASSWORD sẽ không đổi mật khẩu của tài khoản đã tồn tại. Ghi nhớ mật khẩu mới sau lần đổi đầu tiên. Không xóa bảng users để reset.

Nếu thích Blueprint, file **render.yaml** đã có sẵn: New → Blueprint → chọn repository → nhập các biến sync:false. Tuy nhiên vẫn phải thêm Secret File **mysql-ca.pem** trong service rồi deploy lại. Với lần đầu, cách Web Service thủ công ở trên dễ theo dõi hơn.

## 7. Nhập dữ liệu và sử dụng

Database online mới không có dữ liệu demo. Tạo theo thứ tự: **Khoa → Ngành → Lớp hành chính**, rồi **Giảng viên, Sinh viên, Môn học, Học kỳ**, tiếp theo **Lớp học phần → Đăng ký học → Điểm**. Cấp tài khoản cho hồ sơ sinh viên/giảng viên trong mục Tài khoản nếu cần.

Sau khi online, có thể tắt máy cá nhân. Dữ liệu nằm trong MySQL Aiven và không mất chỉ vì Render deploy lại. Vẫn cần sao lưu database định kỳ; đừng coi CSV sinh viên là backup toàn bộ. Tắt/xóa service Render không tự xóa database Aiven. Xóa database Aiven là thao tác riêng và có thể mất dữ liệu.

Muốn tạm ngừng truy cập: dùng **Suspend service** trong Render nếu có trên giao diện tài khoản. Không xóa database chỉ để tắt website. Khi cần dùng lại, resume/redeploy dịch vụ.

## 8. Lỗi thường gặp

| Hiện tượng | Kiểm tra |
| --- | --- |
| Missing /etc/secrets/mysql-ca.pem | Thêm đúng Secret File và deploy lại, không chỉ tạo biến môi trường cùng tên. |
| Communications link failure / timeout | Aiven đang Running, đúng Host/Port, kiểm tra IP allowlist và kết nối mạng. |
| Access denied for user | Kiểm tra DB_USERNAME/DB_PASSWORD, không nhầm với tài khoản admin của ứng dụng. |
| SSL handshake / PKIX / certificate | Lấy lại CA đúng của dịch vụ Aiven hiện tại; dùng hostname Aiven, không IP. Giữ sslMode=VERIFY_IDENTITY. |
| Set ADMIN_PASSWORD… | Mật khẩu lần khởi tạo trống hoặc quá ngắn; sửa biến rồi deploy lại. |
| No open ports / Health check failed | Xem lỗi khởi động/database phía trước trong Logs; phải bật profile render, Health Check Path đúng. |
| Cannot find Dockerfile | Dockerfile ở gốc repo; Root Directory và Dockerfile Path phải khớp. |
| Đăng nhập không giữ phiên | Dùng URL HTTPS của Render; cookie của profile render chỉ gửi qua HTTPS. |
| Trang mở chậm sau thời gian không dùng | Đây có thể là lúc Render Free đang thức dậy. |
| Out of memory / exit 137 | Gói RAM thấp; kiểm tra Logs, nâng instance nếu tải vượt khả năng. |
| Chữ/logo còn như cũ | Ctrl+F5; kiểm tra bản build/deploy mới đã Live. |

## 9. Các file cấu hình đã thêm

- **Dockerfile**: build React, chạy kiểm thử/build Spring Boot, đóng gói Java 21 runtime.
- **render.yaml**: Blueprint mẫu, không chứa mật khẩu.
- **deploy/start-render.sh**: nạp chứng chỉ CA, chạy JAR với heap tối đa 256 MB.
- **application-render.properties**: nhận PORT, cookie HTTPS, MySQL TLS xác minh hostname, pool tối đa 5 kết nối; tắt seed demo.
- **BootstrapAdmin.java**: tạo quản trị viên đầu tiên bằng mật khẩu môi trường, không reset khi restart.
- **Health.java**: endpoint kiểm tra database, không công khai thông tin kết nối.

Bản đóng gói được kiểm thử tại môi trường phát triển; chưa deploy vào tài khoản Render/Aiven của bạn. Kiểm tra MySQL/TLS thực tế sẽ diễn ra ở lần deploy của bạn.

## Tài liệu chính thức

- https://render.com/docs/docker
- https://render.com/docs/web-services
- https://render.com/docs/free
- https://render.com/docs/configure-environment-variables
- https://aiven.io/docs/products/mysql/concepts/mysql-free-tier
- https://aiven.io/docs/products/mysql/howto/connect-from-mysql-workbench
