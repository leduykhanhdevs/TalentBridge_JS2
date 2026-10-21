# HRPM-58 — Đánh giá Tri-Vector, RAG và MCP

**Ngày kiểm tra:** 19/10/2026. **Use case:** hỗ trợ nhà tuyển dụng đánh giá mức độ tương đồng tham khảo giữa hồ sơ ứng viên đã nộp và tin tuyển dụng; quyết định tuyển dụng vẫn do con người đưa ra.

## Những gì đã được prototype

- **Matching trực tiếp:** `TriVectorRagMatchingService` tính ba thành phần có thể giải thích từ yêu cầu công việc, bằng chứng hồ sơ và năng lực liên quan được suy luận thận trọng. Source hiện tại dùng xử lý từ vựng và cosine trên vector từ khóa; không dùng embedding, vector database, ground truth tuyển dụng hay mô hình sinh câu trả lời cho điểm số.
- **Công cụ MCP:** `talentbridge_evaluate_cv_fit` gọi `TalentBridgeMcpUseCaseImpl.evaluateCandidate(...)`, qua use case kiểm tra recruiter, công ty sở hữu tin và đơn ứng tuyển, rồi dùng chung `CandidateJobMatchingPort`. MCP cung cấp giao thức gọi công cụ và kiểm soát quyền; nó không phải thuật toán chấm điểm thứ hai.
- **So sánh có thể chạy lại:** `AiMatchingApproachComparisonTest` dùng cùng tin và hai hồ sơ tổng hợp, so sánh toàn bộ `CandidateJobMatchResult` giữa service trực tiếp và đường use case mà MCP gọi. Test khởi động 200 lượt, đo 500 lượt mỗi đường, đồng thời kiểm tra hồ sơ phù hợp có điểm cao hơn hồ sơ không liên quan. `TalentBridgeMcpProtocolIntegrationTest` riêng xác nhận initialize, tool discovery/call qua Streamable HTTP, 401 cho anonymous, 403 cho Candidate và chặn ghi nếu thiếu xác nhận.

## Kết quả trên fixture tổng hợp

Chạy bằng JDK 21, cùng tiến trình JVM, sau khi sửa lỗi chuẩn hóa phần trăm:

| Lượt đo 500 mẫu/đường | Matching trực tiếp | Use case MCP |
| --- | ---: | ---: |
| Chạy focused test | 229.41 µs | 285.50 µs |
| Chạy trong `mvnw -B verify` | 245.42 µs | 343.98 µs |
| Hồ sơ Backend phù hợp | 77.1% | 77.1% |
| Hồ sơ Marketing không liên quan | 0.0% | 0.0% |
| Độ giống nhau của toàn bộ kết quả | — | 100% |

Đường use case MCP thêm 56.09 µs (24.4%) ở focused run và 98.56 µs (40.2%) khi chạy trong full suite. Độ dao động cho thấy đây chỉ là microbenchmark test JVM, không phải JMH hay phép đo HTTP/JSON-RPC; overhead mạng và serialize chưa được benchmark. Các điểm 77.1%/0.0% chỉ xác nhận thứ tự trên hai hồ sơ giả lập, không phải độ chính xác tuyển dụng, độ công bằng hoặc xác suất trúng tuyển. Cần bộ CV/JD được chuyên gia gán nhãn và đánh giá theo tiêu chí đã định trước để đo precision/recall hay chất lượng xếp hạng.

Trong khi đối chiếu, phát hiện hàm làm tròn dùng chung đã chặn `matchPercentage` ở mức tối đa 1.0, dù DTO công bố thang 0–100. Sửa riêng phần làm tròn phần trăm, giữ ba điểm thành phần ở thang 0–1; thêm assertion để lỗi này không tái diễn.

## So sánh triển khai

| Khía cạnh | Tri-Vector trực tiếp | MCP gọi matching | RAG trợ lý tri thức |
| --- | --- | --- | --- |
| Thiết lập | Thấp: tái dùng hồ sơ, tin tuyển dụng và Java; không cần model/vector store. | Thấp–trung bình: endpoint Streamable HTTP, schema tool, xác thực Recruiter, tenant/ownership checks, giới hạn input và kiểm thử quyền. | Đã có luồng trợ lý truy xuất tài liệu hướng dẫn đã duyệt rồi gọi bộ sinh câu trả lời Gemini; cần duy trì corpus, nguồn trích dẫn, cấu hình provider, bảo vệ dữ liệu và fallback. |
| Latency | 229.41 µs trong fixture, chỉ gồm gọi xử lý trực tiếp. | 285.50 µs cho use case trong tiến trình; chưa tính JSON-RPC qua mạng. | Phụ thuộc retrieval và provider Gemini; chưa đo trong benchmark này, thường chịu thời gian mạng/model nên không thể so bằng số hiện có. |
| Chất lượng | 77.1% so với 0.0% trên hai fixture tổng hợp; chưa có nhãn chuẩn thực tế. | Điểm/evidence bằng khớp với service trực tiếp vì tái sử dụng cùng thuật toán; MCP không tăng độ chính xác matching. | Phù hợp trả lời câu hỏi dựa trên tài liệu và có thể trích căn cứ; chưa được prototype như bộ chấm CV–job, và cũng chưa có bộ dữ liệu đánh giá độ đúng. |
| Bảo trì | Luật từ khóa dễ kiểm thử và giải thích; thay đổi yêu cầu phải cập nhật test. | Duy trì hợp đồng tool, quyền và kiểm soát tenant; luật nghiệp vụ vẫn ở use case/service chung để tránh nhân đôi. | Duy trì nội dung/phiên bản tài liệu, retrieval, prompt, kiểm thử groundedness và provider/fallback. |

## Khuyến nghị

Dùng **kiến trúc kết hợp theo nhiệm vụ**: Tri-Vector tạo tín hiệu matching có thể giải thích; MCP cung cấp thao tác ATS động và được kiểm soát theo vai trò; RAG giúp trợ lý trả lời từ tài liệu TalentBridge đã duyệt. Không dùng điểm tương đồng để tự quyết định tuyển dụng. Không coi MCP và RAG là hai thuật toán thay thế nhau.

## Giới hạn nghiệm thu

Jira yêu cầu nhóm đồng thuận trước khi đóng HRPM-58. Hiện tài liệu ghi khuyến nghị kỹ thuật nhưng không đại diện cho biểu quyết của các thành viên, nên ticket vẫn ở **In Review** cho tới khi nhóm xác nhận. Ngoài ra, benchmark này đo đường matching trực tiếp so với MCP-use-case trên cùng dữ liệu; source hiện không có một bộ chấm CV–job RAG độc lập để so latency/chất lượng apples-to-apples. Không suy diễn benchmark hiện tại thành đánh giá RAG matcher.

## Tái lập

Với JDK 21, từ thư mục gốc repo:

```powershell
./mvnw.cmd "-Dtest=TriVectorRagMatchingServiceTest,AiMatchingApproachComparisonTest" test
```
