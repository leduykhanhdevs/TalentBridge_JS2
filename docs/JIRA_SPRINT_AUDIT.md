# Đối chiếu Jira Spring 0, 1, 2 — 23/10/2026

## Kết quả trạng thái

Rà trực tiếp trên Jira HR Platform board 5. Bộ lọc `project = HRPM AND Sprint in ("Sprint 0", "Sprint 1", "Sprint 2") AND issuetype != Epic` cho **56 issue khác nhau**. Có 2 issue carry-over xuất hiện trong hơn một Sprint, nên tổng các dòng theo từng Sprint là 58 membership.

| Sprint | Issue được gắn trong Sprint | Done | Còn mở | Nhận xét |
| --- | ---: | ---: | ---: | --- |
| Sprint 0 | 22 | 22 | 0 | Hoàn tất theo trạng thái Jira. |
| Sprint 1 | 20 | 20 | 0 | Hoàn tất theo trạng thái Jira. |
| Sprint 2 | 16 | 13 | 3 | Chưa hoàn tất; ba việc AI còn To Do. |
| Khác nhau trong cả ba Sprint | 56 | 53 | 3 | HRPM-8 và HRPM-50 là các issue carry-over; không cộng lặp. |

Các issue chưa hoàn thành trong phạm vi ba Sprint:

- [HRPM-68 — xác thực Gemini thật và chốt quyết định MCP/RAG](https://khanhdevs19.atlassian.net/browse/HRPM-68) — To Do, Epic AI Integration.
- [HRPM-69 — gợi ý nháp mô tả tuyển dụng](https://khanhdevs19.atlassian.net/browse/HRPM-69) — To Do, Epic AI Integration.
- [HRPM-70 — tạo bộ câu hỏi phỏng vấn](https://khanhdevs19.atlassian.net/browse/HRPM-70) — To Do, Epic AI Integration.

Đã tạo đủ ba ticket theo yêu cầu và xác minh cả ba nằm trong Sprint 2; backlog Jira đang trống. Sprint 2 board ghi 32/32 story points Done nhưng ba ticket mới chưa được estimate, vì vậy phần trăm điểm không phản ánh việc hoàn thành toàn bộ scope.

## Độ bám yêu cầu và phần còn thiếu

Đối chiếu đề xuất AI trong `02.docx` với code hiện tại:

| Đề xuất | Trạng thái trong ứng dụng | Việc còn lại |
| --- | --- | --- |
| Trích xuất thông tin CV | Đã có parser, quy tắc fallback và đường gọi Gemini khi có key hợp lệ. | Chưa có bằng chứng cuộc gọi Gemini production/non-production thành công; theo HRPM-68. |
| Gợi ý mô tả tin tuyển dụng | Chưa có chức năng sinh bản nháp. | HRPM-69, mới tạo trên Sprint 2. |
| Giải thích độ phù hợp CV–việc làm | Đã có điểm xác định có giải thích theo ba nhóm bằng chứng; không phải xác suất tuyển dụng. | Kiểm thử accuracy/live provider không áp dụng cho bộ matching xác định hiện tại. |
| Câu hỏi phỏng vấn | Có câu hỏi trung lập để recruiter xác minh bằng chứng/thiếu bằng chứng trong matching. | Chưa có bộ câu hỏi phỏng vấn có cấu trúc đầy đủ theo vị trí/hồ sơ; theo HRPM-70. |

Toàn bộ 56 issue khác nhau đang theo dõi trong ba Sprint có 53 Done, 3 To Do; do đó **không thể kết luận cả ba Sprint đã hoàn tất**. Sprint 0/1 đều Done theo Jira; Sprint 2 chưa Done. Trạng thái Done trong Jira là bằng chứng quản lý tiến độ, không tự nó chứng minh mọi acceptance criteria đã được kiểm thử bằng người dùng.

Các điểm quản lý cần xác nhận:

- Năm Epic HRPM-1, HRPM-2, HRPM-3, HRPM-33 và HRPM-38 vẫn ở To Do; chúng là container nên cần quyết định đóng/điều chỉnh trạng thái theo toàn bộ child work.
- [HRPM-58 — AI: MCP vs RAG](https://khanhdevs19.atlassian.net/browse/HRPM-58) đang Done, nhưng tiêu chí gốc yêu cầu “team agrees on the direction”; chưa thấy bằng chứng xác nhận quyết định của cả nhóm. HRPM-68 ghi nhận việc hoàn tất runtime proof và quyết định này.
- [HRPM-61 — Analytics & Reporting](https://khanhdevs19.atlassian.net/browse/HRPM-61) vẫn là Epic To Do, chưa có mô tả; cần chủ sản phẩm xác nhận phạm vi ngoài API Statistic HRPM-59.
- [HRPM-25 — giao diện đăng ký ứng viên](https://khanhdevs19.atlassian.net/browse/HRPM-25) vẫn In Review và chưa gắn Sprint. Đây là issue cũ đang mở, không tạo bản trùng; cần reviewer/owner quyết định nghiệm thu hoặc đưa trở lại Sprint.

## Phạm vi mới đã đưa vào Sprint 2

- HRPM-68 theo dõi kiểm tra Gemini thật, fallback/source label, kiểm thử MCP client ngoài ứng dụng và quyết định RAG/MCP có kiểm soát.
- HRPM-69 theo dõi bản nháp mô tả tuyển dụng, có recruiter duyệt/sửa trước khi lưu hoặc đăng.
- HRPM-70 theo dõi bộ câu hỏi phỏng vấn giải thích được, không tự chấm/loại ứng viên.

Không chuyển hồ sơ ứng viên production cho Gemini/Claude hoặc agent ngoài để làm audit. Bằng chứng MCP hiện tại là integration test dùng Java MCP client trong dự án; chưa có Claude/Anthropic hay agent bên ngoài cấu hình để kết nối thật.
