# KẾ HOẠCH TỔNG THỂ (MASTER PLAN): CI/CD, DEPLOY RENDER & HỆ THỐNG AI (MCP VS RAG)

> **Dự án**: TalentBridge – Nền tảng Tuyển dụng Trực tuyến & ATS  
> **Cơ sở học thuật**: Dựa trên công trình *AI–KM: Knowledge enhancement with RAG and workflow* (SoftwareX 31, 2025)  
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
  │ 3. HỆ THỐNG AI THÔNG MINH: KẾT HỢP RAG & MCP (HYBRID ARCHITECTURE)                     │
  │    ┌────────────────────────────────────────┐   ┌──────────────────────────────────┐   │
  │    │ TRI-VECTOR RAG ENGINE                  │   │ MODEL CONTEXT PROTOCOL (MCP)     │   │
  │    │ (Tra cứu tri thức, CV matching theo    │   │ (Giao thức thực thi công cụ,     │   │
  │    │  nguyên lý bài báo SoftwareX 2025)     │   │  thao tác dữ liệu DB & Workflow) │   │
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

### TRỤ CỘT 3: AI INTELLIGENCE – ĐỐI CHIẾU & TÍCH HỢP RAG VS MCP

#### 1. Bảng đối chiếu học thuật & thực tiễn: RAG vs MCP

| Tiêu chí | RAG (Retrieval-Augmented Generation) | MCP (Model Context Protocol) |
| :--- | :--- | :--- |
| **Bản chất cốt lõi** | Mở rộng **Bộ nhớ tri thức** (Knowledge Retrieval) từ dữ liệu phi cấu trúc qua vector ngữ nghĩa. | Mở rộng **Năng lực hành động & Công cụ** (Tool Execution Protocol) qua giao thức 2 chiều chuẩn hóa. |
| **Phương thức hoạt động** | Embedding $\to$ Vector Database $\to$ Cosine Similarity $\to$ Chèn ngữ cảnh vào Prompt. | Khai báo JSON Schema Tools $\to$ LLM quyết định gọi hàm $\to$ Server MCP thực thi $\to$ Trả kết quả JSON. |
| **Loại dữ liệu xử lý** | Dữ liệu tĩnh/bán tĩnh (Tài liệu CV PDF, Sổ tay quy định, JD mô tả công việc, Luật lao động). | Dữ liệu động theo thời gian thực (Trạng thái đơn ứng tuyển trong DB, Lịch trống của HR, Gửi email, Tạo tài khoản). |
| **Khả năng tương tác** | **Thụ động (Read-only)**: Chỉ đọc và tổng hợp thông tin, không thể sửa đổi trạng thái hệ thống. | **Chủ động (Read-Write & Action)**: Có thể thay đổi CSDL, gọi webhook, gửi email, giao tiếp liên ứng dụng. |
| **Rào cản & Nhược điểm** | Chi phí tính toán embedding, vấn đề chunking, dễ mất ngữ cảnh nếu câu hỏi quá mơ hồ. | Đòi hỏi mô hình có khả năng Function Calling tốt, cần kiểm soát phân quyền chặt chẽ (Security & RBAC). |
| **Vị trí trong bài báo AI-KM** | Là trọng tâm của bài báo (Tri-Vector Hybrid Similarity $Merge(q, v_i, u_i)$). | Là hướng phát triển mở rộng được khẳng định ở phần Kết luận (Section 5) để tiến tới Agent tự chủ. |

#### 2. Kiến trúc giải pháp lai kết hợp (Hybrid AI Architecture cho TalentBridge):

```
                        NGƯỜI DÙNG / HR MANAGER / ỨNG VIÊN
                                        │
                                        ▼
                   ┌──────────────────────────────────────────┐
                   │        TALENTBRIDGE AI ORCHESTRATOR      │
                   │     (Gemini 2.5 Flash / Pro Engine)      │
                   └──────┬────────────────────────────┬──────┘
                          │                            │
             [Khi cần tra cứu tri thức]     [Khi cần thao tác hệ thống]
                          │                            │
                          ▼                            ▼
            ┌──────────────────────────┐  ┌──────────────────────────┐
            │   RAG KNOWLEDGE ENGINE   │  │   TALENTBRIDGE MCP SERVER│
            │ (Kế thừa bài báo AI-KM)  │  │   (Model Context Protocol)│
            ├──────────────────────────┤  ├──────────────────────────┤
            │ 1. CV Vector Store       │  │ • candidate_get_profile  │
            │ 2. Reverse Skill Gen     │  │ • recruiter_filter_cvs   │
            │ 3. Tri-Vector Matching   │  │ • app_update_status      │
            │    Cosine(JD, CV, Skill) │  │ • interview_schedule     │
            │ 4. Interview Q&A Bank    │  │ • email_notify_applicant │
            └──────────────────────────┘  └─────────────┬────────────┘
                                                        │
                                                        ▼
                                          ┌──────────────────────────┐
                                          │ TALENTBRIDGE CORE DB     │
                                          │ (MySQL / JPA Repository) │
                                          └──────────────────────────┘
```

#### 3. Các thành phần AI cụ thể cần hiện thực:
1. **Module RAG Tuyển dụng (Tri-Vector Job-CV Matching)**:
   - Áp dụng thuật toán từ bài báo SoftwareX 2025: Khi bóc tách CV, sinh ra các kỹ năng tiềm năng (Inferred Capabilities) rồi tính điểm khớp 3 chiều với JD tuyển dụng.
   - Xếp hạng độ phù hợp (Match Score %): Tránh trường hợp ứng viên tiềm năng bị loại chỉ vì CV viết thiếu từ khóa chính xác.
2. **Module MCP Server (TalentBridge Tool Suite)**:
   - Xây dựng MCP Server chuẩn REST/SSE hoặc stdio cho phép trợ lý AI có thể:
     - `search_candidates(skills, experience, location)`
     - `get_candidate_cv(candidate_id)`
     - `shortlist_candidate(application_id, note)`
     - `generate_tailored_interview_questions(job_id, candidate_id)`

---

## III. BẢNG PHÂN CÔNG & THỜI GIAN THỰC HIỆN DỰ KIẾN

| Giai đoạn | Nội dung công việc | Đầu ra (Deliverables) |
| :--- | :--- | :--- |
| **Phase 1** | Chuẩn hóa quy chuẩn, lưu trữ tài liệu nghiên cứu bài báo | `AI_KM_RAG_WORKFLOW_ANALYSIS.md`, `WORKING_CAPSULE_GUIDE.md`, Rule & Skill |
| **Phase 2** | Nâng cấp CI/CD Pipeline & Cấu hình Docker Production | `.github/workflows/ci.yml`, `.github/workflows/cd.yml`, Dockerfile tối ưu |
| **Phase 3** | Cấu hình triển khai Render (Backend + Frontend + Database) | `render.yaml`, Spring Boot Prod profile, Uptime & Memory configuration |
| **Phase 4** | Thiết kế & Cài đặt Kiến trúc AI (RAG Tri-Vector + MCP Tools) | Dịch vụ tính điểm CV matching theo bài báo + TalentBridge MCP Server |
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
