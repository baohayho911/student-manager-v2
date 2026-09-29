# Kiểm thử và kịch bản trình diễn

## Kết quả đã thực hiện

- Backend: **19 kiểm thử tích hợp đạt**, 0 lỗi, 0 bỏ qua. Spring Boot chạy với H2 trong bộ nhớ riêng.
- Frontend: build Vite thành công.
- Bản JAR đóng gói: đã chạy giao diện tĩnh, đăng nhập, trang sinh viên và danh mục lớp, không dùng tiến trình Vite.
- Trình duyệt Chromium: các luồng bên dưới đã thao tác tự động qua giao diện, không chỉ gọi API.
- Đã xem ảnh giao diện đăng nhập, tổng quan, bảng sinh viên và màn hình điện thoại.
- Môi trường xác minh: Linux, Java 17, Node.js 24. Chưa trực tiếp chạy script Windows hoặc máy chủ MySQL thật.

## Các kiểm thử backend

1. Người chưa đăng nhập không đọc được dữ liệu quản trị.
2. Sai mật khẩu bị từ chối.
3. Đăng nhập cần CSRF; sinh viên không vào API ADMIN.
4. Xóa sinh viên chưa có lịch sử đồng thời xóa tài khoản liên kết.
5. Chặn xóa sinh viên đã có lịch sử đăng ký.
6. Xóa riêng tài khoản giữ hồ sơ/điểm và thu hồi phiên cũ.
7. Khóa tài khoản từ chối đăng nhập và phiên đang hoạt động.
8. Reset mật khẩu thu hồi phiên và bắt buộc đổi mật khẩu mới.
9. Chặn đăng ký trùng, cho hủy và đăng ký lại cùng bản ghi.
10. Sinh viên không hủy được đăng ký của người khác.
11. Điểm chưa chốt bị ẩn; chốt cần đóng đăng ký; điểm đã chốt không sửa được; mở lại chỉ ADMIN, có lý do.
12. Không hủy đăng ký sau khi đã nhập bất kỳ điểm nào.
13. Sinh viên không đọc danh sách lớp/ghi điểm bằng API giảng viên.
14. Từ chối điểm vượt 10 hoặc quá hai chữ số thập phân.
15. Nhập CSV gặp dòng lỗi hoàn tác toàn bộ batch và tài khoản đã tạo trong batch.
16. Hai sinh viên cùng đăng ký suất cuối: chỉ một người thành công.
17. GPA tích lũy xử lý học lại bằng điểm tổng kết cao nhất, chỉ tính môn một lần.
18. Giảng viên không xem bảng điểm lớp của giảng viên khác.
19. Mật khẩu có khoảng trắng đầu/cuối vẫn được xác minh đúng, không tự trim.

Mã kiểm thử: `backend/src/test/java/vn/studentmanager/ApplicationTest.java`.

## Các luồng giao diện đã chạy

| Luồng | Kết quả |
|---|---|
| ADMIN tạo sinh viên kèm tài khoản | Đạt |
| Tài khoản mới đổi mật khẩu lần đầu và đăng nhập lại | Đạt |
| ADMIN xóa hồ sơ/tài khoản; phiên người bị xóa trở về đăng nhập ở yêu cầu tiếp theo | Đạt |
| Cấp tài khoản cho hồ sơ có sẵn chưa liên kết | Đạt |
| Nhập sinh viên qua CSV | Đạt |
| Giảng viên nhập điểm lớp phụ trách | Đạt |
| Sinh viên đăng ký, xem bảng điểm và hủy học phần chưa có điểm | Đạt |
| Màn hình điện thoại không tràn toàn trang, bảng cuộn ngang | Đạt |

## Kịch bản demo đồ án

1. Chạy ứng dụng, đăng nhập ADMIN, xem thống kê và danh mục.
2. Tạo sinh viên TEST01, chọn lớp, đặt mật khẩu ban đầu.
3. Mở cửa sổ ẩn danh, đăng nhập test01, đổi mật khẩu theo yêu cầu.
4. Sinh viên đăng ký một lớp mở. ADMIN quan sát sĩ số tăng.
5. ADMIN đóng đăng ký của lớp vừa chọn.
6. Đăng nhập giảng viên phụ trách, vào Danh sách / Điểm và nhập đủ điểm cho tất cả sinh viên.
7. Chốt điểm. Sinh viên tải lại bảng điểm, xem tổng kết và GPA.
8. ADMIN mở lại điểm với lý do; giảng viên sửa điểm với lý do; ADMIN xem nhật ký.
9. ADMIN khóa tài khoản test01; tài khoản này không gọi API tiếp được.
10. Mở khóa; thử xóa hồ sơ test01: bị chặn do có lịch sử học tập.
11. Xóa riêng tài khoản test01: hồ sơ và điểm vẫn còn. Cấp tài khoản lại từ hồ sơ nếu muốn tiếp tục.
12. Tạo một hồ sơ khác chưa đăng ký, rồi xóa hồ sơ cùng tài khoản qua cửa sổ xác nhận.

Không đánh dấu toàn bộ hệ thống “đạt” chỉ từ những kiểm thử này. Khi bổ sung chức năng hoặc thay đổi MySQL/quy chế, cần kiểm tra lại các luồng liên quan.

## Cập nhật HaUI và Render — 29/09/2026

- Backend: 21 kiểm thử đạt, 0 lỗi; bao gồm hai kiểm thử mới về khởi tạo quản trị riêng, giữ mật khẩu khi restart và health public.
- Frontend production build đạt. JAR chạy độc lập đã kiểm tra đăng nhập, dữ liệu, logo tải thành công, cỡ chữ gốc 16px, danh mục và màn hình 390px không tràn trang.
- Script entrypoint đã kiểm tra cú pháp sh. Chưa chạy Docker build hoặc deploy thực trên Render; kiểm thử profile render dùng H2 thay MySQL nên chưa xác thực kết nối Aiven/TLS trên môi trường thật.
