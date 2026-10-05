# NHẬT KÝ BÁO CÁO TIẾN ĐỘ DỰ ÁN

## Hotfix Render backend build - 06/10/2026
- **Đã làm được gì**: Bỏ `.mvn` khỏi `.dockerignore` vì Render build log tại commit `48410b9` cho thấy `COPY .mvn/ .mvn/` thất bại do thư mục bị loại khỏi Docker build context. Maven Wrapper `dependency:go-offline` và `clean package -DskipTests=true` chạy thành công bằng JDK 21. Docker image chưa được kiểm chứng vì Docker Desktop Linux engine không chạy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Docker multi-stage build, `.dockerignore`, Maven Wrapper, JDK 21.
- **Mục đích chức năng / việc đã làm**: Đưa Maven Wrapper cần thiết vào Docker build context để Render có thể đóng gói backend.
