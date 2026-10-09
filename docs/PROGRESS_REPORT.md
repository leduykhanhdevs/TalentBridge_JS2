# NHẬT KÝ BÁO CÁO TIẾN ĐỘ DỰ ÁN

## Hotfix Render backend build - 06/10/2026
- **Đã làm được gì**: Bỏ `.mvn` khỏi `.dockerignore` vì Render build log tại commit `48410b9` cho thấy `COPY .mvn/ .mvn/` thất bại do thư mục bị loại khỏi Docker build context. Maven Wrapper `dependency:go-offline` và `clean package -DskipTests=true` chạy thành công bằng JDK 21. Docker image chưa được kiểm chứng vì Docker Desktop Linux engine không chạy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Docker multi-stage build, `.dockerignore`, Maven Wrapper, JDK 21.
- **Mục đích chức năng / việc đã làm**: Đưa Maven Wrapper cần thiết vào Docker build context để Render có thể đóng gói backend.

---

## Chuẩn bị bootstrap Admin production an toàn - 09/10/2026
- **Đã làm được gì**: Thêm inbound use case tạo Admin từ thông tin do operator cấu hình, chỉ nối với `ApplicationRunner` khi profile `prod` và cờ `ADMIN_BOOTSTRAP_ENABLED=true`. Email được chuẩn hóa; mật khẩu được BCrypt hóa, yêu cầu ít nhất 16 ký tự và không vượt 72 byte UTF-8. Nếu email đã thuộc user thường thì từ chối nâng quyền; nếu Admin đã tồn tại thì giữ nguyên mật khẩu. Thêm unit tests và hướng dẫn vận hành Render. `./mvnw -B verify` chạy bằng JDK 21 thành công: 262 test, 0 lỗi. PR #14 mở vào `dev`; GitHub Backend Verify và Frontend Test/Lint/Build đều pass. Chưa tạo tài khoản production vì PR chưa được merge và operator chưa cấu hình secret/redeploy. Riêng CD run #37830109135 kết thúc xanh nhưng log xác nhận `RENDER_DEPLOY_HOOK_URL` rỗng, nên không gọi Render.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Hexagonal Architecture (inbound port/use case/outbound `UserRepositoryPort` và `PasswordEncoderPort`), Spring Boot `ApplicationRunner` chỉ trong profile `prod`, BCrypt, biến môi trường Render.
- **Mục đích chức năng / việc đã làm**: Tạo đường cấp Admin production không mở đăng ký Admin công khai, không đưa password mặc định vào production và không ghi password vào log. Sau bootstrap, operator tắt cờ và xóa biến chứa secret.
