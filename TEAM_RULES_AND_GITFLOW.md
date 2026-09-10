# Bộ Quy Tắc Làm Việc Nhóm & Quy Trình Git Flow: TalentBridge

> **Dự án**: TalentBridge – Nền tảng tuyển dụng trực tuyến (Java Spring 2)  
> **Trưởng nhóm (Team Leader / Reviewer)**: **Lê Duy Khánh**  
> **Các thành viên**: Trần Đình Tình, Nguyễn Phan Minh Hiếu, Đặng Trường Thịnh, Phan Thị Ánh Tuyền  
> **GitHub Repository**: [https://github.com/leduykhanhdevs/TalentBridge_JS2.git](https://github.com/leduykhanhdevs/TalentBridge_JS2.git)

---

## 1. Nguyên Tắc Cốt Lõi Về Phân Nhánh Git (Strict Git Branching Rules)

### ⚠️ QUY ĐỊNH BẮT BUỘC 100% THÀNH VIÊN PHẢI TUÂN THỦ:
1. **TUYỆT ĐỐI KHÔNG PUSH / COMMIT TRỰC TIẾP LÊN NHÁNH `main`**.
2. **KHÔNG TỰ Ý MERGE BẤT KỲ PULL REQUEST NÀO VÀO `main` HOẶC `dev`**.
3. **MỌI CODE MỚI PHẢI ĐƯỢC TẠO TRÊN NHÁNH RIÊNG (`feat/...`, `fix/...`) VÀ PHẢI TẠO PULL REQUEST (PR)**.
4. **DUY NHẤT TRƯỞNG NHÓM (LÊ DUY KHÁNH) ĐƯỢC QUYỀN DUYỆT (APPROVE) VÀ MERGE VÀO `main` / `dev`**.

---

## 2. Cấu Trúc Nhánh Trên GitHub

- `main`: Nhánh ổn định cao nhất (Production-ready). Chỉ chứa code đã được Trưởng nhóm kiểm thử và xác nhận chạy không lỗi. Dùng để nộp bài và demo với giảng viên.
- `dev`: Nhánh tích hợp (Development branch). Nơi hội tụ các tính năng từ các thành viên sau khi Trưởng nhóm review và merge.
- **Nhánh tính năng cá nhân (Feature branches)**: Đặt tên theo chuẩn:
  - `feat/<ten-thanh-vien>/<tinh-nang>`  
    *Ví dụ*:  
    - `feat/tinh/job-crud` (Trần Đình Tình làm CRUD Job)
    - `feat/hieu/company-ats-pipeline` (Nguyễn Phan Minh Hiếu làm ATS Pipeline)
    - `feat/thinh/candidate-profile-apply` (Đặng Trường Thịnh làm Profile & Apply)
    - `feat/tuyen/interview-notification` (Phan Thị Ánh Tuyền làm Lịch PV & Email)
    - `feat/khanh/auth-jwt-admin` (Lê Duy Khánh làm Security & Admin)
  - `fix/<ten-thanh-vien>/<ten-loi>` (Sửa lỗi phát sinh)
  - `docs/<ten-thanh-vien>/<tai-lieu>` (Cập nhật tài liệu)

---

## 3. Hướng Dẫn Thao Tác Git Từng Bước Cho Thành Viên

### Bước 1: Clone repo về máy (làm lần đầu)
```bash
git clone https://github.com/leduykhanhdevs/TalentBridge_JS2.git
cd TalentBridge_JS2
```

### Bước 2: Cập nhật code mới nhất từ nhánh `dev` trước khi làm việc
Mỗi ngày trước khi bắt đầu code, luôn kéo code mới nhất về:
```bash
git checkout dev
git pull origin dev
```

### Bước 3: Tạo nhánh mới cho tính năng được giao
```bash
# Ví dụ: Thành viên Tình tạo nhánh làm chức năng tìm kiếm việc làm
git checkout -b feat/tinh/job-search
```

### Bước 4: Viết code và commit đúng quy chuẩn (Conventional Commits)
Cú pháp commit:
`<type>(<scope>): <mô tả việc đã làm>`

- Ví dụ chuẩn:
  - `feat(job): add advanced search filter by salary and city`
  - `fix(auth): fix password encoding issue with bcrypt`
  - `docs(db): add relationship details in table schema`
  - `test(candidate): add unit test for apply job service`

```bash
git add .
git commit -m "feat(job): implement filter jobs by category and salary range"
```

### Bước 5: Chạy thử local và kiểm tra trước khi đẩy code
- Đảm bảo ứng dụng chạy thành công, không có lỗi biên dịch (build error).
- Không commit các file rác như `.idea/`, `target/`, `.vscode/`, `.env` chứa mật khẩu cá nhân.

### Bước 6: Đẩy nhánh cá nhân lên GitHub
```bash
git push -u origin feat/tinh/job-search
```

### Bước 7: Mở Pull Request (PR) & Báo Trưởng nhóm Review
1. Vào đường link GitHub repo: [https://github.com/leduykhanhdevs/TalentBridge_JS2.git](https://github.com/leduykhanhdevs/TalentBridge_JS2.git)
2. Bấm **Compare & pull request**.
3. **Base branch**: Chọn `dev`.
4. **Compare branch**: Nhánh cá nhân của bạn (ví dụ `feat/tinh/job-search`).
5. Đặt tiêu đề PR rõ ràng, mô tả tóm tắt những gì đã làm, ảnh chụp màn hình chạy thử nghiệm (nếu có).
6. **Reviewer**: Tag chọn **@leduykhanhdevs** (Lê Duy Khánh).
7. Nhắn tin vào nhóm chat: *"Khánh ơi, mình vừa tạo PR cho module [Tên module], nhờ Khánh review giúp nhé!"*

---

## 4. Quy Trình Kiểm Duyệt Của Trưởng Nhóm (Lê Duy Khánh)

Khi nhận được Pull Request từ thành viên, Trưởng nhóm thực hiện theo checklist:

1. **Kiểm tra xung đột (Conflicts)**: Nếu có conflict, yêu cầu thành viên pull nhánh `dev` về rebase/merge và giải quyết conflict trên máy cá nhân trước.
2. **Kiểm tra chất lượng code (Code Review)**:
   - Code có viết theo cấu trúc chuẩn (Controller $\to$ Service $\to$ Repository $\to$ DTO)?
   - Có tuân thủ quy tắc clean code (không hardcode dữ liệu nhạy cảm, không thừa comment rác)?
   - Các API có validation dữ liệu đầu vào (`@Valid`, `@NotNull`)?
   - Đã qua kiểm thử đầy đủ chưa?
3. **Hành động của Leader**:
   - **Nếu cần chỉnh sửa**: Bấm **Request Changes** kèm comment giải thích lý do cụ thể vào từng dòng code.
   - **Nếu đạt yêu cầu**: Bấm **Approve** và thực hiện **Squash and Merge** hoặc **Merge Pull Request** vào `dev`.

---

## 5. Quy Tắc Giao Tiếp & Báo Cáo Tiến Độ (Standup & Deadline)

1. **Cập nhật tiến độ mỗi tối (Daily Update)**: Mỗi thành viên gửi 3 dòng ngắn gọn trước 22:00:
   - *Hôm nay đã hoàn thành:* ...
   - *Ngày mai sẽ làm:* ...
   - *Vướng mắc (Blocker) cần hỗ trợ:* ...
2. **Khi gặp khó khăn**: Không được im lặng quá 12 tiếng. Hãy chủ động tag Trưởng nhóm hoặc bạn cùng nhóm để hỗ trợ gỡ rối (pair programming).
3. **Tôn trọng Deadline**: Mốc thời gian thiết kế CSDL là Thứ Hai (14/09/2026). Các mốc chức năng tiếp theo sẽ được chốt trong bảng phân công.

---

## 6. QUY CHUẨN BẮT BUỘC KHI SỬ DỤNG AI ĐỂ CODE (AI PAIR-PROGRAMMING & TESTING PROTOCOL)

> **LƯU Ý ĐẶC BIỆT**: Nhóm hoàn toàn khuyến khích sử dụng AI (ChatGPT, Claude, Gemini, GitHub Copilot, Cursor, v.v.) để gia tăng tốc độ, nhưng **TUYỆT ĐỐI KHÔNG ĐƯỢC TẠO RA CODE RÁC / CẨU THẢ (AI-SLOP)**.  
> Mọi thành viên khi dùng AI để hỗ trợ viết code BẮT BUỘC phải thực thi nghiêm ngặt theo quy trình 5 bước sau:

### 🧠 Bước 6.1: Bắt Buộc AI Phải Đọc Toàn Bộ Dự Án Trước Khi Viết Code (Full Context Awareness)
- Trước khi prompt AI viết bất kỳ dòng code nào, thành viên **bắt buộc** phải cung cấp context đầy đủ hoặc yêu cầu AI đọc:
  - Tài liệu tổng quan kiến trúc và luồng: `README.md`
  - Thiết kế CSDL và kiểu dữ liệu: `docs/DATABASE_DESIGN.md` và `database/schema.sql`
  - Cấu trúc thư mục mã nguồn và các class/DTO/Entity hiện có liên quan.
- **Nghiêm cấm**: Không để AI code trong tình trạng "mù context", tự bịa ra tên cột, kiểu dữ liệu hoặc viết trùng lặp class đã có trong project.

### 🔄 Bước 6.2: Luôn Cập Nhật Bản Mới Nhất Từ `main` / `dev`
- Trước khi đưa code cho AI xử lý, thành viên phải pull code mới nhất:
  ```bash
  git checkout dev
  git pull origin dev
  git checkout feat/<ten-ban>/<ten-tinh-nang>
  git merge dev
  ```
- Đảm bảo AI đang làm việc trên nền tảng source code mới nhất của cả nhóm, tránh tình trạng viết code dựa trên phiên bản cũ gây conflict nghiêm trọng khi mở PR.

### 🧩 Bước 6.3: Chia Nhỏ Thành Từng Task Nguyên Tử (Atomic Tasks - Làm Từng Việc Một)
- **CẤM**: Không prompt yêu cầu AI "Làm toàn bộ module X", "Viết hết cả Controller, Service, Repo trong 1 lần".
- **BẮT BUỘC**: Chia nhỏ quy trình phát triển thành từng bước đơn lẻ, làm xong bước nào dứt điểm bước đó:
  1. *Task nhỏ 1*: Tạo Entity & JPA Repository (mapping chuẩn xác với `database/schema.sql`).
  2. *Task nhỏ 2*: Tạo Request DTO, Response DTO, bổ sung Validation annotations (`@NotBlank`, `@Min`, `@Size`).
  3. *Task nhỏ 3*: Viết Service Interface & Service Implementation xử lý Business Logic và Custom Exception.
  4. *Task nhỏ 4*: Viết Controller & Mapping REST Endpoint.

### 🧪 Bước 6.4: AI Phải Đóng Vai Tester Kiểm Thử Lại Toàn Bộ (AI Verification & Test Loop)
- Ngay sau khi hoàn thành mỗi task nhỏ, **BẮT BUỘC AI PHẢI THỰC HIỆN KIỂM THỬ TOÀN DIỆN**:
  - Biên dịch và kiểm tra lỗi cú pháp (Syntax / Compilation errors), không còn cảnh báo vàng/đỏ.
  - Kiểm tra các trường hợp biên nguy hiểm (Edge Cases): Dữ liệu `null`, chuỗi rỗng `""`, số âm, dữ liệu trùng lặp (Duplicate Key), phân trang số âm.
  - Viết Unit Test / Integration Test với Mockito hoặc cung cấp script kiểm thử cURL / Postman chi tiết.
  - **Đưa ra Báo Cáo Kiểm Thử (Test Report)** rõ ràng: Liệt kê các kịch bản test đã chạy và kết quả (PASS / FAIL).
- 🛑 **ĐIỀU KIỆN CHUYỂN BƯỚC**: **CHỈ KHI VÀ CHỈ KHI KẾT QUẢ TEST ĐẠT 100% (ALL PASS)**, thành viên mới được phép yêu cầu AI chuyển sang task nhỏ tiếp theo. Nếu còn lỗi (fail/warning), phải sửa dứt điểm trước khi làm tiếp.

### 👔 Bước 6.5: Tiêu Chuẩn Chất Lượng Code "Chuẩn CEO - Enterprise Grade"
Mọi dòng code được AI sinh ra khi gửi lên cho Trưởng nhóm Lê Duy Khánh review phải đạt tiêu chuẩn:
1. **Code tối ưu & Sạch sẽ (Clean Code & DRY)**:
   - Tuân thủ cấu trúc phân lớp chuẩn: `Controller` $\to$ `Service` $\to$ `Repository` $\to$ `Entity` / `DTO`.
   - Không lặp code (Don't Repeat Yourself), không code thừa (no dead code), loại bỏ toàn bộ unused imports.
2. **Dễ đọc & Dễ bảo trì (Readability & Maintainability)**:
   - Đặt tên biến, hàm, class theo chuẩn Java: CamelCase, rõ nghĩa, tự giải thích (self-documenting).
   - Có JavaDoc hoặc comment ngắn gọn ở các đoạn logic nghiệp vụ phức tạp.
3. **Xử lý Ngoại lệ chuẩn hóa (Robust Exception Handling)**:
   - Tuyệt đối không nuốt lỗi (`try { ... } catch (Exception e) {}` để trống).
   - Sử dụng `@RestControllerAdvice` và trả về JSON format thống nhất (`ApiResponse<T>` gồm `statusCode`, `message`, `data`, `timestamp`).
4. **Bảo mật & Hiệu năng cao**:
   - Sử dụng Parameterized Queries / Spring Data JPA chuẩn để chống SQL Injection.
   - Kiểm soát FetchType (`LAZY` cho các mối quan hệ `@ManyToOne`, `@OneToMany`) để chống lỗi N+1 Query.
   - Không bao giờ commit hardcode password, secret key vào git (luôn dùng `application.properties` hoặc biến môi trường).
