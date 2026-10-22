# Kết quả UAT Candidate/Recruiter — 23/10/2026

## Xác minh local

- `JAVA_HOME=<JDK 21> .\mvnw.cmd -B verify`: **378 tests**, 0 failure/error/skip; `BUILD SUCCESS`.
- `npm test` trong `frontend`: **135 tests**, 23 files passed.
- `npm run lint`: pass.
- `npm run build`: TypeScript và Vite production build pass.
- `scripts/run-e2e.ps1`: API E2E **28/28** và Playwright UI **30/30** trên backend H2 cô lập.
- `git diff --check`: pass.

Lần chạy full-site đạt cuối cùng lưu log và ảnh ở `docs/qa/full-site-uat-2026-10-20/runs/2026-10-23-015610/`. Các lần chạy trước có lưu log thất bại trong workspace để giữ dấu vết khắc phục; chỉ kết quả trên là lần xác minh cuối.

## Giới hạn xác minh

- E2E dùng API response mock và dữ liệu H2 cục bộ; không sử dụng CV/tài khoản production.
- Candidate job-fit chạy matching xác định trên backend; chưa xác minh lời gọi Gemini thật vì cần `GEMINI_API_KEY` do chủ dự án cấu hình.
- Google Calendar mở liên kết ngoài; mạng/DNS của browser có thể chặn dịch vụ. Luồng tải `.ics` qua ứng dụng đã được kiểm thử riêng và yêu cầu xác thực.
- Các kết quả local này không thay thế GitHub Actions, Render CD, production health hoặc kiểm tra build SHA sau deploy.

## Trạng thái phát hành

PR #65 đã merge vào `main` bằng commit `5132b0b28da8a82ce52b8da0d33e727e2e6d381c`. Cả ba job GitHub CI (backend, frontend, Docker) pass. Render CD run `39837408472` pass: backend health `UP` và `buildCommit` đúng; frontend HTML marker đúng SHA; `/api/v1/jobs`, trang frontend và CORS preflight đều HTTP 200. Lần triển khai backend mất khoảng 4 phút để khởi động; không coi các probe timeout trước khi Render sẵn sàng là pass.
