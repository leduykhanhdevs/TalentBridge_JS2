# NHẬT KÝ BÁO CÁO TIẾN ĐỘ DỰ ÁN

## Khắc phục gửi yêu cầu thành lập doanh nghiệp - 09/10/2026
- **Đã làm được gì**: Đồng bộ kiểm tra website/logo với DTO backend, hiển thị lỗi ngay dưới trường nhập và hỗ trợ trình đọc màn hình; bỏ thao tác tải tệp tạo data URI không được API chấp nhận. Xác minh `pnpm test` (88/88), `pnpm lint` và `pnpm build` đều thành công.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Hàm kiểm tra TypeScript thuần dùng `URL` để xác nhận URL tuyệt đối HTTP(S), kết hợp giới hạn độ dài trong `RequestCreateCompanyRequest.java`; kiểm thử Vitest.
- **Mục đích chức năng / việc đã làm**: Ngăn yêu cầu thất bại vì nhập email vào Website hoặc gửi logo dạng base64; hướng dẫn dùng URL ảnh công khai hoặc để trống logo, đồng thời hiển thị lỗi backend validation dễ xử lý.

## Hotfix Render backend build - 06/10/2026
- **Đã làm được gì**: Bỏ `.mvn` khỏi `.dockerignore` vì Render build log tại commit `48410b9` cho thấy `COPY .mvn/ .mvn/` thất bại do thư mục bị loại khỏi Docker build context. Maven Wrapper `dependency:go-offline` và `clean package -DskipTests=true` chạy thành công bằng JDK 21. Docker image chưa được kiểm chứng vì Docker Desktop Linux engine không chạy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Docker multi-stage build, `.dockerignore`, Maven Wrapper, JDK 21.
- **Mục đích chức năng / việc đã làm**: Đưa Maven Wrapper cần thiết vào Docker build context để Render có thể đóng gói backend.
