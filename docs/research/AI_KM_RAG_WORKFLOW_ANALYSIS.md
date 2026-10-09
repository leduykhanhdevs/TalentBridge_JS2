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

- Chatbot truy xuất một tập hướng dẫn TalentBridge được khai báo trong source bằng chuẩn hóa từ khóa và synonym expansion; sau đó dùng nội dung đã truy xuất làm căn cứ cho Gemini nếu có API key, hoặc trả hướng dẫn dự phòng. Đây là retrieval theo từ khóa trên tập nhỏ, chưa phải vector-embedding RAG.
- Matching CV–tin dùng kết quả Gemini khi cấu hình được; đường dự phòng dùng độ phủ token và một tập quy tắc kỹ năng suy luận. Các token là tập đặc trưng từ văn bản, không phải vector embedding học được. Điểm matching là tín hiệu tham khảo, chưa được hiệu chuẩn bằng tập dữ liệu tuyển dụng có nhãn.
- Endpoint `/api/v1/mcp` là giao diện JSON-RPC tùy biến, có tools/list, tools/call và ping, phân quyền Recruiter và gọi use case. Chưa tuyên bố đây là MCP server tương thích đầy đủ với vòng đời, transport, capability negotiation và schema của đặc tả MCP hiện hành.

## Cách trình bày phù hợp trong báo cáo

Ghi riêng: (1) điều bài báo AI-KM thực sự mô tả; (2) heuristic/kiến trúc TalentBridge tự xây dựng; (3) kết quả đo được từ test hoặc dữ liệu đánh giá. Không dùng test unit để tuyên bố độ chính xác tuyển dụng, không gọi matching hiện tại là semantic vector RAG, và không gọi endpoint JSON-RPC tùy biến là MCP chuẩn.
