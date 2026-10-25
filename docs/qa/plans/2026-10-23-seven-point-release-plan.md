# Kế hoạch sửa 7 hạng mục TalentBridge — 23/10/2026

## Mục tiêu

Hoàn thiện luồng tuyển dụng từ quản trị doanh nghiệp, nhà tuyển dụng đến ứng viên; xác minh hành vi bằng unit/integration test và UI E2E trên H2 cô lập. Chỉ phát hành sau khi CI xanh và các nhà cung cấp OTP thật được cấu hình, xác nhận nhận mã trên môi trường đích.

## Phạm vi và tiêu chí nghiệm thu

| # | Hạng mục | Tiêu chí nghiệm thu | Trạng thái mã và kiểm chứng |
|---|---|---|---|
| 1 | Thứ tự duyệt doanh nghiệp | Doanh nghiệp mới nhất đứng đầu; có tiêu chí phụ ổn định khi cùng thời điểm. | Đã sửa truy vấn `createdAt DESC, id DESC`; unit test sắp xếp pass. |
| 2 | Hủy ứng viên | Nhà tuyển dụng phải xác nhận trước khi hủy; hủy chuyển hồ sơ sang `REJECTED`; quay lại không gửi thay đổi. | Đã thêm nút/confirmation và UI E2E cho cả xác nhận lẫn quay lại; pass. |
| 3 | Tài khoản và dữ liệu demo | Kiểm thử dùng tài khoản được tạo qua UI và dữ liệu giả lập trên H2; job/application phải đi qua logic thật của backend. Dữ liệu dùng demo trên môi trường chung phải tách biệt và có quy trình xóa. | UI E2E đăng ký/đăng nhập trên H2 pass; API E2E hệ thống 28/28 pass. Không seed mật khẩu người dùng vào source hoặc production. Tạo HRPM-73 để provision tài khoản và job/apply demo trong môi trường riêng; còn chờ xử lý. |
| 4 | Hiện/ẩn mật khẩu | Login/register có nút icon đổi kiểu hiển thị và vẫn có tên trợ năng rõ ràng. | Đã thêm nút; UI E2E registration/login và smoke đa vai trò pass. |
| 5 | Lưu CV sau nhập nhanh | File CV gốc được lưu vào danh sách CV đã tải lên, kể cả sau khi đồng bộ dữ liệu hồ sơ. | Đã upload file trước khi đồng bộ thông tin; UI E2E kiểm tra file xuất hiện trong “CV của tôi”; pass. |
| 6 | “CV của tôi” khi ứng tuyển | Mặc định chọn CV chính; ứng viên có thể chọn CV đã lưu hoặc upload CV khác. | Đã hiển thị/chọn CV chính và upload trong modal; UI E2E ứng tuyển bằng CV chính pass. |
| 7 | Xác minh email và điện thoại | Mỗi OTP đúng tài khoản/kênh, hết hạn sau 10 phút, giới hạn gửi lại và số lần thử; API từ chối ứng tuyển nếu một trong hai kênh chưa xác minh. | Đã triển khai endpoint, HMAC OTP, rate limit, UI hai kênh và backend gate; unit/integration/UI mock-provider test pass. Gửi mail/Zalo thật chưa xác minh vì cần cấu hình dịch vụ; HRPM-71 và HRPM-72 đang chờ cấu hình/kiểm thử live. |

## Trình tự thực hiện

1. Sắp xếp danh sách doanh nghiệp và thêm test thứ tự.
2. Thêm hành động hủy ứng viên có modal xác nhận, không gọi API khi quay lại.
3. Giữ dữ liệu tài khoản/job cho kiểm thử trong H2; tách provision demo dùng chung sang HRPM-73, không lưu credential vào Git/Jira.
4. Bổ sung nút hiện/ẩn password và kiểm tra accessible name.
5. Lưu CV gốc trước khi đồng bộ trường trích xuất từ CV.
6. Tích hợp chọn CV chính/CV lưu sẵn hoặc tải CV khác vào quy trình ứng tuyển.
7. Bổ sung email OTP và Zalo OTP, bảo vệ token, hết hạn/giới hạn thử, rồi xác minh provider thật.
8. Chạy Maven verify, TypeScript, Vitest, ESLint, frontend build, API E2E và Playwright full-site.
9. Chạy CI trên PR; chỉ merge/deploy sau khi HRPM-71 và HRPM-72 có bằng chứng gửi/nhận OTP live và HRPM-73 có demo data an toàn nếu cần demo bằng tài khoản thật.

## Bằng chứng kiểm thử cục bộ

- `scripts/run-e2e.ps1`: API E2E 28/28; Playwright 34/34 trên backend H2 tạm thời, gồm CV chính và upload CV ngay trong modal.
- `mvnw.cmd -B -ntp verify`: 391 test, 0 failure, 0 error.
- Frontend: TypeScript pass; Vitest 24 file / 138 test pass; ESLint pass; Vite production build pass.
- Ảnh và log lượt UI pass: `docs/qa/full-site-uat-2026-10-20/runs/2026-10-23-111129/`.

## Release gates còn mở

- HRPM-71: Zalo OA/app, quyền ZBS, template OTP được duyệt, secret Render và xác nhận nhận OTP thật.
- HRPM-72: triển khai Apps Script relay, đặt shared secret trong Script Properties/Render, xác nhận OTP email thật. Render Free chặn SMTP outbound nên cấu hình hiện dùng HTTPS relay.
- HRPM-73: provision tài khoản/job/application demo trong môi trường tách biệt; không đưa mật khẩu người dùng vào repository.
- CI PR #70 đã pass cả backend, frontend và Docker ở run `39899357130`. CD và xác nhận SHA frontend/backend chưa chạy vì chưa merge; hai provider OTP live và dữ liệu demo riêng vẫn là các điều kiện phát hành còn mở.

## Quyết định phát hành

Không merge/deploy bản áp dụng bắt buộc OTP khi chưa có provider thật: cấu hình thiếu sẽ khiến người dùng không thể xin mã và hồ sơ ứng tuyển bị chặn. Sau khi hoàn tất ticket provider, chạy lại smoke live chỉ với mailbox/số điện thoại thử đã được đồng ý, kiểm tra SHA/CI/CD rồi mới phát hành.
