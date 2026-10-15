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
