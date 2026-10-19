# Nhật ký thực thi UAT — 2026-10-20

Mọi request ghi dữ liệu và xác thực theo vai trò đều dùng fixture local với H2 trong bộ nhớ. Production chỉ nhận GET/OPTIONS. Không lưu mật khẩu, token, secret hoặc dữ liệu cá nhân thật trong các log này.

## Baseline production trước UAT

- GitHub Actions workflow `39291716914` — **TalentBridge Continuous Deployment (CD to Render)** — hoàn tất `success` cho SHA `474b749982a7690cf37b3752c61c680fa51cd092`: https://github.com/leduykhanhdevs/TalentBridge_JS2/actions/runs/39291716914.
- Một lần dò health ban đầu timeout do nhắm hostname backend cũ/sai; hostname đúng được lấy từ workflow CD là `talentbridge-backend-6rmd.onrender.com`. Recheck chỉ đọc xác nhận API hoạt động, nên lần dò đầu không được tính là lỗi production còn tồn tại.
- Production smoke lúc 01:10 +07: `/`, `/login`, `/register`, `/jobs`, `/assistant` HTTP 200, HTML có marker `474b749`; `/api/v1/health` HTTP 200, `UP`, cùng SHA; `/api/v1/jobs?page=0&size=1` HTTP 200, 100 jobs, item đầu `isDemo=true`; CORS OPTIONS HTTP 200, allow-origin đúng frontend, credentials=true. Chi tiết ở `production-smoke.log`.

## Chất lượng mã nguồn và kiểm thử local

- Backend `mvnw verify`: **324 tests, 0 failures/errors/skips**, `BUILD SUCCESS`; `backend-maven-verify.log`.
- Frontend Vitest cuối: **14 files, 100 tests passed**; `frontend-vitest-final.log`.
- ESLint exit 0; TypeScript + Vite production build exit 0; `frontend-lint-final.log`, `frontend-build-final.log`.
- E2E API trên Spring Boot + H2: **28 tests passed**. Playwright: **11 tests passed**; `full-e2e-final.log`.
- Playwright rà nội dung/quyền và overflow cho public/auth, candidate, recruiter và admin; 25 tổ hợp route-vai trò ở 360/390/1280/1440 px, ba vai trò bổ sung ở 555 px; kiểm tra route lỗi/rỗng, chatbot, đăng ký/đăng xuất/đăng nhập và luồng lỗi/timeout. **125 ảnh** ở `screenshots/final/`.
- Regression mới: tạo job thất bại giữ dialog/form và hiện alert; lịch sử ứng tuyển timeout có retry; logout bị treo kết thúc và xóa phiên local; response body timeout không treo; login body timeout hiện thông báo timeout dễ hiểu.
- `git diff --check` sạch. H2 E2E backend và Playwright servers đã dừng sau lần chạy.

## Đánh giá và phát hành

- Bugbot và Security Review đã chạy theo yêu cầu; mỗi lượt nêu một lỗi P2, cả hai đã được sửa và có regression test. Rà soát sau đó bổ sung UAT-05 về thông báo timeout body đăng nhập.
- Không còn lỗi có thể tái hiện trong phạm vi UAT local. Production vẫn chạy SHA `474b749`; các sửa đổi UAT chưa được deploy.
- `origin/dev` khác nền `main`: có 4 commit chỉ ở `dev` và 28 commit chỉ ở `main`. Để không kéo thay đổi ngoài phạm vi vào PR phát hành này, PR hotfix dự kiến target `main`.
- Bước phát hành còn lại: tạo PR, chờ trưởng nhóm Lê Duy Khánh review/merge theo `TEAM_RULES_AND_GITFLOW.md`, kiểm tra CD và xác minh frontend/backend cùng báo SHA mới, rồi chạy production smoke sau deploy.
