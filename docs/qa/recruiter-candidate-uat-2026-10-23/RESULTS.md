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

Chín hạng mục Candidate/Recruiter đã được PR #65 phát hành như phần trên. PR #67 chỉ sửa backend để khi Gemini trả nội dung rỗng, trợ lý gắn nguồn fallback là `KNOWLEDGE_BASE`, đồng thời cập nhật test/tài liệu; không thay đổi mã FE.

- PR [#67](https://github.com/leduykhanhdevs/TalentBridge_JS2/pull/67) đã merge vào `main` bằng commit `d33d5ddf50e3681dd29d372dfc8948ac2a0680d9`; cả ba job CI (backend, frontend, Docker) pass.
- Render CD run [39847644086](https://github.com/leduykhanhdevs/TalentBridge_JS2/actions/runs/39847644086) pass cho FE và BE. Frontend `render-git-commit`, backend `/api/v1/health` `data.buildCommit`, và `headSha` của CD cùng bằng `d33d5ddf50e3681dd29d372dfc8948ac2a0680d9`.
- Kiểm tra live sau CD: frontend HTTP 200 với đúng title/build marker; backend health HTTP 200, `status=UP`; `/api/v1/jobs` HTTP 200; CORS preflight từ frontend trả HTTP 200 với đúng `Access-Control-Allow-Origin` và `Access-Control-Allow-Credentials: true`.
- Bản FE/BE đã deploy cùng source tree với localhost QA; follow-up PR chỉ thay adapter AI backend và tài liệu. Không chạy đăng ký/ghi dữ liệu trên production; self-registration đã được kiểm thử trên H2 cô lập trong Playwright 30/30.
