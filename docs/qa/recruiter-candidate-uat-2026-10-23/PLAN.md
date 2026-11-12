# Kế hoạch xử lý UAT Candidate và Recruiter — 23/10/2026

## Mục tiêu và cách làm

Sửa 9 vấn đề người dùng phản ánh ở hồ sơ, trợ lý AI, ATS, lịch phỏng vấn, biểu mẫu, liên kết doanh nghiệp và điều hướng. Mỗi luồng phải có kiểm thử phù hợp với quyền Candidate/Recruiter; không gọi provider AI thật nếu thiếu cấu hình, không dùng CV hoặc tài khoản thật trong E2E. Tạo PR vào `main` và chỉ merge sau khi CI bắt buộc đạt; xác minh CD và SHA triển khai sau merge.

## Nguyên nhân được xác định khi rà source

- **Avatar**: trang hồ sơ Recruiter chỉ nhận URL; phần chấm xanh trên avatar là chi tiết trang trí. Candidate đã có UI upload/drop và API upload có giới hạn 5 MB; có thể tái sử dụng file storage hiện tại.
- **Fit công việc**: trang chi tiết tin chưa có phần tự đánh giá cho Candidate. Matching hiện có là heuristic xác định ba thành phần lexical (bằng chứng trực tiếp, ngữ cảnh nghề nghiệp, năng lực liên quan); phải trình bày là điểm tham khảo, không phải xác suất trúng tuyển. Việc gọi Gemini thật cần API key và không được suy ra từ fake/test provider.
- **Trợ lý Recruiter**: `AssistantChatPanel` mặc định chỉ trả lời hướng dẫn. Đã có nhánh công cụ ATS theo tin, bộ lọc, thống kê giai đoạn và CSV, nhưng `AssistantPage`/widget chưa truyền `recruiterMode`; vì vậy user Recruiter không thể thấy hay gọi các công cụ này.
- **CV ứng viên**: hàng ATS mở `resumeUrl` từ API trực tiếp trong tab; request đó không đính kèm Bearer token nên tải nội dung không hoạt động qua endpoint được bảo vệ. Endpoint mới cần đối chiếu đồng thời công ty, tin, đơn ứng tuyển và đúng CV được gắn vào đơn trước khi đọc bytes.
- **Calendar**: ảnh Google Calendar cho thấy browser không có Internet/DNS; đây là lỗi mạng ngoài ứng dụng. Lối tải `.ics` hiện được permitAll và dùng URL mở tab không có Bearer token; use case chưa kiểm tra Candidate/Recruiter là người tham gia. Trường datetime-local cũng bị `toISOString()` đổi giờ địa phương trước khi gửi. `.ics` cần xác thực, kiểm tra quyền, timezone `Asia/Ho_Chi_Minh`, escape nội dung RFC 5545 và là phương án dùng được khi Google không truy cập được.
- **Input**: nhiều màn đặt chiều cao/padding/font riêng; chưa có quy tắc chung cho input/select/textarea. Chuẩn hóa có ngoại lệ checkbox/radio/file/hidden và không đặt lại kiểu nội tại của file control.
- **Yêu cầu gia nhập**: API trả trạng thái theo user; các React Query key trước đó không chứa user id, nên cache từ phiên Recruiter trước có thể xuất hiện khi đổi tài khoản. Đã chuyển key pending/profile sang account-scoped key; giữ nguyên quy tắc domain chặn yêu cầu trùng thực sự.
- **Tin tuyển dụng không có công ty**: trang hiện dùng empty-state thông thường và lối tạo tin. Khi hồ sơ chưa có `companyId`, cần ngăn tải/tạo tin và hiện alertdialog điều hướng tới hồ sơ doanh nghiệp.

## Hạng mục và tiêu chí chấp nhận

1. **Avatar** — bỏ chấm trạng thái trang trí dưới ảnh; cho phép kéo-thả/chọn ảnh máy tính, kiểm tra loại và dung lượng, xem trước, lưu qua endpoint có kiểm soát quyền; giữ nhập URL nếu API hiện tại hỗ trợ. Kiểm thử Candidate và Recruiter.
2. **Đánh giá độ phù hợp việc làm** — kiểm tra endpoint và matching hiện có; trình bày kết quả/nguồn/fallback rõ ràng trong Candidate job detail hoặc danh sách; không tuyên bố đây là xác suất trúng tuyển. Có trạng thái thiếu CV, lỗi và chưa có dữ liệu.
3. **Trợ lý Recruiter** — bổ sung câu hỏi/tác vụ về số đơn ứng tuyển, phân bố giai đoạn, lọc ứng viên và xuất thống kê; mọi truy vấn giới hạn theo công ty/việc mà recruiter sở hữu. Tái sử dụng thống kê ATS và định dạng export hiện có khi phù hợp; kiểm thử quyền chéo công ty và các báo cáo.
4. **Xem CV ứng viên** — lần theo nút, URL và API download; sửa luồng tải/xem bằng token xác thực và kiểm tra quyền sở hữu job/company, đơn ứng tuyển và CV gắn với đơn; file hợp lệ mở được, thiếu quyền/trạng thái rỗng có thông báo.
5. **Calendar** — kiểm tra tạo sự kiện, múi giờ, URL Google Calendar và luồng Candidate/Recruiter. API `.ics` yêu cầu xác thực và chỉ người ứng tuyển hoặc Recruiter đúng công ty được tải. Giữ Google Calendar làm liên kết ngoài, cung cấp `.ics` qua API làm phương án nhập lịch khi browser mất DNS/mạng.
6. **Input** — thống nhất kiểu dáng, chiều cao, font, viền, focus/error/disabled và khoảng cách cho biểu mẫu Candidate/Recruiter đang lệch; xác nhận ở desktop/mobile và không làm hỏng select/date/file controls.
7. **Yêu cầu gia nhập doanh nghiệp** — truy vấn dữ liệu theo đúng recruiter và doanh nghiệp; phân biệt yêu cầu gia nhập công ty với đăng ký/tạo công ty mới. Không hiện trạng thái chờ sai tài khoản/công ty; chặn gửi trùng đúng phạm vi nghiệp vụ. Thêm kiểm thử use case/API.
8. **Tin tuyển dụng khi chưa liên kết công ty** — thay trạng thái rỗng hiện tại bằng thông báo “Bạn cần liên kết doanh nghiệp trước khi tuyển dụng”; khi đóng thì chuyển tới `/recruiter/company`; không cho tạo tin khi chưa có quyền công ty.
9. **CI/CD và phát hành** — chạy Maven verify, Vitest, ESLint, TypeScript/Vite build, API E2E và Playwright theo vai trò/kích thước; kiểm tra GitHub Actions, PR checks, merge `main`, sau đó xác nhận CD, health và SHA live. Báo rõ dịch vụ/provider nào cần secret hoặc thao tác dashboard.

## Trình tự thực hiện

1. Rà nguồn, API, quyền, dữ liệu khởi tạo và các test hiện có; ghi bước tái hiện/điểm lỗi cụ thể.
2. Sửa backend contract và kiểm thử quyền/nghiệp vụ trước; kế tiếp nối frontend, trạng thái lỗi/rỗng và responsive.
3. Chạy test gần nhất theo từng nhóm; sau khi hoàn tất chạy lại toàn bộ bộ kiểm thử và ghi log/ảnh vào thư mục `runs/`.
4. Rà diff, tạo commit nguyên tử trên nhánh tính năng, push và mở PR vào `main` theo quy trình dự án.
5. Chỉ merge khi mọi check bắt buộc xanh; theo dõi deploy và kiểm chứng mã triển khai thực tế.

## Rủi ro và giới hạn cần phân biệt

- Gemini cần `GEMINI_API_KEY` do chủ dự án cấu hình; test dùng fake provider, không coi đó là bằng chứng gọi Gemini thật.
- Google Calendar là dịch vụ ngoài; `.ics` là đường dự phòng khi DNS/mạng của trình duyệt người dùng không truy cập được.
- CV chỉ được phục vụ cho người có quyền trên hồ sơ ứng tuyển và doanh nghiệp tương ứng; không mở endpoint file công khai.
- Mọi thống kê/trợ lý Recruiter chỉ được sử dụng dữ liệu ATS mà tài khoản đó được phép xem.

## Trạng thái thực hiện — 23/10/2026

- [x] Avatar Recruiter: bỏ dấu chấm trang trí; nhập URL, chọn file và kéo-thả; preview/lưu qua API.
- [x] Đánh giá mức phù hợp Candidate: endpoint và giao diện hiển thị điểm, bằng chứng khớp/thiếu, trạng thái CV và cảnh báo chỉ mang tính tham khảo.
- [x] Trợ lý Recruiter: lọc hồ sơ theo tin/giai đoạn/từ khóa/kinh nghiệm, thống kê pipeline và xuất CSV; không đẩy truy vấn ATS riêng tư sang chat provider.
- [x] Xem CV: endpoint có Bearer auth và kiểm tra phạm vi; tab preview có iframe PDF cùng đường tải dự phòng.
- [x] Calendar: xác thực và kiểm tra người tham gia; `.ics` theo RFC 5545, thời gian `Asia/Ho_Chi_Minh`, và giữ nguyên giờ địa phương từ `datetime-local`.
- [x] Chuẩn hóa input trên các form Candidate/Recruiter.
- [x] Query key trạng thái công ty/yêu cầu gia nhập có phạm vi tài khoản; thêm kiểm tra không rò cache khi đổi recruiter.
- [x] Không cho đăng tuyển khi thiếu công ty; hiển thị đúng thông báo và nút Đóng chuyển tới `/recruiter/company`.
- [x] Local verification: Maven verify 378/378; Vitest 135/135; ESLint; TypeScript/Vite build; API E2E 28/28; Playwright 30/30; `git diff --check`.
- [x] Phát hành: PR #65 merge commit `5132b0b28da8a82ce52b8da0d33e727e2e6d381c`; GitHub CI pass backend/frontend/Docker; Render CD run `39837408472` pass. Backend `/api/v1/health` báo `UP` và SHA đúng, frontend build marker cùng SHA, jobs API/frontend/CORS đều trả HTTP 200.

Log/ảnh của lần chạy API + Playwright đạt cuối cùng: `docs/qa/full-site-uat-2026-10-20/runs/2026-10-23-015610/`. Gemini thật và khả năng kết nối Google Calendar ngoài mạng chưa được kiểm chứng; không tính fixture/matching cục bộ là lời gọi provider thật. Build marker đã xác minh trên Render là `5132b0b28da8a82ce52b8da0d33e727e2e6d381c`.
