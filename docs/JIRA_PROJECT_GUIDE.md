# 📋 HƯỚNG DẪN THIẾT LẬP & QUẢN TRỊ DỰ ÁN TALENTBRIDGE TRÊN JIRA SOFTWARE

> **Dự án**: TalentBridge – Nền tảng tuyển dụng & Quản lý ứng viên (ATS)  
> **Repository**: [https://github.com/leduykhanhdevs/TalentBridge_JS2.git](https://github.com/leduykhanhdevs/TalentBridge_JS2.git)  
> **Tech Lead**: Lê Duy Khánh  
> **File CSV Import sẵn có**: `docs/talentbridge_jira_import.csv`

---

## 1. 🏗️ KHỞI TẠO DỰ ÁN TRÊN JIRA (JIRA PROJECT SETUP)

1. Đăng nhập vào [Atlassian Jira](https://www.atlassian.com/software/jira) (bản Cloud miễn phí tối đa 10 người, rất phù hợp cho nhóm 5 người).
2. Chọn **Create Project**:
   - **Template**: Chọn **Scrum** (khuyên dùng vì có Sprint 1-2 tuần, Backlog và Story Points) hoặc **Kanban**.
   - **Project Type**: Chọn **Team-managed** (nếu muốn cấu hình nhanh) hoặc **Company-managed** (nếu muốn phân quyền chi tiết).
   - **Project Name**: `TalentBridge`
   - **Project Key**: `TB` (các task sẽ có mã dạng `TB-1`, `TB-2`, ...).

---

## 2. ⚡ CÁCH IMPORT 1-CLICK TOÀN BỘ TASK TỪ FILE CSV VÀO JIRA

Thay vì phải gõ thủ công từng task, bạn dùng tính năng **External System Import** của Jira:

1. Vào biểu tượng **Bánh răng (Settings)** ở góc trên bên phải $\to$ chọn **System**.
2. Tìm mục **External System Import** $\to$ chọn **CSV**.
3. Tải lên file: `docs/talentbridge_jira_import.csv` nằm trong thư mục dự án.
4. Ở bước **Map fields**:
   - `Issue Type` $\to$ **Issue Type**
   - `Summary` $\to$ **Summary**
   - `Description` $\to$ **Description**
   - `Epic Name` $\to$ **Epic Name**
   - `Epic Link` $\to$ **Epic Link**
   - `Assignee` $\to$ **Assignee**
   - `Priority` $\to$ **Priority**
   - `Story Points` $\to$ **Story Points**
   - `Component` $\to$ **Component**
   - `Status` $\to$ **Status**
5. Bấm **Next** $\to$ **Begin Import**. Chỉ mất 10 giây, toàn bộ 5 Epics và 22 User Stories/Tasks sẽ xuất hiện đầy đủ trên Jira!

---

## 3. 🎯 PHÂN RÃ CÁC EPICS & USER STORIES CHO 5 THÀNH VIÊN

| Issue Key | Loại | Thành viên phụ trách | Tên công việc / User Story | Story Points | Ưu tiên | Trạng thái hiện tại |
| :--- | :--- | :--- | :--- | :---: | :---: | :---: |
| **TB-1** | **Epic** | **Lê Duy Khánh** | **Epic 1: System Architecture, Security & Admin Module** | **13** | Highest | In Progress |
| TB-6 | Task | Lê Duy Khánh | [Khánh] Khởi tạo Skeleton Spring Boot 3 & Clean Architecture | 3 | Highest | ✅ **Done** |
| TB-7 | Task | Lê Duy Khánh | [Khánh] Xây dựng Module Authentication & JWT Security | 5 | Highest | ✅ **Done** |
| TB-8 | Task | Lê Duy Khánh | [Khánh] Xây dựng Module Quản trị viên (Admin Management APIs) | 5 | High | ✅ **Done** |
| TB-27 | Task | Lê Duy Khánh | [Khánh] Code Review, Security Audit & Merge Pull Requests | 3 | High | ⏳ To Do |
| **TB-2** | **Epic** | **Trần Đình Tình** | **Epic 2: Job Management & Search Engine** | **13** | High | To Do |
| TB-9 | Task | Trần Đình Tình | [Tình] Thiết kế Entity, Repository & DTO cho Job, Category, Skill | 3 | High | ⏳ To Do |
| TB-10 | Story | Trần Đình Tình | [Tình] Xây dựng CRUD Tin tuyển dụng cho Nhà tuyển dụng | 5 | High | ⏳ To Do |
| TB-11 | Story | Trần Đình Tình | [Tình] Xây dựng Bộ lọc & Tìm kiếm việc làm đa tiêu chí | 5 | High | ⏳ To Do |
| TB-12 | Task | Trần Đình Tình | [Tình] Tự động cập nhật tin quá hạn bằng Scheduled Task (Cron Job) | 2 | Medium | ⏳ To Do |
| **TB-3** | **Epic** | **Nguyễn Phan Minh Hiếu** | **Epic 3: Company Profile & ATS Recruitment Pipeline** | **13** | High | To Do |
| TB-13 | Task | Nguyễn Phan Minh Hiếu | [Hiếu] Thiết kế Entity, Repository & DTO cho Company, Recruiter, ATS | 3 | High | ⏳ To Do |
| TB-14 | Story | Nguyễn Phan Minh Hiếu | [Hiếu] Xây dựng API Quản lý Hồ sơ Doanh nghiệp (Company Profile) | 3 | High | ⏳ To Do |
| TB-15 | Story | Nguyễn Phan Minh Hiếu | [Hiếu] Xây dựng Quy trình quản lý ứng viên ATS qua các vòng (Kanban Pipeline) | 5 | Highest | ⏳ To Do |
| TB-16 | Story | Nguyễn Phan Minh Hiếu | [Hiếu] Xây dựng tính năng Đánh giá ứng viên & Ghi chú nội bộ HR | 2 | Medium | ⏳ To Do |
| **TB-4** | **Epic** | **Đặng Trường Thịnh** | **Epic 4: Candidate Profile & Application Flow** | **13** | High | To Do |
| TB-17 | Task | Đặng Trường Thịnh | [Thịnh] Thiết kế Entity, Repository & DTO cho Candidate, Resume, Application | 3 | High | ⏳ To Do |
| TB-18 | Story | Đặng Trường Thịnh | [Thịnh] Xây dựng API Quản lý Thông tin cá nhân Ứng viên | 3 | High | ⏳ To Do |
| TB-19 | Story | Đặng Trường Thịnh | [Thịnh] Xây dựng tính năng Upload & Quản lý CV (PDF) | 5 | High | ⏳ To Do |
| TB-20 | Story | Đặng Trường Thịnh | [Thịnh] Xây dựng Luồng nộp đơn ứng tuyển (Chống nộp trùng lặp) | 5 | Highest | ⏳ To Do |
| TB-21 | Story | Đặng Trường Thịnh | [Thịnh] Xây dựng tính năng Lưu tin việc làm & Lịch sử đơn ứng tuyển | 2 | Medium | ⏳ To Do |
| **TB-5** | **Epic** | **Phan Thị Ánh Tuyền** | **Epic 5: Interview Scheduling, Email Service & Analytics** | **13** | High | To Do |
| TB-22 | Task | Phan Thị Ánh Tuyền | [Tuyền] Thiết kế Entity, Repository & DTO cho Interview, Notification | 3 | High | ⏳ To Do |
| TB-23 | Story | Phan Thị Ánh Tuyền | [Tuyền] Xây dựng tính năng Lên lịch phỏng vấn & Phân loại hình thức | 5 | High | ⏳ To Do |
| TB-24 | Story | Phan Thị Ánh Tuyền | [Tuyền] Tích hợp Spring Mail gửi Email thông báo tự động | 5 | Highest | ⏳ To Do |
| TB-25 | Story | Phan Thị Ánh Tuyền | [Tuyền] Xây dựng Hệ thống Thông báo In-App (Chuông thông báo) | 3 | Medium | ⏳ To Do |
| TB-26 | Story | Phan Thị Ánh Tuyền | [Tuyền] Xây dựng API Báo cáo & Thống kê tuyển dụng (Dashboard Metrics) | 3 | Medium | ⏳ To Do |

---

## 4. 🗓️ KẾ HOẠCH SPRINT (SPRINT PLANNING)

Đề xuất chia dự án thành **3 Sprints (mỗi Sprint 1 tuần)**:

### 🔹 Sprint 1: Khung nền tảng & CRUD Entities cơ bản (Foundation Sprint)
- **Mục tiêu**: Hoàn tất Auth/Admin, dựng xong toàn bộ 16 Entity JPA và các API CRUD cơ bản.
- **Tasks**:
  - `TB-6`, `TB-7`, `TB-8` (Khánh - Đã xong ✅)
  - `TB-9`, `TB-10` (Tình - Entity & CRUD Job)
  - `TB-13`, `TB-14` (Hiếu - Entity & Company Profile)
  - `TB-17`, `TB-18`, `TB-19` (Thịnh - Entity & Candidate, Upload Resume)
  - `TB-22`, `TB-24` (Tuyền - Entity & Cấu hình Spring Mail)

### 🔹 Sprint 2: Nghiệp vụ cốt lõi & Quy trình xử lý (Core Workflow Sprint)
- **Mục tiêu**: Hoàn tất bộ lọc tìm kiếm, luồng nộp đơn chống trùng lặp, quy trình ATS Kanban và lên lịch phỏng vấn.
- **Tasks**:
  - `TB-11`, `TB-12` (Tình - Search Engine & Cron Job)
  - `TB-15`, `TB-16` (Hiếu - ATS Pipeline & Rating/Notes)
  - `TB-20`, `TB-21` (Thịnh - Application Submission & Saved Jobs)
  - `TB-23`, `TB-25` (Tuyền - Interview Booking & In-app Notifications)

### 🔹 Sprint 3: Báo cáo thống kê, Tối ưu & Đóng gói hoàn thiện (Finalization Sprint)
- **Mục tiêu**: Hoàn tất Dashboard Metrics, kiểm thử bảo mật, rà soát hiệu năng (N+1 query, Indexing) và tổng duyệt PR.
- **Tasks**:
  - `TB-26` (Tuyền - Analytics Dashboard)
  - `TB-27` (Khánh - Code Review & Security Audit toàn bộ dự án)
  - Cả nhóm: Viết Test Cases, Postman Documentation và báo cáo slide.

---

## 5. 🔄 CẤU HÌNH WORKFLOW BẢNG JIRA (BOARD COLUMNS)

Thiết lập các cột hiển thị trên Jira Board theo quy trình chất lượng cao:

```
[ TO DO ] ──> [ IN PROGRESS ] ──> [ CODE REVIEW (PR) ] ──> [ TESTING / QA ] ──> [ DONE ]
```

1. **TO DO**: Nhiệm vụ đã được giao, thành viên chuẩn bị làm.
2. **IN PROGRESS**: Đang code trên nhánh `feat/<tên-thành-viên>/<tên-feature>`.
3. **CODE REVIEW (PR)**: Đã tạo Pull Request trên GitHub hướng vào nhánh `dev`, chờ Trưởng nhóm Lê Duy Khánh duyệt.
4. **TESTING / QA**: Đã merge vào `dev`, chạy test tự động (`mvn test`) và Postman đạt 100%.
5. **DONE**: Hoàn thành nghiệm thu và sẵn sàng release.

---

## 6. 🔗 TÍCH HỢP JIRA VỚI GITHUB & SMART COMMITS

Khi thành viên làm việc, sử dụng mã Jira Ticket (ví dụ: `TB-10`) trong Git để Jira tự động liên kết và cập nhật trạng thái:

### 1. Quy tắc đặt tên nhánh (Branch Name):
```bash
git checkout -b feat/tinh/TB-10-crud-job
git checkout -b feat/hieu/TB-14-company-profile
git checkout -b feat/thinh/TB-19-upload-resume
git checkout -b feat/tuyen/TB-24-spring-mail
```

### 2. Cú pháp Commit chuẩn (Smart Commits):
Format: `<Jira-Key> <type>(<scope>): <mô tả>`
```bash
# Ví dụ commit thông thường:
git commit -m "TB-10 feat(job): implement create and update job API"

# Ví dụ commit kèm lệnh Smart Commit của Jira:
# Chuyển ticket sang In Progress và log thời gian 2 giờ:
git commit -m "TB-10 feat: add validation for job salary #in-progress #time 2h"

# Đánh dấu hoàn thành khi merge PR:
git commit -m "TB-10 fix: handle invalid deadline exception #resolve"
```

---

## 7. 👑 CHECKLIST DÀNH CHO TECH LEAD LÊ DUY KHÁNH

Trước khi kéo bất kỳ Ticket nào từ cột **CODE REVIEW** sang **DONE**:
- [ ] Thành viên KHÔNG push trực tiếp vào `main` hay `dev`.
- [ ] Pull Request trỏ vào nhánh `dev`.
- [ ] Mã nguồn tuân thủ Clean Code, không hardcode mật khẩu/đường dẫn ổ đĩa.
- [ ] Đã bổ sung Unit Test và chạy `mvn test` đạt 100% không có lỗi.
- [ ] Đã xử lý `@Transactional` cho các hàm ghi và tránh N+1 Query (sử dụng `@EntityGraph` hoặc `JOIN FETCH`).
