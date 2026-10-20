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

## Re-review sau khi sửa findings — 2026-10-20

- Bugbot re-review SHA `4a24adcb1c06b6cbbfc87639c7f9f872aab0ccea` nêu thêm hai P2: logout bị kéo dài qua chu kỳ refresh 8 giây và timeout body chatbot bị báo thành JSON hỏng. Lần Playwright đầu sau khi sửa logout API vẫn không chuyển trang ngay vì hai layout đang `await logoutUser()`; log và trace lỗi được giữ ở `reviews/full-e2e-review-failure.log` và `reviews/reliability-workflows-expi-8aca5-iting-for-a-stalled-refresh/`.
- Khắc phục: `logoutUser` đặt logout intent ngay, chờ Web Lock tối đa 10.5 giây để refresh đang chạy có thể hoàn tất, rồi xóa local auth và gửi revoke với timeout tối đa 2.5 giây; `MainLayout` và `WorkspaceLayoutFrame` không chờ network mới điều hướng `/login`. Trợ lý dùng `FetchTimeoutError` với deadline 15 giây áp dụng đến hết body, chuyển timeout body sang thông báo 408.
- Regression tests: Vitest kiểm tra logout 401 + refresh treo vẫn bounded và timeout chatbot body vẫn được báo đúng; Playwright bổ sung thao tác đăng xuất khi refresh bị giữ, xác nhận tới `/login` trong tối đa 1.5 giây.
- Lần chạy cuối: Vitest **15 files / 102 passed**; ESLint exit 0; TypeScript/Vite production build exit 0; API E2E H2 **28 passed**; Playwright Chromium **12 passed**. Log build/test ở thư mục này; ảnh mới **125 file** tại `screenshots/post-review/`; backend H2 stdout/stderr ở `backend-h2-post-review.*.log`.
- Một lần gọi script bằng Windows PowerShell 5 gặp lỗi parser do mã hóa tiếng Việt của script UTF-8; không thay đổi source. Chạy lại cùng script bằng PowerShell 7 (`pwsh`) thành công. Production vẫn chỉ được smoke ở chế độ GET/OPTIONS; các sửa đổi mới chưa deploy.
- Sau khi CI và Bugbot/Security re-review trên SHA cập nhật đạt, bước còn lại là leader duyệt/merge PR #48; sau đó mới kiểm tra CD và chạy production smoke xác nhận SHA.

## Re-review lần cuối và full regression — 2026-10-20

- Bugbot tìm thêm hai lỗi: refresh phiên cũ có thể lưu token sau logout (P2), và timeout trợ lý có thể bị cộng dồn qua HTTP/refresh/retry (P2). Security xác nhận lỗi race logout là P2 và phân loại timeout cộng dồn P3. Đã chặn ghi token bằng auth-mutation version + refresh-token snapshot; `authenticatedFetch` giờ dùng deadline tuyệt đối và truyền thời gian còn lại cho mỗi giai đoạn. Regression test mô phỏng logout giữa refresh và một lượt 401→refresh→retry tổng cộng 40 ms.
- Bugbot re-review mới nhất không còn P1/P2; Security re-review cuối vẫn đang chờ kết quả tại thời điểm ghi log.
- Full regression cuối: Maven `verify` **324/324**; Vitest **16 file / 104 test**; ESLint exit 0; TypeScript/Vite build exit 0; API E2E H2 **28/28**; Playwright **12/12** trên 360/390/555/1280/1440 px. Hình chụp đại diện public home, candidate applications, recruiter applicants và admin metrics đã được rà trực quan; bộ 125 ảnh nằm trong `screenshots/post-review/`.
- Log mới: `backend-maven-verify-final.log`, `frontend-vitest-final.log`, `frontend-lint-final.log`, `frontend-build-final.log`, `full-e2e-post-review-final.log`, `backend-h2-post-review-final.stdout.log` và `backend-h2-post-review-final.stderr.log`.
- Test dùng H2 trong bộ nhớ và fixture tài khoản hư cấu; không gửi request ghi dữ liệu hoặc thông tin đăng nhập lên production. Production vẫn là SHA nền `474b749...`; chưa redeploy các sửa đổi vì `TEAM_RULES_AND_GITFLOW.md` yêu cầu trưởng nhóm review/merge PR #48 trước CD.
- Bước còn lại: nhận Security re-review, cập nhật PR/CI, chờ leader merge; xác minh Render chạy đúng SHA mới và thực hiện production smoke chỉ đọc.

## Sửa race thu hồi phiên và regression tiếp theo — 2026-10-20

- Security re-review phát hiện UAT-10: nếu refresh request xoay token ngay trước khi logout, request logout chỉ dùng access token hết hạn; fallback refresh token cũ bị từ chối và phiên server có thể còn hiệu lực.
- Sửa contract logout theo hướng tương thích ngược: frontend gửi refresh token hiện có trong body; backend tự xác minh chữ ký, hạn dùng, loại token, session ID và user ID rồi thu hồi session. Khi gửi cả hai token, chúng phải cùng phiên. Endpoint logout được đưa vào danh sách public để access token hết hạn không chặn body refresh; controller/use case vẫn xác thực token trước khi thu hồi. Token refresh chỉ có quyền kết thúc session, không truy cập API nghiệp vụ.
- Regression mới: backend integration test xoay refresh token trước, logout bằng refresh token cũ, rồi xác nhận cả access token mới và refresh token mới đều bị từ chối; unit test cho refresh-only và cặp token khác session. Frontend Vitest xác nhận refresh token được gửi trong request logout. Focused backend **22/22** và focused frontend **8/8** đạt; một lần Maven trước đó dùng Java 8 runtime bị lỗi `No compiler is provided`, chạy lại trên JBR/JDK 21 thành công.
- UAT-10 đã được sửa local; cần chạy lại toàn bộ verify/lint/build/API E2E/Playwright và re-review Bugbot/Security trên diff mới. Production vẫn chưa deploy; đang chờ leader review/merge PR #48 theo `TEAM_RULES_AND_GITFLOW.md`.

## Regression cuối sau UAT-10 — 2026-10-20

- Backend Maven `verify`: **327/327**, `BUILD SUCCESS`; `backend-maven-verify-uat10.log`.
- Frontend Vitest: **16 file / 104 test**; ESLint exit 0; TypeScript/Vite production build exit 0; log `frontend-vitest-uat10.log`, `frontend-lint-uat10.log`, `frontend-build-uat10.log`.
- API E2E với H2 cô lập: **28/28**; Playwright Chromium: **12/12**, gồm 3 vai trò và responsive 360/390/555/1280/1440px. Bản chụp mới: `runs/2026-10-20-015604/screenshots/`; backend stdout/stderr cùng thư mục; log lệnh `uat10-full-e2e-run.log`.
- Test hồi quy UAT-10 xác nhận refresh token cũ không thể tự logout; access token còn hạn của chính session đó vẫn có thể logout kể cả khi body mang refresh token cũ. Sau logout, access token mới và refresh token mới đều bị từ chối. Focused backend 22/22 và frontend 8/8 cũng đạt.
- `scripts/run-e2e.ps1` trước đó ghim vào một đường dẫn JDK cục bộ không tồn tại trong workspace; nay chọn JDK 21 từ `JAVA_HOME` hoặc `javac` trên PATH, xác thực phiên bản và lưu log/ảnh theo lần chạy để tái kiểm chứng được.
- `git diff --check` đạt. Kiểm tra production chỉ đọc chưa chạy lại; production vẫn ở SHA nền `474b749...`. Bugbot/Security re-review diff UAT-10 đang chạy trước khi cập nhật PR #48.

## Regression cuối sau UAT-11 — 2026-10-20

- Security review phát hiện nhánh lock `navigator.locks.request()` reject/timeout trước callback chỉ dọn local auth mà chưa thử thu hồi session server. Đã thêm fallback: chỉ khi logout intent hiện tại còn hiệu lực, chụp cặp token đang lưu, dọn phiên local rồi thử đúng một POST revoke với timeout 2.5 giây. Nếu callback đã bắt đầu revoke nhưng request lỗi, không gửi bản sao.
- Regression Vitest cho lock reject và lock abort/timeout đều đạt. Frontend hiện đạt **17 files / 111 tests**; ESLint exit 0; TypeScript/Vite production build exit 0. API E2E H2 đạt **28/28**; Playwright Chromium đạt **15/15**, gồm luồng auth/job lỗi/chatbot, ba vai trò và các viewport 360/390/555/1280/1440 px. Maven backend `verify` gần nhất sau thay đổi backend UAT-10 đạt **327/327**; UAT-11 chỉ đổi frontend/test harness.
- Log Maven package, backend stdout/stderr, API E2E, Playwright, Vitest, ESLint và build được lưu tại `runs/2026-10-20-024948/`; có 125 ảnh QA trong `runs/2026-10-20-024948/screenshots/`. `scripts/run-e2e.ps1` lưu từng luồng lệnh thành log riêng. `git diff --check` đạt.
- Bugbot và Security Review trên diff cuối không còn finding có thể hành động. Production vẫn chưa nhận commit UAT này; bước tiếp theo là cập nhật PR #48, chờ CI mới pass, merge theo yêu cầu deploy của trưởng nhóm rồi kiểm tra CD và marker SHA trước production smoke chỉ đọc.
