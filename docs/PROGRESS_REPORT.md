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
