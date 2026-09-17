# 🚀 TalentBridge ATS – Frontend Application

Giao diện người dùng nền tảng Tuyển dụng & Quản trị ứng viên (ATS) TalentBridge.

---

## 🛠️ Công nghệ sử dụng

- **Framework**: React 19 (`react`, `react-dom`)
- **Build Tool**: Vite 8
- **Ngôn ngữ**: TypeScript 6 (Strict Mode, Zero-Any)
- **Styling**: Tailwind CSS v4 (`@tailwindcss/vite`)
- **Routing**: React Router v8
- **Server State Management**: TanStack React Query v5
- **Local State**: Zustand v5
- **Biểu tượng**: Lucide React
- **Kiểm thử tự động**: Vitest 5

---

## 📦 Cài đặt & Chạy ứng dụng

### 1. Cài đặt Dependencies
Bạn có thể sử dụng `pnpm` (khuyên dùng) hoặc `npm`:

```bash
# Sử dụng pnpm
pnpm install

# Hoặc sử dụng npm
npm install
```

### 2. Chạy môi trường phát triển (Development)
```bash
pnpm dev
# hoặc
npm run dev
```
Ứng dụng sẽ khởi chạy tại: `http://localhost:5173`.

### 3. Kiểm thử tự động (Unit Tests)
```bash
pnpm test
# hoặc
npm run test
```

### 4. Kiểm tra cú pháp (Linting)
```bash
pnpm lint
# hoặc
npm run lint
```

### 5. Đóng gói ứng dụng (Production Build)
```bash
pnpm build
# hoặc
npm run build
```

---

## 🧭 Cấu trúc thư mục

```text
frontend/
├── src/
│   ├── app/
│   │   ├── providers/    # AppProviders (QueryClientProvider)
│   │   └── router/       # AppRouter (Cấu hình Route cho toàn ứng dụng)
│   ├── features/
│   │   └── auth/         # Module Xác thực (API, Types, TokenStorage, Tests)
│   │       ├── __tests__/
│   │       ├── authApi.ts
│   │       ├── authTypes.ts
│   │       └── tokenStorage.ts
│   ├── layouts/          # Layout chung (MainLayout, Header, Footer, Auth State)
│   └── pages/            # Các trang giao diện
│       ├── HomePage.tsx
│       ├── LoginPage.tsx
│       ├── RegisterPage.tsx
│       ├── ForgotPasswordPage.tsx
│       ├── ResetPasswordPage.tsx
│       └── NotFoundPage.tsx
├── package.json
└── vite.config.ts
```
