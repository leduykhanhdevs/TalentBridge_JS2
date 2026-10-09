# RAG và MCP trong TalentBridge: phân biệt khái niệm, đối chiếu source

**Ngày cập nhật:** 09/10/2026
**Trạng thái:** ghi chú kiến trúc dựa trên source hiện có; phần đề xuất không đồng nghĩa với tính năng đã triển khai hoặc đã deploy.

## 1. RAG và MCP giải quyết hai việc khác nhau

- **RAG (Retrieval-Augmented Generation)** lấy các tài liệu liên quan làm ngữ cảnh cho mô hình sinh. Vector embedding là một cách truy xuất, không phải điều kiện bắt buộc cho mọi hệ thống RAG; keyword, SQL hoặc hybrid retrieval cũng có thể được dùng.
- **MCP (Model Context Protocol)** chuẩn hóa cách host/client khám phá và gọi tools, resources, prompts. Tool có thể chỉ đọc hoặc có tác dụng ghi; quyền và mức độ an toàn phải được ứng dụng kiểm tra riêng. MCP không tự cấp quyền database và không bảo đảm an toàn cho thao tác.
- Hai kỹ thuật có thể phối hợp nhưng không thay thế nhau: retrieval cung cấp căn cứ; tool protocol cung cấp giao diện tương tác với ứng dụng. Một chatbot chỉ có retrieval vẫn hợp lệ nếu phạm vi sản phẩm không cần thao tác dữ liệu.

Nguồn giao thức: [MCP Server Overview](https://modelcontextprotocol.io/specification/draft/server/index). Bài báo AI-KM được trích ở [ghi chú kiểm chứng riêng](AI_KM_RAG_WORKFLOW_ANALYSIS.md); không dùng bài báo làm bằng chứng cho công thức matching hoặc MCP nếu không xác minh được trong toàn văn.

## 2. Trạng thái được xác nhận trong source TalentBridge

### Chatbot hỗ trợ trong ứng dụng

`GroundedTalentBridgeAssistantAdapter` truy xuất tối đa ba mục từ bộ hướng dẫn cố định bằng chuẩn hóa văn bản, mở rộng synonym và giao nhau từ khóa. Nếu có `GEMINI_API_KEY`, nội dung truy xuất được gửi cùng câu hỏi/lịch sử đến Gemini; nếu thiếu key hoặc provider lỗi thì trả hướng dẫn truy xuất. API yêu cầu tài khoản có vai trò Candidate, Recruiter hoặc Admin. Chatbot không nhận quyền đọc dữ liệu CV riêng hay thay đổi hồ sơ.

Giới hạn: đây là knowledge-base retrieval dạng lexical, không có vector store, embedding search, ingestion/indexing pipeline hay benchmark truy hồi. Gemini là tùy chọn sinh câu trả lời, không phải bằng chứng rằng retrieval đã trở thành vector RAG.

### Matching CV với tin

`TriVectorRagMatchingService` dùng ba tập token khái niệm: yêu cầu tin, token trong CV/hồ sơ và kỹ năng heuristic được suy ra từ một số công nghệ. Mỗi điểm thành phần kết hợp độ phủ từ khóa với cosine trên tập token; hai điểm được trộn thành phần trăm gợi ý. Đây là heuristic lexical mang tên lớp legacy, không phải vector embedding hay thuật toán được chứng minh bởi bài AI-KM. Khi Gemini được bật, AI trả đánh giá JSON; khi lỗi, có fallback xác định. Không diễn giải điểm như xác suất tuyển dụng hoặc kết luận khách quan về năng lực.

### JSON-RPC tools cho ATS

`TalentBridgeMcpController` công bố `tools/list`, `tools/call` và `ping`, yêu cầu role Recruiter; use case kiểm tra quyền sở hữu công ty/tin và hồ sơ đã ứng tuyển trước khi đọc hồ sơ hay cập nhật stage. Controller mới đi qua inbound port và phần xử lý dữ liệu qua outbound ports.

Đây là endpoint JSON-RPC riêng, hiện khai báo protocolVersion trong metadata nhưng chưa triển khai đủ handshake/lifecycle, transport và capability negotiation của MCP. Vì vậy chỉ gọi là “TalentBridge JSON-RPC tool endpoint”; không tuyên bố tương thích MCP đầy đủ. Chatbot FAQ hiện không tự gọi endpoint này.

## 3. Phân biệt kết quả và giới hạn

- Điểm chất lượng tin chấm mức đầy đủ/rõ của trường dữ liệu và tín hiệu kiểm duyệt nội bộ. Việc công ty APPROVED trong TalentBridge không xác minh độc lập tư cách pháp nhân hoặc tính chân thực của nội dung.
- Đánh giá CV–job kết hợp match hiện có với phần trăm đầy đủ hồ sơ. Đây là gợi ý cho Recruiter, không thay thế xác minh hồ sơ, phỏng vấn hay quyết định tuyển dụng.
- Pipeline là ảnh chụp số đơn theo `currentStage` hiện tại; không phải lịch sử chuyển đổi, thời gian tuyển hay tỷ lệ thành công theo cohort.
- Unit/integration tests xác minh quy tắc source/API và ranh giới quyền. Chúng không đo độ chính xác mô hình, chất lượng tuyển dụng hoặc lợi ích của AI.

## 4. Đề xuất phát triển tiếp theo

1. Giữ chatbot FAQ không có quyền mutation mặc định. Nếu cần trả lời thống kê/tình trạng riêng, thêm tool đọc có schema hẹp, xác thực actor và ownership trong use case; không cho LLM truy cập repository/JPA trực tiếp.
2. Nếu cần semantic RAG, bổ sung corpus có nguồn, chunking, embedding, retrieval evaluation set, citation tới tài liệu và kiểm thử câu hỏi ngoài phạm vi. Đo precision/recall hoặc hit-rate trước khi mô tả hiệu quả.
3. Nếu yêu cầu MCP chuẩn, dùng SDK MCP Java chính thức phù hợp với Spring Boot và viết interoperability test với client MCP thật; không tự thêm nhãn chuẩn cho JSON-RPC endpoint hiện tại.
4. Với CV matching, xây tập đánh giá có consent/ẩn danh, so sánh baseline và model, kiểm tra bias theo nhóm, hiệu chuẩn điểm và lưu rõ model/source/version. Trước khi có các bằng chứng đó, giữ nhãn advisory.

## 5. Nguồn tham khảo

- Wen et al., *Version 5.4.18 – AI–KM: Knowledge enhancement with RAG and workflow*, SoftwareX 31 (2025) 102349, [ScienceDirect](https://www.sciencedirect.com/science/article/pii/S2352711025003152), [DOI](https://doi.org/10.1016/j.softx.2025.102349). Nội dung bài viết ở đây được giới hạn theo abstract/metadata truy cập được.
- [Model Context Protocol: Server Overview](https://modelcontextprotocol.io/specification/draft/server/index), mô tả primitives Prompts, Resources và Tools.
- Gemini model configuration: [Google AI models](https://ai.google.dev/gemini-api/docs/models).
