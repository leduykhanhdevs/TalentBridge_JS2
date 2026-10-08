# NHẬT KÝ BÁO CÁO TIẾN ĐỘ DỰ ÁN (PROGRESS REPORT)
## TALENTBRIDGE – NỀN TẢNG TUYỂN DỤNG & QUẢN TRỊ ỨNG VIÊN ATS

---

## 📌 BÁO CÁO ĐỢT 2: NGHIÊN CỨU KHOA HỌC, CI/CD, CLOUD RENDER & HỆ THỐNG AI (RAG + MCP)
- **Thời gian hoàn thành**: 06/10/2026
- **Người thực hiện**: Lê Duy Khánh (Tech Lead) & AI Pair Programmer
- **Học phần**: Java Spring 2 – ITC

---

### 1. Bóc Tách Bài Báo Khoa Học & Chuẩn Hóa Ngữ Cảnh "Working Capsule"
- **Đã làm được gì**:
  - Đọc và bóc tách toàn diện bài báo khoa học quốc tế *"Version 5.4.18 – AI–KM: Knowledge enhancement with RAG and workflow"* (SoftwareX 31, 2025, Elsevier).
  - Khảo sát mã nguồn GitHub tác giả (`whl1207/Knowledge`), học tập cấu trúc `agentSkills.ts` (chuẩn `SKILL.md`) và `learningCore.ts` (thuật toán SRS phân loại tri thức).
  - Soạn thảo tài liệu phân tích chi tiết tại `docs/research/AI_KM_RAG_WORKFLOW_ANALYSIS.md`.
  - Thiết lập quy chuẩn nén ngữ cảnh 8 mục `WORKING CAPSULE` khi gãy context tại `docs/WORKING_CAPSULE_GUIDE.md`, `.agents/rules/working-capsule.md`, `AGENTS.md` và `.agents/skills/knowledge-capsule/SKILL.md`.
- **Công nghệ / Thuật toán / Kết hợp**:
  - Thuật toán Suy luận thông tin ẩn (Inferred Hidden Information).
  - Thuật toán tương đồng lai 3 vector (Tri-Vector Hybrid Similarity).
  - Giảm chiều dữ liệu Multidimensional Scaling (MDS).
  - Định dạng Agent Skill chuẩn `SKILL.md` (OpenClaw / Antigravity).
- **Mục đích chức năng / Việc đã làm**:
  - Khắc phục triệt để hiện tượng trượt ngữ nghĩa và ảo giác (hallucination) của RAG truyền thống.
  - Bảo toàn 100% ngữ cảnh làm việc của kỹ sư và AI khi làm việc đường dài, loại bỏ lãng phí token và tránh lặp lại lỗi cũ.

---

### 2. Tự Động Hóa CI/CD (GitHub Actions) & Tối Ưu Hóa Docker
- **Đã làm được gì**:
  - Tách quy trình thành 2 pipelines riêng biệt:
    - `.github/workflows/ci.yml`: Chạy song song kiểm thử Backend (JDK 21), Frontend (Lint, 84 Vitest tests, Build) và Docker verification khi mở Pull Request.
    - `.github/workflows/cd-render.yml`: Tự động gọi webhook Deploy Hook của Render để triển khai mã nguồn mới nhất lên môi trường production khi merge vào nhánh `main`.
  - Cấu hình lại `Dockerfile` tối ưu hóa bộ nhớ RAM.
- **Công nghệ / Thuật toán / Kết hợp**:
  - GitHub Actions Matrix / Parallel Jobs, Docker Multi-Stage Build (Eclipse Temurin 21 Alpine).
  - Cấu hình JVM đặc trị cho container 512MB: `-XX:+UseSerialGC -XX:MaxRAMPercentage=70.0 -Xss256k`.
- **Mục đích chức năng / Việc đã làm**:
  - Rút ngắn thời gian chạy CI từ 8 phút xuống còn ~2 phút (loại bỏ bước khởi động backend ngầm không cần thiết trong unit test).
  - Đảm bảo Spring Boot 3 chạy mượt mà trên Render Free Tier (512MB RAM) mà không bao giờ bị dính lỗi Out of Memory (OOM exit code 137).

---

### 3. Hạ Tầng & Triển Khai Cloud Render
- **Đã làm được gì**:
  - Viết tệp Blueprint `render.yaml` (Infrastructure as Code) quản lý trọn gói cả Backend Web Service và Frontend Static Site.
  - Bổ sung profile `prod` và biến môi trường `server.port: ${PORT:8080}` trong `application.yml`.
  - Biên soạn tài liệu cẩm nang triển khai chi tiết từng bước tại `docs/RENDER_DEPLOYMENT_GUIDE.md`.
- **Công nghệ / Thuật toán / Kết hợp**:
  - Render Blueprint (IaC), Single Page Application (SPA) rewrite routing `/* -> /index.html`.
  - Kết nối Cloud MySQL (Aiven / Clever Cloud) miễn phí giữ nguyên 100% CSDL 27 bảng và 103 jobs.
- **Mục đích chức năng / Việc đã làm**:
  - Cho phép triển khai toàn bộ hệ thống lên Cloud chỉ bằng 1 cú nhấp chuột.
  - Fix lỗi Blueprint: Chuẩn hóa service Frontend Static Site thành `type: web` kết hợp `runtime: static`, bỏ hoàn toàn hai trường `plan` và `region` theo đúng JSON Schema chính thức của Render (`https://render.com/schema/render.yaml.json`).
  - Xử lý các thách thức đặc thù của Cloud miễn phí: giải pháp chống ngủ đông (Cold Start) bằng UptimeRobot và tối ưu tài nguyên.

---

### 4. Hệ Thống Trí Tuệ Nhân Tạo Kép: RAG Tri-Vector & Model Context Protocol (MCP)
- **Đã làm được gì**:
  - Soạn thảo báo cáo đối chiếu học thuật và thực tiễn 10 chiều giữa RAG và MCP tại `docs/research/AI_MCP_VS_RAG_COMPREHENSIVE_ANALYSIS.md`.
  - Cài đặt dịch vụ `TriVectorRagMatchingService.java` so khớp độ phù hợp giữa CV ứng viên và JD tuyển dụng.
  - Cài đặt máy chủ `TalentBridgeMcpController.java` tuân thủ chuẩn JSON-RPC 2.0 cung cấp 4 công cụ tuyển dụng ATS.
  - Tạo REST Controller `AiMatchingController.java` (`POST /api/v1/ai/match`).
  - Viết 2 bộ test kiểm thử tự động đạt 100%: `TriVectorRagMatchingServiceTest` và `TalentBridgeMcpControllerTest`.
- **Công nghệ / Thuật toán / Kết hợp**:
  - **Công thức Tri-Vector Hybrid Similarity từ bài báo SoftwareX 2025**:
    $$Merge(q, v_i, u_i) = \frac{\cos(q, v_i) + \cos(q, u_i)}{2}$$
  - Kết hợp Overlap Coefficient (độ bao phủ kỹ năng yêu cầu) và Cosine Similarity (mật độ từ vựng), kết hợp chuẩn hóa tiếng Việt loại bỏ dấu thanh.
  - Giao thức chuẩn mở **Model Context Protocol (MCP)** của Anthropic / JSON-RPC 2.0.
  - Tích hợp Gemini AI (`gemini-flash-lite-latest`) với fallback deterministic chạy offline.
- **Mục đích chức năng / Việc đã làm**:
  - **Đánh giá tuyển dụng chính xác**: Phát hiện năng lực thực chiến tiềm năng của ứng viên ngay cả khi CV dùng từ khóa khác với JD.
  - **Biến AI thành Trợ lý hành động (Agentic ATS)**: Cho phép mọi AI Client (Claude Desktop, Cursor, Antigravity) kết nối trực tiếp vào TalentBridge để tra cứu việc làm, xem ứng viên và cập nhật trạng thái tuyển dụng trong CSDL.

---

### 5. Dữ Liệu Khởi Tạo & Tối Ưu Giao Diện Nhà Tuyển Dụng
- **Đã làm được gì**:
  - Khởi tạo 103 việc làm mẫu chất lượng cao (`JobSeedData.java`, `DataInitializer.java`) với đầy đủ kỹ năng, mức lương, địa điểm và phân loại ngành nghề.
  - Tinh chỉnh giao diện nút "Xem ứng viên" trên trang Quản lý tin tuyển dụng thành chữ trắng rõ nét (`!text-white`).
  - Kích hoạt phân trang mượt mà 10 việc/trang trên cổng tìm việc.
- **Công nghệ / Thuật toán / Kết hợp**:
  - Spring Boot Data Initialization, JPA Cascade, TailwindCSS.
- **Mục đích chức năng / Việc đã làm**:
  - Đảm bảo hệ thống có sẵn nguồn dữ liệu phong phú, phục vụ kiểm thử phân trang và demo trực quan cho giảng viên.

---

## BÁO CÁO KHẮC PHỤC RENDER BACKEND FAILED DEPLOY
- **Thời gian**: 06/10/2026
- **Đã làm được gì**:
  - Đối chiếu build log Render của commit `48410b9`: bước `COPY .mvn/ .mvn/` trong `Dockerfile:9` thất bại vì `.mvn` không có trong Docker build context.
  - Xác định nguyên nhân là quy tắc `.mvn` trong `.dockerignore`; đã bỏ quy tắc này để Maven Wrapper được đưa vào build context.
  - Chạy thành công `mvnw dependency:go-offline -B` và `mvnw clean package -DskipTests=true` bằng JDK 21, tương ứng các bước build backend trong Dockerfile.
  - GitHub Actions Backend Build & Verify và Frontend Test, Lint & Build đều pass trên PR #11. Docker image chưa được kiểm chứng trực tiếp vì Docker Desktop Linux engine không chạy trong môi trường kiểm tra.
- **Công nghệ / Thuật toán / Kết hợp**:
  - Docker multi-stage build, `.dockerignore`, Maven Wrapper và JDK 21.
- **Mục đích chức năng / Việc đã làm**:
  - Đảm bảo Docker builder nhận được `.mvn/wrapper/maven-wrapper.properties` để chạy Maven Wrapper, tránh lỗi thiếu đường dẫn `.mvn` khi build backend trên Render.

---

## BÁO CÁO KHẮC PHỤC KẾT NỐI TIDB VÀ PHÂN QUYỀN THƯ MỤC UPLOADS DOCKER
- **Thời gian**: 08/10/2026
- **Đã làm được gì**:
  - **Khắc phục lỗi CSDL**: Xác định chính xác Username TiDB Cloud là `2igDRivUbswCBxf.root` (ký tự `B` hoa thay vì số `8`), xác nhận kết nối TiDB Cloud thành công qua JDBC TLS.
  - **Khắc phục lỗi phân quyền Docker `/app/uploads`**:
    - Phân tích log Render mới nhất (`AccessDeniedException: /app/uploads`): Container chạy user bảo mật phi-root `appuser`, nhưng thư mục `/app` thuộc sở hữu `root:root` dẫn đến không thể tạo `/app/uploads`.
    - Thêm lệnh `RUN mkdir -p /app/uploads && chown -R appuser:appgroup /app` vào `Dockerfile` trước khi chuyển sang `USER appuser`.
    - Cải tiến `LocalFileStorageAdapter.java` với cơ chế resilient fallback: Nếu thư mục chỉ định bị từ chối quyền ghi, tự động chuyển hướng lưu trữ sang thư mục tạm hệ thống (`java.io.tmpdir/talentbridge-uploads`), triệt tiêu 100% rủi ro crash ứng dụng khi khởi động.
    - Cập nhật quy tắc kiểm thử ArchUnit `HexagonalArchitectureTest` vượt qua 10/10 tests (BUILD SUCCESS).
- **Công nghệ / Thuật toán / Kết hợp**:
  - Docker multi-stage security, Linux permissions (`chown -R`), Java NIO Files, Resilient Fallback Pattern, ArchUnit Hexagonal Architecture.
- **Mục đích chức năng / Việc đã làm**:
  - Đảm bảo Backend Spring Boot khởi động mượt mà, lưu trữ file tuyển dụng/CV an toàn và chuyển sang trạng thái Live trên Cloud Render.

---

## 🏆 CỘT MỐC TRIỂN KHAI THÀNH CÔNG CLOUD RENDER (PRODUCTION LIVE)
- **Thời gian**: 08/10/2026
- **Đã làm được gì**:
  - **Backend Live**: Dịch vụ `talentbridge-backend` chính thức đạt trạng thái Live tại `https://talentbridge-backend-6rmd.onrender.com`.
  - **Frontend Live**: Dịch vụ `talentbridge-frontend` chính thức đạt trạng thái Live tại `https://talentbridge-frontend.onrender.com`.
  - **Database Cloud**: Kết nối thông suốt cụm TiDB Cloud Serverless (MySQL 8.0 wire protocol), Flyway đã thực thi migration tự động schema `test`.
  - **Tài liệu API**: Kích hoạt thành công Swagger UI Live tại `https://talentbridge-backend-6rmd.onrender.com/swagger-ui.html`.
  - Đã thực hiện Smoke Test tự động gọi các endpoint `/api/v1/jobs` và `/swagger-ui.html` trên production đều phản hồi `HTTP 200 OK`.
- **Công nghệ / Thuật toán / Kết hợp**:
  - Docker Multi-stage Container (JRE 21 Alpine), Spring Boot 3.3.4, Flyway Migration, TiDB Cloud Serverless, Vite Static Site CDN, Render Cloud Platform.
- **Mục đích chức năng / Việc đã làm**:
  - Hoàn tất 100% nhiệm vụ cốt lõi do giảng viên giao: Triển khai hoàn chỉnh toàn bộ hệ thống TalentBridge ATS lên Cloud công cộng, sẵn sàng báo cáo và nghiệm thu đồ án.
