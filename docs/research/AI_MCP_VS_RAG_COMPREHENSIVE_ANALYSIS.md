# BÁO CÁO NGHIÊN CỨU & ĐỐI CHIẾU CHUYÊN SÂU: RAG VÀ MCP
## XÂY DỰNG HỆ SINH THÁI ĐẶC VỤ TUYỂN DỤNG THÔNG MINH CHO TALENTBRIDGE

> **Học phần**: Java Spring 2 – Dự án TalentBridge  
> **Nền tảng nghiên cứu**: Dựa trên công trình khoa học quốc tế *AI–KM: Knowledge enhancement with RAG and workflow* (SoftwareX 31, 2025)  
> **Tác giả nghiên cứu**: Lê Duy Khánh & AI Pair Programmer  

---

## 1. MỞ ĐẦU & BỐI CẢNH KHOA HỌC

Trong kỷ nguyên phát triển bùng nổ của các Mô hình ngôn ngữ lớn (LLMs), hai thách thức lớn nhất khi áp dụng AI vào các hệ thống doanh nghiệp phức tạp (như Hệ thống Quản lý Tuyển dụng & ATS) là:
1. **Thiếu hụt tri thức chuyên biệt và ảo giác thông tin (Hallucination)**: Mô hình không nắm được dữ liệu nội bộ (hàng ngàn CV ứng viên, yêu cầu công việc chi tiết của từng doanh nghiệp) và có xu hướng tự bịa ra thông tin khi được hỏi.
2. **Sự cô lập hành động (Isolation & Lack of System Agency)**: LLM chỉ là bộ sinh văn bản thuần túy, không có khả năng tự truy vấn CSDL quan hệ thời gian thực, không thể thay đổi trạng thái tuyển dụng hay gửi email thông báo cho ứng viên.

Để giải quyết triệt để hai bài toán này, hai mô hình công nghệ tiên tiến nhất hiện nay là:
- **RAG (Retrieval-Augmented Generation)**: Đại diện cho năng lực **Bộ nhớ ngoài & Mở rộng tri thức**.
- **MCP (Model Context Protocol)**: Đại diện cho năng lực **Hành động & Giao thức thực thi công cụ**.

Bài báo khoa học *AI-KM* (SoftwareX, 2025) của nhóm tác giả Đại học Hải Nam đã chứng minh tính ưu việt của việc cải tiến RAG thông qua suy luận thông tin ẩn, và đồng thời trong phần Kết luận (Section 5), các tác giả đã định hướng rõ: **Tương lai của RAG phải kết hợp cùng Model Context Protocol (MCP)** để tạo ra một hệ thống Agent tự chủ toàn diện.

---

## 2. BẢN CHẤT & NGUYÊN LÝ HOẠT ĐỘNG

### 2.1. RAG (Retrieval-Augmented Generation)

```
                            LUỒNG HOẠT ĐỘNG CỦA RAG
┌──────────────┐     ┌──────────────┐     ┌──────────────┐     ┌──────────────┐
│  Tài liệu CV │────►│  Embedding   │────►│ Vector Store │────►│ Top-K Chunks │
│   và JD thô  │     │   (bge-m3)   │     │  (Chroma/DB) │     │ có độ khớp   │
└──────────────┘     └──────────────┘     └──────────────┘     └──────┬───────┘
                                                                      │
                                     Câu hỏi tuyển dụng               │ Ngữ cảnh liên quan
                                             │                        ▼
                                             └───────────────►┌──────────────┐
                                                              │  LLM Engine  │──► Câu trả lời
                                                              └──────────────┘
```

- **Nguyên lý**: 
  - Tài liệu phi cấu trúc (CV PDF, JD) được cắt nhỏ thành các đoạn (*chunks*), chuyển hóa thành vector số học đa chiều (*dense embeddings*) và lưu trữ trong cơ sở dữ liệu vector.
  - Khi có yêu cầu, hệ thống tính toán khoảng cách vector (Cosine Similarity) để rút trích các đoạn có ngữ nghĩa gần nhất, ghép vào Prompt gửi tới LLM.
- **Hạn chế của Naive RAG**: Nếu ứng viên viết CV dùng từ ngữ khác với mô tả công việc (ví dụ: CV viết *"Xây dựng hệ thống phân tán chịu tải cao"* trong khi JD yêu cầu *"Kinh nghiệm với Kafka và Microservices"*), Naive RAG sẽ đánh giá độ tương đồng thấp do khoảng cách từ vựng xa.
- **Đột phá từ bài báo AI-KM (SoftwareX 2025)**:
  - Ứng dụng **Suy luận thông tin ẩn (Inferred Hidden Information)**: Cho LLM sinh ngược lại các năng lực tiềm năng $V = \{v_1, \dots, v_n\}$ mà CV sở hữu.
  - Áp dụng **Công thức tương đồng lai 3 vector (Tri-Vector Hybrid Similarity)**:
    $$Merge(q, v_i, u_i) = \frac{\cos(q, v_i) + \cos(q, u_i)}{2}$$
    Triệt tiêu hoàn toàn khoảng cách từ vựng giữa ứng viên và nhà tuyển dụng.

### 2.2. MCP (Model Context Protocol)

```
                            LUỒNG HOẠT ĐỘNG CỦA MCP
┌──────────────┐                       ┌────────────────────────────────────────┐
│  LLM Client  │◄── JSON-RPC 2.0 ─────►│          TALENTBRIDGE MCP SERVER       │
│  (Orchestrator)                      ├────────────────────────────────────────┤
│              │── tools/call ────────►│ • candidate_get_profile(id)            │
│              │                       │ • recruiter_filter_cvs(criteria)       │
│              │◄── tool_result ───────│ • application_update_stage(id, stage)  │
└──────────────┘                       └───────────────────┬────────────────────┘
                                                           │ JPA Repository
                                                           ▼
                                               ┌───────────────────────┐
                                               │ TalentBridge Database │
                                               └───────────────────────┘
```

- **Nguyên lý**: 
  - MCP là giao thức mở chuẩn hóa do Anthropic công bố vào cuối năm 2024, được thiết kế như chuẩn **"USB-C cho Trí tuệ Nhân tạo"**.
  - Thay vì hard-code các hàm gọi API riêng lẻ, MCP Server định nghĩa danh mục tài nguyên (*Resources*), công cụ (*Tools*) và mẫu câu hỏi (*Prompts*) dưới dạng JSON Schema chuẩn hóa.
  - Mô hình AI kết nối qua giao thức tiêu chuẩn (JSON-RPC qua Stdio hoặc Server-Sent Events SSE) và có thể tự động lựa chọn công cụ phù hợp để thực thi các tác vụ thực tế.
- **Điểm mạnh cốt lõi**:
  - **Tính tương tác 2 chiều (Read & Write)**: Không chỉ đọc dữ liệu, MCP cho phép AI ghi trực tiếp vào CSDL quan hệ, chuyển trạng thái đơn ứng tuyển, gửi email thông báo.
  - **Tính mô-đun hóa (Decoupling)**: Client AI có thể thay đổi tùy ý (Gemini, Claude, DeepSeek-R1, ChatGPT) mà không cần viết lại bất kỳ dòng code backend nào.

---

## 3. BẢNG ĐỐI CHIẾU TOÀN DIỆN: RAG VS MCP (10 CHIỀU ĐÁNH GIÁ)

| Tiêu chí so sánh | RAG (Retrieval-Augmented Generation) | MCP (Model Context Protocol) |
| :--- | :--- | :--- |
| **1. Bản chất công nghệ** | Kỹ thuật truy xuất ngữ nghĩa văn bản từ không gian vector. | Giao thức mở chuẩn hóa kết nối LLM với công cụ và ứng dụng ngoài. |
| **2. Mục đích chính** | Bổ sung **Bộ nhớ tri thức ngoài**, loại bỏ ảo giác thông tin. | Cung cấp **Năng lực hành động thực tế** (Action & Tool Invocation). |
| **3. Loại dữ liệu xử lý** | Dữ liệu phi cấu trúc hoặc bán cấu trúc (CV PDF, JD, chính sách). | Dữ liệu quan hệ, API thời gian thực, sự kiện hệ thống (ACID DB). |
| **4. Quyền hạn hệ thống** | **Chỉ đọc (Read-Only)**. Không thể can thiệp trạng thái ứng dụng. | **Đọc & Ghi (Read-Write)**. Có thể thực thi các thao tác thay đổi trạng thái. |
| **5. Cơ chế hoạt động** | Vector Embedding $\to$ K-NN Cosine Similarity $\to$ Chèn ngữ cảnh. | Khai báo Tools Schema $\to$ Function Calling $\to$ JSON-RPC Execution. |
| **6. Độ tươi mới dữ liệu** | Phụ thuộc vào chu kỳ cập nhật & re-indexing vector (có độ trễ). | **Thời gian thực tuyệt đối (Real-Time)**: Truy vấn trực tiếp vào CSDL. |
| **7. Độ chính xác dữ liệu số**| Tương đối (Dễ bị sai lệch số liệu thống kê phức tạp). | Tuyệt đối (Vì kết quả được truy vấn trực tiếp từ SQL/JPA queries). |
| **8. Cơ chế kiểm soát an toàn**| Lọc ngữ cảnh đầu vào, kiểm tra bản quyền tài liệu. | Phân quyền RBAC, cơ chế Human-in-the-Loop (phê duyệt trước khi ghi). |
| **9. Chi phí tính toán** | Tốn tài nguyên cho mô hình nhúng (Embedding) và Vector DB. | Nhẹ về tài nguyên, phụ thuộc vào số lượt gọi tool và kích thước token. |
| **10. Vai trò trong bài báo AI-KM**| Đóng vai trò là phương pháp luận chính (Tri-Vector Similarity). | Đóng vai trò là kiến trúc mở rộng tương lai được nhóm tác giả khẳng định. |

---

## 4. KIẾN TRÚC KẾT HỢP HYBRID TRONG TALENTBRIDGE

Không thể thay thế RAG bằng MCP và ngược lại. Giải pháp tối ưu nhất cho hệ thống tuyển dụng doanh nghiệp TalentBridge là **Kiến trúc Lai Hợp nhất (Unified Hybrid Architecture)**:

```
                            NGƯỜI DÙNG / HR MANAGER
                                       │
                                       ▼
                     ┌───────────────────────────────────┐
                     │     TALENTBRIDGE AI ORCHESTRATOR  │
                     │       (Gemini 2.5 Flash Engine)   │
                     └─────────────────┬─────────────────┘
                                       │
            ┌──────────────────────────┴──────────────────────────┐
            ▼                                                     ▼
┌───────────────────────────────┐             ┌───────────────────────────────────┐
│     RAG KNOWLEDGE TIER        │             │        MCP TOOL EXECUTION TIER    │
│  (Kho Tri thức & Bóc tách)    │             │   (Giao thức Hành động Nghiệp vụ) │
├───────────────────────────────┤             ├───────────────────────────────────┤
│ • Inferred Hidden Information │             │ • candidate_get_profile           │
│ • Tri-Vector Job-CV Matching  │             │ • recruiter_filter_applications   │
│   Merge(JD, CV, Inferred)     │             │ • application_update_stage        │
│ • Kho câu hỏi phỏng vấn chuẩn │             │ • interview_schedule_notification │
└───────────────────────────────┘             └───────────────────────────────────┘
```

### Phân công trách nhiệm rõ ràng:
1. **RAG Tier phụ trách tác vụ Nhận thức & Đánh giá**:
   - Khi ứng viên nộp CV: RAG trích xuất thông tin, suy luận kỹ năng tiềm năng, tính điểm khớp 3 chiều với JD tuyển dụng theo công thức $Merge(q, v_i, u_i)$ từ bài báo.
2. **MCP Tier phụ trách tác vụ Quản trị & Vận hành ATS**:
   - Khi HR ra lệnh: *"Lên lịch phỏng vấn và gửi mail cho tất cả ứng viên Backend đạt điểm matching trên 85%"*, LLM gọi công cụ MCP để lọc danh sách từ DB, cập nhật cột `application_stages` sang `INTERVIEW`, và kích hoạt Mail Service.

---

## 5. KẾT LUẬN

Việc kết hợp giữa **RAG Nâng cao (theo phương pháp luận của bài báo SoftwareX 2025)** và **Model Context Protocol (MCP)** đưa TalentBridge trở thành một nền tảng tuyển dụng thế hệ mới:
- **Thông minh hơn**: Đánh giá ứng viên chính xác, không bỏ sót nhân tài nhờ Tri-Vector Matching.
- **Tự động hóa cao hơn**: Biến LLM thành trợ lý HR chủ động, tương tác trực tiếp với cơ sở dữ liệu quan hệ an toàn và tuân thủ các chuẩn mực doanh nghiệp.
