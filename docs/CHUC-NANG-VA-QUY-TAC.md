# Phạm vi chức năng và quy tắc nghiệp vụ

Đây là bản đồ án có thể chạy trọn luồng quản lý hồ sơ, học phần và điểm; không phải toàn bộ hệ thống vận hành của một trường đại học. Các quy tắc điểm là quy ước minh họa của dự án, cần đối chiếu yêu cầu giảng viên trước khi dùng làm quy chế trường.

## Vai trò

| Vai trò | Quyền |
|---|---|
| ADMIN | Danh mục, hồ sơ, lớp học phần, đăng ký thay sinh viên, điểm, chốt/mở điểm, tài khoản, nhật ký |
| TEACHER | Chỉ lớp được phân công; xem danh sách, nhập/sửa điểm khi chưa chốt, chốt điểm khi đủ điều kiện |
| STUDENT | Xem hồ sơ mình; tra cứu lớp mở, đăng ký/hủy; xem điểm đã công bố của mình |

Quyền được kiểm tra ở backend. Giấu nút ở giao diện không phải cơ chế bảo vệ duy nhất. Có phiên cookie HttpOnly, SameSite=Lax và CSRF cho thao tác thay đổi dữ liệu. Mật khẩu băm BCrypt. Mật khẩu mới ít nhất 12 ký tự và tối đa 72 byte UTF-8.

## Đã triển khai

- Khoa, ngành, lớp hành chính, môn học/tín chỉ, học kỳ: thêm/sửa/xóa có kiểm tra liên kết.
- Sinh viên: thông tin liên hệ, ngày sinh, giới tính, lớp và trạng thái đang học/bảo lưu/thôi học/tốt nghiệp.
- Giảng viên: thông tin liên hệ, khoa và liên kết tài khoản.
- Tạo sinh viên/giảng viên đồng thời tạo tài khoản trong một giao dịch; lỗi thì hoàn tác.
- Tên đăng nhập lấy từ mã hồ sơ chuyển chữ thường. Sửa mã hồ sơ sau này không tự đổi tên đăng nhập đã cấp.
- Quản lý tài khoản: cấp lại cho hồ sơ chưa có tài khoản, reset mật khẩu, khóa/mở khóa, xóa riêng tài khoản.
- Khóa/xóa/reset/đổi mật khẩu làm phiên cũ mất quyền ở yêu cầu API kế tiếp; không giả định rằng đóng giao diện trên máy người dùng có thể xóa dữ liệu họ đã xem.
- Tài khoản mới/reset bắt buộc đổi mật khẩu; người dùng được tự đổi mật khẩu sau khi xác minh mật khẩu hiện tại.
- Không khóa/xóa ADMIN qua chức năng quản trị này, tránh mất tài khoản quản lý cuối cùng.
- Lớp học phần: chọn môn/học kỳ/giảng viên, sĩ số, phòng, lịch mô tả, trạng thái, tỷ trọng.
- Đăng ký: kiểm tra sinh viên đang học, lớp mở, không vượt sĩ số, không đăng ký hai lớp của cùng môn trong cùng học kỳ.
- Khóa hàng dữ liệu sinh viên và lớp trong giao dịch để bảo vệ đăng ký đồng thời.
- Hủy chỉ khi lớp đang mở, chưa chốt và chưa có bất kỳ bản ghi điểm. Giữ lịch sử đăng ký đã hủy.
- Điểm TX1, TX2, KTHP: từ 0 đến 10, tối đa 2 chữ số thập phân; nhập đủ ba thành phần.
- Tổng tỷ trọng 100%; không sửa tỷ trọng khi đã có điểm. Không đổi tín chỉ của môn đã có lịch sử đăng ký.
- Chốt điểm cần lớp đã đóng đăng ký và mọi đăng ký đang hoạt động đã có điểm.
- Giảng viên chỉ nhập/chốt lớp mình phụ trách. Chỉ ADMIN mở lại điểm, bắt buộc lý do.
- Sửa điểm đã có bắt buộc lý do. Nhật ký ghi người sửa, thời điểm, điểm cũ/mới.
- Sinh viên chỉ thấy điểm sau khi chốt; mở lại thì tạm ẩn điểm khỏi bảng điểm và GPA đến lần chốt tiếp theo.
- Tổng quan theo vai trò, lọc danh sách lớp theo học kỳ, tìm kiếm/sắp xếp/phân trang phía trình duyệt.
- Nhập sinh viên CSV trong giao dịch, xuất CSV cho các bảng.
- Nhật ký 500 thao tác gần nhất; quy trình sao lưu/khôi phục bằng tệp H2 hoặc MySQL Workbench.

## Quy tắc xóa

| Đối tượng | Xử lý |
|---|---|
| Chỉ tài khoản | Gỡ liên kết rồi xóa tài khoản; giữ hồ sơ, đăng ký và điểm |
| Sinh viên chưa có lịch sử đăng ký | Sau xác nhận, xóa hồ sơ và tài khoản liên kết cùng giao dịch |
| Sinh viên đã có lịch sử, kể cả đã hủy | Chặn xóa hồ sơ; có thể khóa/xóa riêng tài khoản hoặc chuyển trạng thái học tập |
| Giảng viên đang phụ trách lớp | Chặn xóa hồ sơ; chuyển phân công trước |
| Giảng viên chưa có lớp phụ trách | Xóa hồ sơ và tài khoản sau xác nhận |
| Danh mục được sử dụng/lớp có đăng ký | Không xóa; trả thông báo xung đột |

Khóa tài khoản và trạng thái học tập là hai thuộc tính riêng. Ví dụ bảo lưu sẽ ngăn đăng ký mới, nhưng không tự khóa quyền xem thông tin. ADMIN quyết định khóa tài khoản khi cần.

## Tính điểm và GPA

Tổng kết = (TX1 × tỷ trọng TX1 + TX2 × tỷ trọng TX2 + KTHP × tỷ trọng KTHP) / 100. Làm tròn HALF_UP đến 2 chữ số.

| Tổng kết đã làm tròn | Điểm chữ | Điểm hệ 4 |
|---|---|---|
| 8.50–10 | A | 4 |
| 7.00–8.49 | B | 3 |
| 5.50–6.99 | C | 2 |
| 4.00–5.49 | D | 1 |
| Dưới 4.00 | F | 0 |

GPA = tổng (điểm hệ 4 × tín chỉ) / tổng tín chỉ được tính; bao gồm môn F. Chỉ lấy đăng ký ACTIVE đã được chốt và có điểm. GPA học kỳ tính các lần học trong học kỳ đó. GPA tích lũy chọn lần có tổng kết cao nhất của từng mã môn, mỗi môn chỉ tính một lần. Tín chỉ đạt là tín chỉ của môn có điểm A/B/C/D theo cùng phạm vi. Chưa có điểm thì GPA hiển thị “—”, không giả thành 0.

## Giới hạn đã xác định

- Lịch học là văn bản, chưa kiểm tra trùng tiết/phòng/giảng viên.
- Chưa có môn tiên quyết, khung chương trình theo khóa, hạn mức tín chỉ hoặc đợt đăng ký tự động theo ngày.
- Chưa xử lý điểm liệt, vắng thi, miễn học, học phần không tính GPA và quy tắc riêng từng trường.
- Chưa có tài liệu học phần, học phí, thanh toán, điểm danh, thông báo email, phúc khảo hoặc xét tốt nghiệp tự động.
- Chưa có quên mật khẩu qua email; người dùng liên hệ ADMIN.
- CSV tương thích Excel; không đọc trực tiếp XLSX.
- Các bảng tải dữ liệu để tìm kiếm/phân trang ở client; phù hợp dữ liệu đồ án, chưa tối ưu cho hàng trăm nghìn hồ sơ.
- Dùng Spring JDBC, không phải JPA. React JavaScript/CSS, không bắt buộc TypeScript/Tailwind/JWT như PDF mẫu.
- H2 là chế độ chạy nhanh. Có migration/cấu hình MySQL, nhưng chưa kiểm thử trên MySQL thật trong môi trường xây dựng này.
- Chưa đánh giá tải lớn, kiểm thử xâm nhập hay triển khai Internet. Chế độ mặc định chỉ truy cập trên máy cục bộ.
