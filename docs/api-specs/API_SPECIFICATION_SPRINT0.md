# 📘 TÀI LIỆU ĐẶC TẢ HỢP ĐỒNG API (API SPECIFICATION) – TALENTBRIDGE SPRINT 0

> **Dự án**: TalentBridge ATS Platform  
> **Người thực hiện**: Lê Duy Khánh (Tech Lead)  
> **Áp dụng cho**: Toàn bộ các task Sprint 0 (`HRPM-8` đến `HRPM-25`)  
> **Tệp OpenAPI Spec đính kèm**: [`docs/api-specs/talentbridge-sprint0-openapi.yaml`](./talentbridge-sprint0-openapi.yaml)  
> **Tệp Mock Data cho Frontend**: [`docs/api-specs/fe-mock-data.json`](./fe-mock-data.json)  
> **Bộ Postman Collection**: [`docs/postman/TalentBridge_Sprint0_Complete.postman_collection.json`](../postman/TalentBridge_Sprint0_Complete.postman_collection.json)

---

## 1. 🔍 ĐỐI CHIẾU VỚI TÀI LIỆU CŨ & ĐÁNH GIÁ

Theo nhận xét và chỉ đạo của Giảng viên:
* **Hạn chế của bản cũ**: Bản cũ chỉ mới tập trung vào Authentication đơn lẻ (`auth-api-spec.yaml`), chưa bao quát hết các task thực tế đang làm trong Sprint 0 (Profile, Admin, Company, Join Request, Forgot Password) và chưa có hướng dẫn Mock cụ thể cho FE.
* **Cải tiến trong bản mới này**:
  1. ✅ **Đặc tả đầy đủ 100% các task Sprint 0**: Bao gồm Auth, Quên mật khẩu OTP, Candidate Profile, Recruiter Profile, Request tạo công ty, Xin gia nhập công ty, HR duyệt nội bộ và trọn bộ Admin Management.
  2. ✅ **Chuẩn hóa API List**: 100% các API danh sách đều có **Phân trang (`page`, `size`)**, **Tìm kiếm (`keyword`)**, **Lọc (`status`)**, **Sắp xếp (`sortBy`, `sortDirection`)** và trả về cấu trúc `PageResponse<T>`.
  3. ✅ **Tách biệt giá trị cho 2 đầu BE & FE**:
     * **Phía BE**: Có hợp đồng DTO, HTTP status, validation rules chính xác để code đúng, ngăn chặn AI sinh code lan man.
     * **Phía FE**: Có sẵn file JSON Mock Data (`fe-mock-data.json`) để mock UI ngay lập tức mà không phải chờ BE deploy.

---

## 2. 📐 QUY CHUẨN THIẾT KẾ HỆ THỐNG API (DESIGN STANDARDS)

### 2.1. Môi trường & Headers chung
* **Base URL**: `http://localhost:8080/api/v1` (Local) hoặc `https://api.talentbridge.vn/api/v1` (Production).
* **Content-Type**: `application/json; charset=UTF-8` cho tất cả các Request Body.
* **Authorization Header**: Truyền Access Token cho các endpoint yêu cầu quyền:
  ```http
  Authorization: Bearer <accessToken>
  ```

### 2.2. Chuẩn thiết kế API Danh sách (List API: Pagination, Filter, Search, Sort)
Tất cả các API lấy danh sách (`/admin/candidates`, `/admin/recruiters`, `/admin/companies`, `/recruiters/companies/search`,...) bắt buộc nhận các Query Parameters sau:

| Query Param | Kiểu dữ liệu | Mặc định | Mô tả |
| :--- | :---: | :---: | :--- |
| `page` | `Integer` | `1` | Số thứ tự trang cần lấy (bắt đầu từ 1) |
| `size` | `Integer` | `10` | Số lượng bản ghi trên một trang (tối đa 100) |
| `keyword` | `String` | `null` | Từ khóa tìm kiếm (tìm kiếm mờ theo tên, email, sđt, mã số thuế) |
| `status` | `String` | `null` | Bộ lọc trạng thái (ví dụ: `ACTIVE`, `BANNED`, `PENDING`, `APPROVED`) |
| `sortBy` | `String` | `createdAt` | Tên trường cần sắp xếp (ví dụ: `createdAt`, `fullName`, `name`) |
| `sortDirection` | `String` | `DESC` | Hướng sắp xếp: `ASC` (tăng dần) hoặc `DESC` (giảm dần) |

#### Cấu trúc phản hồi chuẩn cho API List (`PageResponse<T>`):
```json
{
  "statusCode": 200,
  "message": "Thao tác thành công",
  "data": {
    "content": [ ... ],
    "pageNumber": 1,
    "pageSize": 10,
    "totalElements": 45,
    "totalPages": 5,
    "isLast": false
  },
  "timestamp": "2026-09-14 10:00:00"
}
```

### 2.3. Bảng mã HTTP Status Code chuẩn hóa
* `200 OK`: Thành công với dữ liệu trả về trong `data`.
* `201 Created`: Tạo mới thành công bản ghi (Đăng ký, Tạo công ty, Gửi request).
* `400 Bad Request`: Lỗi validation dữ liệu đầu vào (thiếu trường, sai định dạng).
* `401 Unauthorized`: Chưa đăng nhập, sai tài khoản/mật khẩu, hoặc Token hết hạn.
* `403 Forbidden`: Đã đăng nhập nhưng không có quyền hạn (hoặc tài khoản bị BANNED).
* `404 Not Found`: Không tìm thấy tài nguyên (User, Company, Candidate không tồn tại).
* `409 Conflict`: Xung đột dữ liệu (Trùng email, Trùng mã số thuế, Đã gửi yêu cầu trước đó).

---

## 3. 📑 DANH MỤC CHI TIẾT CÁC ENDPOINT THEO TỪNG TASK SPRINT 0

### 🟢 MODULE 1: AUTHENTICATION & SECURITY (`HRPM-8`, `HRPM-9`, `HRPM-10`, `HRPM-24`)

#### 1. Đăng ký tài khoản (`POST /api/v1/auth/register`)
* **Mô tả**: Tạo tài khoản cho Candidate hoặc Recruiter.
* **Header**: `Content-Type: application/json`
* **Request Body**:
  ```json
  {
    "email": "candidate@talentbridge.vn",
    "password": "Password@123",
    "fullName": "Đặng Trường Thịnh",
    "phone": "0912345678",
    "role": "ROLE_CANDIDATE" 
  }
  ```
  *(Role chỉ nhận: `ROLE_CANDIDATE` hoặc `ROLE_RECRUITER`)*
* **Response Status**: `201 Created`
* **Mock Response**:
  ```json
  {
    "statusCode": 201,
    "message": "Đăng ký tài khoản thành công",
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "sampleRefreshToken...",
      "tokenType": "Bearer",
      "expiresInMs": 86400000,
      "user": {
        "id": 1,
        "email": "candidate@talentbridge.vn",
        "fullName": "Đặng Trường Thịnh",
        "phone": "0912345678",
        "status": "ACTIVE",
        "roles": ["ROLE_CANDIDATE"]
      }
    }
  }
  ```

#### 2. Đăng nhập hệ thống (`POST /api/v1/auth/login`)
* **Mô tả**: Xác thực người dùng, trả về Access Token (24h) và Refresh Token (7 ngày).
* **Request Body**:
  ```json
  {
    "email": "candidate@talentbridge.vn",
    "password": "Password@123"
  }
  ```
* **Response Status**: `200 OK` (hoặc `401 Unauthorized` nếu sai mật khẩu).

#### 3. Làm mới Access Token (`POST /api/v1/auth/refresh-token`)
* **Request Body**: `{"refreshToken": "eyJhbGciOi..."}`
* **Response Status**: `200 OK`

#### 4. Lấy thông tin tài khoản hiện tại (`GET /api/v1/auth/me`)
* **Header**: `Authorization: Bearer <accessToken>`
* **Response Status**: `200 OK`

---

### 🟡 MODULE 2: FORGOT & RESET PASSWORD (`HRPM-11`, `HRPM-12`)

#### 5. Yêu cầu gửi OTP quên mật khẩu (`POST /api/v1/auth/forgot-password`)
* **Mô tả**: Sinh mã OTP 6 chữ số ngẫu nhiên (hết hạn sau 15 phút) và gửi qua Spring Mail.
* **Request Body**:
  ```json
  {
    "email": "candidate@talentbridge.vn"
  }
  ```
* **Response Status**: `200 OK`
* **Mock Response**: `{"statusCode": 200, "message": "Mã OTP đặt lại mật khẩu đã được gửi về email của bạn"}`

#### 6. Xác nhận OTP & Đặt lại mật khẩu (`POST /api/v1/auth/reset-password`)
* **Request Body**:
  ```json
  {
    "email": "candidate@talentbridge.vn",
    "otp": "123456",
    "newPassword": "NewPassword@123"
  }
  ```
* **Response Status**: `200 OK` (hoặc `400 Bad Request` nếu OTP sai hoặc hết hạn).

---

### 🔵 MODULE 3: CANDIDATE PROFILE (`HRPM-14`)

#### 7. Xem hồ sơ Ứng viên (`GET /api/v1/candidates/profile`)
* **Header**: `Authorization: Bearer <accessToken>`
* **Response Status**: `200 OK`
* **Mock Response**:
  ```json
  {
    "statusCode": 200,
    "data": {
      "id": 1,
      "fullName": "Đặng Trường Thịnh",
      "title": "Senior Java Developer",
      "dob": "1998-05-15",
      "gender": "MALE",
      "summary": "3 năm kinh nghiệm phát triển backend Spring Boot",
      "experienceYears": 3,
      "currentSalary": 18000000,
      "expectedSalary": 25000000,
      "city": "Hồ Chí Minh",
      "address": "Quận 1, TP.HCM",
      "personalWebsite": "https://thinhdang.dev",
      "linkedinUrl": "https://linkedin.com/in/thinhdang",
      "githubUrl": "https://github.com/thinhdang"
    }
  }
  ```

#### 8. Cập nhật hồ sơ Ứng viên (`PUT /api/v1/candidates/profile`)
* **Header**: `Authorization: Bearer <accessToken>`
* **Request Body**: Tương tự như DTO trên (chỉ gửi các trường muốn cập nhật).
* **Response Status**: `200 OK`

---

### 🟣 MODULE 4: RECRUITER PROFILE & COMPANY CREATION (`HRPM-15`, `HRPM-21`)

#### 9. Xem & Cập nhật hồ sơ HR (`GET` / `PUT /api/v1/recruiters/profile`)
* **Request Body (PUT)**:
  ```json
  {
    "fullName": "Nguyễn Phan Minh Hiếu",
    "phone": "0987654321",
    "position": "Talent Acquisition Lead",
    "avatarUrl": "https://talentbridge.vn/avatars/hr-1.png"
  }
  ```
* **Response Status**: `200 OK`

#### 10. HR yêu cầu tạo mới Doanh nghiệp (`POST /api/v1/recruiters/companies/request-create`)
* **Mô tả**: Tạo doanh nghiệp ở trạng thái `PENDING` để Admin duyệt.
* **Header**: `Authorization: Bearer <accessToken>`
* **Request Body**:
  ```json
  {
    "name": "Công ty Cổ phần Công nghệ FPT Software",
    "taxCode": "0101234567",
    "website": "https://fptsoftware.com",
    "companySize": "1000+",
    "address": "Khu công nghệ cao, TP. Thủ Đức",
    "city": "Hồ Chí Minh",
    "description": "Tập đoàn công nghệ và dịch vụ CNTT hàng đầu",
    "logoUrl": "https://talentbridge.vn/logos/fpt.png"
  }
  ```
* **Response Status**: `201 Created`

---

### 🟠 MODULE 5: JOIN COMPANY & PEER APPROVAL (`HRPM-23`, `HRPM-25`)

#### 11. Tìm kiếm công ty đã duyệt (`GET /api/v1/recruiters/companies/search`)
* **Query Params**: `keyword=FPT&page=1&size=10&sortBy=name&sortDirection=ASC`
* **Response Status**: `200 OK` (trả về danh sách công ty `APPROVED` có phân trang).

#### 12. Gửi yêu cầu xin gia nhập công ty (`POST /api/v1/recruiters/companies/{companyId}/join-request`)
* **Request Body**:
  ```json
  {
    "position": "Senior Technical Recruiter",
    "message": "Tôi phụ trách tuyển dụng mảng Java Backend cho chi nhánh miền Nam."
  }
  ```
* **Response Status**: `201 Created`

#### 13. HR xem danh sách yêu cầu gia nhập công ty mình (`GET /api/v1/recruiters/companies/my-company/join-requests`)
* **Query Params**: `page=1&size=10&status=PENDING`
* **Response Status**: `200 OK`

#### 14. HR phê duyệt hoặc từ chối yêu cầu gia nhập (`PATCH /api/v1/recruiters/companies/join-requests/{requestId}`)
* **Request Body**:
  ```json
  {
    "status": "ACCEPTED", 
    "reason": "Chào mừng gia nhập team tuyển dụng!"
  }
  ```
  *(status nhận: `ACCEPTED` hoặc `REJECTED`)*
* **Response Status**: `200 OK` (Khi ACCEPTED, hệ thống tự động gán `company_id` cho HR xin gia nhập).

---

### 🔴 MODULE 6: ADMIN MANAGEMENT SUITE (`HRPM-16` -> `HRPM-20`, `HRPM-22`)

#### 15. Admin xem danh sách Ứng viên (`GET /api/v1/admin/candidates`)
* **Query Params**: `page=1&size=10&keyword=Nam&status=ACTIVE`
* **Response Status**: `200 OK` (trả về `PageResponse<CandidateAdminResponse>`).

#### 16. Admin Khóa / Mở khóa tài khoản (`PATCH /api/v1/admin/users/{id}/status`)
* **Request Body**: `{"status": "BANNED"}` *(hoặc `ACTIVE`)*
* **Response Status**: `200 OK`

#### 17. Admin xem danh sách Nhà tuyển dụng (`GET /api/v1/admin/recruiters`)
* **Query Params**: `page=1&size=10&keyword=FPT`
* **Response Status**: `200 OK` (trả về `PageResponse<RecruiterAdminResponse>`).

#### 18. Admin xem danh sách Doanh nghiệp (`GET /api/v1/admin/companies`)
* **Query Params**: `page=1&size=10&status=PENDING`
* **Response Status**: `200 OK` (trả về `PageResponse<CompanyAdminResponse>`).

#### 19. Admin Phê duyệt / Từ chối Doanh nghiệp (`PATCH /api/v1/admin/companies/{id}/status`)
* **Request Body**:
  ```json
  {
    "status": "APPROVED",
    "reason": "Mã số thuế và giấy phép kinh doanh hợp lệ."
  }
  ```
* **Response Status**: `200 OK` (Khi APPROVED, công ty chính thức hoạt động và HR tạo công ty có quyền tuyển dụng).

#### 20. Admin xem số liệu Dashboard (`GET /api/v1/admin/dashboard/stats`)
* **Response Status**: `200 OK` (trả về `totalUsers`, `totalCandidates`, `totalRecruiters`, `totalCompanies`, `pendingCompanies`, `totalJobs`).

---

## 4. 🛠️ HƯỚNG DẪN SỬ DỤNG CHO BACKEND (BE) VÀ FRONTEND (FE)

### Dành cho Backend (BE):
1. **Kiểm tra chặt chẽ DTOs**: Không tự ý đổi tên trường hoặc thêm bớt trường ngoài bản đặc tả trên.
2. **Luôn bọc Response trong `ApiResponse<T>`**: Đối với danh sách, dùng `ApiResponse<PageResponse<T>>`.
3. **Mã lỗi bắt buộc**: Sử dụng `GlobalExceptionHandler` bắt lỗi và trả về đúng HTTP status code (400, 401, 403, 404, 409).

### Dành cho Frontend (FE):
1. **Dùng ngay Mock Data**: Copy dữ liệu từ file [`docs/api-specs/fe-mock-data.json`](./fe-mock-data.json) để mock dữ liệu hiển thị bảng, thẻ, avatar, form mà không phải chờ Backend chạy.
2. **Sử dụng Postman Mock Server**: Import file [`docs/postman/TalentBridge_Sprint0_Complete.postman_collection.json`](../postman/TalentBridge_Sprint0_Complete.postman_collection.json) vào Postman, chọn chuột phải $\to$ **Mock Collection** để tạo một Mock Server URL trả về kết quả thật 100%.
