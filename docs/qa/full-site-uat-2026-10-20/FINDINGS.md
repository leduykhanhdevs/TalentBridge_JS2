# Findings UAT — TalentBridge — 2026-10-20

| ID | Mức độ | Khu vực | Lỗi và cách tái hiện | Bằng chứng | Trạng thái |
| --- | --- | --- | --- | --- | --- |
| UAT-01 | P2 | Recruiter / tạo tin | Khi API POST tin tuyển dụng trả lỗi, hộp tạo tin đóng và người dùng không thấy lỗi tại nơi đang thao tác; dữ liệu nhập có thể mất. | `recruiter-job-error-baseline.log` tái hiện hộp thoại không còn; ảnh form trong issue ban đầu. | Đã sửa local: giữ dialog/dữ liệu, hiện `role=alert` trong dialog; regression Playwright `reliability-workflows.spec.ts`. |
| UAT-02 | P2 | Candidate / lịch sử ứng tuyển | Khi API treo, màn lịch sử giữ spinner vô hạn và không có lối thử lại. | `candidate-applications-timeout.log`; ảnh `screenshots/final/candidate-applications-timeout.png`. | Đã sửa local: timeout/retry hiển thị ngay trên trang; regression Playwright. |
| UAT-03 | P2 | Auth / đăng xuất | Request logout treo làm UI giữ phiên; request với access token hết hạn cũng không thử refresh để thu hồi phiên phía server. | Regression trước/sau tại `authApi.test.ts`, `authenticatedFetch.test.ts`; Security Review P2. | Đã sửa local: timeout giới hạn, thử refresh + gửi lại logout, luôn xóa phiên local trong `finally`; unit và Playwright kiểm tra. Khi mạng/backend không thể truy cập thì thu hồi phía server vẫn best-effort. |
| UAT-04 | P2 | API client / deadline | Deadline chỉ bao tới response headers; response có thể gửi header rồi treo body, làm `.json()`/`.blob()` chờ mãi. | Bugbot P2; test mới trong `app/__tests__/fetchWithTimeout.test.ts`. | Đã sửa local: deadline giữ tới khi body stream kết thúc/hủy; timeout body trả `FetchTimeoutError`; test timeout body và body thành công. |
| UAT-05 | P3 | Auth / thông báo lỗi | Khi response đăng nhập trả headers nhưng body treo, timeout được kích hoạt nhưng parser cũ nuốt lỗi và hiện “Phản hồi từ máy chủ không hợp lệ”, không nói rõ cần thử lại. | Regression `authApi.test.ts`: response body stream treo ở HTTP 200. | Đã sửa local: giữ thông báo timeout và mã lỗi kết nối; test Vitest xác nhận `AuthApiError` có status 0 và hướng dẫn timeout. |

## Ghi chú UI/UX

- Không phát hiện tràn ngang trên các route/viewport đã rà; các màn route có nội dung và không có JavaScript page error trong Playwright.
- Panel trợ lý khi người dùng chủ động mở sẽ phủ một phần nội dung nền ở desktop; thao tác đóng panel hoạt động. Đây là overlay có chủ ý, không được tính là lỗi chức năng. Cần đánh giá riêng nếu muốn panel không phủ danh sách việc làm.
- Các luồng OTP/email thật, upload storage production và thao tác có tài khoản production chưa được xác nhận do UAT chỉ chạy production ở chế độ chỉ đọc.

## Mức độ còn mở

Không còn lỗi code đã tái hiện nào chờ sửa trong phạm vi UAT local. Mọi sửa đổi đang ở nhánh review; production hiện vẫn chạy SHA `474b749...` cho tới khi leader duyệt/merge PR và CD xác nhận SHA mới.

## Kế hoạch khắc phục và phát hành

1. Hoàn tất các sửa lỗi UAT-01–05 trong nhánh riêng; giữ fixture H2 và không ghi dữ liệu production.
2. Chạy lại unit, lint, production build, backend Maven verify, API E2E H2 và Playwright đầy đủ; lưu log và ảnh vào thư mục UAT này.
3. Mở PR hotfix từ nhánh UAT vào `main` để tránh kéo 28 commit chỉ có trên `main` và 4 commit chỉ có trên `dev` vào cùng PR; chạy CI đầy đủ.
4. Chờ trưởng nhóm Lê Duy Khánh review/merge theo `TEAM_RULES_AND_GITFLOW.md`; sau merge, xác nhận CD Render thành công và health/build marker frontend/backend trùng SHA mới.
5. Chạy lại production smoke chỉ đọc (frontend routes, health, jobs, CORS) và ghi kết quả sau deploy. Chỉ khi bước này đạt mới đánh dấu các sửa UAT là đã redeploy.
