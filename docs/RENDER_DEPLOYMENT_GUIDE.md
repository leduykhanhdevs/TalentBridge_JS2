# HƯỚNG DẪN TRIỂN KHAI TALENTBRIDGE LÊN NỀN TẢNG CLOUD RENDER

> **Tài liệu chuẩn hóa**: Triển khai trọn gói Full-stack (Spring Boot 3 + React 19 + Cloud MySQL) trên Render Free Tier.

---

## 1. TỔNG QUAN KIẾN TRÚC TRIỂN KHAI

```
┌─────────────────────────────────┐       ┌─────────────────────────────────┐
│     TALENTBRIDGE FRONTEND       │       │      TALENTBRIDGE BACKEND       │
│      Render Static Site         │──────►│       Render Web Service        │
│   (React 19 + Vite SPA dist)    │       │     (Docker Container JRE 21)   │
└─────────────────────────────────┘       └────────────────┬────────────────┘
                                                           │
                                                           ▼
                                          ┌─────────────────────────────────┐
                                          │         CLOUD MYSQL DB          │
                                          │     (Aiven / Clever Cloud)      │
                                          │    Bảo lưu 103 việc làm & 3NF   │
                                          └─────────────────────────────────┘
```

---

## 2. BƯỚC 1: KHỞI TẠO CƠ SỞ DỮ LIỆU CLOUD MYSQL MIỄN PHÍ

Để giữ nguyên vẹn 100% CSDL MySQL và 103 tin tuyển dụng mẫu mà không tốn phí:

1. Truy cập [Aiven.io](https://aiven.io/) hoặc [Clever Cloud](https://www.clever-cloud.com/) (đều có gói Free vĩnh viễn không cần thẻ tín dụng).
2. Tạo 1 dịch vụ **MySQL Free**:
   - Chọn Region gần nhất (Singapore hoặc Châu Á).
   - Đặt tên database: `talentbridge_db`.
3. Sau khi tạo xong, lấy thông tin kết nối:
   - **Host**: `mysql-xxx.aivencloud.com`
   - **Port**: `xxx`
   - **User**: `avnadmin` (hoặc root)
   - **Password**: `xxx`
   - **JDBC URL**: `jdbc:mysql://<Host>:<Port>/talentbridge_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh`
4. Chạy tệp `database/schema.sql` vào database mới tạo để khởi tạo 27 bảng.

---

## 3. BƯỚC 2: TRIỂN KHAI BACKEND LÊN RENDER

1. Đăng nhập [Render Dashboard](https://dashboard.render.com/).
2. Chọn **New +** $\to$ **Web Service**.
3. Kết nối với GitHub Repository: `leduykhanhdevs/TalentBridge_JS2`.
4. Điền các thông số:
   - **Name**: `talentbridge-backend`
   - **Region**: Singapore
   - **Branch**: `main`
   - **Runtime**: `Docker` (Render sẽ tự động dùng file `Dockerfile` đã tối ưu hóa).
   - **Instance Type**: `Free`
5. Thêm các biến môi trường (**Environment Variables**):
   - `SPRING_PROFILES_ACTIVE`: `prod`
   - `SPRING_DATASOURCE_URL`: *(Điền JDBC URL từ Bước 1)*
   - `SPRING_DATASOURCE_USERNAME`: *(Điền user MySQL)*
   - `SPRING_DATASOURCE_PASSWORD`: *(Điền password MySQL)*
   - `TALENTBRIDGE_JWT_SECRET`: *(Tạo 1 chuỗi ngẫu nhiên tối thiểu 32 ký tự)*
   - `GEMINI_API_KEY`: *(Điền Gemini API Key của bạn)*
   - `CORS_ALLOWED_ORIGINS`: `*` *(sau khi có link frontend sẽ đổi lại link frontend)*
6. Bấm **Create Web Service**. Đợi 3-5 phút để Render build và khởi chạy.
7. Khi thành công, copy URL Backend: `https://talentbridge-backend-xxxx.onrender.com`.

---

## 4. BƯỚC 3: TRIỂN KHAI FRONTEND LÊN RENDER

1. Tại Render Dashboard, chọn **New +** $\to$ **Static Site**.
2. Kết nối với GitHub Repository: `leduykhanhdevs/TalentBridge_JS2`.
3. Cấu hình thông số:
   - **Name**: `talentbridge-frontend`
   - **Branch**: `main`
   - **Build Command**: `cd frontend && npm install && npm run build`
   - **Publish Directory**: `frontend/dist`
4. Thêm biến môi trường:
   - `VITE_API_BASE_URL`: `https://talentbridge-backend-xxxx.onrender.com/api/v1` *(URL Backend lấy từ Bước 2)*
5. Cấu hình định tuyến Single Page Application (SPA):
   - Vào mục **Redirects/Rewrites** $\to$ Thêm quy tắc:
     - **Type**: `Rewrite`
     - **Source**: `/*`
     - **Destination**: `/index.html`
6. Bấm **Create Static Site**.

---

## 5. BƯỚC 4: KÍCH HOẠT CI/CD TỰ ĐỘNG (CD DEPLOY HOOK)

1. Tại Web Service Backend trên Render:
   - Vào **Settings** $\to$ cuộn xuống mục **Deploy Hook**.
   - Bấm **Create Deploy Hook** $\to$ Copy URL Webhook.
2. Tại GitHub Repository:
   - Vào **Settings** $\to$ **Secrets and variables** $\to$ **Actions**.
   - Bấm **New repository secret**.
   - Name: `RENDER_DEPLOY_HOOK_URL`.
   - Value: *(Dán URL Webhook vừa copy)*.
3. **Hoàn tất**: Kể từ bây giờ, mỗi khi bạn merge mã nguồn vào nhánh `main`, GitHub Actions workflow `cd-render.yml` sẽ tự động bắn webhook yêu cầu Render kéo bản mới nhất về cập nhật!

---

## 6. GIẢI QUYẾT CÁC ĐẶC THÙ CỦA RENDER FREE TIER

1. **Cold Start (Ngủ đông sau 15 phút idle)**:
   - Gói miễn phí của Render sẽ tạm dừng container nếu không có truy cập trong 15 phút. Khi có lượt truy cập mới, container mất khoảng 30-45 giây để thức dậy.
   - **Mẹo**: Dùng dịch vụ miễn phí [UptimeRobot](https://uptimerobot.com/) ping URL `/actuator/health` 10 phút một lần để giữ Backend luôn thức 24/7.
2. **Bộ nhớ RAM 512MB**:
   - `Dockerfile` của dự án đã được cài đặt sẵn cờ tối ưu:
     `-XX:+UseSerialGC -XX:MaxRAMPercentage=70.0 -Xss256k`
   - Đảm bảo Spring Boot 3 chỉ tiêu thụ ~280MB - 350MB RAM, tuyệt đối không bị dính lỗi tràn RAM (OOM exit 137).
