# Hướng dẫn triển khai TalentBridge trên Render và Amazon RDS

Tài liệu này mô tả cấu hình hiện tại của TalentBridge: React/Vite trên Render Static Site, Spring Boot 3 trên Render Web Service và MySQL trên Amazon RDS Singapore. Mật khẩu, JWT secret, Gemini key và Deploy Hook URL chỉ lưu trong dashboard tương ứng; không ghi chúng vào Git hoặc chat.

## Kiến trúc triển khai

| Thành phần | Nền tảng | Cấu hình |
| --- | --- | --- |
| Frontend | Render Static Site | React 19, Vite, thư mục xuất bản frontend/dist |
| Backend | Render Web Service | Spring Boot 3, Docker, Java 21 |
| Database | Amazon RDS for MySQL | Singapore (ap-southeast-1), TLS VERIFY_IDENTITY |

Tên các dịch vụ Render trong Blueprint là talentbridge-frontend và talentbridge-backend. Blueprint hiện đặt autoDeploy: false; phát hành production được kích hoạt qua GitHub Actions khi có push vào main.

## Biến môi trường backend

Lưu các giá trị sau trong **Render → talentbridge-backend → Environment**:

- SPRING_PROFILES_ACTIVE=prod
- SPRING_DATASOURCE_URL: JDBC URL trỏ tới schema TalentBridge trên RDS; bật sslMode=VERIFY_IDENTITY và truststore /app/rds-ca-truststore.p12.
- SPRING_DATASOURCE_USERNAME và SPRING_DATASOURCE_PASSWORD: tài khoản runtime có quyền DML, không dùng master account RDS.
- SPRING_FLYWAY_USER và SPRING_FLYWAY_PASSWORD: tài khoản migration có quyền DDL; nếu để trống, cấu hình hiện tại dùng lại tài khoản datasource.
- TALENTBRIDGE_JWT_SECRET: chuỗi ngẫu nhiên bảo mật. Ứng dụng vẫn hỗ trợ alias cũ JWT_SECRET.
- CORS_ALLOWED_ORIGINS: URL origin chính xác của frontend, không thêm đường dẫn API và không dùng * trong production.
- GEMINI_API_KEY: tùy chọn nếu bật gọi Gemini; chatbot nội bộ vẫn có tri thức dự phòng khi nhà cung cấp AI không khả dụng.
- RATE_LIMIT_ENABLED=true.

Mẫu URL JDBC có cấu trúc:

jdbc:mysql://<RDS-endpoint>:3306/<database>?sslMode=VERIFY_IDENTITY&trustCertificateKeyStoreUrl=file:/app/rds-ca-truststore.p12&trustCertificateKeyStoreType=PKCS12&trustCertificateKeyStorePassword=changeit&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8

Thay endpoint và tên database theo giá trị đang dùng. changeit bảo vệ truststore chứa CA công khai, không phải mật khẩu database. Không lưu mật khẩu trong URL.

Nếu bootstrap Admin được bật tạm thời, sau khi xác nhận tài khoản đã được tạo hãy tắt ADMIN_BOOTSTRAP_ENABLED và xóa các biến bootstrap khỏi Render.

## Cấu hình frontend

Blueprint dùng VITE_API_BASE_URL trỏ tới backend, ví dụ:

https://talentbridge-backend-6rmd.onrender.com/api/v1

render.yaml chạy build frontend với VITE_RENDER_GIT_COMMIT lấy từ biến build có sẵn RENDER_GIT_COMMIT. HTML đầu ra có meta render-git-commit, còn health API trả data.buildCommit. Hai dấu build này cho phép CD đối chiếu đúng commit đang được phục vụ. Giá trị mặc định unknown chỉ dành cho build ngoài Render.

Sau khi thay đổi render.yaml, kiểm tra Blueprint đã đồng bộ cấu hình build mới cho static site trước khi chạy release gate. SPA rewrite /* → /index.html được khai báo trong Blueprint.

## Thiết lập GitHub Actions CD

Tạo hai Deploy Hook riêng vì frontend và backend là hai service Render độc lập:

1. Trên service backend: **Settings → Deploy Hook**. Lưu URL trong GitHub Actions secret RENDER_BACKEND_DEPLOY_HOOK_URL. Tương thích ngược: workflow còn đọc secret hiện tại RENDER_DEPLOY_HOOK_URL nếu secret mới chưa có.
2. Trên service frontend: tạo Deploy Hook riêng và lưu trong secret RENDER_FRONTEND_DEPLOY_HOOK_URL.
3. Không đưa Deploy Hook URL vào file, issue, log công khai hoặc chat. URL này cho phép kích hoạt deploy.
4. Workflow .github/workflows/cd-render.yml chạy khi push lên main. Nó gửi đúng commit SHA tới cả hai hook, chờ health API và HTML báo đúng SHA, rồi kiểm tra health, API việc làm, tiêu đề trang và CORS credentials.

Workflow không cần Render API key. Các URL có thể cấu hình qua GitHub Actions variables RENDER_BACKEND_URL và RENDER_FRONTEND_URL; nếu bỏ trống, workflow dùng hai URL onrender.com trong file workflow.

## Quy trình phát hành

1. Tạo nhánh fix/... hoặc feat/..., chạy test phù hợp, rồi mở PR vào dev.
2. Review và merge PR vào dev theo quy trình nhóm.
3. Khi dev đã được kiểm tra, mở PR phát hành từ dev sang main.
4. Sau khi PR phát hành được merge, GitHub Actions chạy CI/CD. Release chỉ được coi là thành công khi cả hai dịch vụ Render đang phục vụ đúng commit và production smoke tests đều pass.
5. Nếu workflow báo thiếu secret, thêm secret tương ứng trong **GitHub → Settings → Secrets and variables → Actions**; không gửi secret cho người khác qua chat.

Không commit hoặc push trực tiếp lên main/dev, không merge PR khi leader chưa review và xác nhận.

## Giới hạn và chẩn đoán

- Render Free có thể ngủ khi ít truy cập; request đầu tiên sau thời gian idle có thể chậm. Việc tối ưu frontend không loại bỏ giới hạn cold start của hạ tầng.
- Nếu CD hết thời gian chờ backend, kiểm tra service đã nhận commit trên main, build mới thành công và /api/v1/health trả data.buildCommit đúng SHA của workflow.
- Nếu CD hết thời gian chờ frontend, kiểm tra Blueprint đã sync build command có VITE_RENDER_GIT_COMMIT=$RENDER_GIT_COMMIT và HTML có meta render-git-commit đúng SHA.
- Nếu jobs API trả HTTP 200 nhưng 0 việc làm, điều đó xác nhận endpoint/DB query phản hồi chứ không chứng minh đã có dữ liệu nghiệp vụ. Cần kiểm tra số lượng dữ liệu RDS riêng.
- Nếu preflight CORS fail, kiểm tra CORS_ALLOWED_ORIGINS khớp chính xác frontend origin, không có dấu / cuối và không có wildcard.
