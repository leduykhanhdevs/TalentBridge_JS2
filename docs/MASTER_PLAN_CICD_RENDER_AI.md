# KẾ HOẠCH TỔNG THỂ (MASTER PLAN): CI/CD, DEPLOY RENDER & HỆ THỐNG AI (MCP VS RAG)

> **Ghi chú trạng thái 09/10/2026:** Đây là tài liệu kế hoạch có nội dung lịch sử, không phải xác nhận mọi hạng mục đã chạy production. Phần AI bên dưới đã được thay bằng trạng thái kiểm chứng từ source. Bài báo AI-KM chỉ được trích theo metadata/abstract; các claim về Tri-Vector/MCP không được gán cho bài báo nếu chưa có dẫn chứng toàn văn.

> **Dự án**: TalentBridge – Nền tảng Tuyển dụng Trực tuyến & ATS  
> **Tài liệu tham khảo**: *AI–KM: Knowledge enhancement with RAG and workflow* (SoftwareX 31, 2025); phạm vi claim được giới hạn theo abstract đã truy cập.
> **Tác giả kế hoạch**: Tech Lead & AI Pair Programmer  

---

## I. TỔNG QUAN 3 NHIỆM VỤ CHIẾN LƯỢC

```
                      HỆ SINH THÁI PHÁT TRIỂN & VẬN HÀNH TALENTBRIDGE
  ┌────────────────────────────────────────────────────────────────────────────────────────┐
  │ 1. CI/CD AUTOMATION (GitHub Actions)                                                  │
  │    Code Push ──► Lint & Test ──► Docker Build ──► Security Scan ──► CD Deploy Trigger   │
  └───────────────────────────────────────────┬────────────────────────────────────────────┘
                                              │ Auto Deploy Hook
  ┌───────────────────────────────────────────▼────────────────────────────────────────────┐
  │ 2. CLOUD INFRASTRUCTURE (Render Platform)                                              │
  │    ┌───────────────────────────────┐     ┌────────────────────────────────────────┐   │
  │    │ Frontend Static Site (Vite)   │ ──► │ Backend Web Service (Spring Boot 3)    │   │
  │    └───────────────────────────────┘     └───────────────────┬────────────────────┘   │
  │                                                              │                         │
  │                                          ┌───────────────────┴────────────────────┐   │
  │                                          │ Cloud Database (MySQL/PostgreSQL)      │   │
  │                                          └────────────────────────────────────────┘   │
  └────────────────────────────────────────────────────────────────────────────────────────┘
                                              ▲
                                              │ Tích hợp trí tuệ nhân tạo
  ┌───────────────────────────────────────────┴────────────────────────────────────────────┐
  │ 3. AI FEATURES: IN-APP GUIDE + ADVISORY MATCHING + ATS TOOLS (SEPARATE CAPABILITIES)    │
  │    ┌────────────────────────────────────────┐   ┌──────────────────────────────────┐   │
  │    │ HƯỚNG DẪN RETRIEVAL + GEMINI TÙY CHỌN │   │ RECRUITER JSON-RPC TOOL ENDPOINT│   │
  │    │ MATCH CV–JOB DẠNG ADVISORY            │   │ CÓ RBAC VÀ USE CASE OWNERSHIP   │   │
  │    └────────────────────────────────────────┘   └──────────────────────────────────┘   │
  └────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## II. KẾ HOẠCH CHI TIẾT TỪNG TRỤ CỘT (PILLAR BREAKDOWN)

---

### TRỤ CỘT 1: CI/CD PIPELINE (GITHUB ACTIONS AUTOMATION)

#### 1. Hiện trạng dự án:
- Đã có `.github/workflows/ci.yml` kiểm thử cơ bản (Spring Boot Maven verify, Vite Vitest & Build).
- Đang gộp cả bước chạy Backend ngầm để chạy E2E test trong cùng 1 job, thời gian chạy khá lâu (~5-8 phút).

#### 2. Mục tiêu cải tiến chuẩn doanh nghiệp:
- **Tối ưu hóa thời gian chạy**:
  - Tách thành 2 giai đoạn: **CI (Kiểm thử & Đảm bảo chất lượng)** khi tạo Pull Request, và **CD (Đóng gói Docker & Triển khai)** khi merge vào nhánh `main`.
  - Tận dụng triệt để Cache (Maven dependencies, pnpm store, Docker layer cache).
- **Security & Quality Gates**:
  - Kiểm tra lỗ hổng bảo mật mã nguồn (SonarCloud / Trivy container scan).
  - Kiểm tra rò rỉ Secrets (Gitleaks).
- **Tự động kích hoạt Render Deploy**:
  - Sử dụng Render Deploy Hook URL qua GitHub Actions Secret `RENDER_DEPLOY_HOOK_URL`.

#### 3. Lộ trình triển khai (Action Steps):
1. **Bước 1.1**: Tái cấu trúc workflow `.github/workflows/ci.yml` thành Pipeline song song (Parallel Matrix).
2. **Bước 1.2**: Tối ưu hóa Dockerfile với Multi-stage caching và giảm dung lượng image (< 200MB).
3. **Bước 1.3**: Thiết lập `.github/workflows/cd-render.yml` kích hoạt khi push vào `main`.

---

### TRỤ CỘT 2: DEPLOY LÊN NỀN TẢNG CLOUD RENDER

#### 1. Thách thức kỹ thuật trên Render Free/Starter Tier:
- **Giới hạn RAM (512MB)**:
  - Spring Boot 3 + Java 21 có thể chiếm 300-450MB nếu không cấu hình JVM hợp lý.
  - Giải pháp: Cấu hình JVM Flags tối ưu: `-XX:+UseSerialGC -XX:MaxRAMPercentage=70.0 -Xss256k -XX:+TieredCompilation -XX:TieredStopAtLevel=1`.
- **Hiện tượng Cold Start (Ngủ đông sau 15 phút không hoạt động)**:
  - Máy chủ mất 30-50s để khởi động lại khi có request mới.
  - Giải pháp: Thiết kế trang thông báo Loading thông minh trên Frontend hoặc thiết lập UptimeRobot ping định kỳ `/actuator/health`.
- **Cơ sở dữ liệu (Database)**:
  - Render cung cấp PostgreSQL miễn phí (hết hạn sau 30 ngày) hoặc PostgreSQL trả phí.
  - Nhưng hiện tại TalentBridge viết bằng MySQL (`talentbridge_db`).
  - Lựa chọn giải pháp DB:
    - *Phương án A (Khuyến nghị)*: Dùng Cloud MySQL miễn phí vĩnh viễn (Aiven MySQL / Clever Cloud / TiDB Serverless) kết nối vào Render Web Service.
    - *Phương án B*: Sử dụng H2 File Database lưu trên Render Disk (Dành riêng cho demo bảo vệ đồ án).
    - *Phương án C*: Cấu hình Spring Boot hỗ trợ profile `prod-postgres` để tương thích trực tiếp với Render Postgres.

#### 2. Mô hình kiến trúc triển khai trên Render:
- **Dịch vụ 1 (Backend)**: Render Web Service (Docker Environment).
  - Build command: Docker build tự động từ `Dockerfile`.
  - Environment variables: `SPRING_PROFILES_ACTIVE=prod`, `JWT_SECRET`, `SPRING_DATASOURCE_URL`, `GEMINI_API_KEY`.
- **Dịch vụ 2 (Frontend)**: Render Static Site.
  - Build command: `cd frontend && pnpm install && pnpm build`.
  - Publish directory: `frontend/dist`.
  - Rewrite rule: `/*` $\to$ `/index.html` (Single Page Application SPA routing).
- **Quản lý hạ tầng bằng mã (Infrastructure as Code)**:
  - Viết file `render.yaml` (Render Blueprint) cho phép Render tự động tạo toàn bộ dịch vụ chỉ bằng 1 cú click chuột ("Deploy to Render").

---

### TRỤ CỘT 3: AI – TRẠNG THÁI SOURCE VÀ GIỚI HẠN

#### 1. Phân biệt hai lớp

RAG truy xuất tri thức để làm căn cứ cho câu trả lời; MCP chuẩn hóa cách ứng dụng công bố tools, resources và prompts cho client. Tool có thể đọc hoặc ghi theo chính sách của ứng dụng; MCP không tự xác thực quyền hay bảo đảm an toàn. Xem [MCP Server Overview](https://modelcontextprotocol.io/specification/draft/server/index).

#### 2. Phần hiện có trong source

- Chatbot `/api/v1/assistant/chat`: truy xuất lexical từ hướng dẫn TalentBridge, rồi tùy chọn gọi Gemini; fallback trả nội dung đã truy xuất. Không có embedding/vector store; không tra cứu hồ sơ riêng và không thực hiện mutation.
- CV–job matching: Gemini khi có cấu hình; fallback từ độ phủ token, cosine trên tập token và quy tắc suy luận kỹ năng. Đây là heuristic advisory, chưa có bộ dữ liệu đánh giá để chứng minh accuracy.
- `/api/v1/mcp`: JSON-RPC tùy biến, có tools/list, tools/call, ping; yêu cầu Recruiter và kiểm quyền qua use case. Chưa tuyên bố tương thích MCP đầy đủ, chatbot FAQ chưa gọi endpoint này.
- Statistics: chất lượng tin dựa vào trường dữ liệu và tín hiệu nội bộ; candidate assessment là gợi ý; pipeline là ảnh chụp theo vòng hiện tại, không phải chuyển đổi lịch sử.

#### 3. Việc cần làm trước khi nâng mức khẳng định

Nếu cần semantic RAG, xây corpus có nguồn, chunking/embedding và evaluation set có nhãn; báo precision/recall hoặc hit-rate sau đo. Nếu cần MCP chuẩn, dùng SDK tương thích và interoperability test với client thật. Nếu cần chatbot gọi tool, chỉ công bố tool tối thiểu theo role/ownership, không cho LLM đi thẳng đến repository. Tất cả kết quả tuyển dụng phải giữ nhãn tham khảo cho đến khi được hiệu chuẩn và đánh giá bias.

Nguồn, cách phân biệt claim và chi tiết thuật toán hiện có: [AI-KM research note](research/AI_KM_RAG_WORKFLOW_ANALYSIS.md), [RAG/MCP and source audit](research/AI_MCP_VS_RAG_COMPREHENSIVE_ANALYSIS.md).

---

## III. BẢNG PHÂN CÔNG & THỜI GIAN THỰC HIỆN DỰ KIẾN

| Giai đoạn | Nội dung công việc | Đầu ra (Deliverables) |
| :--- | :--- | :--- |
| **Phase 1** | Chuẩn hóa quy chuẩn, lưu trữ tài liệu nghiên cứu bài báo | `AI_KM_RAG_WORKFLOW_ANALYSIS.md`, `WORKING_CAPSULE_GUIDE.md`, Rule & Skill |
| **Phase 2** | Nâng cấp CI/CD Pipeline & Cấu hình Docker Production | `.github/workflows/ci.yml`, `.github/workflows/cd.yml`, Dockerfile tối ưu |
| **Phase 3** | Cấu hình triển khai Render (Backend + Frontend + Database) | `render.yaml`, Spring Boot Prod profile, Uptime & Memory configuration |
| **Phase 4** | Chatbot hướng dẫn, advisory CV matching, recruiter JSON-RPC tools | Phần source có trong nhánh tính năng; chưa phải full semantic RAG/MCP interop và chưa chứng minh accuracy |
| **Phase 5** | Kiểm thử đầu-cuối (E2E), Tài liệu báo cáo gửi Giảng viên | Báo cáo kiến trúc hoàn chỉnh, Video/Demo link triển khai |

---

## IV. BỘ CÂU HỎI LÀM RÕ (CLARIFYING QUESTIONS)

*Để đảm bảo triển khai chính xác 100% đúng ý của bạn và yêu cầu của thầy, chúng ta cần thống nhất các quyết định chiến lược sau đây trước khi viết mã:*
1. **Về Nền tảng Cloud Render**:
   - Bạn sẽ sử dụng tài khoản Render cá nhân (Free Tier) hay gói có trả phí?
   - Cơ sở dữ liệu cho Render: Bạn ưu tiên dùng Cloud MySQL miễn phí (như Aiven/Clever Cloud để giữ nguyên CSDL hiện tại) hay muốn chuyển sang PostgreSQL của Render?
2. **Về Quy trình CI/CD**:
   - Bạn muốn CI chạy tự động kiểm thử mỗi khi mở Pull Request vào nhánh `dev`/`main`, và CD chỉ tự động kích hoạt deploy lên Render khi merge vào nhánh `main`, đúng không?
3. **Về Nhiệm vụ AI (MCP vs RAG)**:
   - Thầy yêu cầu nộp **bài báo cáo phân tích/thiết kế kiến trúc** (Architecture Proposal Document) hay yêu cầu **phải code thực tế tính năng chạy demo** trên web?
   - Nếu code demo, bạn muốn ưu tiên: (1) Tính năng tính điểm phù hợp CV - JD ứng dụng thuật toán RAG từ bài báo, hay (2) Trợ lý AI chat gọi công cụ MCP để quản lý hồ sơ ứng viên?
