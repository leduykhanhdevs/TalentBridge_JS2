# Trạng thái tích hợp AI của TalentBridge

**Kiểm tra source:** `origin/main` tại `db91be982e4ad009e7417a959552bc67d8c09687`, cộng với sửa nhãn nguồn fallback Gemini trên nhánh hậu kiểm; cập nhật audit ngày 23/10/2026.

## Các thành phần đã có

| Thành phần | Luồng đang chạy trong mã nguồn | Bằng chứng kiểm thử | Giới hạn cần nói rõ |
| --- | --- | --- | --- |
| Phân tích CV bằng Gemini | `GeminiCvParserService` trích văn bản CV ở backend; khi có cấu hình key thì gọi Gemini, nếu thiếu key/lỗi thì trả parser dự phòng kèm `processingSource`. | `GeminiCvParserServiceTest` dùng HTTP stub cục bộ để kiểm tra request/response và fallback. | Local hiện báo thiếu `GEMINI_API_KEY`. Test không gọi Google thật; secret được hiển thị trong Render không chứng minh key hợp lệ hoặc provider đã trả lời thành công. |
| Trợ lý hỏi đáp có grounding | `GroundedTalentBridgeAssistantAdapter` truy xuất knowledge base nội bộ cục bộ rồi mới diễn đạt bằng Gemini nếu được cấu hình. Truy vấn phổ biến có thể trả lời trực tiếp từ knowledge base; hội thoại cũ và dữ liệu riêng tư không được gửi cùng prompt. | `TalentBridgeAssistantAdapterTest`, `AssistantControllerTest`; có kiểm tra scope, references, privacy và fallback. | Retrieval hiện dựa trên từ khóa, synonym và trigram; đây chưa phải pipeline vector embedding hay vector database. Gemini live chưa được xác minh. |
| Đánh giá CV–việc làm | `TriVectorRagMatchingService` tạo điểm xác định từ bằng chứng trực tiếp 60%, ngữ cảnh nghề nghiệp 25%, năng lực liên quan suy luận 15%; trả giải thích, thiếu bằng chứng và câu hỏi xác minh cho Recruiter. | `TriVectorRagMatchingServiceTest`, `AssessMyJobFitUseCaseImplTest`, `AiMatchingControllerSecurityTest`, `AiMatchingApproachComparisonTest`. | Dùng vector từ vựng và cosine similarity, không dùng embedding hoặc mô hình sinh để quyết định điểm. Điểm là tư vấn, không phải xác suất được tuyển; matching không gửi CV sang Gemini. |
| MCP server | Java SDK chính thức đăng ký Streamable HTTP ở `/api/v1/mcp`; 5 tools gồm tìm việc, đọc hồ sơ, đánh giá độ phù hợp, cập nhật giai đoạn đơn và tìm hướng dẫn. Use case kiểm tra JWT, vai trò và quyền sở hữu dữ liệu; thao tác ghi yêu cầu `confirm=true`. | `TalentBridgeMcpProtocolIntegrationTest` chạy initialize, tools/list, tools/call và kiểm tra 401/403/thiếu xác nhận. | MCP là lớp giao thức công cụ; nó không tự tạo câu trả lời RAG và chatbot trong sản phẩm hiện chưa gọi MCP. Kiểm thử dùng MCP Java client, chưa có bằng chứng Claude hoặc agent bên ngoài kết nối. |
| Trợ lý Recruiter/Admin | `AssistantChatPanel` nhận intent theo quy tắc và gọi API thống kê ATS/ lọc ứng viên; Recruiter xuất CSV, Admin xuất PNG/Excel/Word. | `assistant-widget`, `admin-assistant-reports` và các test API theo vai trò. | Đây là công cụ UI/API xác định, không phải một LLM agent tự lập kế hoạch hoặc tự gọi MCP. |

## Kết luận vận hành

- MCP và RAG bổ trợ nhau: RAG tìm tri thức để trả lời; MCP mô tả/cung cấp thao tác có schema cho một MCP client. Trong bản hiện tại, hai phần chia sẻ một số use case/knowledge nhưng chưa được nối thành agent tự quyết định gọi tool.
- Gemini có đường gọi thật trong code, nhưng các test dùng stub và local thiếu key. Chưa có bằng chứng runtime `source=GEMINI` từ môi trường thật. Vì vậy không thể kết luận Gemini đang hoạt động trên production chỉ dựa vào biến môi trường bị che.
- Không tìm thấy runtime tích hợp Anthropic/Claude, OpenAI, Ollama hoặc embedding provider trong source `main`. Không có vector database, ingestion/chunking pipeline hay benchmark retrieval/accuracy.
- Bản so sánh Jira HRPM-58 được đánh dấu Done nhưng tiêu chí “team agrees on the direction” chưa có bằng chứng quyết định của cả nhóm. Cần ghi nhận quyết định trước khi xem phần acceptance này hoàn tất.
- Đã sửa lỗi gắn nhãn nguồn: khi Gemini trả nội dung rỗng, câu trả lời fallback phải báo `KNOWLEDGE_BASE`, không được ghi `GEMINI`.

## Đối chiếu các đề xuất trong `02.docx`

| Năng lực đề xuất | Hiện trạng | Jira |
| --- | --- | --- |
| Trích xuất nhanh thông tin CV | Parser và fallback đã có; Gemini live chưa được xác minh. | [HRPM-68](https://khanhdevs19.atlassian.net/browse/HRPM-68) |
| Gợi ý nội dung mô tả tuyển dụng | Chưa tích hợp chức năng sinh bản nháp. | [HRPM-69](https://khanhdevs19.atlassian.net/browse/HRPM-69) |
| Giải thích mức phù hợp công việc | Đã có matching xác định Tri-Vector với bằng chứng khớp/thiếu. Không dùng LLM để quyết định điểm. | [HRPM-63](https://khanhdevs19.atlassian.net/browse/HRPM-63) |
| Tạo câu hỏi phỏng vấn | Đã có câu hỏi xác minh trung lập gắn với bằng chứng matching; chưa có bộ câu hỏi phỏng vấn có cấu trúc rộng. | [HRPM-70](https://khanhdevs19.atlassian.net/browse/HRPM-70) |

MCP đã được kiểm tra qua Java MCP client trong integration tests (`initialize`, `tools/list`, `tools/call`, auth/role và xác nhận thao tác ghi). Chưa có bằng chứng kết nối Claude/Anthropic, Anti/Claude agent khác hoặc MCP client ngoài hệ thống. Không gửi dữ liệu ứng viên thật cho agent bên ngoài trong lượt kiểm tra này.

## Tài liệu tham chiếu

- Wen et al., *Version 5.4.18 – AI–KM: Knowledge enhancement with RAG and workflow*, SoftwareX 31 (2025), DOI [10.1016/j.softx.2025.102349](https://doi.org/10.1016/j.softx.2025.102349). Bài mô tả một hệ thống AI-KM có nhiều LLM, RAG và workflow; TalentBridge hiện chỉ có các phần hẹp hơn nêu ở bảng trên, không tuyên bố tái tạo hệ thống nghiên cứu đó.
- [MCP Tools specification](https://modelcontextprotocol.io/specification/2025-11-25/server/tools): server công bố tool/schema để MCP client khám phá và gọi; thao tác nhạy cảm nên có human-in-the-loop.
- [Gemini GenerateContent API](https://ai.google.dev/api/generate-content): API mà adapter Gemini của dự án gọi khi được cấu hình.
