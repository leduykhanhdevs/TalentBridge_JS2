# 📋 BẢNG PHÂN CHIA SUB-TASK (FE & BE) & ASSIGN CÔNG VIỆC TRÊN JIRA SPRINT 1

> **Dự án**: TalentBridge – Nền tảng tuyển dụng & Quản trị ứng viên (ATS)  
> **Jira Board**: [HRPM Board 5](https://khanhdevs19.atlassian.net/jira/software/projects/HRPM/boards/5?filter=&groupBy=none)  
> **Người thực hiện**: Lê Duy Khánh (Tech Lead / Nhóm trưởng)  
> **File CSV import sub-tasks**: docs/jira_sprint1_subtasks.csv

---

## 1. 🎯 NGUYÊN TẮC PHÂN CHIA SUB-TASK THEO YÊU CẦU CỦA THẦY

Theo đúng chỉ đạo của Giảng viên hướng dẫn:
1. **Mỗi User Story / Task lớn** trong Sprint 1 được phân rã thành đúng **2 Sub-task cụ thể**:
   - 🎨 **Sub-task 1: [FE]** – Xây dựng giao diện UI/UX, Form nhập liệu, Client Validation, Quản lý State, tích hợp gọi API RESTful (Axios/Fetch), xử lý phản hồi HTTP Status, thông báo Toast / Alert.
   - ⚙️ **Sub-task 2: [BE]** – Thiết kế kiến trúc Clean / Hexagonal Architecture (Domain Entities, Inbound Controllers, Inbound/Outbound Ports, Adapters JPA, DTOs, Business Use Cases, Security RBAC, Database Indexing, Unit Test & MockMvc).
2. **Điền người phụ trách vào chỗ Unassigned**:
   - Tất cả các Story và Sub-task đều được gán đích danh thành viên trong nhóm 5 người.
   - Áp dụng mô hình **Feature Ownership (Fullstack per Feature)**: Mỗi thành viên chịu trách nhiệm trọn vẹn cả giao diện (FE) và xử lý logic nghiệp vụ (BE) cho phân hệ mình đảm nhiệm. Cách tiếp cận này giúp các bạn hiểu sâu toàn bộ luồng dữ liệu E2E, không bị phụ thuộc/chờ đợi nhau và có đầy đủ đóng góp cả FE lẫn BE khi chấm điểm đồ án.

---

## 2. 👥 MA TRẬN PHÂN CÔNG THÀNH VIÊN (TEAM ASSIGNMENT MATRIX)

| STT | Thành viên | Vai trò chính | Phân hệ phụ trách (Sprint 1) | Tổng Story Points |
| :---: | :--- | :--- | :--- | :---: |
| **1** | **Lê Duy Khánh** | **Tech Lead & BE Architect** | Khung kiến trúc Clean Architecture, Module Admin (Quản lý Candidate, HR, Duyệt Doanh nghiệp) & Review code | **8 pts** |
| **2** | **Đặng Trường Thịnh** | **Developer (Candidate)** | Module Ứng viên: Đăng ký, Đăng nhập Ứng viên (JWT) & Xem/Cập nhật Hồ sơ cá nhân | **5 pts** |
| **3** | **Nguyễn Phan Minh Hiếu** | **Developer (HR & Company)** | Module Nhà tuyển dụng: Đăng ký, Đăng nhập HR, Quản lý Profile & Tạo yêu cầu mở Công ty mới | **5 pts** |
| **4** | **Trần Đình Tình** | **Developer (HR Join Request)** | Module Gia nhập Công ty: Tìm kiếm công ty đã duyệt, Gửi yêu cầu gia nhập & HR phê duyệt nội bộ | **5 pts** |
| **5** | **Phan Thị Ánh Tuyền** | **Developer (Auth & Mail)** | Module Quên mật khẩu: Gửi OTP qua Spring Mail, Thiết kế HTML Email Template & Đặt lại mật khẩu | **5 pts** |

---

## 3. 📑 CHI TIẾT 11 USER STORIES & 22 SUB-TASKS (FE & BE)

### 🏢 PHÂN HỆ 1: ADMIN MODULE (LÊ DUY KHÁNH PHỤ TRÁCH)

#### 🔹 Story 1: [Khánh] Quản lý ứng viên (Candidate Management) dành cho Admin (3 pts)
- **Sub-task 1 [FE]** (1 pt) – *Assignee: Lê Duy Khánh*
  - **Tên**: [FE] Giao diện Admin xem danh sách, tìm kiếm và Khóa/Mở ứng viên
  - **Mô tả chi tiết**:
    - Bảng danh sách ứng viên có phân trang (Pagination: page, size), hiển thị avatar, họ tên, email, sđt, trạng thái (ACTIVE/BANNED).
    - Bộ lọc tìm kiếm theo họ tên hoặc email; bộ lọc nhanh trạng thái.
    - Modal xem chi tiết hồ sơ ứng viên.
    - Nút Khóa / Mở khóa tài khoản kèm dialog xác nhận hành động nguy hiểm và toast thông báo thành công.
    - Tích hợp gọi API GET /api/v1/admin/candidates và PATCH /api/v1/admin/users/{id}/status.
- **Sub-task 2 [BE]** (2 pts) – *Assignee: Lê Duy Khánh*
  - **Tên**: [BE] API Admin lấy danh sách ứng viên và cập nhật trạng thái User (Clean Architecture)
  - **Mô tả chi tiết**:
    - Xây dựng Inbound Controller: AdminCandidateController, AdminUserController.
    - Viết UseCase: GetCandidatesUseCase, ChangeUserStatusUseCase.
    - Viết Outbound Adapter: CandidatePersistenceAdapter, UserPersistenceAdapter truy vấn DB bằng Spring Data JPA.
    - Phân quyền RBAC @PreAuthorize("hasRole('ADMIN')"), trả về 403 Forbidden nếu không có quyền.
    - Viết Unit Test và ArchUnit Test đảm bảo không vi phạm quy tắc Hexagonal.

#### 🔹 Story 2: [Khánh] Quản lý nhà tuyển dụng (HR Management) dành cho Admin (2 pts)
- **Sub-task 1 [FE]** (1 pt) – *Assignee: Lê Duy Khánh*
  - **Tên**: [FE] Giao diện Admin quản lý danh sách HR và công ty trực thuộc
  - **Mô tả chi tiết**:
    - Bảng hiển thị danh sách Recruiter: Họ tên, Email, SĐT, Vị trí công tác, Tên công ty làm việc, Trạng thái tài khoản.
    - Bộ lọc theo Công ty và Trạng thái người dùng.
    - Modal xem chi tiết hồ sơ Recruiter và thông tin công ty liên kết.
    - Thao tác khóa/mở khóa tài khoản HR.
    - Tích hợp gọi API GET /api/v1/admin/recruiters và PATCH /api/v1/admin/users/{id}/status.
- **Sub-task 2 [BE]** (1 pt) – *Assignee: Lê Duy Khánh*
  - **Tên**: [BE] API Admin lấy danh sách HR và khóa tài khoản vi phạm (Clean Architecture)
  - **Mô tả chi tiết**:
    - Inbound Controller: AdminRecruiterController.
    - UseCase: GetRecruitersUseCase, GetRecruiterDetailUseCase.
    - Eager query join giữa bảng ecruiters, users và companies tránh lỗi N+1 query.
    - Bắt ngoại lệ và trả về mã lỗi chuẩn RFC 7807 (UserNotFoundException -> 404, AccessDeniedException -> 403).
    - Viết Unit test và MockMvc integration test.

#### 🔹 Story 3: [Khánh] Quản lý & Phê duyệt doanh nghiệp do HR yêu cầu (Company Approval) (3 pts)
- **Sub-task 1 [FE]** (1 pt) – *Assignee: Lê Duy Khánh*
  - **Tên**: [FE] Giao diện Admin duyệt doanh nghiệp: Tab chờ duyệt, chi tiết và lý do từ chối
  - **Mô tả chi tiết**:
    - Giao diện chia 3 Tabs: PENDING (Chờ duyệt), APPROVED (Đã duyệt), REJECTED (Đã từ chối).
    - Hiển thị Card/Row thông tin công ty: Logo, Tên công ty, Mã số thuế, Website, Địa chỉ, HR nộp yêu cầu.
    - Modal xem xét chi tiết hồ sơ pháp lý công ty.
    - Modal popup nhập lý do khi bấm Từ chối (Reject Reason).
    - Tích hợp API GET /api/v1/admin/companies?status=PENDING và PATCH /api/v1/admin/companies/{id}/status.
- **Sub-task 2 [BE]** (2 pts) – *Assignee: Lê Duy Khánh*
  - **Tên**: [BE] API Admin phê duyệt/từ chối công ty và liên kết quyền HR đại diện (Clean Architecture)
  - **Mô tả chi tiết**:
    - Inbound Controller: AdminCompanyController.
    - UseCase: GetCompaniesUseCase, ApproveCompanyUseCase.
    - Xử lý nghiệp vụ Transactional: Nếu trạng thái là APPROVED, đổi trạng thái công ty sang APPROVED và đồng thời cập nhật ecruiter.setCompany(company) cho HR đã gửi đơn tạo công ty.
    - Nếu trạng thái là REJECTED, lưu lý do từ chối.
    - Viết Unit test kiểm tra rollback giao dịch khi có lỗi và ArchUnit kiểm thử quy chuẩn.

---

### 👤 PHÂN HỆ 2: CANDIDATE MODULE (ĐẶNG TRƯỜNG THỊNH PHỤ TRÁCH)

#### 🔹 Story 4: [Thịnh] Đăng ký & Đăng nhập tài khoản Ứng viên (Candidate Auth) (2 pts)
- **Sub-task 1 [FE]** (1 pt) – *Assignee: Đặng Trường Thịnh*
  - **Tên**: [FE] Màn hình Đăng ký & Đăng nhập Ứng viên: Form validation và lưu JWT Token
  - **Mô tả chi tiết**:
    - Form Đăng ký: Họ tên, Email, SĐT, Mật khẩu, Nhập lại mật khẩu; kiểm tra định dạng email và mật khẩu >= 6 ký tự.
    - Form Đăng nhập: Email, Mật khẩu; ghi nhớ đăng nhập (Remember Me).
    - Lưu trữ Access Token & Refresh Token vào LocalStorage / Cookie; tự động gắn Bearer Header trong các request sau.
    - Bắt lỗi giao diện: Báo lỗi trùng email (409 Conflict), sai mật khẩu (401 Unauthorized).
- **Sub-task 2 [BE]** (1 pt) – *Assignee: Đặng Trường Thịnh*
  - **Tên**: [BE] API Đăng ký và Đăng nhập Ứng viên: Lưu User/Candidate và cấp JWT Token
  - **Mô tả chi tiết**:
    - Inbound Controller: AuthController (POST /api/v1/auth/register, POST /api/v1/auth/login).
    - UseCase: RegisterCandidateUseCase, LoginUseCase.
    - Xử lý Transactional: Lưu User, cấp ROLE_CANDIDATE trong bảng user_roles, tự động tạo bản ghi trong bảng candidates.
    - Mã hóa mật khẩu BCrypt; sinh chuỗi JWT với Claims vai trò.
    - Bắt lỗi trùng email ném ngoại lệ chuẩn 409 Conflict.

#### 🔹 Story 5: [Thịnh] Xem & Cập nhật Hồ sơ Ứng viên (Update Candidate Profile) (3 pts)
- **Sub-task 1 [FE]** (1 pt) – *Assignee: Đặng Trường Thịnh*
  - **Tên**: [FE] Giao diện xem và cập nhật thông tin cá nhân & liên kết mạng xã hội của Ứng viên
  - **Mô tả chi tiết**:
    - Màn hình trang cá nhân: Xem tổng quan hồ sơ ứng viên (Overview).
    - Form chỉnh sửa: Ngày sinh (Datepicker), Giới tính, Địa chỉ, Thành phố, Mục tiêu nghề nghiệp (Bio textarea), Mức lương mong muốn, Vị trí mong muốn.
    - Khu vực liên kết mạng xã hội: Website cá nhân, GitHub URL, LinkedIn URL.
    - Validate định dạng số điện thoại, URL hợp lệ; hiển thị thông báo thành công.
    - Tích hợp gọi API GET /api/v1/candidates/profile và PUT /api/v1/candidates/profile.
- **Sub-task 2 [BE]** (2 pts) – *Assignee: Đặng Trường Thịnh*
  - **Tên**: [BE] API xem và cập nhật hồ sơ Ứng viên (Clean Architecture & Validation)
  - **Mô tả chi tiết**:
    - Inbound Controller: CandidateProfileController.
    - UseCase: GetCandidateProfileUseCase, UpdateCandidateProfileUseCase.
    - DTO Request với Bean Validation (@Pattern, @Past, @Size).
    - Bảo vệ endpoint @PreAuthorize("hasRole('CANDIDATE')"), tự động trích xuất userId từ JWT Token của người đang đăng nhập.
    - Outbound Adapter: Cập nhật dữ liệu vào bảng candidates và đồng bộ họ tên/sđt sang bảng users.

---

### 💼 PHÂN HỆ 3: HR AUTH & PROFILE (NGUYỄN PHAN MINH HIẾU PHỤ TRÁCH)

#### 🔹 Story 6: [Hiếu] Đăng ký, Đăng nhập & Quản lý Profile Nhà tuyển dụng (HR Auth & Profile) (2 pts)
- **Sub-task 1 [FE]** (1 pt) – *Assignee: Nguyễn Phan Minh Hiếu*
  - **Tên**: [FE] Màn hình Đăng ký, Đăng nhập & Trang cập nhật Profile Nhà tuyển dụng (HR)
  - **Mô tả chi tiết**:
    - Form Đăng ký HR: Thêm trường Chức danh tuyển dụng (Position).
    - Form Đăng nhập HR: Điều hướng vào không gian làm việc của Nhà tuyển dụng.
    - Trang cập nhật hồ sơ HR: Họ tên, Số điện thoại, Chức vụ, Ảnh đại diện (Avatar).
    - Hiển thị thông tin công ty hiện tại hoặc nút 'Yêu cầu mở công ty' / 'Xin gia nhập công ty' nếu chưa có công ty.
- **Sub-task 2 [BE]** (1 pt) – *Assignee: Nguyễn Phan Minh Hiếu*
  - **Tên**: [BE] API Đăng ký, Đăng nhập và Cập nhật Profile HR (Recruiter Entity & RBAC)
  - **Mô tả chi tiết**:
    - Đăng ký tài khoản với vai trò ROLE_RECRUITER, tự động tạo bản ghi trong bảng ecruiters với company_id = NULL.
    - Inbound Controller: RecruiterProfileController (GET/PUT /api/v1/recruiters/profile).
    - UseCase: GetRecruiterProfileUseCase, UpdateRecruiterProfileUseCase.
    - Outbound Adapter: Cập nhật thông tin Recruiter trong DB.
    - Bảo mật RBAC @PreAuthorize("hasRole('RECRUITER')").

#### 🔹 Story 7: [Hiếu] Yêu cầu tạo mới một Công ty (Request Create New Company) (3 pts)
- **Sub-task 1 [FE]** (1 pt) – *Assignee: Nguyễn Phan Minh Hiếu*
  - **Tên**: [FE] Màn hình Form tạo yêu cầu mở Công ty mới & Upload logo dành cho HR
  - **Mô tả chi tiết**:
    - Form nhập thông tin công ty: Tên công ty, Mã số thuế, Website, Quy mô nhân sự (10-50, 50-100,...), Địa chỉ trụ sở, Tỉnh/Thành phố, Giới thiệu công ty.
    - Khung tải lên Logo công ty (Upload file hình ảnh có xem trước preview).
    - Màn hình thông báo trạng thái: "Yêu cầu mở công ty đã được gửi và đang chờ Ban quản trị duyệt".
    - Tích hợp gọi API POST /api/v1/recruiters/companies/request-create.
- **Sub-task 2 [BE]** (2 pts) – *Assignee: Nguyễn Phan Minh Hiếu*
  - **Tên**: [BE] API tạo yêu cầu đăng ký Công ty mới với trạng thái PENDING (Clean Architecture)
  - **Mô tả chi tiết**:
    - Inbound Controller: RecruiterCompanyController.
    - UseCase: RequestCreateCompanyUseCase.
    - Ràng buộc nghiệp vụ: Kiểm tra trùng lặp Mã số thuế (trả về 409 Conflict), kiểm tra HR hiện tại chưa thuộc công ty nào khác.
    - Lưu bản ghi vào bảng companies với trạng thái PENDING và gán created_by_user_id là HR gửi đơn.
    - Viết Unit test cho UseCase kiểm tra đầy đủ các điều kiện ràng buộc.

---

### 🤝 PHÂN HỆ 4: HR JOIN COMPANY & PEER APPROVAL (TRẦN ĐÌNH TÌNH PHỤ TRÁCH)

#### 🔹 Story 8: [Tình] Tìm kiếm & Gửi yêu cầu gia nhập Công ty có sẵn (Join Company Request) (2 pts)
- **Sub-task 1 [FE]** (1 pt) – *Assignee: Trần Đình Tình*
  - **Tên**: [FE] Giao diện Tìm kiếm công ty và Modal gửi yêu cầu xin gia nhập công ty
  - **Mô tả chi tiết**:
    - Thanh tìm kiếm công ty dạng Auto-complete / Danh sách thẻ công ty đã duyệt (APPROVED).
    - Thẻ tóm tắt thông tin công ty: Logo, Tên công ty, Website, Quy mô, Địa chỉ.
    - Modal popup "Gửi yêu cầu xin gia nhập": Nhập vị trí công tác và lời nhắn gửi HR công ty.
    - Khóa nút gửi yêu cầu nếu HR này đang có một yêu cầu gia nhập khác ở trạng thái PENDING.
    - Tích hợp API GET /api/v1/recruiters/companies/search và POST /api/v1/recruiters/companies/{companyId}/join-request.
- **Sub-task 2 [BE]** (1 pt) – *Assignee: Trần Đình Tình*
  - **Tên**: [BE] API Tìm kiếm công ty đã duyệt và Gửi yêu cầu gia nhập chống trùng lặp
  - **Mô tả chi tiết**:
    - Inbound Controller: CompanyJoinRequestController.
    - UseCase: SearchApprovedCompaniesUseCase, SubmitJoinCompanyRequestUseCase.
    - Nghiệp vụ: Chỉ cho phép tìm công ty APPROVED. Kiểm tra HR chưa thuộc công ty nào và không có yêu cầu PENDING nào khác trong bảng company_join_requests.
    - Trả về mã lỗi 409 Conflict nếu vi phạm quy định trùng lặp.
    - Viết Unit test nghiệp vụ gửi yêu cầu.

#### 🔹 Story 9: [Tình] HR phê duyệt hoặc từ chối yêu cầu gia nhập của HR khác (HR Peer Approval) (3 pts)
- **Sub-task 1 [FE]** (1 pt) – *Assignee: Trần Đình Tình*
  - **Tên**: [FE] Giao diện Quản lý yêu cầu gia nhập công ty nội bộ (Approve / Reject kèm lý do)
  - **Mô tả chi tiết**:
    - Trang danh sách các HR đang xin gia nhập công ty (Tab "Yêu cầu chờ duyệt").
    - Hiển thị chi tiết: Họ tên, Email, SĐT, Vị trí xin vào, Lời nhắn, Thời gian gửi yêu cầu.
    - Nút Chấp thuận (Approve): Dialog xác nhận thêm HR vào công ty.
    - Nút Từ chối (Reject): Dialog bắt buộc nhập lý do từ chối.
    - Badge hiển thị số lượng yêu cầu chưa xử lý; cập nhật giao diện thời gian thực sau khi bấm.
    - Tích hợp API GET /api/v1/recruiters/companies/my-company/join-requests và PATCH /api/v1/recruiters/companies/join-requests/{requestId}.
- **Sub-task 2 [BE]** (2 pts) – *Assignee: Trần Đình Tình*
  - **Tên**: [BE] API Lấy danh sách yêu cầu gia nhập và Phê duyệt thành viên HR nội bộ công ty
  - **Mô tả chi tiết**:
    - Inbound Controller: CompanyPeerApprovalController.
    - UseCase: GetPendingJoinRequestsUseCase, ReviewJoinRequestUseCase.
    - Nghiệp vụ bảo mật: Chỉ HR đang thuộc đúng công ty đó mới được phép xem và duyệt yêu cầu (kiểm tra ecruiter.company_id == request.company_id, ném 403 Forbidden nếu không khớp).
    - Transactional: Nếu ACCEPTED, đổi trạng thái yêu cầu sang ACCEPTED đồng thời cập nhật ecruiter.company_id cho HR xin gia nhập. Nếu REJECTED, lưu lý do.
    - Viết Unit test và MockMvc test cho cả 2 kịch bản Accept và Reject.

---

### ✉️ PHÂN HỆ 5: AUTH & EMAIL SERVICE (PHAN THỊ ÁNH TUYỀN PHỤ TRÁCH)

#### 🔹 Story 10: [Tuyền] Xây dựng tính năng Quên mật khẩu qua Email (Forget Password) (3 pts)
- **Sub-task 1 [FE]** (1 pt) – *Assignee: Phan Thị Ánh Tuyền*
  - **Tên**: [FE] Màn hình Quên mật khẩu: Form nhập Email, OTP 6 số và Form Đặt lại mật khẩu
  - **Mô tả chi tiết**:
    - Giao diện 3 bước mượt mà:
      1. Bước 1: Nhập Email yêu cầu khôi phục, validate định dạng email, nút gửi kèm bộ đếm ngược 60s trước khi gửi lại.
      2. Bước 2: Nhập mã OTP gồm 6 ô số tự động focus, có đếm ngược thời hạn hiệu lực 15 phút.
      3. Bước 3: Nhập Mật khẩu mới và Xác nhận mật khẩu mới (kiểm tra độ mạnh mật khẩu).
    - Bắt lỗi khi mã OTP hết hạn hoặc sai mã (hiển thị alert thông báo rõ ràng).
    - Tích hợp gọi API POST /api/v1/auth/forgot-password và POST /api/v1/auth/reset-password.
- **Sub-task 2 [BE]** (2 pts) – *Assignee: Phan Thị Ánh Tuyền*
  - **Tên**: [BE] API Quên mật khẩu: Sinh mã OTP, gửi email và mã hóa mật khẩu mới BCrypt
  - **Mô tả chi tiết**:
    - Inbound Controller: ForgotPasswordController.
    - UseCase: SendResetPasswordOtpUseCase, ResetPasswordUseCase.
    - Nghiệp vụ: Kiểm tra email có tồn tại không. Sinh mã OTP ngẫu nhiên 6 chữ số an toàn mã hóa, lưu token kèm hạn 15 phút.
    - Sử dụng PasswordEncoder (BCrypt) để mã hóa mật khẩu mới và lưu vào bảng users. Hủy/xóa token OTP sau khi sử dụng thành công.
    - Xử lý mã lỗi chuẩn: 400 Bad Request nếu OTP sai/hết hạn, 404 nếu email không tồn tại.
    - Viết Unit test nghiệp vụ sinh OTP và đổi mật khẩu.

#### 🔹 Task 11: [Tuyền] Thiết kế HTML Template Email thông báo đặt lại mật khẩu (2 pts)
- **Sub-task 1 [FE]** (1 pt) – *Assignee: Phan Thị Ánh Tuyền*
  - **Tên**: [FE] Thiết kế Responsive HTML Template Email thông báo OTP/Reset Password
  - **Mô tả chi tiết**:
    - Thiết kế file template HTML email chuẩn nhận diện thương hiệu TalentBridge.
    - Áp dụng cấu trúc Responsive bằng CSS inline (table layout) đảm bảo hiển thị hoàn hảo trên Gmail, Microsoft Outlook, Apple Mail trên cả Desktop và Điện thoại.
    - Bố cục gồm: Logo TalentBridge, Lời chào cá nhân hóa, Khung hiển thị mã OTP 6 số cỡ lớn nổi bật, Nút kêu gọi hành động (CTA) "Đặt lại mật khẩu", Chú thích bảo mật "Mã hết hạn sau 15 phút và không chia sẻ cho bất kỳ ai".
- **Sub-task 2 [BE]** (1 pt) – *Assignee: Phan Thị Ánh Tuyền*
  - **Tên**: [BE] Tích hợp Spring Mail & Thymeleaf Engine render biến động email bất đồng bộ
  - **Mô tả chi tiết**:
    - Cấu hình Spring Boot Starter Mail (JavaMailSender) kết nối máy chủ SMTP.
    - Tích hợp Thymeleaf SpringTemplateEngine để parse file password-reset-email.html và truyền các biến động: ${recipientName}, ${otpCode}, ${expireMinutes}.
    - Cấu hình phương thức gửi mail bất đồng bộ với @Async (TaskExecutor) để API phản hồi ngay lập tức cho client mà không bị chờ độ trễ mạng của SMTP.
    - Bắt ngoại lệ MailException ghi log chi tiết.

---

## 4. 🚀 HƯỚNG DẪN THAO TÁC TRÊN GIAO DIỆN JIRA CLOUD

### Cách 1: Tạo Sub-task thủ công trên giao diện Jira Board (Khuyên dùng)
1. Mở Board dự án: [khanhdevs19.atlassian.net/jira/software/projects/HRPM/boards/5](https://khanhdevs19.atlassian.net/jira/software/projects/HRPM/boards/5?filter=&groupBy=none)
2. Bấm vào một **User Story / Task cha** (ví dụ: [Khánh] Quản lý ứng viên).
3. Dưới tiêu đề của Issue, tìm và bấm nút **"Add child issue"** (hoặc biểu tượng dấu cộng + có chữ Subtask).
4. Nhập tiêu đề Sub-task:
   - Nhập [FE] ... cho task giao diện.
   - Nhập [BE] ... cho task backend.
5. Tại cột **Assignee**: Bấm vào ô chữ Unassigned $\to$ chọn đúng tên thành viên phụ trách (Khánh, Thịnh, Hiếu, Tình, Tuyền).
6. Bấm vào Sub-task vừa tạo để điền thêm:
   - **Description**: Dán nội dung mô tả chi tiết từ bảng phía trên.
   - **Story Points**: Điền 1 hoặc 2 điểm.
7. Lặp lại cho Sub-task thứ hai của Story đó.

### Cách 2: Import nhanh toàn bộ 22 Sub-tasks bằng file CSV
Nhóm trưởng đã tạo sẵn file CSV chuẩn hóa tại:  
📁 docs/jira_sprint1_subtasks.csv

1. Trên Jira, vào **Settings (Bánh răng)** góc trên bên phải $\to$ chọn **System**.
2. Chọn **External System Import** $\to$ chọn **CSV**.
3. Tải lên file docs/jira_sprint1_subtasks.csv.
4. Ánh xạ các cột tương ứng:
   - Issue Type $\to$ **Issue Type** (Chọn Sub-task)
   - Summary $\to$ **Summary**
   - Parent Summary $\to$ **Parent Issue / Parent Summary**
   - Description $\to$ **Description**
   - Assignee $\to$ **Assignee**
   - Priority $\to$ **Priority**
   - Story Points $\to$ **Story Points**
   - Component $\to$ **Component**
5. Bấm **Next** $\to$ **Begin Import**. Toàn bộ 22 Sub-task sẽ được tự động gắn đúng vào các Story cha và gán đích danh từng thành viên!

---

## 5. 📊 BẢNG TỔNG HỢP REVIEW CHO GIẢNG VIÊN HƯỚNG DẪN

Khi thầy kiểm tra Jira Board, nhóm báo cáo tóm tắt như sau:
> *"Thưa thầy, nhóm em đã phân rã toàn bộ 11 User Stories trong Sprint 1 thành 22 Sub-tasks cụ thể (gồm 11 Sub-task Frontend và 11 Sub-task Backend). Tất cả các vị trí 'Unassigned' đều đã được gán đích danh cho 5 thành viên theo nguyên tắc Feature Ownership (mỗi bạn làm trọn vẹn FE & BE cho phân hệ của mình để hiểu sâu nghiệp vụ và có đủ khối lượng công việc đánh giá). Hiện tại phần Backend Admin (TB-1, TB-2, TB-3) đã được Tech Lead hoàn thiện 100% kiểm thử theo kiến trúc Clean/Hexagonal Architecture; các thành viên khác đang tiến hành thực hiện các sub-task tiếp theo theo đúng kế hoạch."*

---

## 6. 🚀 MA TRẬN PHÂN CÔNG SPRINT 2 (ATS & RECRUITMENT PIPELINE)

### 📌 Nguyên tắc phân bổ Sprint 2:
1. **Tech Lead (Lê Duy Khánh)**: Xây dựng nền tảng Backend Core, Kiến trúc Hexagonal và các API xử lý đăng tin tuyển dụng (`HRPM-27`, `HRPM-28`, `HRPM-29`, `HRPM-30`, `HRPM-31`, `HRPM-32`, `HRPM-34`). Toàn bộ đã đạt `Done` với 154/154 unit & integration tests pass 100%.
2. **Đặng Trường Thịnh** (4 stories Candidate Discovery & Status):
   - `HRPM-35`: Filter Jobs by Location Salary Experience and Type
   - `HRPM-36`: Job List Sorting and Pagination
   - `HRPM-37`: Job Detail Page UI
   - `HRPM-42`: Candidate View My Applications (Xem lịch sử đơn đã nộp)
3. **Phan Thị Ánh Tuyền** (4 stories Candidate Application Lifecycle):
   - `HRPM-39`: Candidate Apply to Job
   - `HRPM-40`: Attach CV and Cover Letter to Application
   - `HRPM-41`: Prevent Duplicate Application (`UNIQUE(candidate_id, job_id)`)
   - `HRPM-43`: Candidate Withdraw Application
4. **Trần Đình Tình** (5 stories Recruiter ATS Screening Pipeline):
   - `HRPM-45`: HR View Applicants of a Job
   - `HRPM-46`: Filter Applicants by Criteria
   - `HRPM-47`: Sort Applicants
   - `HRPM-48`: Update Applicant Status (Kanban / ATS Stage Pipeline)
   - `HRPM-49`: Rate and Note Applicant (Đánh giá sao & Ghi chú nội bộ HR)
5. **Nguyễn Phan Minh Hiếu** (4 subtasks Recruiter Job Portal Frontend):
   - `HRPM-51` (Parent `HRPM-28`): [FE] Giao diện Form HR Đăng tin tuyển dụng mới
   - `HRPM-52` (Parent `HRPM-29`): [FE] Giao diện Form HR Chỉnh sửa tin tuyển dụng
   - `HRPM-53` (Parent `HRPM-30`): [FE] Giao diện Thao tác Đóng và Quản lý trạng thái tin tuyển dụng
   - `HRPM-54` (Parent `HRPM-31`): [FE] Giao diện Bảng danh sách tin tuyển dụng của công ty (My Jobs Dashboard)

