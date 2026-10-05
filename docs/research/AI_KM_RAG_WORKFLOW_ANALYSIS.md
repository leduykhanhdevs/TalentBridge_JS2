# TÀI LIỆU BÓC TÁCH & NGHIÊN CỨU CHUYÊN SÂU BÀI BÁO KHOA HỌC
## AI–KM: Knowledge Enhancement with RAG and Workflow (SoftwareX 2025)

---

### THÔNG TIN ĐỊNH DANH BÀI BÁO (CITATION METADATA)
- **Tên bài báo**: *Version 5.4.18 – AI–KM: Knowledge enhancement with RAG and workflow*
- **Tác giả**: Haolong Wu, Wei Jiang, Xuesong Zhang, Hongjie Zhang, Mengxing Huang
- **Đơn vị nghiên cứu**: School of Information and Communication Engineering, State Key Laboratory of Marine Resource Utilization in South China Sea, Hainan University, Haikou, China.
- **Tạp chí công bố**: **SoftwareX**, Volume 31 (2025) 102349, Elsevier.
- **DOI**: [10.1016/j.softx.2025.102349](https://doi.org/10.1016/j.softx.2025.102349)
- **Mã nguồn chính thức**: [https://github.com/whl1207/Knowledge](https://github.com/whl1207/Knowledge)
- **Ngày nhận bài (Received)**: 15/06/2025 | **Ngày duyệt chấp thuận (Accepted)**: 19/08/2025

---

## 1. TỔNG QUAN BỐI CẢNH & Ý NGHĨA KHOA HỌC (MOTIVATION & SIGNIFICANCE)

### 1.1. Thách thức cốt lõi của Mô hình ngôn ngữ lớn (LLMs)
1. **Ảo giác tri thức (Hallucination)**: LLM tạo ra câu trả lời nghe rất thuyết phục nhưng sai sự thật do thiếu dữ liệu kiểm chứng cập nhật hoặc hạn chế về miền tri thức chuyên sâu.
2. **Chi phí Fine-Tuning quá lớn**: Việc huấn luyện lại hoặc tinh chỉnh mô hình đòi hỏi phần cứng GPU đắt đỏ, kỹ sư dữ liệu tay nghề cao và chu kỳ cập nhật chậm.
3. **Nguy cơ rò rỉ dữ liệu (Data Privacy Leakage)**: Khi doanh nghiệp/tổ chức gửi tài liệu mật qua các API đám mây thương mại (OpenAI, Anthropic), rủi ro rò rỉ bí mật kinh doanh và vi phạm pháp lý là rất lớn.
4. **Hạn chế của RAG truyền thống (Naive RAG)**:
   - Chỉ dựa vào Cosine Similarity giữa câu hỏi thô của người dùng $u_i$ và đoạn văn bản gốc $q$.
   - Khi câu hỏi của người dùng mơ hồ hoặc dùng từ đồng nghĩa/khác biệt với văn bản gốc, Naive RAG thất bại trong việc tìm trúng ngữ cảnh.

### 1.2. Giải pháp đột phá từ AI-KM
AI-KM giới thiệu giải pháp quản lý tri thức cục bộ (Local Knowledge Base) kết hợp 3 trụ cột:
- **Chạy cục bộ 100%**: Sử dụng Ollama làm inference engine, bảo vệ tuyệt đối dữ liệu nội bộ.
- **RAG nâng cao với kỹ thuật Suy luận thông tin ẩn (Inferred Hidden Information)**.
- **Visual Workflow Automation**: Cho phép tự động hóa quy trình phân tích và trích xuất dữ liệu đa nguồn mà không cần viết mã.

---

## 2. BÓC TÁCH KIẾN TRÚC PHẦN MỀM & MÔ HÌNH HỖ TRỢ (SOFTWARE ARCHITECTURE)

### 2.1. Cấu trúc 3 tầng (3-Tier Decoupled Architecture)
```
┌────────────────────────────────────────────────────────┐
│                   GIAO DIỆN NGƯỜI DÙNG                 │
│         Electron + Vue 3 + Pinia + TailwindCSS          │
│   (Quản lý KB, Trực quan hóa RAG, Trình dựng Workflow)  │
└───────────────────────────┬────────────────────────────┘
                            │ IPC / Local REST
┌───────────────────────────▼────────────────────────────┐
│                    TẦNG LOGIC & DỊCH VỤ                │
│    Chunking Engine | Reverse Question Gen | Vector Math│
│    MDS Dimensionality Reduction | Agent Skills Engine   │
└───────────────────────────┬────────────────────────────┘
                            │ Ollama API / Embeddings
┌───────────────────────────▼────────────────────────────┐
│               LOCAL INFERENCE & RUNTIME                │
│        Ollama | bge-m3 | nomic-embed-text | Local DB   │
└────────────────────────────────────────────────────────┘
```

### 2.2. Danh mục mô hình được hỗ trợ & tích hợp
- **Mô hình suy luận chuyên sâu (Reasoning Models)**:
  - DeepSeek-R1 (Mô hình suy luận chuỗi tư duy mở hàng đầu).
  - QwQ (Qwen with Questioning reasoning).
  - LLaMA 3.3 (Meta 70B đa năng).
- **Mô hình nhúng Vector (Embedding Models)**:
  - `bge-m3` (BAAI): Hỗ trợ đa ngôn ngữ, mật độ thông tin cao, hỗ trợ đa độ dài (Dense + Sparse).
  - `nomic-embed-text`: Nhúng văn bản ngữ cảnh dài với chi phí tài nguyên thấp.
- **Mô hình đa phương thức (Multimodal Models)**:
  - Gemma 3 (Google).
  - Mistral-Small 3.1.

---

## 3. PHƯƠNG PHÁP LUẬN TOÁN HỌC & ĐỘT PHÁ KỸ THUẬT (CORE METHODOLOGY)

### 3.1. Kỹ thuật suy luận thông tin ẩn (Inferred Hidden Information & Reverse Generation)
Thay vì chỉ index đoạn văn bản gốc $q$, hệ thống kích hoạt LLM để tạo ra tập hợp các câu hỏi tiềm năng $V = \{v_1, v_2, \dots, v_n\}$ mà đoạn văn bản $q$ có thể trả lời được:
$$q \xrightarrow{\text{LLM Reasoning}} V = \{v_1, v_2, \dots, v_n\}$$

### 3.2. Công thức tương đồng lai 3 Vector (Tri-Vector Hybrid Similarity)
Khi người dùng đưa ra câu hỏi $u_i$, hệ thống không chỉ so sánh $u_i$ với $q$, mà tính độ tương đồng trung bình kết hợp giữa cả câu hỏi của người dùng với đoạn $q$ và câu hỏi tiềm năng $v_i$ được sinh ra:

$$Merge(q, v_i, u_i) = \frac{\cos(q, v_i) + \cos(q, u_i)}{2} = \frac{\frac{q \cdot v_i}{\|q\| \|v_i\|} + \frac{q \cdot u_i}{\|q\| \|u_i\|}}{2}$$

Trong đó:
- $q \cdot v_i$ và $q \cdot u_i$ là tích vô hướng (dot product) giữa vector nhúng của đoạn kiến thức gốc, câu hỏi suy luận và truy vấn thực tế.
- $\|q\|$, $\|v_i\|$, $\|u_i\|$ là độ dài Euclid của các vector tương ứng.
- Phép tính kết hợp này triệt tiêu khoảng cách ngữ nghĩa giữa cách đặt câu hỏi của người dùng và văn bản hành chính/kỹ thuật.

### 3.3. Giảm chiều dữ liệu & Trực quan hóa không gian ngữ nghĩa (MDS)
- Sử dụng **MDS (Multidimensional Scaling)** để ánh xạ không gian vector đa chiều (ví dụ 1024-dim của `bge-m3`) xuống không gian 2D/3D.
- Giúp người dùng nhìn thấy các cụm tri thức (Clusters), phát hiện các vùng tri thức bị cô lập hoặc khoảng trống thông tin trong tài liệu.

### 3.4. Hệ thống trực quan hóa quy trình (Visual Workflow Engine)
Hệ thống cung cấp 3 khối Node cơ bản:
1. **Local File Node**: Đọc và bóc tách tài liệu cục bộ (.pdf, .docx, .md, .kb).
2. **Web Search Node**: Tích hợp công cụ trích xuất web định dạng Markdown sạch (qua Jina AI Reader).
3. **LLM Node**: Nhận dữ liệu đầu vào từ các node trước, áp dụng prompt hệ thống và sinh câu trả lời có cấu trúc.

---

## 4. KẾT QUẢ THỰC NGHIỆM TRONG BÀI BÁO (EMPIRICAL BENCHMARK)

- **Bộ dữ liệu kiểm thử**: Bộ câu hỏi chuẩn hóa (Standard Questions Dataset) bao gồm văn bản chính sách, quy chuẩn nội bộ và kiến thức nghiệp vụ.
- **Kết quả so sánh**:
  - Naive RAG: Dễ bỏ sót thông tin quan trọng khi câu hỏi của người dùng ngắn hoặc dùng thuật ngữ không trùng khớp với văn bản.
  - RAG + MDS + Inferred Hidden Information: Tăng độ chính xác truy xuất (Retrieval Accuracy) lên mức cao nhất, độ tương đồng ngữ nghĩa đạt độ khớp vượt trội và triệt tiêu gần như hoàn toàn hiện tượng ảo giác thông tin.

---

## 5. BÓC TÁCH MÃ NGUỒN GITHUB: `whl1207/Knowledge`

Qua khảo sát trực tiếp cấu trúc kho mã nguồn GitHub chính thức của tác giả:
1. **`src/services/agentSkills.ts`**:
   - Tác giả cài đặt giao thức **Agent Skill tương thích chuẩn `SKILL.md` (OpenClaw / Antigravity format)**.
   - Hỗ trợ bộ công cụ chuẩn hóa: `read_file`, `list_dir`, `search_files`, `kb_search` (tìm kiếm trong kho tri thức .kb qua RAG) và `mcp_call`!
2. **`src/services/learningCore.ts`**:
   - Thuật toán học tập dựa trên cơ chế **Spaced Repetition System (SRS)** phân loại tri thức thành 4 nhóm:
     - `memory` (ghi nhớ thực thể, sự kiện): chu kỳ lặp [1, 3, 7, 14, 30, 60] ngày.
     - `procedure` (quy trình, thao tác): chu kỳ lặp [3, 7, 14, 30] ngày.
     - `concept` (khái niệm lý thuyết): chu kỳ lặp [3, 7, 14, 30, 60] ngày.
     - `design` (thiết kế, đánh giá, cân nhắc đánh đổi): chu kỳ lặp [14, 28, 60] ngày.
   - Hàm `computeMastery(correctness: boolean[])`: Tính toán trọng số theo thời gian gần nhất (Recency Weights `[0.5, 0.7, 0.85, 0.95, 1.0]`) và chặn ngưỡng tin cậy thấp (Confidence Cap) để đảm bảo không bị đánh giá sai do đoán mò.
3. **Tầm nhìn tương lai trong Kết luận bài báo (Section 5)**:
   - Nhóm tác giả khẳng định việc tích hợp **Model Context Protocol (MCP)** vào RAG và Workflow là hướng phát triển tất yếu để biến hệ thống tri thức tĩnh thành **Hệ thống Đặc vụ Hành động Tự chủ (Autonomous Agentic System)**.

---

## 6. BÀI HỌC ÁP DỤNG TRỰC TIẾP VÀO DỰ ÁN TALENTBRIDGE

| Nguyên lý từ bài báo AI-KM | Ứng dụng thực tế trong TalentBridge |
| :--- | :--- |
| **Inferred Hidden Information** | Khi ứng viên nộp CV, hệ thống không chỉ bóc tách chữ thô, mà LLM tự động suy luận: *"Ứng viên này giải quyết được những bài toán kỹ thuật nào? Phù hợp với những vị trí nào?"* |
| **Tri-Vector Matching** | Khớp nối tuyển dụng 3 chiều: $\text{Cosine}(JD, \text{CV Thô}) + \text{Cosine}(JD, \text{Kỹ năng suy luận}) \to$ Cho điểm chính xác tuyệt đối, tránh bỏ sót ứng viên giỏi diễn đạt kém. |
| **Workflow Engine** | Xây dựng pipeline tuyển dụng tự động: Tải CV $\to$ Bóc tách Gemini AI $\to$ Vector matching JD $\to$ Sinh câu hỏi phỏng vấn cá nhân hóa theo từng ứng viên. |
| **MCP Integration** | Đóng gói TalentBridge thành một MCP Server: Cung cấp các Tools cho AI (`get_candidate_profile`, `match_jobs_for_candidate`, `schedule_interview`, `update_application_status`). |

---

*Tài liệu được biên soạn phục vụ đồ án môn học Java Spring Boot 2 & Hệ thống Tuyển dụng TalentBridge.*
