# NHẬT KÝ BÁO CÁO TIẾN ĐỘ DỰ ÁN (PROGRESS REPORT)
## TALENTBRIDGE – NỀN TẢNG TUYỂN DỤNG & QUẢN TRỊ ỨNG VIÊN ATS

---

- **Đã làm được gì**: Đưa doanh nghiệp mới lên đầu danh sách duyệt theo `createdAt DESC, id DESC`; thêm nút hủy ứng viên có xác nhận; thêm hiện/ẩn mật khẩu; lưu file CV gốc sau nhập nhanh; chọn CV chính hoặc tải CV mới khi ứng tuyển; triển khai xác minh OTP email và số điện thoại, đồng thời chặn apply ở backend khi thiếu một trong hai xác minh. UI E2E đăng ký tài khoản thử trên H2, kiểm tra cancel, import CV, OTP mock, apply bằng CV chính và upload CV trong modal: 34/34; API E2E 28/28; Maven verify 391/391; Vitest 24 file / 138 test; TypeScript, ESLint, Vite build pass. GitHub Actions CI cho PR #70 pass cả backend, frontend và Docker (run `39899357130`). Kế hoạch và ma trận tiêu chí: `docs/qa/plans/2026-10-23-seven-point-release-plan.md`. Ảnh/log lượt pass: `docs/qa/full-site-uat-2026-10-20/runs/2026-10-23-111129/`. Không seed mật khẩu người dùng vào source. HRPM-71/72 chờ cấu hình và kiểm tra provider OTP thật; HRPM-73 theo dõi tài khoản/job/apply demo an toàn. Chưa merge/deploy: CD sẽ tự triển khai sau khi merge vào `main`, còn thiếu cấu hình và xác nhận OTP email/Zalo live.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Spring Boot, Flyway, JPA và kiểm soát OTP HMAC có TTL 10 phút, cooldown gửi 60 giây, giới hạn 5 lần; adapter email HTTPS Google Apps Script và Zalo Business Solutions; React/TypeScript, React Query, Playwright, Vitest, JUnit/Spring Integration Test, H2 cô lập.
- **Mục đích chức năng / việc đã làm**: Đặt doanh nghiệp mới ở vị trí dễ duyệt; để nhà tuyển dụng hủy ứng viên có xác nhận; giúp ứng viên kiểm soát mật khẩu và dùng lại CV; yêu cầu thông tin liên hệ được xác minh trước khi ứng tuyển để nhà tuyển dụng có cơ sở liên hệ. Provider live và dữ liệu demo dùng chung là các cổng vận hành ngoài mã nguồn, được theo dõi riêng trên Jira.

---

## BẮT BUỘC SỐ ĐIỆN THOẠI KHI ĐĂNG KÝ & SỬA DUYỆT DOANH NGHIỆP — 23/10/2026
- **Đã làm được gì**: Chuyển số điện thoại khỏi nhóm thông tin tùy chọn thành trường bắt buộc trên form đăng ký và API; đồng bộ định dạng số Việt Nam `0...` / `+84...`, cập nhật hợp đồng API và các fixture đăng ký. Sửa luồng duyệt công ty để không từ chối chỉ vì người tạo đã liên kết với công ty khác; khi duyệt vẫn giữ nguyên liên kết hiện tại. Kiểm thử cục bộ: Maven `verify` 381/381; Vitest 24 file / 138 test; ESLint và Vite production build đều pass. CI/CD Render được theo dõi sau khi merge PR.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Spring Boot Bean Validation (`@NotBlank`, `@Pattern`) và React/TypeScript validation dùng chung; JUnit/Vitest cho kiểm thử validation, integration API và use case; GitHub Actions chạy CI và CD Render theo commit trên `main`.
- **Mục đích chức năng / việc đã làm**: Đảm bảo tài khoản mới luôn có số điện thoại hợp lệ ngay khi đăng ký; cho phép admin phê duyệt pháp nhân do người đã thuộc một công ty khác tạo mà không tự ý chuyển membership của tài khoản đó.

---

## MỞ RỘNG GIAO DIỆN VÀ HOÀN THIỆN HỒ SƠ ỨNG VIÊN — 23/10/2026
- **Đã làm được gì**: Mở rộng khung giao diện các trang và modal CV; thay các hộp chọn native bằng hộp chọn dùng chung có tìm kiếm, bàn phím và menu portal; đổi trường ngày sang hiển thị/nhập dd/mm/yyyy trong khi API vẫn nhận ISO. Hoàn thiện luồng nhập CV với thông báo nguồn phân tích AI và fallback rõ ràng; bổ sung/diễn đạt lại lịch sử kinh nghiệm để hệ thống tự tính năm từ các khoảng làm việc, gộp khoảng chồng lấn và không lấy số năm cũ khi thiếu lịch sử. Thêm cập nhật avatar bằng URL hoặc kéo thả/chọn PNG/JPEG (tối đa 5 MB). Kiểm thử: Maven verify 369/369; Vitest 22 file / 134 test; ESLint, Vite production build; API E2E H2 28/28; Playwright 23/23, gồm đăng nhập ba vai trò và kiểm tra tràn ngang ở 360/390/555/1280/1440 px. Log và ảnh E2E: docs/qa/full-site-uat-2026-10-20/runs/2026-10-23-000847/.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, Tailwind, React Portal/ARIA, Playwright và Vitest; CustomSelect dùng chung, định dạng ngày UI dd/mm/yyyy chuyển đổi ISO ở biên API; Java/Spring Boot multipart upload với kiểm tra MIME, giải mã ảnh, kích thước, UUID và quyền ứng viên; Gemini CV parser phân biệt nguồn AI, fallback và lỗi; tính kinh nghiệm bằng gộp khoảng ngày làm việc và tổng số tháng lịch.
- **Mục đích chức năng / việc đã làm**: Giúp các trang, chữ và biểu mẫu dùng được trên màn hình lớn/nhỏ, giảm vỡ bố cục và lỗi chọn/ngày; cho ứng viên cập nhật hồ sơ và avatar; tránh hiển thị sai tổng năm kinh nghiệm khi chưa có lịch sử. Gemini thật cần GEMINI_API_KEY; xác minh đã dùng parser tests/fake endpoint, chưa có xác nhận gọi dịch vụ Gemini thật. File avatar hiện dùng local storage adapter; trên Render cần persistent disk hoặc storage bền vững để giữ ảnh sau khi instance khởi động lại. PR/CI chưa được xác nhận ở thời điểm ghi mục này.

## 📌 BÁO CÁO ĐỢT 2: NGHIÊN CỨU KHOA HỌC, CI/CD, CLOUD RENDER & HỆ THỐNG AI (RAG + MCP)
- **Thời gian hoàn thành**: 06/10/2026
- **Người thực hiện**: Lê Duy Khánh (Tech Lead) & AI Pair Programmer
- **Học phần**: Java Spring 2 – ITC

---

### 1. Bóc Tách Bài Báo Khoa Học & Chuẩn Hóa Ngữ Cảnh "Working Capsule"
- **Đã làm được gì**:
  - **Hiệu chỉnh theo rà soát nguồn ngày 09/10/2026:** Báo cáo cũ ghi đã đọc toàn văn, nhưng lần xác minh hiện tại chỉ truy cập được metadata/abstract từ nhà xuất bản; không coi những chi tiết kỹ thuật bên dưới là đã được xác nhận từ toàn văn.
  - Khảo sát mã nguồn GitHub tác giả (`whl1207/Knowledge`), học tập cấu trúc `agentSkills.ts` (chuẩn `SKILL.md`) và `learningCore.ts` (thuật toán SRS phân loại tri thức).
  - Soạn thảo tài liệu phân tích chi tiết tại `docs/research/AI_KM_RAG_WORKFLOW_ANALYSIS.md`.
  - Thiết lập quy chuẩn nén ngữ cảnh 8 mục `WORKING CAPSULE` khi gãy context tại `docs/WORKING_CAPSULE_GUIDE.md`, `.agents/rules/working-capsule.md`, `AGENTS.md` và `.agents/skills/knowledge-capsule/SKILL.md`.
- **Công nghệ / Thuật toán / Kết hợp**:
  - Các mục Inferred Hidden Information, Tri-Vector và MDS trong bản báo cáo cũ chưa được dẫn nguồn toàn văn; xem chúng là ý tưởng/đề xuất riêng, không phải kết quả đã xác minh từ bài báo.
  - Quy trình Working Capsule là hướng dẫn nội bộ của dự án, không phải kết luận của bài báo AI-KM.
- **Mục đích chức năng / Việc đã làm**:
  - Phân tích RAG và tổ chức hướng dẫn làm việc; không có bằng chứng trong report này rằng hallucination bị loại bỏ triệt để hoặc ngữ cảnh được bảo toàn tuyệt đối.

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

### 4. AI Matching và Recruiter JSON-RPC Tools (đính chính cách gọi)
- **Đã làm được gì**:
  - Soạn thảo báo cáo đối chiếu học thuật và thực tiễn 10 chiều giữa RAG và MCP tại `docs/research/AI_MCP_VS_RAG_COMPREHENSIVE_ANALYSIS.md`.
  - Cài đặt dịch vụ `TriVectorRagMatchingService.java` so khớp độ phù hợp giữa CV ứng viên và JD tuyển dụng.
  - Cài đặt `TalentBridgeMcpController.java`, một endpoint JSON-RPC 2.0 tùy biến với 4 công cụ ATS; chưa có interoperability test xác nhận MCP đầy đủ.
  - Tạo REST Controller `AiMatchingController.java` (`POST /api/v1/ai/match`).
  - Có test cho matching và các method JSON-RPC; kết quả pass của test không phải phần trăm độ chính xác tuyển dụng hoặc test coverage.
- **Công nghệ / Thuật toán / Kết hợp**:
  - Matching fallback hiện tại kết hợp độ phủ token và cosine trên tập token, rồi trộn điểm hồ sơ thô với tập kỹ năng heuristic; không phải embedding RAG và không được gán cho bài AI-KM.
  - Endpoint Recruiter dùng JSON-RPC tùy biến; MCP chuẩn đòi hỏi kiểm thử tương thích riêng.
  - Tích hợp Gemini AI (`gemini-flash-lite-latest`) với fallback deterministic chạy offline.
- **Mục đích chức năng / Việc đã làm**:
  - Hỗ trợ tham khảo việc đối chiếu CV với JD; chưa có nhãn dữ liệu/benchmark để khẳng định độ chính xác hoặc công bằng.
  - Recruiter được xác thực có thể gọi các tool đã định nghĩa; chưa xác nhận mọi MCP client bên ngoài kết nối được.

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
  - Báo cáo cũ ghi đã xác nhận kết nối TiDB Cloud qua JDBC TLS. Username đã được loại khỏi tài liệu tiến độ vì không cần thiết để mô tả kết quả.
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

---

## BÁO CÁO KHẮC PHỤC AI VÀ TỐI ƯU ĐỘ TRỄ (CHƯA DEPLOY)
- **Thời gian**: 09/10/2026
- **Đã làm được gì**:
  - Chuẩn hóa adapter gọi Gemini dùng header `x-goog-api-key`, nhận phản hồi JSON kể cả khi upstream trả `application/octet-stream`, đặt connect/read timeout và không ghi nội dung CV hay API key vào log.
  - Chuyển model mặc định sang ID ổn định `gemini-3.5-flash-lite`, được liệt kê trong tài liệu Gemini GenerateContent; cho phép `GEMINI_MODEL` override.
  - CV parser trả nguồn xử lý `GEMINI` hoặc `RULE_BASED`; giao diện hiển thị rõ khi AI được dùng hay khi chuyển sang xử lý dự phòng.
  - Chuyển phân tích matching ứng viên sang inbound use case/port trong core; giới hạn endpoint cho Recruiter và kiểm tra quyền sở hữu tin cùng đơn ứng tuyển. Endpoint tool yêu cầu role Recruiter; JSON-RPC tùy biến này chưa được xác nhận là MCP đầy đủ.
  - Giảm truy vấn dư thừa khi tải danh sách việc làm/đơn ứng tuyển; đổi tải tập quyền `roles` sang lazy + batch để tránh Hibernate phân trang trong bộ nhớ; cấu hình Hikari cho TiDB, chia nhỏ bundle frontend bằng lazy route và cache tìm việc công khai trong 60 giây.
  - Cập nhật README và hướng dẫn Render. Backend có 267 tests pass (0 failures/errors), gồm ArchUnit; frontend có 84 tests pass, lint và production build đều pass.
  - Chưa triển khai hoặc kiểm tra production sau thay đổi. Cần cấu hình `GEMINI_API_KEY` ở Render; gói Render Free vẫn có thể cold start.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**:
  - Spring `RestClient`, Gemini `generateContent`, UTF-8 response handling, timeout có cấu hình và fallback parser xác định.
  - Hexagonal Architecture với inbound use case và outbound port; kiểm tra quyền bằng Spring Security; JPA lazy loading/batch fetching cho association quyền.
  - Matching hiện có là Gemini tùy chọn với heuristic lexical fallback; không phải vector-embedding RAG.
- **Mục đích chức năng / việc đã làm**:
  - Làm cho kết quả AI/fallback có thể nhận biết trong CV import và matching nhà tuyển dụng, giảm rủi ro lỗi do xác thực hoặc quyền truy cập sai.
  - Giảm tải ban đầu và truy vấn không cần thiết ở các trang/list thường dùng. Chưa đo latency production nên chưa khẳng định phần trăm cải thiện; thời gian cold start của gói Free phụ thuộc Render.

---

## Chuẩn bị bootstrap Admin production an toàn - 09/10/2026
- **Đã làm được gì**: Thêm inbound use case tạo Admin từ thông tin operator cấu hình, chỉ chạy trong profile `prod` khi `ADMIN_BOOTSTRAP_ENABLED=true`. Email được chuẩn hóa; mật khẩu BCrypt được mã hóa, tối thiểu 16 ký tự và tối đa 72 byte UTF-8. Tài khoản Admin hiện hữu không bị đổi mật khẩu; email của tài khoản thường không được tự nâng quyền. Bổ sung unit tests và hướng dẫn vận hành. PR #14 đã merge vào `dev`; CI backend/frontend đều PASS. PR phát hành sang `main` đang được kiểm tra; chưa redeploy production.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Hexagonal Architecture (inbound port/use case/outbound `UserRepositoryPort` và `PasswordEncoderPort`), Spring Boot `ApplicationRunner` theo profile, BCrypt và biến môi trường Render.
- **Mục đích chức năng / việc đã làm**: Tạo quy trình cấp Admin production có kiểm soát, không mở đăng ký Admin công khai, không đưa password mặc định vào production và không ghi password vào log. Sau xác nhận bootstrap thành công, operator phải tắt cờ và xóa biến email/password.

---

## KIỂM TOÁN KIẾN TRÚC VÀ BỔ SUNG CHATBOT / THỐNG KÊ - 09/10/2026
- **Đã làm được gì**:
  - Bổ sung 11 quy tắc ArchUnit cho ranh giới Hexagonal Architecture. Chạy toàn bộ Maven suite: 289 tests, 0 failures/errors; chạy riêng `TalentBridgeMcpControllerTest`: 5 tests, 0 failures/errors. Frontend: 84 tests pass, lint và production build pass.
  - Thêm chatbot hỗ trợ theo phạm vi TalentBridge, có trang riêng và nút mở nhanh hình tròn. Dữ liệu trả lời lấy từ bộ trợ giúp nội bộ; Gemini là tùy chọn, lỗi/mất key thì có fallback tri thức; câu hỏi ngoài phạm vi bị từ chối. Chat không tra cứu dữ liệu tài khoản riêng hoặc thực hiện thay đổi nghiệp vụ.
  - Thêm API đánh giá chất lượng tin, độ đầy đủ/minh bạch nội dung và tín hiệu xác minh nội bộ của công ty; thêm API đánh giá ứng viên theo job bằng matching hiện có và độ đầy đủ hồ sơ; thêm API snapshot phân bố ứng viên theo ATS stage cho Recruiter.
  - Ràng buộc quyền Recruiter, quyền sở hữu tin và quan hệ ứng tuyển trong các đường thống kê. Điểm công ty là tín hiệu nội bộ, không phải xác minh uy tín độc lập; kết quả phù hợp ứng viên là gợi ý, không phải quyết định tuyển dụng.
  - Sửa matcher để dùng outbound ports thay vì truy cập trực tiếp JPA repository; không chấm điểm nền giả khi thiếu bằng chứng hoặc không có phần giao kỹ năng. Mở rộng kiểm tra kiến trúc và trường hợp biên của điểm chất lượng tin.
  - Rà lại tài liệu AI: phân biệt phần paper AI-KM được abstract xác nhận với ý tưởng triển khai trong dự án; không gán Tri-Vector, MCP hoặc số liệu độ chính xác chưa xác minh cho paper. Endpoint JSON-RPC hiện tại được ghi đúng là triển khai tùy biến, chưa xác nhận đầy đủ MCP.
  - Chưa commit, push, mở PR, deploy hoặc thay đổi dữ liệu production trong nhiệm vụ này.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**:
  - Spring Boot REST, inbound use cases/outbound ports, adapter AI Gemini tùy chọn, truy xuất tri thức nội bộ theo từ khóa/ngữ nghĩa từ vựng, heuristic matching sẵn có, điểm chất lượng theo thành phần, JPA aggregation cho ATS stage, ArchUnit, JUnit, H2, React/TypeScript.
- **Mục đích chức năng / việc đã làm**:
  - Giúp người dùng hỏi đáp trực tiếp về cách sử dụng TalentBridge; giúp Recruiter rà soát nội dung tin, mức độ phù hợp CV và phân bố pipeline trong ATS, có giải thích phạm vi và giới hạn để tránh biến điểm số thành kết luận khách quan.

### Nợ clean code còn lại sau kiểm toán
- 11 quy tắc kiến trúc đang pass nhưng điều đó không chứng minh mọi mã nguồn đã đạt Clean Code. Vẫn còn các `catch (Exception)` ở adapter/parser/email/security và một số repository adapter; một số controller lớn vẫn gom nhiều luồng nghiệp vụ; tài khoản mẫu và mật khẩu mẫu còn tồn tại trong seed dành cho profile không production. Các vấn đề này chưa được sửa trong phạm vi chatbot/statistics và cần refactor riêng có kiểm thử hồi quy.

---

## SỬA LỖI MÀN TRẮNG TRỢ LÝ TALENTBRIDGE
- **Thời gian**: 09/10/2026
- **Đã làm được gì**:
  - Tái hiện lỗi React `TypeError: l is not a function` khi đóng chatbot. `useEffect` dùng biểu thức ngắn trả kết quả của `scrollIntoView()`; React xem giá trị đó là hàm cleanup. Đổi effect sang block body, kiểm tra method trước khi gọi và bảo đảm không trả giá trị cleanup ngoài ý muốn.
  - Thêm Playwright regression test cho trang `/assistant`, chuyển route và mở/đóng widget khi `scrollIntoView()` giả lập trả về giá trị không phải `void`; không phát sinh lỗi trang và root vẫn render.
  - Xác minh: frontend 84 unit tests pass; API E2E trên H2 tạm 28 tests pass; Playwright UI E2E 2 tests pass (bao gồm luồng 3 vai trò/viewport); lint và production build pass.
  - Ảnh QA được lưu tại `artifacts/roleplay-qa/assistant-page.png` và `artifacts/roleplay-qa/assistant-widget-open.png`. Chưa merge hoặc redeploy production.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**:
  - React `useEffect`, TypeScript, Vitest, Playwright, Vite và backend H2 cô lập qua `scripts/run-e2e.ps1`.
- **Mục đích chức năng / việc đã làm**:
  - Giữ nguyên trang TalentBridge và widget chat khi trình duyệt/extension làm `scrollIntoView()` trả kết quả khác chuẩn; tránh một lỗi cleanup làm trắng toàn bộ giao diện.

---

## CÔ LẬP LỖI TRỢ LÝ VÀ GIẢM THỜI GIAN CHỜ - 10/10/2026
- **Đã làm được gì**:
  - Thêm React Error Boundary toàn ứng dụng và ranh giới riêng cho trang/widget trợ lý; lỗi render hoặc lazy import hiển thị thông báo phục hồi thay cho trang trắng.
  - Mở đầu một yêu cầu health check nhẹ `GET /api/v1/health` khi ứng dụng tải để đánh thức backend trong lúc người dùng đọc trang; yêu cầu được chia sẻ, không gọi endpoint nghiệp vụ/DB.
  - Ba câu hỏi mẫu trả lời trực tiếp từ knowledge base nội bộ, không gọi Gemini. Đặt Gemini read timeout mặc định 8 giây; lỗi upstream dùng câu trả lời dự phòng hiện có. Phía trình duyệt giới hạn request trợ lý 15 giây và thông báo timeout/kết nối rõ ràng.
  - Playwright kiểm tra câu hỏi mẫu và tự nhập prompt ở trang trợ lý lẫn widget, phản hồi lỗi 503 và health warm-up. Chạy lại sau một lần lỗi tải động không tái hiện: 28 API E2E và 2 UI E2E pass; ảnh QA ở `artifacts/roleplay-qa/`. Frontend 84 tests pass, lint/build pass; Maven 290 tests pass, 0 failures/errors.
  - Build hiện tại có bundle entry 341.08 KB / 101.48 KB gzip và lazy-load các trang; chưa có benchmark production sau deploy nên không ghi nhận mức tăng tốc tổng thể bằng con số. Gói Render Free vẫn có thể cold start.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**:
  - React Error Boundary, React Query, Fetch AbortController, Vite lazy imports, Playwright, Vitest, Spring Boot health endpoint, `RestClient` timeout, Gemini adapter và knowledge-base fallback; giữ gọi AI trong outbound adapter, không đưa Spring vào core.
- **Mục đích chức năng / việc đã làm**:
  - Ngăn lỗi hiển thị của chatbot thay toàn bộ giao diện bằng trang trắng, rút ngắn đường trả lời cho FAQ và khởi động backend sớm hơn. Cải thiện độ trễ là tối ưu có mục tiêu; cần deploy và đo cold/warm latency trước khi kết luận tốc độ production đã tăng.

---

## CHUẨN BỊ CHUYỂN DATABASE SANG AMAZON RDS - 12/10/2026
- **Đã làm được gì**:
  - Tạo nhánh `fix/khanh/rds-mysql-cutover` và runbook cho quy trình backup, import TiDB, đối soát, cutover, rollback và giữ DB cũ trong thời gian soak.
  - Cấu hình production không tự baseline schema và Hibernate chỉ validate cấu trúc; hỗ trợ tài khoản Flyway tách biệt, dự phòng dùng datasource account để tương thích cũ.
  - Đóng gói CA bundle RDS Singapore thành truststore riêng trong Docker; runbook cấu hình MySQL Connector/J `sslMode=VERIFY_IDENTITY` và CA store cho kết nối.
  - Tra AWS Price List Singapore: cấu hình tham khảo `db.t4g.micro` Single-AZ + 20 GiB gp3 khoảng 21.01 USD/tháng cơ sở; chưa gồm backup, truyền dữ liệu, CPU credits và thuế. Cần kiểm tra kích thước DB nguồn và estimate lại trước khi tạo.
  - Bổ sung kiểm thử khởi động profile production với H2, chạy hai migration và kiểm tra Hibernate schema validation. `mvnw -B verify` pass: 291 tests, 0 failures/errors.
  - Chưa tạo tài nguyên RDS, chưa dump/import/đối soát DB, chưa đổi datasource production hoặc xóa TiDB. Chưa có AWS/Render API credentials hoặc RDS endpoint/estimate trong môi trường làm việc.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Amazon RDS for MySQL; Spring Boot profile `prod`; Flyway `validate-on-migrate`/`baseline-on-migrate=false`; Hibernate `ddl-auto=validate`; MySQL Connector/J TLS `VERIFY_IDENTITY`; AWS CA bundle/PKCS12; Docker multi-stage build; H2 integration test.
- **Mục đích chức năng / việc đã làm**: Chuẩn bị chuyển DB mà không để Hibernate tự sửa schema production; giữ nguyên lịch sử migration và dữ liệu, dùng xác thực chứng thư/hostname qua TLS, đồng thời giữ TiDB làm nguồn rollback đến khi dữ liệu RDS được kiểm chứng và chạy ổn định.

---

## PREFLIGHT READ-ONLY CHO RDS - 12/10/2026
- **Đã làm được gì**:
  - Qua TiDB Cloud Overview, xác nhận cluster `talentbridge` đang Active, plan STARTER, vùng Tokyo (`ap-northeast-1`), MySQL compatible; dung lượng hiển thị 0 MiB row-based và 0 MiB columnar.
  - Chạy truy vấn chỉ đọc trên `information_schema.tables`: schema `test` có 29 bảng; `TABLE_ROWS` tổng ước tính 53; tổng metadata `data_length + index_length` là 2,304 byte. Không đọc nội dung hồ sơ/tài khoản.
  - Kiểm tra danh sách RDS trên AWS Console ở Singapore và Sydney; cả hai vùng đều có 0 database.
  - Khôi phục nguyên trạng câu SQL người dùng đang soạn trong TiDB Editor sau khi kết thúc truy vấn metadata.
  - Chưa tạo tài nguyên, chưa đổi cấu hình production, chưa xuất/nhập dữ liệu và chưa xóa TiDB.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: TiDB Cloud Overview; truy vấn metadata `information_schema.tables`; AWS RDS Console inventory; không thay đổi dữ liệu ứng dụng.
- **Mục đích chức năng / việc đã làm**: Ước lượng sơ bộ nguồn trước khi chọn dung lượng RDS, kiểm tra tài nguyên đích đã tồn tại hay chưa, và phát hiện điểm cần xác nhận: schema nguồn tên `test` với số dòng ước tính rất thấp. Cần xác minh đúng đây là DB production và đối soát chính xác từng bảng trước cutover.

---

## CHUẨN BỊ BIỂU MẪU RDS AWS FREE - 12/10/2026
- **Đã làm được gì**:
  - Trong AWS Console vùng Singapore, chuẩn bị nhưng chưa gửi biểu mẫu tạo RDS MySQL Community 8.4.9: template Free tier, `db.t4g.micro`, Single-AZ, 20 GiB gp2, database `test`, bật mã hóa, backup tự động 7 ngày và deletion protection.
  - Để Public access ở `No`; do đó Render chưa thể truy cập DB cho đến khi hoàn tất thiết kế kết nối mạng và firewall. Không mở cổng 3306 ra `0.0.0.0/0`.
  - Chưa nhập master password và chưa bấm `Create database`; tài nguyên chưa được tạo. AWS Console hiển thị `0.029 USD/hour` cho lớp Free tier đã chọn, nên chưa thể khẳng định cấu hình không phát sinh phí hoặc credits của tài khoản sẽ bù hết.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: AWS RDS Console; RDS for MySQL; `db.t4g.micro`; EBS gp2; Single-AZ; AWS KMS encryption; automated backup; private VPC access.
- **Mục đích chức năng / việc đã làm**: Chuẩn bị cấu hình DB đích tương thích ứng dụng và schema quan sát được, giữ mạng riêng và bảo vệ dữ liệu; chờ chủ tài khoản tự nhập master credential, xem lại billing/estimate và thực hiện bước tạo tài nguyên có thể tính phí.

---

## CẬP NHẬT TRẠNG THÁI KẾT NỐI RDS - 13/10/2026
- **Đã làm được gì**:
  - Xác nhận RDS `talentbridge-mysql` đã được tạo ở Singapore, nhưng chủ dự án xác nhận DB đích hiện còn trống, chưa import dữ liệu.
  - Chạy lại `mvnw -B verify` trên JDK 21 tại nhánh `fix/khanh/rds-mysql-cutover`: BUILD SUCCESS; 291 tests, 0 failures, 0 errors. ArchUnit và production-profile integration test đều nằm trong lượt chạy này.
  - PR #23 đã merge vào `dev` ngày 12/10/2026; PR #24 đã merge vào `main` bằng commit `e7a83b01f6659919127c82c639946ac94416bb88`. Các check Backend Build & Verify, Dockerfile Build Verification và Frontend Test/Lint/Build đều pass trên PR #24.
  - Chạy `mvnw.cmd test` với JDK 21: 291 tests, 0 failures/errors; `mvnw.cmd clean package -DskipTests=true` cũng BUILD SUCCESS. Lượt test đầu dùng Java 8 và thất bại do runtime không hỗ trợ class version 65; chạy lại đúng JDK 21 thì pass.
  - Smoke test production: `GET /api/v1/health` trả HTTP 200, `status: UP`; `GET /api/v1/jobs` trả HTTP 200 nhưng `totalElements: 0`. Hai endpoint này chưa chứng minh datasource đang là RDS; request health ban đầu timeout khi service chưa phản hồi rồi thành công sau đó.
  - Sau đó kiểm tra kết nối TCP 3306 từ workstation tới RDS thành công qua rule `/32` tạm thời; rule này cần được gỡ sau khi xác minh Render.
  - Đã tạo schema `talentbridge_db` cùng hai account `talentbridge_app` và `talentbridge_migrator`, đều bắt buộc TLS. Kiểm tra JDK 21/MySQL Connector/J xác nhận cả hai account đăng nhập TLS, app không có quyền DDL, migrator có quyền DDL cần thiết. Backend sau đó đã chạy Flyway V1/V2 thành công trên schema này.
  - Password master chỉ được nhập tại cửa sổ PowerShell cục bộ với input ẩn. Credential cho Render được lưu trong thư mục Windows chỉ user hiện tại đọc được; không ghi secret vào Git, chat hoặc log.
  - Chưa đổi biến môi trường Render, chưa deploy datasource lên Render hoặc đối soát dữ liệu production. Không xóa TiDB. Việc chạy Flyway bên dưới là từ một smoke test production-profile cục bộ, không phải deploy Render.
  - Người dùng xác nhận RDS còn trống và cho phép tạo lại dữ liệu người dùng/tin tuyển dụng; không cần import bản sao dữ liệu TiDB để bắt đầu. TiDB vẫn được giữ làm đường lui đến khi RDS được xác minh.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Maven Wrapper với JDK 21; JUnit/Spring Boot integration tests chạy trên H2; ArchUnit; cấu hình production dùng Flyway và Hibernate schema validation; TLS MySQL/RDS `VERIFY_IDENTITY` đã chuẩn bị trong PR.
- **Mục đích chức năng / việc đã làm**: Xác minh PR RDS đã lên `main`, CI/local tests xanh và backend production phản hồi; phân biệt smoke test API với bằng chứng datasource. RDS đã có schema đích và account riêng được kiểm thử TLS; việc chuyển Render datasource còn chờ PR và cấu hình Render.

---

## ĐỒNG BỘ BIẾN JWT GIỮA RENDER VÀ SPRING BOOT
- **Thời gian**: 13/10/2026
- **Đã làm được gì**:
  - Phát hiện `render.yaml` tạo `TALENTBRIDGE_JWT_SECRET` nhưng `application.yml` chỉ đọc `JWT_SECRET`, khiến cấu hình deploy thiếu alias tương thích.
  - Cập nhật Spring để ưu tiên `TALENTBRIDGE_JWT_SECRET`, vẫn chấp nhận `JWT_SECRET`; thông báo fail-fast nêu rõ cả hai tên biến.
  - Sửa kiểm thử production profile để khởi động với riêng `TALENTBRIDGE_JWT_SECRET`; chạy test mục tiêu 4/4 pass và toàn bộ backend `mvnw.cmd test` 291/291 pass.
  - PR #25 đã được mở từ nhánh `fix/khanh/rds-render-env-compat` vào `dev`; backend verify, Dockerfile build và frontend test/lint/build đều PASS. PR chưa được leader duyệt/merge; chưa deploy và không đọc/đổi secret Render.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Spring Boot externalized configuration, YAML nested placeholder, JUnit/Spring Boot integration test, Maven Wrapper/JDK 21.
- **Mục đích chức năng / việc đã làm**: Đảm bảo backend có thể đọc đúng tên secret được Render Blueprint tạo trước khi redeploy và cutover RDS.

---

## KHỞI TẠO SCHEMA VÀ ACCOUNT RDS - 13/10/2026
- **Đã làm được gì**:
  - Xác minh kết nối TCP từ workstation đến RDS Singapore trên cổng 3306; security rule workstation là `/32` tạm thời, còn Render allowlist giữ nguyên.
  - Tạo schema `talentbridge_db` trên RDS MySQL với charset/collation `utf8mb4`; sau đó smoke test production-profile đã áp dụng Flyway V1/V2 và Hibernate xác thực mapping thành công.
  - Tạo hai account riêng có `REQUIRE SSL`: `talentbridge_app` chỉ có quyền DML, `talentbridge_migrator` có quyền DML và các quyền DDL Flyway cần.
  - Kiểm thử trực tiếp cả hai account bằng MySQL Connector/J 9.3 và CA bundle AWS Singapore: `sslMode=VERIFY_IDENTITY`, `SELECT 1` thành công; quyền ứng dụng không có DDL; số bảng ban đầu bằng 0 trước Flyway.
  - Sinh mật khẩu cục bộ và lưu trong tệp nằm dưới ACL chỉ user Windows hiện tại đọc được. Chưa thay đổi datasource Render, chưa redeploy và chưa xóa TiDB.
  - PR #25 (`fix(render): accept configured TalentBridge JWT secret`) đã có đủ 3 CI checks PASS nhưng chưa merge theo quy trình leader review. GitHub secret `RENDER_DEPLOY_HOOK_URL` chưa được cấu hình nên CD hiện không tự gọi Render.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Amazon RDS MySQL; AWS Singapore CA bundle; MySQL Connector/J 9.3; Java 21; TLS `VERIFY_IDENTITY`; MySQL least-privilege grants; Windows ACL; GitHub Actions CI/CD.
- **Mục đích chức năng / việc đã làm**: Chuẩn bị database an toàn, tách quyền runtime khỏi quyền migration, xác minh TLS và chạy Flyway/Hibernate validation từ production profile cục bộ trước khi cấu hình datasource Render.

---

## KIỂM CHỨNG BACKEND PRODUCTION PROFILE KẾT NỐI RDS - 13/10/2026
- **Đã làm được gì**:
  - Khởi động JAR bằng JDK 21 với profile `prod`, đọc credential từ tệp cục bộ giới hạn ACL; không đưa secret vào chat, Git hoặc log. Tắt bootstrap admin trong smoke test và dùng JWT secret tạm thời.
  - Kết nối RDS Singapore bằng TLS `VERIFY_IDENTITY`; Flyway xác thực 2 migration và áp dụng V1/V2 thành công, schema đạt version v2.
  - Hibernate khởi tạo `EntityManagerFactory`, `ddl-auto=validate` không báo lỗi schema; ứng dụng báo `Started TalentBridgeApplication`, process exit code 0 và Hikari đóng kết nối sạch.
  - Đây là kiểm chứng backend cục bộ truy cập RDS thật; chưa chứng minh Render đang dùng RDS. Không tạo tài khoản/job dữ liệu mẫu, không đổi Render env, không deploy và không xóa TiDB.
  - Flyway cảnh báo MySQL 8.4 mới hơn bản đã được kiểm thử hỗ trợ (MySQL 8.1); migration vẫn hoàn tất thành công. Cần cân nhắc nâng Flyway và kiểm thử lại trước khi coi cảnh báo tương thích đã được xử lý.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Spring Boot production profile; JDK 21; MySQL Connector/J; TLS `VERIFY_IDENTITY` với CA bundle AWS Singapore; Flyway V1/V2; Hibernate schema validation; HikariCP.
- **Mục đích chức năng / việc đã làm**: Chứng minh backend build hiện tại có thể kết nối RDS thật, chạy migration và khởi động JPA mà không phụ thuộc H2. Tạo cơ sở an toàn cho bước cấu hình Render, rồi mới tái tạo dữ liệu ứng dụng.

## HOÀN THIỆN CI/CD DEPLOY CẢ FRONTEND VÀ BACKEND
- **Thời gian**: 14/10/2026
- **Đã làm được gì**:
  - Cập nhật workflow CD để deploy hai Render service song song đúng SHA của lần chạy, chờ mỗi service báo đúng commit rồi mới chạy smoke test health, jobs, title và credentialed CORS. Thiếu deploy hook sẽ fail sớm thay vì báo xanh giả.
  - Bổ sung build marker vào health API và frontend HTML, cập nhật hướng dẫn deploy, sửa cleanup FK trong integration test.
  - Kiểm thử cục bộ: Maven verify 291/291; `HexagonalArchitectureTest` 11/11; frontend lint đạt, Vitest 84/84, production build đạt; `git diff --check` đạt.
  - Render production hiện trả health/jobs HTTP 200 nhưng jobs có 0 tin; response đang chạy bản cũ chưa có build marker mới. GitHub chỉ có secret deploy hook backend; `RENDER_FRONTEND_DEPLOY_HOOK_URL` chưa được tạo. Chưa mở PR, merge hoặc deploy phiên bản này.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**:
  - GitHub Actions, Render Deploy Hooks, exact-commit marker qua `RENDER_GIT_COMMIT`, Spring Boot health API, Vite build-time env, PowerShell HTTP smoke test, Maven, Vitest và ESLint.
- **Mục đích chức năng / việc đã làm**:
  - Bảo đảm CI/CD chỉ báo thành công khi cả hai service thực sự phục vụ đúng commit và smoke test production qua; nêu rõ secret frontend còn thiếu trước khi có thể redeploy.
---

## KHẮC PHỤC CHATBOT TRẮNG MÀN VÀ SIẾT KẾT NỐI RDS - 14/10/2026
- **Đã làm được gì**:
  - Tái hiện lỗi trắng màn bằng Playwright: `scrollIntoView()` ném `NotSupportedError` sau khi bấm câu hỏi mẫu, làm React ErrorBoundary thay nội dung chatbot.
  - Bọc thao tác cuộn trong fallback an toàn; khi API cuộn lỗi, đặt vị trí cuộn trực tiếp trong khung hội thoại để câu trả lời và giao diện tiếp tục hiển thị.
  - Chạy lại `scripts/run-e2e.ps1`: API E2E 28/28 và UI E2E 3/3 pass; frontend lint và production build pass.
  - Sau khi chủ tài khoản xác nhận, xóa rule MySQL 3306 tạm từ IP workstation `14.187.179.143/32`. Xác minh security group `talentbridge-render-db` chỉ còn hai CIDR Render `74.220.52.0/24` và `74.220.60.0/24`.
  - Kiểm tra live: RDS `talentbridge-mysql` trạng thái `Available`, lớp `db.t4g.micro`; `/api/v1/health` HTTP 200 (354 ms), `/api/v1/jobs` HTTP 200 (783 ms), nhưng hiện không có tin tuyển dụng (`totalElements: 0`).
  - Chưa redeploy code chatbot; thay đổi đang ở nhánh tính năng riêng và cần CI, PR review/merge rồi mới kiểm tra bản live.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React `useEffect`/DOM refs; `scrollIntoView()` với fallback `scrollTop`; Playwright; Vitest API E2E chạy backend H2 biệt lập; ESLint; Vite production build; AWS EC2 Security Groups; Amazon RDS MySQL; HTTP smoke test.
- **Mục đích chức năng / việc đã làm**: Giữ chatbot và bong bóng trợ lý hoạt động khi trình duyệt/WebView không hỗ trợ cuộn phần tử; hạn chế truy cập DB production đúng các dải IP backend Render; kiểm chứng API công khai và tình trạng DB mà không đưa secret vào chat.
---

## KIỂM TRA TÀI NGUYÊN TIDB VÀ RULE RDS - 14/10/2026
- **Đã làm được gì**:
  - Đăng nhập TiDB Cloud dưới tổ chức `Khanh's Org`, chuyển sang Resource View và tải lại trang: danh sách tài nguyên trống, giao diện hiển thị “Get started by creating a resource”. Project View xác nhận `JS2` có 0 instance và ngoài project cũng có 0 instance.
  - Không thực hiện thao tác xóa vì tài nguyên TiDB `talentbridge` đã không còn trong danh sách; bằng chứng hiện tại không cho biết ai hoặc khi nào đã xóa.
  - Xác minh lại Security Group RDS `talentbridge-render-db` tại Singapore: chỉ còn hai rule MySQL/TCP 3306 cho CIDR Render `74.220.60.0/24` và `74.220.52.0/24`; không còn IP workstation `/32` tạm.
  - Việc này chỉ xác nhận inventory TiDB và firewall RDS; chưa chứng minh Render đang kết nối RDS hoặc dữ liệu nghiệp vụ đã được nạp/đối soát.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: TiDB Cloud Resource View/Project View; AWS EC2 Security Groups; Computer Use để đọc giao diện console đã đăng nhập.
- **Mục đích chức năng / việc đã làm**: Xác định trạng thái yêu cầu ngừng TiDB và xác nhận RDS chỉ nhận kết nối từ các CIDR backend Render đã cấu hình.

---

## ĐÁNH GIÁ CV ỨNG VIÊN RIÊNG TƯ VÀ ĐỐI SÁNH CỤC BỘ - 15/10/2026
- **Đã làm được gì**:
  - Nhà tuyển dụng chỉ được đánh giá ứng viên đã nộp đơn vào tin thuộc công ty của mình; quyền được xác minh trước khi mở/tải CV.
  - Hệ thống chọn CV mặc định/mới nhất theo thứ tự repository, trích xuất PDF/DOCX qua outbound port và chỉ gửi văn bản vào bộ đối sánh cục bộ; luồng này không gọi Gemini.
  - Kết quả trả trạng thái CV rõ ràng: đã trích xuất, chưa có CV, không truy cập được hoặc không có văn bản đọc được. UI giải thích giới hạn và nhắc HR kiểm tra hồ sơ gốc.
  - Backend: `mvnw test` đạt 297/297; `HexagonalArchitectureTest` đạt 11/11. Frontend: 84 test, lint và build đều đạt; `git diff --check` đạt.
  - Đã sửa một test Mockito có stubbing thừa; không thay đổi logic chỉ để làm test xanh.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Java 21, Spring Boot, outbound ports `CvTextExtractionPort`/`FileStoragePort`, parser CV hiện có, kiểm tra quyền ở application use case, deterministic keyword/rule matching. Đây là đối sánh từ khóa/quy tắc; không dùng embedding hoặc vector database và không gọi Gemini cho CV riêng tư.
- **Mục đích chức năng / việc đã làm**: Giúp recruiter xem mức độ đáp ứng của ứng viên dựa trên CV đã tải lên mà giảm việc gửi dữ liệu cá nhân cho nhà cung cấp AI bên ngoài, đồng thời giữ ranh giới Hexagonal giữa use case và adapter. Thay đổi hiện đã qua kiểm thử cục bộ; chưa được merge hoặc triển khai Render.

---

## TỰ GIA HẠN PHIÊN ĐĂNG NHẬP VÀ SỬA 401 TRÊN API ĐÃ XÁC THỰC - 15/10/2026
- **Đã làm được gì**:
  - Kiểm tra log Render xác định request từ giao diện vẫn dùng access token hết hạn ngày 10/10; dữ liệu tên/vai trò trong localStorage khiến giao diện trông như còn đăng nhập dù API đã trả 401.
  - Thêm một client dùng chung: khi API được bảo vệ trả 401, client gọi `/auth/refresh-token`, lưu cặp token đã xoay và thử lại request đúng một lần. Các request đồng thời dùng chung một refresh operation; refresh token không hợp lệ thì xóa phiên cũ và phát sự kiện để UI cập nhật.
  - Áp dụng cho API trợ lý, ứng viên, recruiter, admin, thống kê, đổi mật khẩu và đăng xuất; không thay đổi các API công khai.
  - Backend JDK 21: `mvnw verify` đạt 297/297; frontend: 87/87 tests, ESLint và production build đạt; `git diff --check` đạt. Chưa merge hoặc redeploy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/Vite TypeScript; `authenticatedFetch`; single-flight promise để tránh refresh trùng trong cùng tab; refresh-token rotation hiện có ở Spring Boot; Vitest, ESLint và Vite.
- **Mục đích chức năng / việc đã làm**: Ngăn token hết hạn làm chatbot, bong bóng trợ lý và các màn có xác thực liên tục báo 401; tự gia hạn phiên hợp lệ an toàn, đồng thời bỏ trạng thái quyền cũ nếu phiên không thể khôi phục.

---

## MCP JAVA SDK VÀ TÁI SỬ DỤNG RAG KNOWLEDGE PORT - 15/10/2026
- **Đã làm được gì**:
  - Thay controller JSON-RPC tự viết bằng MCP Java SDK 2.0.1, Streamable HTTP Servlet tại `/api/v1/mcp`, protocol version `2025-11-25` và schema cho các tool ATS.
  - Bổ sung `TalentBridgeKnowledgePort` cùng inbound search use case; chatbot FAQ và MCP tool `talentbridge_search_help` dùng chung nguồn hướng dẫn TalentBridge.
  - Giữ MCP tools ở phạm vi use case hẹp; yêu cầu Recruiter ở route và handler, quyền sở hữu dữ liệu được giữ trong application use case. Không cấp truy cập JPA/SQL trực tiếp.
  - Sửa phản hồi phân quyền: Candidate có token hợp lệ nhưng bị thiếu role Recruiter trước đây bị luồng `/error` đổi 403 thành 401; `JwtAccessDeniedHandler` hiện trả JSON 403 trực tiếp.
  - Test HTTP tích hợp xác nhận initialize, `notifications/initialized`, `tools/list`, `tools/call`, anonymous 401 và Candidate 403. Backend `mvnw verify` đạt 298/298; `HexagonalArchitectureTest` đạt 11/11; `git diff --check` đạt.
  - Chưa kiểm thử giao diện live vì desktop đang ở màn hình khóa Windows; thay đổi còn ở nhánh tính năng, chưa merge hoặc deploy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: MCP Java SDK, Streamable HTTP, Jackson 2, Spring Security, application ports/use cases, lexical retrieval gồm token chính xác + synonym expansion + character-trigram Dice similarity, JUnit/Spring Boot integration tests, H2 test profile.
- **Mục đích chức năng / việc đã làm**: Chuẩn hóa tích hợp MCP cho ATS trong khi chatbot vẫn chỉ trả lời theo tài liệu TalentBridge; chia sẻ một cổng RAG có kiểm soát, bảo vệ dữ liệu Recruiter bằng quyền và ownership, đồng thời trả đúng 403 cho người đã đăng nhập nhưng không đủ vai trò. Retrieval lexical hiện tại không phải embedding/vector RAG và test chưa chứng minh độ chính xác hay interoperability với MCP client độc lập.

---

## XÁC MINH PHÁT HÀNH MCP/RAG TRÊN RENDER - 15/10/2026
- **Đã làm được gì**:
  - PR #34 đã merge vào `main` tại commit `ac11cce4257c9539beb91d885be9ce192f7b7e2b`; CI đạt 3/3 (backend verify, frontend test/lint/build, Dockerfile build).
  - CD Render đã trigger cả frontend và backend, xác minh build marker của hai dịch vụ đúng commit trên, rồi smoke test health, API việc làm dùng DB, frontend và CORS đều đạt.
  - Kiểm tra trực tiếp sau deploy: `/api/v1/health` trả build commit `ac11cce...`; `/api/v1/jobs` trả HTTP 200 nhưng `totalElements=0`; MCP từ chối truy cập ẩn danh bằng HTTP 401 như yêu cầu.
  - Giao diện production và bong bóng Trợ lý hiển thị; phiên trình duyệt hiện chưa đăng nhập nên ô gửi câu hỏi bị khóa. Chưa có bằng chứng kiểm thử hội thoại hoặc gọi MCP tool bằng phiên Recruiter trên production.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: GitHub Actions CI/CD, Render deploy hooks, build markers, production smoke tests, MCP Streamable HTTP, RAG lexical hybrid.
- **Mục đích chức năng / việc đã làm**: Phát hành và xác minh thay đổi MCP/RAG trên cả hai dịch vụ Render; ghi rõ giới hạn còn lại: cần phiên đăng nhập Recruiter để kiểm thử hội thoại/tool thật và cần nạp dữ liệu việc làm nghiệp vụ vì danh sách công khai hiện rỗng.

---

## SỬA FORM GỬI YÊU CẦU DOANH NGHIỆP - 15/10/2026
- **Đã làm được gì**:
  - Chuẩn hóa xác thực URL website và logo về HTTP(S); chặn email nhập nhầm vào website và logo dạng Data URI trước khi gửi API.
  - Bỏ giao diện tải logo giả lập không tương thích với DTO backend; làm rõ logo URL công khai là tùy chọn, và thêm thông báo lỗi ngay tại trường nhập.
  - Bổ sung giới hạn độ dài khớp backend, test unit, API integration và Playwright cho cả dữ liệu không hợp lệ lẫn gửi thành công.
  - Sau khi cập nhật với main, CI trên head 90418a8 đã pass đủ 3/3: backend, frontend và Docker; kiểm thử cục bộ gần nhất đạt Maven 299/299, frontend 90/90, API E2E 28/28 và UI E2E 4/4.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, URL parser của trình duyệt, Spring Boot Bean Validation, Vitest, MockMvc/JUnit, Playwright, Maven Wrapper và GitHub Actions.
- **Mục đích chức năng / việc đã làm**: Ngăn yêu cầu tạo doanh nghiệp thất bại do website/email hoặc logo sai hợp đồng; người dùng nhận chỉ dẫn chính xác trước khi request tới backend.

---

## XÁC MINH RELEASE FORM DOANH NGHIỆP VÀ TRẠNG THÁI LIVE - 15/10/2026
- **Đã làm được gì**:
  - PR #33 đã merge vào main tại 52d808e; CI hoàn tất 3/3 (backend verify, frontend test/lint/build, Docker build). GitHub Actions/CD Render run 38689713703 thành công và xác minh cả hai dịch vụ phục vụ đúng commit.
  - Kiểm tra trực tiếp production: frontend marker `52d808e76a67816896b0871d69fa2b50cd833016`; `/api/v1/health` HTTP 200 trong 0,19 giây; `/api/v1/jobs` HTTP 200 trong 0,57 giây và `totalElements=0`. Smoke test CORS credentialed cũng đạt trong workflow.
  - Mở trang `/assistant` và bong bóng trợ lý trên production: đều render, không trắng. Trạng thái khách thấy thông báo cần đăng nhập và không thể gửi prompt; tab TalentBridge tại thời điểm kiểm tra vẫn chưa có phiên được backend nhận.
  - RDS `talentbridge-mysql` đã thấy ở trạng thái Available; security group chỉ cho MySQL từ hai CIDR Render `74.220.52.0/24` và `74.220.60.0/24`. Chưa xác minh được inventory TiDB trong tab hiện đăng xuất, chưa tạo job vì API công khai không có dữ liệu và chưa có phiên Recruiter đã xác thực cùng công ty được duyệt.
  - Cấu hình RDS đang là `db.t4g.micro`, 20 GiB gp2, Single-AZ, autoscaling bật với mức tối đa 1000 GiB. Chưa xác minh Free Tier/credits trong Billing và chưa giới hạn storage autoscaling.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: GitHub Actions, Render Deploy Hooks và exact-commit markers; HTTP health/jobs/CORS smoke tests; Computer Use để kiểm tra trang trợ lý và AWS RDS Console.
- **Mục đích chức năng / việc đã làm**: Xác nhận fix form và release thực sự đang chạy trên production, phân biệt tốc độ request ấm với cold start, và ghi rõ các giới hạn còn lại ở đăng nhập, dữ liệu nghiệp vụ, TiDB và mức miễn phí AWS.

---

## KIỂM TRA RDS, CI/CD VÀ PHIÊN ĐĂNG NHẬP PRODUCTION - 15/10/2026
- **Đã làm được gì**:
  - Production đang phục vụ đúng build `ede188f`; workflow CD Render gần nhất thành công. `/api/v1/health` và `/api/v1/jobs` đều trả HTTP 200; API việc làm hiện trả danh sách rỗng (`totalElements=0`).
  - Log khởi động xác nhận MySQL 8.4 trên RDS kết nối được, Flyway xác thực 2 migration và schema hiện ở version 2; Hikari tạo kết nối thành công. Một lần khởi động ghi nhận 200,699 giây, nên chưa thể kết luận khởi động lạnh nhanh/ổn định.
  - Kiểm thử cục bộ: Maven verify 300/300, `HexagonalArchitectureTest` 11/11, frontend 90/90, lint/build đạt, API E2E 28/28 và Playwright UI E2E 4/4. E2E dùng H2 tạm, không ghi lên RDS production.
  - Log xác nhận admin production đã tồn tại nên `ADMIN_BOOTSTRAP_PASSWORD` không đổi mật khẩu hiện hữu. Tab production sau lần đăng nhập lại vẫn hiển thị form đăng nhập; chưa kiểm thử chatbot có xác thực hoặc luồng ghi dữ liệu nghiệp vụ lên RDS.
  - Không tạo/xóa dữ liệu production trong lượt kiểm tra này.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Spring Boot 3, Java 21, Amazon RDS MySQL 8.4, Flyway, HikariCP, GitHub Actions, Render build marker, Vitest, ESLint, Vite, H2 và Playwright.
- **Mục đích chức năng / việc đã làm**: Phân biệt rõ health/read-path live với kiểm thử ghi đã xác thực; ghi nhận giới hạn đăng nhập hiện tại, dữ liệu việc làm trống và độ trễ khởi động trước khi kết luận hệ thống production hoàn tất.

---

## XÁC MINH LẠI PHIÊN LIVE, API VÀ CI/CD - 15/10/2026
- **Đã làm được gì**:
  - Sau khi người dùng báo đã đăng nhập lại, kiểm tra tab Edge production vẫn đang ở `/login`; không tự nhập thông tin đăng nhập. Tab AWS và TiDB Cloud hiện cũng đang ở trang sign-in, nên chưa xác minh mới được inventory/SG trong lần này.
  - Backend live `/api/v1/health` và `/api/v1/jobs` trả HTTP 200; build marker vẫn là `ede188f`. Năm lượt GET jobs ấm có thời gian 208–527 ms (median 278 ms), kết quả 0 việc làm. Đây là đo request khi dịch vụ đang thức, không đo cold start.
  - Route frontend `/assistant` trả HTTP 200 và SPA shell; chưa gửi prompt do chưa có phiên xác thực. Không ghi hoặc xóa dữ liệu production.
  - CD Render run `38690585931` thành công cho commit `ede188f`; CI run mới nhất nhìn thấy là run PR `38690247959` thành công cho nhánh cập nhật báo cáo. CI branch đó không phải bằng chứng CI chạy trên chính `ede188f`.
  - Đọc lại `HexagonalArchitectureTest` (11 luật phụ thuộc), luồng trợ lý, tạo/sửa/ứng tuyển việc, chấm chất lượng job và đánh giá CV. Các bài test trước đó vẫn xanh; kiểm toán logic toàn dự án chưa hoàn tất và health endpoint hiện chỉ báo trạng thái ứng dụng, không tự kiểm tra DB.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Spring Boot REST API, RDS/MySQL read path qua API việc làm, Render build marker, GitHub Actions, PowerShell Stopwatch cho độ trễ request ấm, ArchUnit cho ranh giới Hexagonal và rà soát use case.
- **Mục đích chức năng / việc đã làm**: Phân biệt xác thực giao diện với phiên API thật, kiểm tra tốc độ có số đo, và ghi rõ giới hạn trước khi chạy chatbot, kiểm thử ghi RDS hoặc xóa resource cloud.

---

## KIỂM THỬ CHATBOT LIVE VÀ ĐỐI CHIẾU CI/CD - 15/10/2026
- **Đã làm được gì**:
  - Trên bản production `ede188fc9b95e14725b7b25c56b75faf68dcbfb6`, trang `/assistant` và widget chatbot nổi ở `/jobs` đều render bình thường. Hai prompt thử nghiệm chỉ hỏi quy trình TalentBridge được trả lời kèm căn cứ; không tái hiện màn trắng.
  - `/api/v1/health` trả HTTP 200, `UP`, đúng build marker; `/api/v1/jobs` trả HTTP 200 nhưng có 0 tin. Hai lượt đọc live đo được lần lượt khoảng 437 ms và 367 ms; đây chỉ là phép đo tức thời khi kiểm tra, không đại diện cold start hay tải cao.
  - CD run `38690585931` thành công cho đúng commit production. CI gần nhất `38690247959` thành công trên PR cập nhật báo cáo (`49530f2`), không phải cùng SHA với production; CI 3/3 của PR release trước đã được ghi ở mục kiểm chứng form.
  - Thanh điều hướng production xác định phiên hiện tại là **Ứng viên**, dù người dùng báo đã vào trang quản trị. Vì vậy chưa chạy thao tác/luồng Admin hoặc Recruiter và không ghi dữ liệu RDS. Tab TiDB hiện vẫn ở trang đăng nhập, nên chưa xác minh resource để xóa; không có thao tác xóa nào được thực hiện.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Production UI qua Computer Use, Spring REST health/jobs, build marker, GitHub Actions run metadata, Render CD và retrieval nội bộ của chatbot.
- **Mục đích chức năng / việc đã làm**: Xác nhận hồi quy chatbot và đường đọc API sau deploy, đồng thời phân định rõ các cổng còn thiếu: tài khoản đúng vai trò để thử ghi nghiệp vụ, dữ liệu job để thử luồng tuyển dụng end-to-end, và đăng nhập TiDB để kiểm kê trước thao tác xóa.

---

## KIỂM THỬ TOÀN BỘ CỤC BỘ VÀ RÀ SOÁT CƠ CHẾ DỮ LIỆU - 15/10/2026
- **Đã làm được gì**:
  - Chạy lại `mvnw verify` bằng JDK 21: 300/300 test đạt, trong đó `HexagonalArchitectureTest` đạt 11/11. Lần chạy ban đầu dùng Java 8 từ PATH nên không nạp được class Java 21; đã dùng JDK 21 cài sẵn cho lượt chạy xác nhận, không đổi cấu hình máy.
  - Frontend: 90/90 unit test, ESLint và Vite/TypeScript production build đều đạt. Script `scripts/run-e2e.ps1` chạy API E2E 28/28 và Playwright UI E2E 4/4 trên backend H2 cô lập; bao gồm chatbot/widget, phục hồi khi cuộn lỗi, form yêu cầu doanh nghiệp và viewport 360/555/1280px cho ba vai trò.
  - Xác nhận `DataInitializer` chỉ chạy ở profile `local`, `test`, `dev`, `e2e`; dữ liệu công ty/tài khoản và 100 tin đang tuyển là dữ liệu demo cho môi trường không production. Không thể bật cấu hình này để nạp RDS production vì sẽ đưa dữ liệu demo sang production và tin được tạo ở trạng thái `ACTIVE`.
  - Không có thay đổi source ứng dụng hoặc thao tác ghi DB production. E2E lưu ảnh QA trong `artifacts/roleplay-qa`; kiểm thử persistence vẫn dùng H2, còn RDS chỉ mới được xác nhận qua API đọc việc làm HTTP 200 với 0 bản ghi.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: JDK 21, Maven Surefire, ArchUnit, Vitest, ESLint, TypeScript/Vite, Playwright và backend H2 tạm thời; Spring profile gating bảo vệ initializer.
- **Mục đích chức năng / việc đã làm**: Xác nhận bộ kiểm thử source hiện tại và ngăn dùng seed demo không phù hợp cho production; để nạp tin nghiệp vụ cần phiên Admin/Recruiter thật và luồng kiểm duyệt, còn xóa TiDB cần đăng nhập để kiểm kê resource trước.

---

## KIỂM TRA LẠI RELEASE LIVE VÀ RELEASE GATE MAIN - 15/10/2026
- **Đã làm được gì**:
  - Đo lại API việc làm production 5 lần: mọi lượt HTTP 200, `totalElements=0`, thời gian 418/205/158/194/201 ms (median 201 ms). Frontend trả HTTP 200 với marker đúng `ede188fc9b95e14725b7b25c56b75faf68dcbfb6`.
  - Xác nhận GitHub CI có 3 check backend/frontend/Docker đều pass ở PR gần nhất trước release; CD main hiện phục vụ đúng commit production. Phát hiện GitHub `main` đang **không có branch protection** và branch ruleset rỗng, nên CI hiện là bằng chứng xanh nhưng chưa được cài làm merge gate bắt buộc. Đã hỏi chủ repo chọn cấu hình bảo vệ; chưa thay đổi setting.
  - Mở trang đăng nhập production để người dùng tự xác thực; trang vẫn nhận diện phiên là Ứng viên, nên chưa thực hiện tạo dữ liệu, thử ghi RDS hoặc xóa TiDB. TiDB Cloud và AWS Console cũng vẫn chờ đăng nhập.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: GitHub Actions check-run metadata, GitHub branch protection API read-only, HTTP Stopwatch và Render build marker.
- **Mục đích chức năng / việc đã làm**: Phân biệt pipeline đã chạy thành công với bảo vệ merge thực sự; xác nhận lại API đọc production và nêu chính xác bước xác thực/quyền còn thiếu trước các thao tác production.

---

## BẬT RULESET BẢO VỆ NHÁNH MAIN - 15/10/2026
- **Đã làm được gì**:
  - Tạo ruleset repository `Protect main: PR and all CI checks` (ID `25142828`), trạng thái active, chỉ áp dụng `refs/heads/main`.
  - Bắt buộc mọi thay đổi vào `main` đi qua Pull Request, yêu cầu ba status check do GitHub Actions phát: `Backend Build & Verify (Spring Boot 3 + JDK 21)`, `Frontend Test, Lint & Build (React 19 + Node 20)`, `Dockerfile Build Verification`; không yêu cầu số review tối thiểu và chặn force-push.
  - Đọc lại ruleset qua GitHub API và endpoint rule evaluation của `main`; xác nhận đủ ba rule PR, required status checks, non-fast-forward. Không sửa source ứng dụng và không redeploy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: GitHub Repository Rulesets REST API, GitHub Actions check-run metadata (integration `github-actions`, ID `15368`), ba job trong `.github/workflows/ci.yml`.
- **Mục đích chức năng / việc đã làm**: Chuyển CI từ trạng thái chỉ chạy khi có sự kiện sang cổng merge bắt buộc cho `main`, đồng thời giữ lựa chọn không yêu cầu người duyệt PR.
---

## TÁI XÁC MINH PRODUCTION SAU KHI BẬT RULESET - 15/10/2026
- **Đã làm được gì**:
  - Production tại build `ede188fc9b95e14725b7b25c56b75faf68dcbfb6`: health HTTP 200 (458 ms), API jobs HTTP 200 (196 ms, `totalElements=0`), frontend route `/assistant` HTTP 200 (125 ms). Đây là một lượt đo tức thời, không phải benchmark tải hoặc cold-start.
  - GitHub API xác nhận ruleset `25142828` đang active trên `main`, yêu cầu PR, đúng ba check backend/frontend/Docker và chặn non-fast-forward.
  - Browser recheck: TalentBridge hiện nhận diện tài khoản `Duy Khánh Lê` là Ứng viên; TiDB Cloud và AWS Console còn ở trang đăng nhập. Không thể chạy luồng ghi RDS, kiểm thử tuyển dụng từ đầu đến cuối hoặc xác định resource TiDB cần xóa.
  - Không ghi/xóa dữ liệu production và không redeploy vì không có thay đổi source ứng dụng.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: HTTP GET/Stopwatch, Spring Boot health/jobs API, Render build marker, GitHub Repository Rulesets API, Computer Use đọc trạng thái phiên.
- **Mục đích chức năng / việc đã làm**: Cập nhật bằng chứng live sau khi bật merge gate; phân định rõ API đọc đang phục vụ tốt với các luồng cần quyền Admin/Recruiter và quyền console cloud chưa được xác minh.

---

## XÁC MINH RDS, TIDB VÀ CI/CD SAU KHI ĐĂNG NHẬP CLOUD - 15/10/2026
- **Đã làm được gì**:
  - AWS Console xác nhận RDS `talentbridge-mysql` ở Singapore đang `Available`, MySQL 8.4.9, `db.t4g.micro`, Single-AZ; mã hóa lưu trữ và deletion protection bật. Storage hiện 20 GiB gp2 nhưng autoscaling cho phép đến 1000 GiB; backup tự động chỉ giữ 1 ngày và có 3 snapshot tự động ở trạng thái Available.
  - Log Render của commit production `ede188fc9b95e14725b7b25c56b75faf68dcbfb6` ghi Flyway schema `talentbridge_db` đã up-to-date và Hikari tạo kết nối MySQL thành công. Biểu đồ RDS có DatabaseConnections dao động 0–4 trong khoảng thời gian gần nhất; kết hợp với log cho thấy luồng BE–DB đang hoạt động. URL datasource bị che nên chưa đối chiếu độc lập được hostname với endpoint RDS; RDS Console đồng thời hiển thị DB name khởi tạo là `test`, cần giữ ghi chú này khi rà soát cấu hình.
  - Smoke test live hiện tại: `/api/v1/health` HTTP 200, build marker đúng; `/api/v1/jobs` HTTP 200 trong 281 ms, `totalElements=0`. Đây là request ấm đơn lẻ, không phải benchmark. Không ghi dữ liệu production.
  - Log khởi động cùng deploy ghi ứng dụng mất 200.699 giây mới sẵn sàng; trước lúc mở port Render nhiều lần báo chưa phát hiện port. Hikari cũng cảnh báo `idleTimeout` mặc định lớn hơn `maxLifetime` đã cấu hình nên bị vô hiệu hóa. Đã chỉnh cấu hình source local thành `idle-timeout=240000` (nhỏ hơn `max-lifetime=300000`) và `mvnw verify` đạt 300/300; thay đổi chưa được merge/deploy. Cold start 200.699 giây vẫn chưa được giải quyết. Render Free không cung cấp metric CPU/RAM nên hiện chưa đủ dữ liệu kết luận nguyên nhân.
  - TiDB Cloud đã đăng nhập: cả Resource View và Project View đều không còn resource/instance trong tổ chức/project JS2. Vì vậy hiện không có cluster TiDB để xóa thêm. Trạng thái danh sách không chứng minh lịch sử xóa hoặc việc dữ liệu TiDB đã được nhập/đối soát; không có bằng chứng migration đầy đủ.
  - CI run `38690247959` pass đủ 3 job backend/frontend/Docker trên head PR `49530f2`; CD run `38690585931` thành công và Render đang phục vụ commit `ede188fc`. Không có CI run gắn trực tiếp với đúng SHA production trong danh sách hiện tại; merge gate PR đã được bật ở mục trước.
  - Tab TalentBridge vẫn xác thực ở vai trò Ứng viên. Vì vậy chưa thể tạo 100 tin chờ duyệt qua luồng Recruiter/Admin hoặc thực hiện kiểm thử ghi/đọc nghiệp vụ bằng tài khoản có quyền. API jobs hiện không có tin.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: AWS RDS Console/CloudWatch metrics, TiDB Cloud Resource View, Render application logs, Spring Boot health/jobs API, HikariCP, Flyway, GitHub Actions CI và Render CD.
- **Mục đích chức năng / việc đã làm**: Xác nhận DB đang nhận kết nối và CI/CD đang chạy, đồng thời tách bạch rõ giữa khả năng kết nối, migration dữ liệu, thao tác ghi nghiệp vụ và hiệu năng cold start. Không xóa thêm resource, không nạp dữ liệu mẫu, không đọc hoặc tiết lộ secret.

---

## GỠ TÀI KHOẢN MẪU VÀ NÂNG QUYỀN ADMIN CÓ KIỂM SOÁT - 15/10/2026
- **Đã làm được gì**: Gỡ khối nút điền nhanh tài khoản mẫu khỏi trang đăng nhập. Bổ sung cờ `ADMIN_BOOTSTRAP_PROMOTE_EXISTING` mặc định tắt; khi bật chỉ nâng đúng tài khoản hiện hữu đang ACTIVE, giữ nguyên mật khẩu/hồ sơ/các role, và lỗi an toàn nếu email không khớp. Cập nhật runbook và test cho các trường hợp tạo mới, đã là Admin, nâng quyền, tài khoản khóa và email không tồn tại. Xác minh `mvnw verify` đạt 304/304; frontend ESLint, 90/90 unit test và Vite production build đều đạt. Chưa thay đổi Render production hoặc cấp role trong DB.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript; Spring Boot ApplicationRunner chỉ trong profile `prod`; inbound use case/port, persistence adapter và role domain theo kiến trúc hexagonal; JUnit 5/Mockito, Vitest, ESLint, TypeScript/Vite.
- **Mục đích chức năng / việc đã làm**: Loại bỏ thông tin đăng nhập mẫu gây nhầm lẫn và cung cấp đường nâng quyền một lần có kiểm soát, không mở đăng ký Admin công khai và không thay mật khẩu tài khoản sẵn có.

---

## KHÔI PHỤC TÀI KHOẢN ADMIN ĐANG BỊ KHÓA - 15/10/2026
- **Đã làm được gì**:
  - Đã xác minh Render Free không hỗ trợ Shell, vì vậy không dùng được SQL trực tiếp từ service.
  - Thêm cờ một lần `ADMIN_BOOTSTRAP_REACTIVATE_LOCKED_ADMIN` mặc định `false`; chỉ khi cờ được bật, email bootstrap khớp tài khoản hiện có có `ROLE_ADMIN`, và trạng thái là `LOCKED` thì use case mới chuyển trạng thái sang `ACTIVE`. Giữ nguyên password hash, hồ sơ và roles; từ chối trạng thái `BANNED`, `INACTIVE`, `PENDING` và không tác động tài khoản không phải Admin.
  - Thêm test cho mở khóa được phê duyệt, cờ mặc định tắt, không tự mở khóa và từ chối tài khoản BANNED. `mvnw verify` bằng JDK 21 đạt 308/308, gồm kiểm tra kiến trúc hexagonal 11/11. Chưa thay đổi production; đang chuẩn bị PR để CI kiểm tra trước khi deploy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Spring Boot ApplicationRunner chỉ trong profile `prod`, cờ cấu hình opt-in, domain `User.activate()`, port/use case hexagonal, JUnit 5/Mockito, Maven và JDK 21.
- **Mục đích chức năng / việc đã làm**: Khôi phục quyền truy cập cho đúng tài khoản Admin bị khóa do đăng nhập sai, không đặt lại mật khẩu hoặc mở rộng quyền cho tài khoản khác; cờ sẽ được tắt và gỡ sau khi xác minh.

---

## TRI-VECTOR MATCHING, MCP CÓ KIỂM SOÁT VÀ CHATBOT RAG - 16/10/2026
- **Đã làm được gì**:
  - Hoàn thiện điểm tương đồng CV–job dạng tham khảo, tách thành bằng chứng trực tiếp 60%, ngữ cảnh nghề nghiệp 25% và năng lực liên quan suy luận thận trọng 15%. Trả kèm điểm thành phần, bằng chứng/kỹ năng khớp, kỹ năng chưa thấy bằng chứng, câu hỏi xác minh trung lập và cảnh báo đây không phải xác suất trúng tuyển. CV được trích xuất và chấm trong backend sau kiểm tra quyền sở hữu ứng tuyển; không gửi CV sang Gemini.
  - MCP Streamable HTTP công bố năm tool ATS giới hạn schema và dữ liệu; yêu cầu Recruiter xác thực, use case kiểm tra tenant/ownership, giới hạn kích thước đầu vào/kết quả và bắt buộc `confirm=true` cho cập nhật giai đoạn ứng tuyển. Kiểm thử protocol xác nhận lifecycle/discovery/tool call, anonymous 401, Candidate 403 và từ chối thao tác ghi thiếu xác nhận.
  - Chatbot truy xuất tối đa ba mục hướng dẫn TalentBridge bằng lexical hybrid (token chính xác, synonym và character-trigram), đưa căn cứ truy xuất vào system instruction để Gemini sinh câu trả lời, và trả câu hướng dẫn truy xuất khi thiếu key/provider lỗi. FAQ vẫn trả lời cục bộ; không gửi lịch sử hội thoại; chặn các mẫu PII/credential phổ biến trước provider. Chatbot không dùng MCP tool hoặc đọc dữ liệu riêng.
  - Xác minh `mvnw verify` với JDK 21: 311/311 test đạt, gồm Hexagonal Architecture 11/11. Frontend: ESLint đạt, Vitest 90/90, TypeScript/Vite production build đạt. E2E H2 cô lập: API 28/28 và Playwright UI 4/4; kiểm thử assistant/widget và viewport. `git diff --check` đạt.
  - Đây là kết quả trên nhánh tính năng; chưa merge/deploy. Chưa gọi Gemini production trực tiếp và chưa chứng minh độ chính xác tuyển dụng hay interoperability với MCP client độc lập.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Spring Boot, Java 21, Spring Security, MCP Java SDK/Streamable HTTP, use case hexagonal, lexical weighted coverage + cosine cho ba vector CV–job, synonym/token + Dice character-trigram cho truy xuất hướng dẫn, Gemini GenerateContent grounded bởi tài liệu, JUnit/ArchUnit, Maven, React/TypeScript, Vitest, ESLint, Vite, H2 và Playwright.
- **Mục đích chức năng / việc đã làm**: Cung cấp tín hiệu tương đồng có thể giải thích cho Recruiter, mở công cụ ATS qua giao thức chuẩn với quyền và xác nhận rõ ràng, và hoàn thiện luồng RAG có sinh câu trả lời nhưng có căn cứ, giới hạn phạm vi và fallback an toàn. Công thức Tri-Vector là thiết kế riêng của TalentBridge, không quy nguồn cho bài báo AI–KM; giới hạn nguồn được ghi trong tài liệu nghiên cứu và README.

## TÍCH HỢP REDESIGN V2 TRÊN MAIN MỚI NHẤT - 16/10/2026
- **Đã làm được gì**:
  - Tích hợp riêng giao diện V2 lên nền `main` mới nhất, giữ các route Trợ lý, bong bóng chatbot và thay đổi đăng nhập đã được merge ở PR #40/#38. Không đưa các sửa đổi backend/AI còn dang dở trong checkout khác vào thay đổi giao diện.
  - Giải quyết xung đột router, đăng nhập và báo cáo tiến độ; điều chỉnh selector Playwright cũ để khớp nhãn đăng nhập V2.
  - Xác minh frontend: lint đạt, 90/90 unit tests, production build đạt. E2E trên H2 cô lập: API 28/28, Playwright UI 7/7, gồm ba vai trò, trang trợ lý/widget, luồng gửi yêu cầu doanh nghiệp và viewport 360/555/1280px.
  - PR #41 đã squash-merge vào `main` tại commit `0538a07ef6b30c781c4c6c82355b8b146a2b8b50`. GitHub Actions run `38866941682` đạt 3/3 check Backend/Frontend/Docker. CD run `38867330201` đã xác minh frontend và backend phục vụ đúng commit, health/jobs API, title trang và credentialed CORS đều đạt.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**:
  - React/TypeScript, Tailwind CSS, Vite, Be Vietnam Pro cục bộ, SVG 2.5D; pnpm 9, Vitest, ESLint, Playwright, Spring Boot và H2 tạm cho E2E.
- **Mục đích chức năng / việc đã làm**:
  - Đưa Sapphire & Graphite V2 vào `main` và Render production, vẫn giữ hành vi trợ lý; xác nhận qua unit, build, E2E và smoke test production đa vai trò/API.

## SỬA LỖI HEADER RESPONSIVE V2 - 16/10/2026
- **Đã làm được gì**: Chặn menu và cụm tài khoản bị ép xuống nhiều dòng; từ 1024–1279px dùng điều hướng biểu tượng có nhãn truy cập, từ 1280px hiển thị đầy đủ nhãn; thu gọn thao tác đổi mật khẩu/đăng xuất thành nút biểu tượng có accessible label. Kiểm tra Playwright ở 1780, 1440, 1280, 1024, 1000 và 390px: không tràn ngang, các mục desktop cao 40px và không xuống dòng. `pnpm lint`, `pnpm test` (12 file/90 test) và `pnpm run build` đều đạt.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React Router `NavLink`, Tailwind responsive utilities, Lucide icons, ARIA labels, Playwright Chromium, ESLint, Vitest, TypeScript và Vite.
- **Mục đích chức năng / việc đã làm**: Làm thanh điều hướng V2 dễ đọc và dùng ổn định trên desktop/tablet/mobile; giữ toàn bộ chức năng theo vai trò và tránh các nhãn tài khoản bị bẻ dòng như ảnh production.

## RÀ SOÁT VÀ ĐỒNG BỘ GIAO DIỆN V2 THEO FIGMA - 16/10/2026
- **Đã làm được gì**:
  - Chuẩn hóa header dùng chung theo cấu trúc Figma: thương hiệu bên trái, nhóm điều hướng dạng pill với mục hiện tại có nền trắng, cụm tài khoản có avatar/vai trò và thao tác đổi mật khẩu/đăng xuất. Gộp khung workspace của Recruiter/Admin để cùng dùng header, sidebar, vùng nội dung và mobile drawer; giữ guard RBAC và các route hiện hành.
  - Rà soát và chỉnh các màn trang chủ, tìm việc/chi tiết việc, trợ lý và bong bóng chat, hồ sơ ứng viên, workspace tuyển dụng/thống kê/sàng lọc ứng viên và danh sách quản trị. Dữ liệu hiển thị vẫn lấy từ API; trạng thái lỗi/rỗng không bị thay bằng số liệu giả. Ảnh QA dùng fixture trên H2 cô lập.
  - Mở rộng kiểm thử viewport để chụp và xác nhận tất cả route công khai cùng route theo ba vai trò tại 360px và 1280px. Lượt kiểm tra phát hiện rồi sửa tràn ngang ở trang tìm việc mobile, bộ lọc trạng thái bị cắt, bảng quản trị bị nén chữ và cụm thao tác ứng viên recruiter chật; bảng admin nay có vùng cuộn ngang đặt tên và nhận focus bàn phím.
  - Kiểm tra ảnh desktop/mobile cho trang chủ, tìm việc, ứng viên, recruiter và admin; E2E cuối trên H2 tạm: API 28/28, Playwright 7/7, không có route trắng hoặc tràn ngang. Frontend `pnpm lint`, Vitest 90/90, TypeScript/Vite production build và `git diff --check` đều đạt.
  - Source đã sẵn sàng để cập nhật PR #43; chưa merge hoặc redeploy. Figma MCP từ chối thêm thao tác do hết quota Starter và tài khoản chỉ có ghế View, nên không thể sửa/đồng bộ trực tiếp file Figma trong lượt này.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, Tailwind CSS, React Router `NavLink`, Lucide icons, layout dùng chung theo vai trò, responsive breakpoints, ARIA labels, ESLint, Vitest, Vite, Playwright Chromium và backend H2 tạm cho E2E.
- **Mục đích chức năng / việc đã làm**: Làm giao diện nhất quán giữa các vai trò và thiết bị theo cấu trúc Sapphire & Graphite V2, giữ luồng điều hướng/quyền truy cập và khả năng thao tác hiện có; phân biệt rõ dữ liệu kiểm thử với dữ liệu production và giới hạn chỉnh sửa trực tiếp trong Figma.

## ẨN HEADER Ở MÀN ĐĂNG NHẬP — 16/10/2026
- **Đã làm được gì**:
  - Ẩn hoàn toàn header điều hướng dùng chung tại `/login`, giữ nguyên header trên trang chủ và các route khác.
  - Giữ nguyên form, footer và bong bóng truy cập nhanh chatbot; kiểm tra trực tiếp trên Vite local: trang đăng nhập có 0 header, trang chủ có 1 header và chatbot vẫn hiện.
  - `pnpm run lint`, `pnpm run test` (90/90) và `pnpm run build` đều đạt; không thay đổi backend hoặc deploy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React Router `useLocation` kiểm tra pathname trong `MainLayout`; render có điều kiện trong React; Vitest, ESLint, TypeScript/Vite và kiểm tra DOM qua trình duyệt local.
- **Mục đích chức năng / việc đã làm**: Tạo màn xác thực gọn, không có điều hướng gây phân tán; giữ trải nghiệm đăng nhập và trợ lý nhanh, không ảnh hưởng điều hướng ở các trang khác.

## CHẨN ĐOÁN ĐĂNG NHẬP VÀ ĐĂNG KÝ LOCAL — 16/10/2026
- **Đã làm được gì**:
  - Xác nhận frontend `127.0.0.1:5175` gọi qua Vite proxy tới backend TalentBridge `127.0.0.1:8081`; `GET /api/v1/health` và đọc danh sách việc làm trả HTTP 200. Endpoint đăng ký/đăng nhập nhận đúng request thiếu dữ liệu và trả HTTP 400 validation; không gửi hay đọc payload người dùng.
  - Xác nhận cổng 8080 hiện do ứng dụng `agent-demo` chiếm; frontend auth dùng cùng-origin `/api/v1`, nên không gọi nhầm ứng dụng này.
  - Chạy kiểm thử Java 21 trên H2 cô lập: `AuthControllerTest` 11/11 và `RegisterRoleValidationIntegrationTest` 3/3 đạt. Lần chạy đầu dùng nhầm Java 8; đã chuyển sang JDK 21 và chạy lại thành công.
  - Backend local hiện chạy profile `local` với H2 file, không phải RDS. Tài khoản chỉ tồn tại trên RDS có thể không đăng nhập được ở localhost. Chưa bắt được HTTP status của lần gửi form thật và chưa xác minh ghi trên H2 file đang chạy; nguyên nhân riêng của lần thử đó còn mở.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Spring Boot, Spring Security, Vite proxy, H2, Maven Wrapper, JDK 21, JUnit, MockMvc và Spring Boot integration tests.
- **Mục đích chức năng / việc đã làm**: Phân biệt lỗi backend ngừng hoạt động, sai proxy và khác môi trường DB; xác nhận luồng auth trong code hoạt động trên DB kiểm thử và khoanh vùng phần còn cần mã HTTP của request thật.

## ĐỒNG BỘ FE LOCAL THEO FIGMA V2 VÀ KIỂM TRA RESPONSIVE — 16/10/2026
- **Đã làm được gì**:
  - Rà giao diện công khai, ứng viên, nhà tuyển dụng và quản trị trên desktop/mobile; đồng bộ lại chi tiết việc làm, danh sách tin nhà tuyển dụng và hồ sơ ứng viên theo bố cục V2, giữ nguyên API, phân quyền, dữ liệu và thao tác hiện có.
  - Sửa bảng ứng viên quản trị để hiển thị đủ cột thao tác ở desktop; thêm kiểm tra Playwright cho bố cục hồ sơ hai cột và vị trí nút thao tác.
  - Xác minh `npm run lint`, `npm run build`, `npm run test` (90/90), E2E API (28/28) và E2E UI (7/7) đạt; kiểm tra viewport 360/555/1280px và lưu ảnh QA riêng tại `artifacts/roleplay-parity-final`.
  - Figma MCP Starter đã hết lượt gọi trong phiên; đối chiếu bằng ảnh thiết kế được cung cấp và ảnh render local, chưa xác nhận parity từng layer/nút với mọi frame trong Figma.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React + TypeScript + Tailwind CSS; CSS Grid responsive; Playwright, Vitest, ESLint, TypeScript/Vite; backend E2E trên H2 cô lập.
- **Mục đích chức năng / việc đã làm**: Đưa các luồng chính về cùng hệ Sapphire & Graphite V2, giảm sai khác bố cục desktop/mobile và ngăn tái xuất hiện lỗi cột thao tác bị cắt mà không đổi hợp đồng backend.

## HIỂN THỊ MÃ LỖI ĐĂNG NHẬP VÀ ĐĂNG KÝ — 16/10/2026
- **Đã làm được gì**:
  - Cập nhật fallback của API xác thực để hiển thị mã HTTP khi backend/proxy trả lỗi không kèm thông báo chuẩn; thêm hướng dẫn rõ cho HTTP 400, 404, 409, 429 và 5xx.
  - Thêm test cho phản hồi HTML 502 của gateway và phản hồi 429 thiếu message; bảo toàn ưu tiên hiển thị thông điệp backend khi có.
  - Bản sửa được cô lập trên nhánh phát hành riêng để không đưa các thay đổi backend/AI chưa liên quan vào production; xác minh `pnpm test` đạt 92/92, `pnpm lint`, `pnpm build` và `git diff --check` đều đạt. Đang chờ CI/review PR trước khi deploy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**:
  - React/TypeScript Fetch API, kiểu lỗi `AuthApiError`, Vitest; tích hợp với envelope `ApiResponse` hiện có của Spring Boot.
- **Mục đích chức năng / việc đã làm**:
  - Giúp phân biệt lỗi dữ liệu/tài khoản với lỗi API, gateway hoặc giới hạn tốc độ để chẩn đoán đăng nhập/đăng ký mà không lộ thông tin xác thực.

## CẬP NHẬT PR VÀ XÁC MINH PREVIEW V2 — 16/10/2026
- **Đã làm được gì**:
  - Đồng bộ nhánh giao diện với `main` sau khi PR #44 được merge; giữ phần xử lý lỗi đăng nhập/đăng ký mới nhất, giải quyết xung đột tài liệu và cập nhật PR #43 lên commit `b5ae44c`.
  - Xác minh local `http://localhost:5176`: trang chủ có header chính; `/login` và `/register` không có menu Trang chủ/Việc làm/Trợ lý; form đăng nhập/đăng ký và chatbot hiển thị.
  - Lint và production build đạt; Vitest 92/92; E2E H2 cô lập gần nhất đạt API 28/28 và UI 7/7. PR #43 hiện mergeable; CI Frontend, Backend và Docker 3/3 đạt.
  - Chưa merge/deploy production; chờ trưởng nhóm review theo quy trình dự án. Figma MCP vẫn bị giới hạn ở Starter/View nên không xác minh pixel/layer cho mọi frame.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, Tailwind CSS, Vite, React Router, Vitest, Playwright, Spring Boot trên H2 cô lập, GitHub Actions.
- **Mục đích chức năng / việc đã làm**: Đưa bản FE V2 có thể xem tại localhost và review trên PR, đồng thời giữ kiểm soát phát hành trước khi Render nhận deploy.

## CATALOG MÔ PHỎNG 20 DOANH NGHIỆP VÀ 100 VIỆC LÀM — 18/10/2026
- **Đã làm được gì**:
  - Thêm Flyway V3 tạo 20 hồ sơ doanh nghiệp hư cấu có tiền tố `DEMO` và 100 tin mô phỏng (5 vai trò mỗi doanh nghiệp), kèm 280 liên kết kỹ năng mẫu. Không dùng website, mã số thuế, văn phòng, nhà tuyển dụng hay lợi ích tuyển dụng có thật; hồ sơ công ty giữ trạng thái `PENDING`.
  - Truyền cờ `isDemo` qua JPA, domain, DTO/API; trang chủ, tìm việc, chi tiết tin và danh sách admin ghi rõ nội dung mô phỏng. Tin chỉ cho xem; use case backend từ chối ứng tuyển, yêu cầu gia nhập hoặc phê duyệt hồ sơ công ty mô phỏng.
  - Thêm kiểm thử migration trên H2 chế độ MySQL: xác nhận đúng 20 công ty, 100 tin `ACTIVE` để xuất hiện trong tìm kiếm, không gắn recruiter, không có đơn ứng tuyển và có đủ liên kết kỹ năng. Backend `mvnw verify` đạt 315/315; frontend `pnpm test` đạt 92/92, `pnpm run lint`, `pnpm run build` và `git diff --check` đạt.
  - Chưa đưa dữ liệu lên production: migration sẽ chạy khi bản thay đổi được trưởng nhóm review/merge và Render deploy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Flyway SQL V3; MySQL/H2 tương thích; Spring Boot, JPA, DTO REST, use case hexagonal; React/TypeScript, Vitest, ESLint, Vite và Maven/JDK 21.
- **Mục đích chức năng / việc đã làm**: Có dữ liệu để xem thử luồng tìm kiếm và ATS trên website mà không mạo nhận tuyển dụng thật, nhận hồ sơ ứng viên hoặc xác thực doanh nghiệp hư cấu.

## ĐỐI CHIẾU MCP VÀ SỬA CHUẨN HÓA ĐIỂM TRI-VECTOR — 19/10/2026
- **Đã làm được gì**:
  - So sánh kết quả matching trực tiếp với use case mà tool MCP gọi trên cùng tin và hai hồ sơ tổng hợp; cả 500 cặp cho kết quả DTO giống nhau, hồ sơ phù hợp đạt 77.1% và hồ sơ không liên quan 0.0%. Phép đo trong JVM dao động giữa 229.41/285.50 µs và 245.42/343.98 µs; không đại diện độ trễ HTTP hoặc dữ liệu thực tế.
  - Phát hiện hàm làm tròn chặn `matchPercentage` ở 1.0 do tái sử dụng clamp của điểm thành phần 0–1. Tách làm tròn phần trăm theo thang 0–100 và thêm assertion hồi quy.
  - `mvnw -B verify` đạt 316/316, gồm HexagonalArchitecture 11/11 và MCP Streamable HTTP integration 4/4. Ghi rõ trong tài liệu rằng benchmark không đo RAG matcher độc lập; HRPM-58 còn chờ đồng thuận nhóm.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Java 21, Spring Boot, Tri-Vector lexical coverage/cosine, use case MCP có kiểm tra công ty và đơn ứng tuyển, JUnit/Mockito, Maven Wrapper; tài liệu so sánh chi phí/latency/chất lượng/bảo trì.
- **Mục đích chức năng / việc đã làm**: Sửa thang điểm tổng để UI và API nhận đúng phần trăm, ngăn regression và cung cấp bằng chứng đo lường trung thực cho quyết định dùng Tri-Vector, MCP và RAG.
## TRIỂN KHAI THỐNG KÊ API QUẢN TRỊ — HRPM-59 — 19/10/2026
- **Đã làm được gì**:
  - Ghi request count, latency trung bình/cao nhất và số/tỷ lệ lỗi 4xx/5xx theo route template; hiển thị xu hướng bucket 5 phút trong trang `/admin/metrics`.
  - Bảo vệ API metrics bằng quyền `ROLE_ADMIN`; loại URL query, body, token, tài khoản và ID động khỏi dữ liệu thu thập. Dọn bucket sau 24 giờ; ghi rõ số liệu nằm trong bộ nhớ từng tiến trình và sẽ mất khi restart.
  - Kiểm thử nhánh từ `origin/main`: backend Maven `verify` đạt 323/323; frontend Vitest 92/92, ESLint và Vite build đều đạt. Thêm test bảo đảm HTTP method tùy ý được gom thành `OTHER` để giới hạn cardinality của metrics.
  - Cấu hình hai dịch vụ Render tự deploy sau khi CI checks pass bằng `autoDeployTrigger: checksPass`, theo yêu cầu bật auto-deploy. Render hiện phục vụ backend health `UP`, build `90a56e3`; phiên bản metrics mới chỉ được xác nhận sau merge/deploy.
  - Sửa request dashboard metrics dùng `authenticatedFetch` để khi access token hết hạn, phiên hợp lệ được refresh và thử lại như các API admin khác. Kiểm tra helper xác thực 3/3 test và frontend production build đạt.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Spring Servlet filter, `ConcurrentHashMap`, `LongAdder`/`LongAccumulator`, allowlist HTTP method, route template, bucket 5 phút, Spring Security `ROLE_ADMIN`, React/TypeScript và Vitest.
- **Mục đích chức năng / việc đã làm**: Cho quản trị viên theo dõi lưu lượng, độ trễ và lỗi API theo endpoint mà không ghi dữ liệu nhạy cảm hoặc truy vấn DB trong luồng xử lý request.

## UAT TOÀN WEBSITE, SỬA LỖI VÀ KIỂM THỬ HỒI QUY — 20/10/2026
- **Đã làm được gì**: Rà các route public, ứng viên, nhà tuyển dụng, quản trị và trợ lý trên 360/390/555/1280/1440 px; ghi 5 lỗi UAT và sửa trong nhánh riêng. Kiểm tra backend mvnw verify 324/324, frontend Vitest 100/100, ESLint, production build, API E2E H2 28/28, Playwright 11/11; lưu 125 ảnh cùng log tại docs/qa/full-site-uat-2026-10-20/. Production smoke chỉ đọc xác nhận bản nền SHA 474b749...; các sửa UAT còn chờ PR/review/CD.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, AbortController và ReadableStream để giới hạn thời gian cả response body; TanStack Query cho trạng thái lỗi/thử lại; Vitest, ESLint, TypeScript/Vite, Playwright Chromium; Spring Boot/Maven với H2 cô lập; GET/OPTIONS smoke trên Render.
- **Mục đích chức năng / việc đã làm**: Giúp người dùng luôn thấy lỗi tạo tin ngay trong form, thoát trạng thái tải vô hạn, đăng xuất có giới hạn thời gian, và nhận thông báo đúng khi response đăng nhập bị timeout; xác nhận responsive và quyền cơ bản mà không ghi dữ liệu production.

## RE-REVIEW VÀ SỬA HỒI QUY UAT — 20/10/2026
- **Đã làm được gì**: Khắc phục hai P2 từ Bugbot: logout khi token hết hạn không còn chờ refresh trước khi UI chuyển trang; timeout body chatbot hiện thông báo chậm thay vì lỗi JSON. `MainLayout` và `WorkspaceLayoutFrame` điều hướng ngay khi logout; thao tác chờ lock tối đa 10.5 giây và mỗi request revoke tối đa 2.5 giây. Regression mới kiểm tra logout 401 + refresh treo và chatbot response body treo. Kiểm tra cuối tại mốc đó đạt frontend Vitest 102/102, ESLint, TypeScript/Vite build, API E2E H2 28/28, Playwright 12/12; lưu log, 125 ảnh hậu review và trace lỗi đã khắc phục trong `docs/qa/full-site-uat-2026-10-20/`. Production khi đó vẫn ở SHA nền `474b749...`; PR #48 chưa merge/deploy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React Router điều hướng logout không chờ mạng; `FetchTimeoutError`, Fetch API và `ReadableStream` giữ timeout tới khi body kết thúc; request auth dùng access/refresh token đã chụp trước khi xóa local; Vitest và Playwright trên Spring Boot/H2.
- **Mục đích chức năng / việc đã làm**: Đảm bảo người dùng thoát giao diện ngay khi chọn đăng xuất kể cả khi backend/refresh bị treo, đồng thời trợ lý giải thích rõ lỗi timeout khi máy chủ đã gửi headers nhưng body chưa về.

## FULL REGRESSION SAU SECURITY/BUGBOT REVIEW — 20/10/2026
- **Đã làm được gì**: Đóng race để refresh không khôi phục token sau logout và áp dụng deadline tuyệt đối cho request có retry. Chạy lại Maven `verify` 324/324, Vitest 104/104, ESLint, TypeScript/Vite build, API E2E H2 28/28 và Playwright 12/12; log/backend output được lưu trong `docs/qa/full-site-uat-2026-10-20/`. Production chưa được cập nhật: PR #48 còn chờ review/merge của trưởng nhóm theo gitflow.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, auth-mutation version, refresh-token snapshot, `FetchTimeoutError`, absolute deadline và thời gian còn lại cho HTTP/refresh/retry; Vitest, Playwright Chromium, Spring Boot/H2, Maven/JDK 21.
- **Mục đích chức năng / việc đã làm**: Tránh người dùng bị đăng nhập trở lại sau khi thoát và đảm bảo timeout chatbot/API phản ánh tổng thời gian chờ thay vì đặt lại ở từng chặng; full regression xác nhận các route/role và viewport trong môi trường dữ liệu cô lập.

## SỬA RACE THU HỒI PHIÊN KHI REFRESH TOKEN BỊ XOAY — 20/10/2026
- **Đã làm được gì**: Security Review tìm ra trường hợp logout thất bại nếu request khác vừa xoay refresh token. Frontend dùng Web Lock để chờ refresh và gửi cặp token mới nhất; backend xác minh chữ ký, thời hạn, loại và cùng session, còn refresh-only cần hash hiện hành. Token cũ không thể tự thu hồi phiên. Thêm kiểm thử hồi quy cho xoay token; E2E runner chọn JDK 21 theo môi trường và lưu log/ảnh. Kiểm thử tại mốc UAT-10: Maven `verify` 327/327, Vitest 104/104, ESLint/build đạt, API E2E 28/28, Playwright 12/12.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Spring Security `permitAll` cho endpoint logout tự xác thực token ở use case; `TokenProviderPort` xác minh JWT; repository thu hồi session theo session ID; Spring MockMvc/JUnit, Vitest và Playwright Chromium.
- **Mục đích chức năng / việc đã làm**: Thu hồi session ổn định dù refresh và logout đến server đồng thời; refresh token chỉ dùng để chấm dứt đúng session, không cấp quyền gọi API. Log và 125 ảnh mới lưu tại `docs/qa/full-site-uat-2026-10-20/runs/2026-10-20-015604/`. Bản sửa chưa deploy production; chờ Bugbot/Security re-review, CI và leader review/merge theo gitflow.

## FULL UAT VÀ KHẮC PHỤC — 20/10/2026
- **Đã làm được gì**: Hoàn tất UAT-01–11 trên local, thêm regression cho timeout, lỗi tạo tin, theo dõi ứng tuyển và đăng xuất/thu hồi phiên. Kiểm thử xác nhận: Maven verify 327/327 (backend không đổi sau lượt verify), API E2E H2 28/28, Vitest 111/111, Playwright 15/15 với ba vai trò và viewport 360/390/555/1280/1440 px; ESLint và production build đạt. Bugbot và Security Review đều không còn finding cần xử lý. Lưu test log, trace lỗi đã sửa và 125 ảnh QA dưới `docs/qa/full-site-uat-2026-10-20/`. Chưa ghi dữ liệu production; PR #48 còn cần CI mới và merge trước khi Render deploy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, Web Locks API, auth-mutation version và refresh-token snapshot; deadline tuyệt đối cho Fetch/body/retry; Spring Boot, JWT, session revocation qua JPA; Vitest, Playwright Chromium, Maven/JDK 21 và H2 cô lập.
- **Mục đích chức năng / việc đã làm**: Giữ luồng đăng nhập, trợ lý, tạo tin và lịch sử ứng tuyển có lỗi/timeout rõ ràng; đăng xuất đưa người dùng về trang đăng nhập ngay và thu hồi đúng phiên server trong giới hạn thời gian. Cải thiện khả năng tái kiểm thử nhờ log từng lệnh và bằng chứng theo viewport.

## SỬA PHẢN HỒI KHÔI PHỤC MẬT KHẨU — 20/10/2026
- **Đã làm được gì**: Bỏ nhãn “Bảo mật tài khoản”; sửa thông báo để không khẳng định email đã được gửi. Backend trả HTTP 503 với nội dung dễ hiểu khi gửi mail bị tắt, trước khi tra cứu tài khoản hoặc tạo token; frontend cũng có thông báo dự phòng cho HTTP 503. Khi kiểm thử qua `127.0.0.1`, phát hiện mặc định CORS chỉ cho `localhost`; bổ sung loopback IP trong cấu hình và fallback của `SecurityConfig` để giao diện tại `127.0.0.1:5173` nhận được phản hồi API. Kiểm thử liên quan sau cập nhật: Maven 9/9, Vitest 112/112; ESLint và Vite build đạt; trình duyệt nhận đúng HTTP 503 và thông báo tiếng Việt chính xác trên preview.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, Spring Boot `DomainException`/`GlobalExceptionHandler`, SMTP config `PASSWORD_RESET_MAIL_ENABLED`, CORS allowlist, JUnit/Mockito và MockMvc.
- **Mục đích chức năng / việc đã làm**: Tránh báo thành công giả khi mail reset password chưa được cấu hình, đồng thời tránh CORS chặn API trên địa chỉ loopback thường dùng; email thực chỉ gửi được sau khi bật cờ và cung cấp credential SMTP hợp lệ qua biến môi trường.

## QUÊN MẬT KHẨU BẰNG OTP EMAIL — 20/10/2026
- **Đã làm được gì**: Thay luồng khôi phục bằng email → gửi OTP 6 chữ số → xác minh → đặt mật khẩu mới. OTP hết hạn sau 15 phút, resend sau 60 giây, tối đa 5 lần nhập sai; mã đúng được đổi thành reset grant dùng một lần, hết hạn sau 5 phút. Thêm migration V4, API/DTO, giao diện trạng thái từng bước, cập nhật OpenAPI và tài liệu DB. Xác minh Maven `verify` 341/341, Vitest 115/115, ESLint và Vite production build đều đạt; `git diff --check` sạch.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Spring Boot, Spring Security rate limit, JPA/Flyway, `SecureRandom`, HMAC-SHA-256 cho OTP và token hash cho reset grant; React/TypeScript, Vitest, JUnit/MockMvc và H2.
- **Mục đích chức năng / việc đã làm**: Chỉ người dùng có email nhận được OTP hợp lệ mới được đặt mật khẩu mới; nhập sai có thể thử lại đến giới hạn, OTP không thể dùng trực tiếp để đổi mật khẩu và email không tồn tại không bị lộ qua phản hồi yêu cầu gửi mã. Gửi email thật cần bật `PASSWORD_RESET_MAIL_ENABLED=true` và cấu hình SMTP hợp lệ. Chưa redeploy production.

## NÚT HIỆN / ẨN MẬT KHẨU KHI KHÔI PHỤC — 21/10/2026
- **Đã làm được gì**: Thêm nút biểu tượng mắt cho ô mật khẩu mới và xác nhận mật khẩu ở bước đặt lại; hai trạng thái bật/tắt độc lập và được đặt lại sau khi đổi mật khẩu thành công. Vitest 115/115, ESLint và Vite production build đạt.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React `useState`, TypeScript, Lucide `Eye`/`EyeOff`, nhãn ARIA và focus-visible để hỗ trợ bàn phím.
- **Mục đích chức năng / việc đã làm**: Giúp người dùng kiểm tra chính xác mật khẩu mới và nội dung xác nhận trước khi lưu, đồng thời giữ nội dung che mặc định.

## BẬT CỜ GỬI OTP TRÊN LOCAL — 21/10/2026
- **Đã làm được gì**: Lưu biến môi trường Windows cấp user `PASSWORD_RESET_MAIL_ENABLED=true` và khởi động lại BE local cổng 8080 bằng JDK 21, profile `local`. Giữ nguyên file H2 hiện có; Flyway xác thực 4 migration và áp dụng V4 thành công. Smoke test trang `/forgot-password` trên FE 5180 trả HTTP 200; POST yêu cầu OTP không có email trả HTTP 400 như validation mong đợi. Không thay đổi Render/production.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Windows User environment kết hợp với Spring Boot externalized configuration trong `application.yml`.
- **Mục đích chức năng / việc đã làm**: Cho phép backend local nhận cờ gửi OTP mới và chạy schema OTP V4; email thực vẫn cần SMTP credentials `MAIL_USERNAME` và `MAIL_PASSWORD`, hiện chưa có trong Windows User environment.

## TRỢ LÝ QUẢN TRỊ VÀ PHÁT HÀNH MAIN — 21/10/2026
- **Đã làm được gì**: Thêm trang `/admin/assistant` trong khu vực Admin, điều hướng sidebar mở đúng trợ lý riêng, ẩn widget trợ lý thường trên mọi route `/admin/*`. Gỡ nhãn “Không gian làm việc” và nhãn vai trò khỏi header/sidebar admin; giữ layout recruiter mặc định. Trang tải số liệu thật từ API quản trị và có lối tắt tới màn quản lý; chat trả lời tổng quan/hàng chờ/API từ dữ liệu tải tại chỗ, câu hỏi quy trình dùng trợ lý RAG hiện có. Xác minh ESLint, Vitest 115/115, frontend production build và Maven verify 341/341 đạt. Docker daemon local không chạy; workflow CD trên main sẽ build và kiểm tra marker production của cả hai service.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, React Router lazy route, API `getAdminDashboardStats`/`getAdminApiMetrics`, xử lý thống kê xác định tại client và RAG knowledge base cho câu hỏi quy trình; pnpm 9, Vitest, ESLint, Vite, Maven/JDK 21.
- **Mục đích chức năng / việc đã làm**: Phân biệt trợ lý quản trị với trợ lý người dùng thường, hỗ trợ đọc số liệu/vận hành và mở nhanh tác vụ quản trị mà không tự thay đổi trạng thái tài khoản, doanh nghiệp hoặc tin tuyển dụng.

## SỬA ĐIỀU HƯỚNG TRỢ LÝ QUẢN TRỊ — 21/10/2026
- **Đã làm được gì**: Sửa mục Trợ lý ở thanh điều hướng chung để tài khoản ROLE_ADMIN mở `/admin/assistant` và hiển thị nhãn “Trợ lý quản trị”; thêm điều hướng dự phòng để admin truy cập trực tiếp `/assistant` cũng được chuyển sang trang riêng. Đã chạy kiểm tra lint, test và build frontend.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React Router `Navigate`, kiểm tra role `ROLE_ADMIN` từ phiên đăng nhập đang lưu, điều hướng theo vai trò trong React.
- **Mục đích chức năng / việc đã làm**: Không để quản trị viên vô tình vào trợ lý user khi chọn mục Trợ lý ở header hoặc mở URL `/assistant` trực tiếp.

## ẨN THAO TÁC TÀI KHOẢN TRONG KHU VỰC QUẢN TRỊ — 21/10/2026
- **Đã làm được gì**: Ẩn hai nút đổi mật khẩu và đăng xuất ở khu vực tài khoản của layout Admin; các layout dùng chung khác vẫn giữ nút và hộp thoại đổi mật khẩu. Đã xác minh bằng lint, test và build frontend.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript; thuộc tính tùy chọn `showAccountActions` trên `WorkspaceLayoutFrame`, được bật mặc định và tắt riêng từ `AdminLayout`.
- **Mục đích chức năng / việc đã làm**: Gọn khu vực tài khoản trong trang quản trị theo yêu cầu mà không thay đổi thao tác ở recruiter hoặc layout khác.

## SỬA PHẢN HỒI LỖI PHÊ DUYỆT DOANH NGHIỆP — 21/10/2026
- **Đã làm được gì**: Đưa lỗi API phê duyệt vào ngay modal xác nhận thay vì chỉ đặt thông báo phía sau lớp phủ; lỗi giữ modal mở để người quản trị sửa nguyên nhân và thử lại. Xóa lỗi cũ khi mở modal/thử lại, hiển thị trạng thái đang gửi rõ ràng, và lấy tên doanh nghiệp từ đúng request khi báo thành công. Thêm Playwright regression mô phỏng HTTP 409 rồi HTTP 200. Vitest 115/115, ESLint, Vite production build đạt; API E2E H2 28/28 và Playwright 16/16 đạt. Không thay đổi dữ liệu production; chưa merge/redeploy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, TanStack Query mutation variables, ARIA dialog/alert, Lucide LoaderCircle, Playwright Chromium, Spring Boot với H2 cô lập; endpoint phê duyệt tiếp tục dùng `TransactionalUpdateCompanyStatusUseCase` để rollback khi lỗi nghiệp vụ.
- **Mục đích chức năng / việc đã làm**: Giúp admin nhìn thấy chính xác lý do API từ chối duyệt ngay tại thao tác, có thể thử lại sau khi khắc phục; xác nhận không nhầm lỗi nghiệp vụ thành trạng thái thành công.

## HOÀN THIỆN KHÔNG GIAN QUẢN TRỊ, XUẤT BÁO CÁO VÀ ĐÓNG HRPM-58/59 — 22/10/2026
- **Đã làm được gì**:
  - Thiết kế lại khu vực quản trị với header ngang co giãn theo màn hình, menu Cài đặt gom đổi mật khẩu/đăng xuất, bỏ nhãn “Phiên làm việc an toàn”; căn chỉnh lại nút, trạng thái và bảng doanh nghiệp để tránh nhãn bị vỡ dòng. Sửa lỗi lớp phủ chặn nút Cài đặt khi đóng menu.
  - Mở rộng trang Trợ lý quản trị và thêm xuất biểu đồ PNG, workbook Excel theo tổng quan/endpoint/bucket 5 phút, cùng báo cáo Word từ số liệu API đã xác thực. Báo cáo nêu nguồn và thời điểm, không tạo số liệu suy diễn.
  - Cải thiện phê duyệt doanh nghiệp: thời hạn chờ 90 giây, đối chiếu lại trạng thái server sau lỗi phản hồi không rõ để thông báo đúng kết quả và không bắt duyệt lại khi server đã lưu thành công.
  - Theo xác nhận thống nhất của nhóm, chuyển HRPM-58 và HRPM-59 sang Done; đối chiếu đủ 13 issue của Sprint 2 (HRPM-8, 50, 55–59, 62–67) và tất cả đều Done. Tài liệu HRPM-58 vẫn ghi rõ benchmark tổng hợp không đại diện độ trễ HTTP hay dữ liệu thực; không đo một RAG matcher độc lập.
  - Xác minh backend `mvnw -B verify` đạt 341/341 trên JDK 21; frontend Vitest 122/122, ESLint, Vite production build và `pnpm audit --prod` (không phát hiện lỗ hổng) đều đạt; API E2E H2 28/28 và Playwright 18/18 đạt. Lần E2E đầu phát hiện lỗi lớp phủ menu, đã sửa và chạy lại toàn bộ thành công.
  - Lưu log kiểm thử và ảnh giao diện tại `docs/qa/full-site-uat-2026-10-20/runs/2026-10-22-003037/`. PR [#54](https://github.com/leduykhanhdevs/TalentBridge_JS2/pull/54) được mở vào `dev` theo quy tắc repo. Tại thời điểm kiểm tra, `dev` còn sau `main`, PR ở trạng thái conflict và GitHub chưa tạo CI checks; Dockerfile verification local cũng chưa chạy vì Docker daemon không sẵn sàng. Chưa merge hoặc redeploy production.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, Tailwind, React Router, TanStack Query; SVG raster hóa bằng Canvas cho PNG, OOXML workbook tạo bằng `fflate`, tài liệu DOCX tạo bằng `docx`; dữ liệu dashboard/metrics từ API `ROLE_ADMIN`; Java 21, Spring Boot, Maven, JUnit/MockMvc, H2, Vitest, Playwright Chromium. Hướng sản phẩm đã thống nhất: Tri-Vector cho matching, MCP cho thao tác ATS có phân quyền và RAG cho trợ lý kiến thức.
- **Mục đích chức năng / việc đã làm**: Làm cho quản trị viên thao tác nhất quán, nhìn rõ kết quả duyệt và xuất được báo cáo từ dữ liệu hệ thống; hoàn tất các yêu cầu triển khai API metrics và quyết định kiến trúc AI đã thống nhất, đồng thời lưu bằng chứng kiểm thử để review.

## UAT FOLLOW-UP: OTP EMAIL, AUTH NAVIGATION VÀ TRỢ LÝ QUẢN TRỊ — 22/10/2026
- **Đã làm được gì**: Đối chiếu Render backend logs và xác định OTP thất bại khi kết nối `smtp.gmail.com:587` bị timeout; đặt giới hạn SMTP 10 giây và sửa phản hồi UI để không tuyên bố email đã gửi khi chưa có bằng chứng gửi thành công. Ẩn điều hướng Home/Jobs/Assistant trên login/register/forgot/reset password. Sửa ưu tiên nhận diện thống kê API trong trợ lý quản trị và phân biệt từ “ảnh” với đại từ “Anh”. Kiểm thử đạt: Maven 341/341, Vitest 127/127, API E2E H2 28/28, Playwright 20/20, ESLint, Vite production build và `pnpm audit --prod` đều pass. Ghi rõ email thật vẫn không gửi được từ Render Free vì chính sách chặn SMTP; xem chẩn đoán ở `docs/qa/full-site-uat-2026-10-20/runs/2026-10-22-otp-nav-followup/production-email-diagnosis.md`.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, React Router, Spring Boot SMTP properties, timeout kết nối/đọc/ghi 10 giây, Vitest, Playwright Chromium, Maven/JDK 21, H2 E2E; kiểm tra chính sách Render Free tại [Render Free web services](https://render.com/docs/free) và [Render SMTP restriction notice](https://render.com/changelog/free-web-services-will-no-longer-allow-outbound-traffic-to-smtp-ports).
- **Mục đích chức năng / việc đã làm**: Tránh thông báo sai về việc gửi OTP, giúp người dùng nhìn thấy lỗi backend kịp thời, tập trung giao diện auth và bảo đảm yêu cầu thống kê API được xử lý đúng. Các sửa đổi mới còn ở PR #54; chưa xác minh CI/CD hay ghi nhận redeploy. Việc gửi mail cần dịch vụ Render cho phép SMTP hoặc nhà cung cấp email qua HTTPS với khóa do chủ dự án cấu hình.

## SỬA FINDINGS CUỐI BUGBOT/SECURITY CHO PR #54 — 22/10/2026
- **Đã làm được gì**: Sửa ba lỗi review: câu hỏi `password` không còn bị nhận nhầm thành xuất Word; trường email OTP bị khóa khi request pending và bước xác minh giữ đúng email đã gửi; reset-grant dạng Base64URL được privacy guard chặn trước khi gửi sang Gemini. Thêm kiểm thử hồi quy và chạy lại: Maven `verify` 341/341, Vitest 20 files / 128 tests, API E2E H2 28/28, Playwright 21/21, ESLint và Vite production build đều đạt. Playwright xác nhận menu điều hướng không còn trên `/forgot-password`. Log đầy đủ tại `docs/qa/full-site-uat-2026-10-20/runs/2026-10-22-012713/`.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: TypeScript whole-term matching bằng Unicode letter/number boundaries; React/TanStack Query mutation variables cho snapshot email OTP; Java regex phát hiện token 32 byte dạng Base64URL dài 43 ký tự; Vitest, JUnit, Spring Boot/H2 và Playwright Chromium.
- **Mục đích chức năng / việc đã làm**: Ngăn chatbot xuất tài liệu sai ý định hoặc tiết lộ grant reset cho Gemini; giữ luồng OTP nhất quán khi request bất đồng bộ; bảo đảm UI auth tập trung. PR #54 chưa có các sửa đổi mới trên remote và chưa deploy production. Render Free vẫn chặn SMTP Gmail; cần gói cho phép SMTP hoặc tích hợp API email HTTPS có secret do chủ dự án cấu hình.

## XÁC NHẬN LẠI FULL UAT VÀ REGRESSION — 22/10/2026
- **Đã làm được gì**: Xác định test Playwright lỗi lần đầu do Chromium báo `ERR_NO_BUFFER_SPACE` khi tải `src/main.tsx`; giữ lại trace và log thất bại, sau đó chạy lại toàn bộ E2E trên backend H2 cô lập. Kết quả xác nhận: Maven `verify` 347/347, API E2E 28/28, Playwright 21/21, Vitest 20 files / 128 tests, ESLint và Vite production build đều đạt; `git diff --check` sạch. Log cuối ở `docs/qa/full-site-uat-2026-10-20/runs/2026-10-22-015549/`, Maven verify ở `runs/2026-10-22-015000/`.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Java 21, Spring Boot, Maven/JUnit, H2; React/TypeScript, Vitest, ESLint, Vite; Playwright Chromium chạy tuần tự với API E2E và ảnh viewport. Rate limiting tin cậy `CF-Connecting-IP` do Cloudflare edge của Render ghi đè và fallback về socket address; request OTP bị từ chối do queue đầy trả HTTP 503 chung trước khi tra cứu tài khoản.
- **Mục đích chức năng / việc đã làm**: Xác nhận những lỗi auth, OTP, trợ lý quản trị và luồng nhà tuyển dụng đã sửa không tái phát trong regression. Tất cả dữ liệu ghi trong E2E dùng H2/fixture; không gửi mail OTP thật hay ghi dữ liệu production. PR #54, CI mới và release/CD vẫn là các cổng cần hoàn tất trước khi ghi nhận redeploy; Render Free vẫn chặn SMTP Gmail.
## XỬ LÝ TIMEOUT ĐĂNG NHẬP KHI RENDER FREE COLD START — 22/10/2026
- **Đã làm được gì**: Luồng đăng nhập chờ warm-up backend, warm-up thử lại khi service còn đang khởi động, cache readiness hết hạn sau 12 phút và timeout riêng cho POST login là 30 giây. Thêm thông báo “Đang kết nối máy chủ” cùng hướng dẫn cold start. Xác minh trên nền `dev`: Vitest 130/130, ESLint và Vite production build đạt; cả ba job CI frontend/backend/Docker của PR đều pass. Production smoke chỉ đọc: frontend đang gọi đúng backend; `/api/v1/health` HTTP 200 (~0,5 giây), truy vấn jobs đầu tiên HTTP 200 (~4,6 giây), lần tiếp theo ~0,9 giây. Chưa gửi login production, chưa merge hoặc redeploy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, `fetchWithTimeout`, warm-up polling có retry/backoff nhẹ, cache TTL, Vitest fake timers. Tài liệu Render nêu Free web services ngủ sau 15 phút idle và khởi động lại khoảng một phút: https://render.com/docs/free.
- **Mục đích chức năng / việc đã làm**: Tránh hủy yêu cầu đăng nhập sau 10 giây trong lúc Render Free đánh thức backend; làm rõ với người dùng vì sao lần đầu có thể chờ lâu và vẫn trả lỗi có nghĩa nếu backend không sẵn sàng trong 70 giây.

## PHÁT HÀNH XỬ LÝ COLD START ĐĂNG NHẬP LÊN RENDER — 22/10/2026
- **Đã làm được gì**: PR phát hành [#57](https://github.com/leduykhanhdevs/TalentBridge_JS2/pull/57) đã merge vào `main`; GitHub Actions CD run [39788712825](https://github.com/leduykhanhdevs/TalentBridge_JS2/actions/runs/39788712825) hoàn tất thành công. Backend và frontend Render đều xác nhận phục vụ commit `ce8caff08202e9065f5fa211840e789ebf5d7df4`. Production smoke checks đạt: health API, jobs API, tiêu đề trang và CORS credentials. Không gửi thông tin đăng nhập hay OTP thật trong production.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: GitHub Actions CD, Render Deploy Hooks riêng cho backend/frontend, xác minh SHA qua health JSON và thẻ meta HTML; smoke test HTTP chỉ đọc cho Spring Boot API và React/Vite frontend.
- **Mục đích chức năng / việc đã làm**: Phát hành xử lý chờ backend khởi động khi Render Free cold start, đồng thời xác minh hai dịch vụ production cùng chạy đúng một commit trước khi kết luận deploy hoàn tất.

## THIẾT KẾ LẠI HEADER USER THEO MẪU ADMIN — 22/10/2026
- **Đã làm được gì**: Chuyển điều hướng khu vực người dùng thành thanh ngang theo `AdminLayout`, hiện trên mọi kích thước và có thể cuộn ngang trên điện thoại. Thêm menu “Cài đặt” cạnh tài khoản; chuyển “Đổi mật khẩu” và “Đăng xuất” vào menu, bỏ các nút thao tác đứng riêng. Giữ nguyên liên kết ứng viên/nhà tuyển dụng/quản trị theo vai trò. Thêm Playwright smoke test desktop/mobile; ảnh kiểm thử tại `docs/qa/header-settings-2026-10-22/screenshots/`. Log tích hợp tại `docs/qa/full-site-uat-2026-10-20/runs/2026-10-22-210108/`.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React + TypeScript (`MainLayout`), React Router `NavLink`, Tailwind responsive flex/overflow, Lucide icons, menu ARIA và Playwright Chromium.
- **Mục đích chức năng / việc đã làm**: Đồng bộ header người dùng với mẫu điều hướng ngang của admin; gom các thao tác tài khoản vào một nơi và giữ điều hướng dùng được trên màn hình nhỏ. Đã kiểm thử Vitest 130/130, ESLint, Vite production build, API E2E H2 28/28 và Playwright 23/23 (bao gồm desktop/mobile). Chưa deploy production.
## KHẮC PHỤC CÁC FINDING UAT TRONG BUG.DOCX — 22/10/2026
- **Đã làm được gì**: Xử lý bốn finding trong tài liệu test: (1) thêm cấu hình provider gửi OTP qua Google Apps Script HTTPS relay để dùng được Gmail cá nhân trên Render Free bị chặn SMTP; (2) căn chiều cao workspace Recruiter theo header/footer để bỏ khoảng trống khi cuộn; (3) liên kết Recruiter với công ty ngay khi gửi yêu cầu tạo công ty, tự sửa các yêu cầu PENDING cũ chưa liên kết và chặn tham gia công ty khác trong lúc đang chờ; (4) approval kiểm tra quan hệ công ty hiện có trước khi lưu, lỗi xung đột trả thông báo có hướng xử lý thay vì modal quay im lặng. Từ chối yêu cầu sẽ gỡ liên kết pending để người dùng có thể gửi lại. Xác minh: Maven `verify` 365/365; API E2E H2 28/28; Playwright 21/21; Vitest 130/130; ESLint và Vite production build đạt. Log Maven, frontend, API E2E, Playwright và ảnh tại `docs/qa/full-site-uat-2026-10-20/runs/2026-10-22-213718/`.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Spring Boot transaction wrappers bảo đảm lưu request và quan hệ Recruiter–Company cùng giao dịch; Spring Data JPA query để tìm pending request cũ; React/Tailwind responsive layout; Google Apps Script `MailApp` qua HTTPS với shared secret chỉ lưu backend/Script Properties; H2, JUnit, Vitest, ESLint, Vite và Playwright Chromium.
- **Mục đích chức năng / việc đã làm**: Giữ trạng thái đăng ký doanh nghiệp nhất quán, tránh recruiter bị mất quyền/ghép nhầm công ty và giúp admin biết chính xác vì sao một approval không thể hoàn tất; đóng thông báo lỗi OTP khi nhà cung cấp không hoạt động mà không báo gửi giả. Email Gmail thật chưa được xác minh vì chưa có URL Web App và secret thật trong môi trường Render; cấu hình theo `docs/operations/otp-mail/SETUP.md`. Bản sửa chưa merge hoặc deploy. Nếu dữ liệu cũ đang liên kết recruiter với một công ty khác, cần admin quyết định chuyển liên kết thay vì hệ thống tự đổi công ty.

## CHẠY LOCAL BẢN SỬA UAT — 22/10/2026
- **Đã làm được gì**: Khởi động backend và frontend trên worktree `fix/khanh/bugdocx-uat-fixes`; backend `/api/v1/health` trả HTTP 200, frontend `http://localhost:5173/` trả HTTP 200. Backend dùng profile `local`, H2 file database và đã áp dụng 4 migration. Mở giao diện trong browser Codex; không thao tác hay ghi dữ liệu production.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Java 21, Spring Boot 3.3.4, Maven, H2, Flyway; Node.js, pnpm và Vite 8; kiểm tra health bằng HTTP và frontend bằng HTTP loopback.
- **Mục đích chức năng / việc đã làm**: Cho phép xem và thử trực tiếp bản sửa các finding trong `bug.docx` trên máy local. Backend chạy tại `http://127.0.0.1:8080`; frontend tại `http://localhost:5173/`. Dữ liệu thử nghiệm thuộc H2 local; việc gửi email thật không được kiểm thử.

## CHUẨN BỊ PHÁT HÀNH DEV LÊN MAIN — 22/10/2026
- **Đã làm được gì**: Tạo nhánh release từ `main`, nhập `dev` vào để chuẩn bị PR phát hành và giải quyết conflict trong `docs/PROGRESS_REPORT.md`, giữ lại các ghi nhận UAT, header và chạy local. `git diff --check` đạt; chưa thay đổi `main` ở bước này.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Git merge theo GitFlow; CI GitHub Actions gồm Maven/JDK 21, pnpm/Vitest/ESLint/Vite và Docker build; CD Render theo workflow chỉ chạy trên `main`.
- **Mục đích chức năng / việc đã làm**: Chuẩn bị đưa các thay đổi đã tích hợp trên `dev` lên `main` qua PR, bảo toàn lịch sử kiểm thử và tài liệu; deployment production chỉ được xác nhận sau khi CD và health checks hoàn tất.

## KHÔI PHỤC SIDEBAR DỌC TRÊN MAIN LOCAL — 23/10/2026
- **Đã làm được gì**: Xác định sidebar dọc chỉ còn trong thay đổi chưa commit của worktree cũ, nên khi chạy commit `main` sau PR #64 giao diện quay về header ngang. Chuyển `AdminLayout` và các trang người dùng đã đăng nhập sang `WorkspaceLayoutFrame`; giữ header công khai cho đăng nhập/đăng ký/khôi phục mật khẩu; đưa đổi mật khẩu và đăng xuất vào Cài đặt dưới sidebar, bỏ liên kết “Về TalentBridge” bị trùng điều hướng. Giữ chiều rộng nội dung hiện có trên `main`. Kiểm thử: Vitest 134/134, Playwright sidebar 3/3 (admin desktop/mobile, ứng viên desktop/mobile), ESLint, Vite build, HTTP local 200 và `git diff --check` đều đạt. Chỉ cập nhật local, chưa commit/push/deploy.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, React Router `NavLink` với `end` cho trạng thái active chính xác, Tailwind responsive sidebar/mobile drawer, Lucide và Playwright Chromium.
- **Mục đích chức năng / việc đã làm**: Khôi phục mẫu sidebar dọc cho admin và người dùng, giữ các thao tác tài khoản trong sidebar và tránh nhầm giao diện local với những thay đổi đã thực sự được merge.

## ĐƯA CÀI ĐẶT THÀNH MỤC ĐIỀU HƯỚNG SIDEBAR — 23/10/2026
- **Đã làm được gì**: Chuyển Cài đặt thành hàng điều hướng không viền, cùng kiểu với các mục sidebar và đặt trong danh sách điều hướng. Khi mở, Đổi mật khẩu và Đăng xuất hiện thụt vào ngay bên dưới; hồ sơ tài khoản vẫn ở cuối sidebar. Kiểm tra Playwright 3/3, Vitest 134/134, ESLint và Vite build đều đạt.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React state cho menu mở/đóng, thuộc tính ARIA `aria-expanded`/`aria-controls`/`role="menu"`, Tailwind dùng chung kiểu với `NavLink` và Playwright kiểm tra menu admin/user trên desktop/mobile.
- **Mục đích chức năng / việc đã làm**: Làm Cài đặt trông và hoạt động như một mục sidebar thực sự, thay vì nút hộp riêng ở khu vực tên tài khoản.

## SỬA CHIỀU CAO SIDEBAR VÀ CỠ CHỮ CÀI ĐẶT — 23/10/2026
- **Đã làm được gì**: Kéo sidebar desktop xuống hết chiều cao viewport bằng chiều cao `100dvh - 84px`; thêm kiểm thử xác nhận đáy sidebar chạm đáy viewport. Sửa CSS reset `font: inherit` trên button vốn ghi đè cỡ chữ và độ đậm Tailwind; dùng chung kiểu chữ với các liên kết sidebar và kiểm tra computed style. Vitest 134/134, API E2E 28/28, Playwright 23/23, ESLint, Vite build và `git diff --check` đạt. Xác nhận parser CV vẫn ở chế độ quy tắc vì `GEMINI_API_KEY` không có trong môi trường backend local; Gemini cần key do chủ dự án cung cấp rồi khởi động lại backend.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: React/TypeScript, Tailwind CSS v4 cascade layers, CSS dynamic viewport unit `dvh`, Playwright đo hình học và computed typography, Vitest, Spring Boot đọc `GEMINI_API_KEY` qua cấu hình môi trường.
- **Mục đích chức năng / việc đã làm**: Loại bỏ dải trống dưới sidebar và làm chữ Cài đặt bằng đúng kích thước/độ đậm như các mục còn lại; xác định rõ lý do tính năng phân tích Gemini chưa bật ở local mà không ghi lộ API key.

## KHẮC PHỤC UAT CANDIDATE VÀ RECRUITER — 23/10/2026
- **Đã làm được gì**: Hoàn tất 9 hạng mục trong `docs/qa/recruiter-candidate-uat-2026-10-23/PLAN.md`: avatar recruiter bỏ chấm trang trí và hỗ trợ URL/chọn/kéo-thả ảnh; đánh giá mức phù hợp việc làm có bằng chứng và giới hạn rõ; trợ lý recruiter có công cụ ATS lọc/thống kê/xuất CSV; mở CV bằng endpoint có Bearer auth và quyền theo công ty/đơn ứng tuyển, kèm preview PDF và link tải dự phòng; lịch phỏng vấn có API `.ics` xác thực/phân quyền, múi giờ Việt Nam và giữ đúng giờ `datetime-local`; thống nhất kiểu input; phân tách cache yêu cầu gia nhập theo tài khoản; chặn tuyển dụng khi chưa liên kết công ty và điều hướng tới `/recruiter/company`. Local verification: Maven verify 378/378; Vitest 135/135; ESLint; TypeScript/Vite production build; API E2E 28/28; Playwright 30/30; `git diff --check` đạt. PR #65 đã merge bằng commit `5132b0b28da8a82ce52b8da0d33e727e2e6d381c`; GitHub CI pass cả backend/frontend/Docker. Render CD run `39837408472` pass: backend status `UP`, frontend/backend cùng build SHA `5132b0b28da8a82ce52b8da0d33e727e2e6d381c`, jobs API 200, frontend 200 và CORS credentialed 200. Log và ảnh của lần E2E pass cuối nằm tại `docs/qa/full-site-uat-2026-10-20/runs/2026-10-23-015610/`.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Java 21, Spring Boot, Maven, H2; React/TypeScript, TanStack Query, Playwright Chromium, Vitest, ESLint và Vite; matching xác định dựa trên ba vector bằng chứng (từ khóa trực tiếp, ngữ cảnh, năng lực suy luận); ICS theo RFC 5545 và `Asia/Ho_Chi_Minh`; upload ảnh qua storage hiện có và CSV tạo tại client. Báo cáo AI-KM (SoftwareX 2025, DOI `10.1016/j.softx.2025.102349`) được dùng làm bối cảnh RAG/workflow; không coi đó là bằng chứng Gemini đã được gọi. Tài liệu: https://www.sciencedirect.com/science/article/pii/S2352711025003152.
- **Mục đích chức năng / việc đã làm**: Sửa các luồng recruiter/candidate bị lỗi hoặc thiếu, khóa dữ liệu nhạy cảm theo phiên và quyền sở hữu, đồng thời giữ minh bạch giữa phân tích xác định và AI provider thật. Gemini và khả năng kết nối Google Calendar ngoài ứng dụng chưa được xác minh live; lịch `.ics` là đường nhập lịch có xác thực. Mã đã merge và deploy với SHA nêu trên; smoke test production đã đạt.

## KIỂM TRA PHÁT HÀNH, ĐỐI CHIẾU JIRA VÀ DEMO LUỒNG CHÍNH — 23/10/2026
- **Đã làm được gì**: Chạy lại giao diện local bằng Playwright trên H2 cô lập: 30/30 pass, bao gồm tự đăng ký Candidate, đăng xuất và đăng nhập lại; lượt hậu kiểm sau sửa nguồn AI đạt 29/29 (chỉ bỏ qua test đăng ký đã đạt ở lượt trước). Backend Maven 379/379; frontend Vitest 135/135, ESLint, TypeScript/Vite build và API E2E 28/28 pass. PR #67 đã merge vào `main` bằng `d33d5ddf50e3681dd29d372dfc8948ac2a0680d9`; CI backend/frontend/Docker pass, Render CD `39847644086` pass. Live FE marker và BE `buildCommit` trùng SHA; health, jobs API, title và credentialed CORS đều HTTP 200. Jira: Sprint 0 22/22 Done, Sprint 1 20/20 Done, Sprint 2 13/16 Done; tổng 53/56 issue khác nhau Done. Đưa HRPM-68/69/70 vào Sprint 2; HRPM-25 còn In Review, 5 Epic container còn To Do và HRPM-58 thiếu bằng chứng nhóm thống nhất. Cập nhật kiểm kê AI/MCP/RAG, kết quả Jira và hướng dẫn demo tại `docs/AI_INTEGRATION_STATUS.md`, `docs/JIRA_SPRINT_AUDIT.md`, `docs/DEMO_MAIN_FLOW.md`.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Java 21/Spring Boot/Maven/H2; React/TypeScript, Vitest, Playwright Chromium, ESLint/Vite; GitHub Actions CI/CD và Render deploy hooks/SHA smoke checks; đánh giá phù hợp Tri-Vector có giải thích (60% bằng chứng trực tiếp, 25% ngữ cảnh, 15% năng lực liên quan suy luận); RAG truy xuất knowledge base bằng từ khóa/synonym/trigram; MCP Streamable HTTP với 5 tool có JWT/RBAC và yêu cầu xác nhận thao tác ghi. Đối chiếu với Wen et al., *AI–KM: Knowledge enhancement with RAG and workflow*, SoftwareX 31 (2025), DOI `10.1016/j.softx.2025.102349`, MCP Tools specification và Gemini GenerateContent API.
- **Mục đích chức năng / việc đã làm**: Xác nhận các lỗi Candidate/Recruiter được kiểm thử qua giao diện local và mã FE/BE tương ứng đã được phát hành lên Render; ghi rõ Jira còn 3 việc AI chưa hoàn tất. Phân biệt thành phần AI thực có với khả năng chưa chứng minh: Gemini live và kết nối Claude/agent MCP ngoài hệ thống chưa được xác minh; chatbot chưa tự gọi MCP; điểm matching là tư vấn xác định, không phải xác suất tuyển dụng. Tài liệu `docs/DEMO_MAIN_FLOW.md` mô tả luồng Candidate → Recruiter → Admin cùng kỹ thuật JWT/RBAC, ports/adapters, AI, ATS và giới hạn demo.

## OTP EMAIL-ONLY, PASSWORD RESET RELAY VÀ RELEASE CHECK — 26/10/2026
- **Đã làm được gì**: Luồng ứng tuyển chỉ yêu cầu xác minh email; bỏ yêu cầu Zalo OTP ở giao diện và backend, cho phép ứng viên chọn CV chính sau khi xác minh. Bổ sung binding cấu hình URL/shared secret cho adapter đặt lại mật khẩu qua Google Apps Script, dùng chung relay Gmail đã được kiểm tra bằng email tổng hợp. Kiểm thử local: Maven verify 393/393; Vitest 138/138; ESLint; Vite production build; API E2E H2 28/28; Playwright H2 34/34, gồm tự đăng ký, xác minh email, ứng tuyển bằng CV chính; `git diff --check` đạt. Bằng chứng Playwright/API/log: `docs/qa/full-site-uat-2026-10-20/runs/2026-10-26-000110/`. Gửi OTP thật qua app production sẽ được xác minh sau khi PR #70 qua CI và deploy; email tổng hợp chỉ chứng minh relay Gmail.
- **Sử dụng công nghệ / thuật toán gì / kết hợp với gì**: Java 21, Spring Boot, Maven, JUnit/MockMvc, H2; React/TypeScript, Vitest, ESLint, Vite, Playwright Chromium; Google Apps Script Web App gọi Gmail bằng URL và shared secret lấy từ biến môi trường Render. OTP dùng xác minh một lần; không yêu cầu SMS/Zalo trong luồng ứng tuyển.
- **Mục đích chức năng / việc đã làm**: Cho ứng viên hoàn tất xác minh bằng email trong môi trường Render không gửi SMTP trực tiếp, đồng thời dùng cùng email relay cho quên mật khẩu; giữ rõ ranh giới giữa kiểm thử UI giả lập, email relay tổng hợp và kiểm tra OTP thật trên production.
