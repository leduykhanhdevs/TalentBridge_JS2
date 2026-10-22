# Kết quả UAT Candidate/Recruiter — 23/10/2026

## Xác minh local

- Trước phát hành PR #65, `JAVA_HOME=<JDK 21> .\mvnw.cmd -B verify`: **378 tests**, 0 failure/error/skip; `BUILD SUCCESS`.
- Trên nhánh hậu kiểm AI: Maven verify **379 tests**, 0 failure/error/skip, `BUILD SUCCESS`; log tại `docs/qa/full-site-uat-2026-10-20/runs/2026-10-23-025230/maven-verify-final.log`.
- `pnpm test` trong `frontend`: **135 tests**, 23 files passed; `pnpm run lint` và `pnpm run build` (TypeScript + Vite) pass.
- API E2E: **28/28** trên backend H2 cô lập.
- Playwright UI run đầy đủ: **30/30**, gồm luồng Candidate tự đăng ký, đăng xuất rồi đăng nhập lại, tại `runs/2026-10-23-024122/`.
- Hậu kiểm sau sửa nhãn nguồn Gemini: Playwright **29/29** tại `runs/2026-10-23-025230/`; chỉ bỏ qua đúng test tạo tài khoản vì test đăng ký đã đạt ở run trước và có bước chấp nhận Điều khoản. Tất cả luồng UI còn lại vẫn chạy.
- `git diff --check`: pass.

Lần chạy full-site trước đây lưu ở `runs/2026-10-23-015610/`; hai lần hậu kiểm nêu trên lưu riêng API/UI logs và ảnh. Có một lần gọi Maven thất bại do PATH ưu tiên Java 8; lần chạy đúng phải đặt `JAVA_HOME=C:\Program Files\Java\jdk-21` và thêm `$JAVA_HOME\bin` lên đầu PATH của tiến trình.

## Giới hạn xác minh

- E2E dùng API response mock và dữ liệu H2 cục bộ; không sử dụng CV/tài khoản production.
- Candidate job-fit chạy matching xác định trên backend; chưa xác minh lời gọi Gemini thật vì cần `GEMINI_API_KEY` do chủ dự án cấu hình.
- Google Calendar mở liên kết ngoài; mạng/DNS của browser có thể chặn dịch vụ. Luồng tải `.ics` qua ứng dụng đã được kiểm thử riêng và yêu cầu xác thực.
- Các kết quả local này không thay thế GitHub Actions, Render CD, production health hoặc kiểm tra build SHA sau deploy.

## Trạng thái phát hành

PR #65 đã merge vào `main` bằng commit `5132b0b28da8a82ce52b8da0d33e727e2e6d381c`. Cả ba job GitHub CI (backend, frontend, Docker) pass. Render CD run `39837408472` pass: backend health `UP` và `buildCommit` đúng; frontend HTML marker đúng SHA; `/api/v1/jobs`, trang frontend và CORS preflight đều HTTP 200. Lần triển khai backend mất khoảng 4 phút để khởi động; không coi các probe timeout trước khi Render sẵn sàng là pass.

## Hậu kiểm local–Render và phát hành phần AI

Chín hạng mục Candidate/Recruiter đã được PR #65 phát hành như phần trên. Nhánh hậu kiểm hiện chỉ có sửa backend để khi Gemini trả nội dung rỗng, trợ lý gắn nguồn fallback là `KNOWLEDGE_BASE`, cùng test và tài liệu; không có thay đổi FE. Chỉ kết luận bản Render tương đồng sau khi PR hậu kiểm được CI, merge, CD chạy cả hai deploy hook và hai dịch vụ cùng báo một SHA mới. Ghi SHA/check run cuối tại đây sau khi live smoke hoàn tất.
