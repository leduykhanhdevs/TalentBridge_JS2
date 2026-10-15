# Tạo tài khoản Admin đầu tiên trên Render

Production không chạy `DataInitializer` của local/dev và không có API đăng ký công khai vai trò Admin. Quy trình chỉ chạy phía server, trong profile `prod` và khi `ADMIN_BOOTSTRAP_ENABLED=true`.

## Cấu hình trên Render

Sau khi PR chứa bootstrap đã được review, merge vào `main` và backend deploy thành công:

1. Mở **talentbridge-backend → Environment**.
2. Thêm `ADMIN_BOOTSTRAP_ENABLED=true`.
3. Thêm `ADMIN_BOOTSTRAP_EMAIL` với email chính xác sẽ dùng làm Admin.
4. Nếu email **chưa tồn tại** trong DB, tạo `ADMIN_BOOTSTRAP_PASSWORD` an toàn (tối thiểu 16 ký tự, tối đa 72 byte UTF-8 cho BCrypt). Không dùng mật khẩu mẫu trong `DataInitializer`.
5. Nếu email thuộc tài khoản đang có, chỉ bật `ADMIN_BOOTSTRAP_PROMOTE_EXISTING=true` khi chủ hệ thống đã phê duyệt nâng chính tài khoản đó. Cờ này mặc định `false`; chỉ tài khoản `ACTIVE` được nâng. Mật khẩu, hồ sơ, trạng thái và các role khác được giữ nguyên. Khi bật cờ này, email không khớp tài khoản hiện có sẽ làm ứng dụng dừng thay vì tạo tài khoản mới.
6. Có thể đặt `ADMIN_BOOTSTRAP_FULL_NAME`; với tài khoản mới, nếu bỏ trống hệ thống dùng `TalentBridge Administrator`. Nâng quyền tài khoản hiện có không đổi tên.
7. Lưu thay đổi và redeploy backend. Log phân biệt tài khoản mới được tạo, tài khoản hiện có được nâng quyền, hoặc tài khoản đã là Admin; không ghi password/email vào log.
8. Ngay sau khi xác nhận đúng tài khoản có `ROLE_ADMIN`, đặt `ADMIN_BOOTSTRAP_ENABLED=false` và xóa `ADMIN_BOOTSTRAP_PROMOTE_EXISTING`, `ADMIN_BOOTSTRAP_PASSWORD` cùng `ADMIN_BOOTSTRAP_EMAIL` khỏi Render rồi lưu lại. Tài khoản và role vẫn tồn tại trong DB.

## Mở khóa một Admin hiện có (thao tác một lần)

Chỉ dùng khi đã xác minh chủ tài khoản và trạng thái trong production là `LOCKED` do đăng nhập sai:

1. Bật `ADMIN_BOOTSTRAP_ENABLED=true` và đặt `ADMIN_BOOTSTRAP_EMAIL` đúng email của Admin đang bị khóa.
2. Đặt `ADMIN_BOOTSTRAP_REACTIVATE_LOCKED_ADMIN=true`; không cần đọc hoặc đổi `ADMIN_BOOTSTRAP_PASSWORD`.
3. Redeploy backend. Use case chỉ đổi trạng thái của tài khoản có sẵn vừa khớp email, có `ROLE_ADMIN` và đang `LOCKED` thành `ACTIVE`. Nó giữ nguyên password hash, hồ sơ và roles; tài khoản `BANNED`, `INACTIVE`, `PENDING`, hoặc không phải Admin không được mở khóa bởi cờ này.
4. Xác minh log có kết quả reactivation, sau đó đăng nhập thử. Ngay lập tức đặt `ADMIN_BOOTSTRAP_ENABLED=false` và xóa các biến bootstrap khỏi Render; nếu password bootstrap từng bị lộ thì xoay secret rồi gỡ bỏ, không tái sử dụng.

Cờ `ADMIN_BOOTSTRAP_REACTIVATE_LOCKED_ADMIN` mặc định `false` và chỉ có hiệu lực trong profile `prod` khi bootstrap được bật. Không giữ cờ bootstrap bật sau khi hoàn tất.

Nếu email đã thuộc tài khoản không phải Admin mà cờ promotion không được bật, bootstrap sẽ dừng với lỗi rõ ràng. Không bật cờ promotion cho email mới, sai chính tả, hoặc tài khoản không còn hoạt động. Không đưa mật khẩu vào Git, issue, chat hay log.

## Giới hạn hiện tại

Việc thêm mã không tự tạo/nâng tài khoản production. Cần review/merge PR, cấu hình biến môi trường trên Render, redeploy và xác nhận log cùng khả năng đăng nhập. Hãy tắt bootstrap ngay sau khi hoàn tất.
