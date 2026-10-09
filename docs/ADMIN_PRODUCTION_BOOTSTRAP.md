# Tạo tài khoản Admin đầu tiên trên Render

Production không chạy `DataInitializer` của local/dev và không có API đăng ký công khai vai trò Admin. Quy trình này dùng một lần bootstrap phía server, chỉ bật khi `SPRING_PROFILES_ACTIVE=prod` và `ADMIN_BOOTSTRAP_ENABLED=true`.

## Cấu hình trên Render

Sau khi PR chứa bootstrap đã được review, merge vào `main` và backend deploy thành công:

1. Mở **talentbridge-backend → Environment**.
2. Thêm `ADMIN_BOOTSTRAP_ENABLED=true`.
3. Thêm `ADMIN_BOOTSTRAP_EMAIL` với email Admin bạn muốn dùng.
4. Tự tạo và lưu an toàn `ADMIN_BOOTSTRAP_PASSWORD`: tối thiểu 16 ký tự, tối đa 72 byte UTF-8 để tương thích BCrypt. Không dùng mật khẩu mẫu trong `DataInitializer`.
5. Có thể đặt `ADMIN_BOOTSTRAP_FULL_NAME`; nếu bỏ trống hệ thống dùng `TalentBridge Administrator`.
6. Lưu thay đổi và redeploy backend. Khi log báo “Initial production administrator was created…”, lần đăng nhập đầu đã được tạo. Mật khẩu không được ghi vào log; tài khoản đã tồn tại với vai trò Admin sẽ không bị đổi mật khẩu.
7. Ngay sau khi xác nhận tạo thành công, đặt `ADMIN_BOOTSTRAP_ENABLED=false`, xóa `ADMIN_BOOTSTRAP_PASSWORD` và `ADMIN_BOOTSTRAP_EMAIL` khỏi Render rồi lưu lại. Tài khoản và mật khẩu đã băm trong DB vẫn tồn tại.

Nếu email đã thuộc một tài khoản không phải Admin, bootstrap sẽ dừng với lỗi rõ ràng thay vì tự nâng quyền tài khoản đó. Hãy chọn email khác hoặc xử lý danh tính hiện có theo quy trình quản trị có kiểm soát. Không bật bootstrap nếu chưa lưu mật khẩu vì hệ thống yêu cầu biến này trước khi chạy.

## Giới hạn hiện tại

Việc thêm mã không tự tạo tài khoản production. Cần review/merge PR, cấu hình biến môi trường bí mật trên Render, redeploy và xác nhận log. Không đưa password hoặc giá trị secret vào Git, issue, chat hay log.
