# UAT toàn website TalentBridge — 2026-10-20

## Mục tiêu và môi trường

Đối chiếu route, quyền theo vai trò, luồng API, xử lý lỗi và khả năng dùng trên desktop/mobile. Baseline production được xác nhận theo commit `474b749982a7690cf37b3752c61c680fa51cd092` trước khi UAT. Các thao tác ghi/đăng nhập dùng tài khoản fixture và dữ liệu tổng hợp trên backend Spring Boot với H2 trong bộ nhớ. Production chỉ nhận GET/OPTIONS; không đăng nhập tài khoản thật và không tạo/sửa/xóa dữ liệu.

## Phạm vi route

| Nhóm | Route được rà |
| --- | --- |
| Public/xác thực | `/`, `/login`, `/register`, `/forgot-password`, `/reset-password`, `/jobs`, `/jobs/1`, route 404; `/assistant` kiểm tra riêng |
| Candidate | `/candidate/profile`, `/candidate/applications` |
| Recruiter | `/recruiter/profile`, `/recruiter/jobs`, `/recruiter/jobs/1/applicants`, `/recruiter/statistics`, `/recruiter/company`, `/recruiter/join-company`, `/recruiter/peer-approval` |
| Admin | `/admin/candidates`, `/admin/recruiters`, `/admin/companies`, `/admin/jobs`, `/admin/metrics` |

25 tổ hợp route/vai trò (public 8, candidate 5, recruiter 7, admin 5) được mở ở 360, 390, 1280 và 1440 px; ba vai trò còn được rà ở 555 px. Kiểm tra nội dung chính, JavaScript error, tràn ngang và hành động đại diện theo vai trò. Bổ sung CV Builder ở 360/555 px, trang trợ lý và bong bóng chat, trạng thái API lỗi/rỗng, tạo tin tuyển dụng thất bại, lịch sử ứng tuyển timeout/thử lại và đăng ký → đăng xuất → đăng nhập.

## Các bước và tiêu chí

1. Kiểm tra CD production đã triển khai đúng SHA; xác minh HTML commit marker, API health/jobs và CORS credentials.
2. Chạy `mvnw verify`, Vitest, ESLint, TypeScript/Vite production build và E2E tách biệt trên H2; kiểm tra cả timeout trước headers và trong response body.
3. Dùng Playwright đi qua route bằng fixture 3 vai trò; đo `documentElement.scrollWidth` so với viewport và lưu ảnh toàn trang.
4. Ghi từng lỗi tái hiện, bước tái hiện, ảnh/log, mức độ và trạng thái sau sửa.
5. Chạy lại regression tests và xác minh mã nguồn sau sửa. Production chỉ được gọi là đã nhận bản sửa sau PR được leader duyệt/merge, CD thành công và build marker trùng SHA.

## Giới hạn

- Không có tài khoản production dùng cho UAT; luồng xác thực và thao tác theo vai trò được kiểm chứng bằng fixture local.
- Không kiểm tra gửi email/OTP thật, provider Gemini thật, upload CV thật vào storage production, dữ liệu RDS riêng tư, billing hoặc khả năng vận hành qua sự cố hạ tầng dài hạn.
- 100 việc làm production là dữ liệu mô phỏng; UAT chỉ xác nhận endpoint công khai trả 100 bản ghi đánh dấu mô phỏng, không xác nhận nghiệp vụ tuyển dụng thật.
- Mở bảng trợ lý che một phần nội dung nền khi người dùng chủ động mở panel (mẫu overlay thông thường); bong bóng thu gọn không gây overflow. Đây là ghi chú UX, không phải lỗi luồng chức năng.
