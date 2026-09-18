# 🚀 TalentBridge ATS – Frontend Application

Giao diện người dùng nền tảng Tuyển dụng Trực tuyến & Quản trị Tuyển dụng Ứng viên (ATS) TalentBridge.

Ứng dụng được thiết kế theo tiêu chuẩn SPA (Single Page Application) hiện đại, phân quyền đa tác nhân (Candidate, Recruiter, Admin), tích hợp hệ thống kiểm duyệt hồ sơ doanh nghiệp và kiểm thử toàn diện.

---

## 🛠️ Công Nghệ Sử Dụng (Tech Stack)

- **UI Framework**: [React 19](https://react.dev/) (`react`, `react-dom`)
- **Build Tool & Bundler**: [Vite 8](https://vitejs.dev/)
- **Styling & CSS**: [Tailwind CSS v4](https://tailwindcss.com/) (`@tailwindcss/vite`)
- **Icon Set**: [Lucide React](https://lucide.dev/)
- **Automated Testing**: [Vitest 5](https://vitest.dev/) (Unit & Integration E2E test suite)
- **Language**: [TypeScript 6](https://www.typescriptlang.org/) (Strict Mode, Zero-Any discipline)
- **Routing**: [React Router v8](https://reactrouter.com/) (File-based & Nested route layouts)
- **Server State Management**: [TanStack React Query v5](https://tanstack.com/query)
- **Client State Management**: [Zustand v5](https://zustand-demo.pmnd.rs/)
- **Code Quality**: ESLint + TypeScript-ESLint

---

## 🏛️ Cấu Trúc Các Phân Hệ & Portals

Ứng dụng phân định rõ 3 cổng nghiệp vụ tương ứng với 3 Actor trong hệ thống:

### 1. Phân Hệ Chung (Public Routes)
- `/`: **Trang chủ (HomePage)** – Giới thiệu nền tảng, banner việc làm nổi bật và thanh tìm kiếm việc làm.
- `/login`: **Đăng nhập (LoginPage)** – Đăng nhập với Email và Password, nhận cặp JWT Access Token (24h) và Refresh Token (7 ngày).
- `/register`: **Đăng ký tài khoản (RegisterPage)** – Đăng ký cho Ứng viên (`ROLE_CANDIDATE`) hoặc Nhà tuyển dụng (`ROLE_RECRUITER`).
- `/forgot-password`: **Quên mật khẩu (ForgotPasswordPage)** – Gửi yêu cầu nhận mã OTP xác thực qua Email.
- `/reset-password`: **Đặt lại mật khẩu (ResetPasswordPage)** – Nhập mã xác thực OTP và thiết lập mật khẩu mới.

### 2. Cổng Ứng Viên (Candidate Portal - `/candidate/*`)
- `/candidate/profile`: **Quản lý Hồ sơ Ứng viên (CandidateProfilePage)**
  - Cập nhật thông tin chuyên môn: Chức danh mong muốn (`title`), số năm kinh nghiệm (`experienceYears`), mức lương hiện tại & kỳ vọng (`currentSalary`, `expectedSalary`).
  - Cập nhật thông tin cá nhân: Ngày sinh (`dob`), giới tính (`gender`: Nam / Nữ / Khác), tỉnh/thành phố (`city`), địa chỉ cụ thể (`address`), mục tiêu nghề nghiệp (`summary`).
  - Cập nhật liên kết xã hội & portfolio: Website cá nhân (`personalWebsite`), LinkedIn (`linkedinUrl`), GitHub (`githubUrl`).

### 3. Cổng Nhà Tuyển Dụng (Recruiter Portal - `/recruiter/*`)
- `/recruiter/profile`: **Hồ sơ Tuyển dụng (RecruiterProfilePage)** – Quản lý chức danh công tác và thông tin nhân sự.
- `/recruiter/company`: **Khởi tạo Doanh nghiệp (RecruiterCompanyPage)** – Đăng ký thành lập công ty mới và gửi yêu cầu phê duyệt tới Admin.
- `/recruiter/join-company`: **Gia nhập Doanh nghiệp (RecruiterJoinCompanyPage)** – Tìm kiếm các doanh nghiệp đã được phê duyệt và gửi đơn xin gia nhập.
- `/recruiter/peer-approval`: **Kiểm duyệt Đồng nghiệp (RecruiterPeerApprovalPage)** – HR trong cùng doanh nghiệp có quyền xem xét, phê duyệt hoặc từ chối đơn gia nhập của đồng nghiệp.

### 4. Cổng Quản Trị Viên (Admin Portal - `/admin/*`)
- `/admin/candidates`: **Quản lý Ứng viên (AdminCandidatesPage)** – Danh sách ứng viên hệ thống, tìm kiếm, lọc theo trạng thái và thao tác Khóa / Mở khóa tài khoản (`ACTIVE` / `BANNED`).
- `/admin/recruiters`: **Quản lý Nhà tuyển dụng (AdminRecruitersPage)** – Danh sách HR tuyển dụng, thông tin doanh nghiệp trực thuộc và thao tác Khóa / Mở khóa tài khoản.
- `/admin/companies`: **Kiểm duyệt Doanh nghiệp (AdminCompaniesPage)** – Xem xét các hồ sơ công ty mới gửi lên, thực hiện Phê duyệt (`APPROVED`) hoặc Từ chối (`REJECTED`).

---

## 🧭 Cấu Trúc Thư Mục Dự Án (Directory Structure)

```text
frontend/
├── src/
│   ├── app/
│   │   ├── providers/              # AppProviders (React Query, Error Boundaries)
│   │   └── router/                 # AppRouter & Cấu hình Route tập trung
│   ├── components/                 # UI Components dùng chung & Illustrations
│   │   └── illustrations/          # Vector phẳng & 3D Soft Badges (Chuẩn Bento Grid)
│   │       ├── AdminControlCenterIllustration.tsx
│   │       ├── Soft3DBadges.tsx    # 3D Soft Badges (User, Active, Shield, Verified, Building)
│   │       └── index.ts
│   ├── features/                   # Kiến trúc Feature-Driven Modules
│   │   ├── admin/                  # Module Quản trị viên (API, Types, Validation, Tests)
│   │   │   ├── __tests__/
│   │   │   ├── adminApi.ts
│   │   │   ├── adminTypes.ts
│   │   │   └── adminValidation.ts
│   │   ├── auth/                   # Module Xác thực (JWT, Login, Register, OTP)
│   │   │   ├── __tests__/
│   │   │   ├── authApi.ts
│   │   │   ├── authTypes.ts
│   │   │   ├── authValidation.ts
│   │   │   └── tokenStorage.ts
│   │   ├── candidate/              # Module Ứng viên (Hồ sơ cá nhân, TopCV CV Data)
│   │   │   ├── __tests__/
│   │   │   ├── candidateApi.ts
│   │   │   ├── candidateTypes.ts
│   │   │   └── candidateValidation.ts
│   │   └── recruiter/              # Module Nhà tuyển dụng (Hồ sơ HR, Công ty, Peer Review)
│   │       ├── __tests__/
│   │       ├── recruiterApi.ts
│   │       ├── recruiterTypes.ts
│   │       └── recruiterValidation.ts
│   ├── layouts/                    # Layout dùng chung cho các phân hệ
│   │   ├── AdminLayout.tsx         # Layout Admin (Sidebar, Admin Navbar)
│   │   ├── MainLayout.tsx          # Layout công khai (Public Header & Footer)
│   │   └── RecruiterLayout.tsx     # Layout Recruiter (HR Portal Menu)
│   ├── pages/                      # Các trang giao diện
│   │   ├── admin/                  # Trang nghiệp vụ Admin
│   │   │   ├── AdminCandidatesPage.tsx
│   │   │   ├── AdminCompaniesPage.tsx
│   │   │   └── AdminRecruitersPage.tsx
│   │   ├── candidate/              # Trang nghiệp vụ Candidate
│   │   │   └── CandidateProfilePage.tsx
│   │   ├── recruiter/              # Trang nghiệp vụ Recruiter
│   │   │   ├── RecruiterCompanyPage.tsx
│   │   │   ├── RecruiterJoinCompanyPage.tsx
│   │   │   ├── RecruiterPeerApprovalPage.tsx
│   │   │   └── RecruiterProfilePage.tsx
│   │   ├── ForgotPasswordPage.tsx
│   │   ├── HomePage.tsx
│   │   ├── LoginPage.tsx
│   │   ├── NotFoundPage.tsx
│   │   ├── RegisterPage.tsx
│   │   └── ResetPasswordPage.tsx
│   └── __tests__/
│       └── e2e-ecosystem.test.ts   # Kiểm thử E2E tích hợp toàn bộ hệ sinh thái (59 tests)
├── package.json
├── tsconfig.json
└── vite.config.ts
```

---

## 📦 Cài Đặt & Chạy Ứng Dụng (Quick Start)

### 1. Cài đặt Thư viện Phụ thuộc (Dependencies)

Khuyến nghị sử dụng **pnpm**:

```bash
# Cài đặt bằng pnpm
pnpm install

# Hoặc sử dụng npm
npm install
```

### 2. Chạy Môi Trường Phát Triển (Development Server)

```bash
pnpm dev
# hoặc
npm run dev
```

Ứng dụng sẽ khởi chạy tại: `http://localhost:5173`.  
Vite dev server đã được cấu hình proxy tự động chuyển tiếp các request `/api/v1/*` sang Backend Spring Boot (`http://localhost:8080`).

### 3. Kiểm Thử Tự Động (Automated Testing)

Chạy bộ kiểm thử tự động toàn diện bao gồm Unit Tests và E2E Flow test:

```bash
# Chạy toàn bộ test suite một lần (CI mode)
pnpm test --run

# Hoặc chạy ở chế độ watch khi phát triển
pnpm test
```

### 4. Kiểm Tra Quy Chuẩn Mã Nguồn (Linting)

```bash
pnpm lint
```

### 5. Đóng Gói Ứng Dụng Sản Phẩm (Production Build)

```bash
pnpm build
```

Kết quả đóng gói tối ưu sẽ được kết xuất vào thư mục `dist/`, sẵn sàng triển khai trên Nginx, Cloudflare Pages hoặc Vercel.
