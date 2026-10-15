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

- Chatbot truy xuất một tập hướng dẫn TalentBridge cố định bằng ba tín hiệu lexical: từ khóa chính xác, từ khóa mở rộng synonym và độ tương đồng character-trigram; một số FAQ có đường trả lời nhanh. Nội dung truy xuất được dùng làm căn cứ cho Gemini nếu có API key, hoặc làm câu trả lời dự phòng. MCP help tool dùng chung cổng tìm kiếm tri thức này. Đây vẫn là retrieval heuristic trên corpus nhỏ, không phải embedding/vector RAG.
- Matching CV–tin dùng kết quả Gemini khi cấu hình được; đường dự phòng dùng độ phủ token và một tập quy tắc kỹ năng suy luận. Các token là tập đặc trưng từ văn bản, không phải vector embedding học được. Điểm matching là tín hiệu tham khảo, chưa được hiệu chuẩn bằng tập dữ liệu tuyển dụng có nhãn.
- Nhánh tính năng MCP hiện thay endpoint JSON-RPC tự viết bằng MCP Java SDK chính thức, Streamable HTTP Servlet tại `/api/v1/mcp`, khai báo giao thức `2025-11-25` và schema cho các tool ATS. Quyền Recruiter được kiểm tra ở Spring Security và tại thời điểm gọi tool; use case tiếp tục kiểm soát quyền sở hữu nghiệp vụ. Test HTTP tích hợp bao phủ initialize, `notifications/initialized`, `tools/list`, `tools/call`, anonymous 401 và Candidate 403. Đây là bằng chứng tương thích giao thức ở mức test trong cùng ứng dụng, chưa phải kiểm thử với MCP client độc lập; thay đổi chưa merge/deploy.

## Cách trình bày phù hợp trong báo cáo

Ghi riêng: (1) điều bài báo AI-KM thực sự mô tả; (2) heuristic/kiến trúc TalentBridge tự xây dựng; (3) kết quả đo được từ test hoặc dữ liệu đánh giá. Không dùng test unit để tuyên bố độ chính xác tuyển dụng, không gọi retrieval hiện tại là semantic vector RAG, và không suy rộng test SDK nội bộ thành bằng chứng interoperability với mọi MCP client.
