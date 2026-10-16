# RAG và MCP trong TalentBridge: phân biệt khái niệm, đối chiếu source

**Ngày cập nhật:** 16/10/2026
**Trạng thái:** ghi chú kiến trúc đối chiếu source trên nhánh `feat/khanh/ai-rag-trivector-release`; chưa được merge/deploy.

## 1. RAG và MCP giải quyết hai việc khác nhau

- **RAG (Retrieval-Augmented Generation)** lấy các tài liệu liên quan làm ngữ cảnh cho mô hình sinh. Vector embedding là một cách truy xuất, không phải điều kiện bắt buộc cho mọi hệ thống RAG; keyword, SQL hoặc hybrid retrieval cũng có thể được dùng.
- **MCP (Model Context Protocol)** chuẩn hóa cách host/client khám phá và gọi tools, resources, prompts. Tool có thể chỉ đọc hoặc có tác dụng ghi; quyền và mức độ an toàn phải được ứng dụng kiểm tra riêng. MCP không tự cấp quyền database và không bảo đảm an toàn cho thao tác.
- Hai kỹ thuật có thể phối hợp nhưng không thay thế nhau: retrieval cung cấp căn cứ; tool protocol cung cấp giao diện tương tác với ứng dụng. Một chatbot chỉ có retrieval vẫn hợp lệ nếu phạm vi sản phẩm không cần thao tác dữ liệu.

Nguồn giao thức: [MCP Tools, specification 2025-11-25](https://modelcontextprotocol.io/specification/2025-11-25/server/tools). Bài báo AI-KM được trích ở [ghi chú kiểm chứng riêng](AI_KM_RAG_WORKFLOW_ANALYSIS.md); không dùng bài báo làm bằng chứng cho công thức matching hoặc MCP nếu không xác minh được trong toàn văn.

## 2. Trạng thái được xác nhận trong source TalentBridge

### Chatbot hỗ trợ trong ứng dụng

`GroundedTalentBridgeAssistantAdapter` truy xuất tối đa ba mục từ bộ hướng dẫn cố định bằng chuẩn hóa văn bản, mở rộng synonym, độ phủ token chính xác và Dice similarity trên character-trigram. FAQ phổ biến có đường trả lời nhanh. Nếu có `GEMINI_API_KEY`, backend gửi phần hướng dẫn đã truy xuất trong system instruction cùng câu hỏi hiện tại để Gemini sinh câu trả lời; lịch sử hội thoại không được gửi. Email, số điện thoại, OTP, token và một số dạng gán mật khẩu/API key bị chặn trước provider. Khi thiếu key/provider lỗi, câu trả lời quay về hướng dẫn được truy xuất. API yêu cầu Candidate, Recruiter hoặc Admin; chatbot không có quyền đọc dữ liệu CV riêng hay thay đổi hồ sơ.

Giới hạn: đây là knowledge-base retrieval dạng lexical, không có vector store, embedding search, ingestion/indexing pipeline hay benchmark truy hồi. Gemini là tùy chọn sinh câu trả lời, không phải bằng chứng rằng retrieval đã trở thành vector RAG.

### Matching CV với tin

`TriVectorRagMatchingService` tính ba thành phần tách biệt, đều từ vector từ vựng chuẩn hóa: (1) bằng chứng trực tiếp trong kỹ năng/tóm tắt/chức danh/CV trích xuất, (2) ngữ cảnh nghề nghiệp từ chức danh và cấp kinh nghiệm, (3) năng lực liên quan suy luận từ một danh sách quy tắc bảo thủ. Mỗi thành phần kết hợp độ phủ có trọng số và cosine similarity; điểm cuối là `0.60 × trực tiếp + 0.25 × ngữ cảnh + 0.15 × suy luận`, chuẩn hóa 0–100. Năng lực suy luận được hiển thị riêng để Recruiter xác minh; quy tắc reverse-skill generation chuyển năng lực liên quan và kỹ năng chưa có bằng chứng thành câu hỏi phỏng vấn trung lập. Thiếu bằng chứng không bị cộng điểm nền. Đây là thuật toán lexical xác định do dự án tự xây, không phải embedding, không được hiệu chuẩn thành xác suất tuyển dụng và không được gán cho AI-KM. Nội dung CV chỉ được trích xuất trong backend sau kiểm tra quan hệ ứng tuyển/quyền sở hữu công ty; matching không gửi CV cho Gemini.

### MCP tools cho ATS

MCP Java SDK (`mcp-core`, Jackson 2 adapter) cung cấp Streamable HTTP Servlet tại `/api/v1/mcp`, protocol version `2025-11-25`. Năm tool có schema hẹp: tìm tin đang tuyển; xem hồ sơ ứng viên đã ứng tuyển; đánh giá CV–job; cập nhật giai đoạn đơn; tìm hướng dẫn. Endpoint chỉ nhận Recruiter đã xác thực; transport context mang recruiter id/role; application use case tiếp tục xác minh quyền sở hữu công ty/tin và quan hệ ứng tuyển trước khi đọc hoặc ghi. Tool ghi chỉ chạy khi có `confirm=true`; đây là cổng tham số rõ ràng, còn MCP client nên trình bày thao tác và yêu cầu người dùng xác nhận theo chính sách của host.

Tool `talentbridge_search_help` và chatbot dùng chung nguồn hướng dẫn, nhưng chatbot không gọi MCP endpoint, không được cấp tool mutation và không truy cập dữ liệu riêng. HTTP integration test kiểm tra initialize, notification initialized, discovery, tool call, anonymous 401, Candidate 403 và việc tool ghi từ chối `confirm=false`. Đây chưa phải interoperability test với client MCP độc lập. Trên nhánh hiện tại, các kiểm thử liên quan đã đạt; thay đổi chưa merge hoặc deploy.

## 3. Phân biệt kết quả và giới hạn

- Điểm chất lượng tin chấm mức đầy đủ/rõ của trường dữ liệu và tín hiệu kiểm duyệt nội bộ. Việc công ty APPROVED trong TalentBridge không xác minh độc lập tư cách pháp nhân hoặc tính chân thực của nội dung.
- Đánh giá CV–job kết hợp match hiện có với phần trăm đầy đủ hồ sơ. Đây là gợi ý cho Recruiter, không thay thế xác minh hồ sơ, phỏng vấn hay quyết định tuyển dụng.
- Pipeline là ảnh chụp số đơn theo `currentStage` hiện tại; không phải lịch sử chuyển đổi, thời gian tuyển hay tỷ lệ thành công theo cohort.
- Unit/integration tests xác minh quy tắc source/API và ranh giới quyền. Chúng không đo độ chính xác mô hình, chất lượng tuyển dụng hoặc lợi ích của AI.

## 4. Đề xuất phát triển tiếp theo

1. Giữ chatbot FAQ không có quyền mutation mặc định. Nếu cần trả lời thống kê/tình trạng riêng, thêm tool đọc có schema hẹp, xác thực actor và ownership trong use case; không cho LLM truy cập repository/JPA trực tiếp.
2. Nếu cần semantic RAG, bổ sung corpus có nguồn, chunking, embedding, retrieval evaluation set, citation tới tài liệu và kiểm thử câu hỏi ngoài phạm vi. Đo precision/recall hoặc hit-rate trước khi mô tả hiệu quả. Retrieval hiện tại là hybrid lexical trên bộ hướng dẫn nhỏ.
3. Bổ sung interoperability test với một client MCP độc lập; hiện tại chỉ xác minh HTTP lifecycle và tools bằng client Java trong integration test. Nếu mở rộng backend thành nhiều instance, đánh giá transport/session strategy để tránh phụ thuộc session trong bộ nhớ của một instance.
4. Với CV matching, xây tập đánh giá có consent/ẩn danh, so sánh baseline và model, kiểm tra bias theo nhóm, hiệu chuẩn điểm và lưu rõ model/source/version. Trước khi có các bằng chứng đó, giữ nhãn advisory.

## 5. Nguồn tham khảo

- Wen et al., *Version 5.4.18 – AI–KM: Knowledge enhancement with RAG and workflow*, SoftwareX 31 (2025) 102349, [ScienceDirect](https://www.sciencedirect.com/science/article/pii/S2352711025003152), [DOI](https://doi.org/10.1016/j.softx.2025.102349). Nội dung bài viết ở đây được giới hạn theo abstract/metadata truy cập được.
- [Model Context Protocol: Tools, specification 2025-11-25](https://modelcontextprotocol.io/specification/2025-11-25/server/tools), mô tả khai báo và gọi tool.
- [MCP Java SDK server documentation](https://github.com/modelcontextprotocol/java-sdk/blob/main/docs/server.md), tài liệu transport server và tool API của SDK chính thức.
- [Gemini model configuration](https://ai.google.dev/gemini-api/docs/models) and [GenerateContent API](https://ai.google.dev/api/generate-content).
