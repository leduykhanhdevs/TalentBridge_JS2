# AI-KM và TalentBridge: ghi chú kiểm chứng nguồn

**Ngày cập nhật:** 09/10/2026
**Mục đích:** tách thông tin được nguồn công bố xác nhận khỏi thiết kế/thuật toán riêng của TalentBridge.

## Nhận diện bài báo

- Wen, H.; Wang, S.; Liang, X.; Li, B.; Hu, W.; Luo, X. *Version 5.4.18 – AI–KM: Knowledge enhancement with RAG and workflow*. SoftwareX, Volume 31 (2025), article 102349. DOI: [10.1016/j.softx.2025.102349](https://doi.org/10.1016/j.softx.2025.102349).
- [Trang bài báo của nhà xuất bản ScienceDirect](https://www.sciencedirect.com/science/article/pii/S2352711025003152).

## Nội dung xác nhận được từ abstract nhà xuất bản

Abstract mô tả AI-KM là hệ thống kết hợp nhiều LLM với RAG dựa trên Ollama; hỗ trợ vector hóa và xử lý Markdown, truy xuất và hỏi đáp trên knowledge base, workflow engine có khả năng mở rộng, cùng visualization/UI. Abstract nêu các bối cảnh ứng dụng như giáo dục, hỗ trợ giảng dạy, quản lý thông tin thiết bị và tư vấn quy định.

Trong phiên rà soát này, trang toàn văn bị từ chối truy cập; vì vậy ghi chú này chỉ xác nhận abstract và metadata nhìn thấy được, không khẳng định đã kiểm chứng toàn văn hay từng công thức trong các mục nội dung.

## Các khẳng định cần tách khỏi bài báo

Abstract đã kiểm tra không xác nhận công thức Tri-Vector, “Inferred Hidden Information” theo dạng reverse question/skill generation, tích hợp MCP, mô hình embedding cụ thể, độ chính xác gần tuyệt đối hoặc mức cải thiện định lượng. Không nên gán các ý tưởng này cho bài báo nếu chưa chỉ ra trang/mục cụ thể trong toàn văn. Các ý tưởng đó có thể được đề xuất hoặc kiểm thử riêng cho TalentBridge.

RAG, embedding search và LLM generation cũng không tự động bảo đảm câu trả lời đúng, bảo mật dữ liệu hay không có hallucination. Các thuộc tính đó cần được đo và kiểm soát trong triển khai thực tế.

## Đối chiếu với source TalentBridge hiện tại

- Chatbot truy xuất tối đa ba hướng dẫn TalentBridge bằng ba tín hiệu lexical: từ khóa chính xác, mở rộng synonym và Dice similarity trên character-trigram. FAQ phổ biến có trả lời cục bộ; câu hỏi phù hợp khác có thể được Gemini diễn đạt từ tài liệu truy xuất. Lịch sử không được gửi tới Gemini, dữ liệu email/điện thoại/OTP/credential phổ biến bị chặn trước provider; khi thiếu key hoặc provider lỗi, chatbot trả hướng dẫn truy xuất. Đây vẫn là retrieval heuristic trên corpus nhỏ, không phải embedding/vector search; phần sinh là RAG grounded generation có fallback, không phải bảo đảm không hallucination.
- Matching CV–tin dùng ba chiều lexical xác định do dự án tự xây: bằng chứng trực tiếp 60%, ngữ cảnh nghề nghiệp 25%, suy luận năng lực thận trọng 15%. Mỗi chiều ghép độ phủ từ khóa có trọng số với cosine similarity trên từ vựng chuẩn hóa; reverse-skill generation tạo câu hỏi trung lập để xác minh năng lực suy luận/kỹ năng thiếu bằng chứng. CV được trích xuất trong backend sau xác minh quyền, không gửi sang Gemini. Điểm là tín hiệu tham khảo, chưa hiệu chuẩn bằng dữ liệu tuyển dụng có nhãn và không phải xác suất trúng tuyển.
- MCP dùng MCP Java SDK chính thức, Streamable HTTP Servlet tại `/api/v1/mcp`, protocol `2025-11-25` và năm tool ATS có schema giới hạn. Spring Security chỉ cho Recruiter xác thực; tool truyền identity qua transport context và gọi application use case có kiểm tra quyền sở hữu. Tool ghi yêu cầu `confirm=true`. HTTP integration tests bao phủ initialize, notification initialized, discovery, tool call, anonymous 401, Candidate 403 và từ chối ghi thiếu confirm. Đây là test tương thích trong ứng dụng, chưa phải interoperability test với MCP client độc lập; nhánh chưa merge/deploy.

## Cách trình bày phù hợp trong báo cáo

Ghi riêng: (1) điều bài báo AI-KM thực sự mô tả; (2) heuristic/kiến trúc TalentBridge tự xây dựng; (3) kết quả đo được từ test hoặc dữ liệu đánh giá. Không dùng test unit để tuyên bố độ chính xác tuyển dụng, không gọi retrieval hiện tại là semantic vector RAG, và không suy rộng test SDK nội bộ thành bằng chứng interoperability với mọi MCP client.
