# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: reliability-workflows.spec.ts >> expired-token logout returns to login without waiting for a stalled refresh
- Location: e2e\reliability-workflows.spec.ts:168:1

# Error details

```
Error: expect(page).toHaveURL(expected) failed

Expected pattern: /\/login$/
Received string:  "http://127.0.0.1:5174/"
Timeout: 1500ms

Call log:
  - Expect "toHaveURL" with timeout 1500ms
    16 × locator resolved to <html lang="vi">…</html>
       - unexpected value "http://127.0.0.1:5174/"

```

```yaml
- banner:
  - link "TalentBridge — Trang chủ":
    - /url: /
    - text: TalentBridge
  - navigation "Điều hướng chính":
    - link "Trang chủ":
      - /url: /
    - link "Việc làm":
      - /url: /jobs
    - link "Trợ lý":
      - /url: /assistant
  - link "Đăng nhập":
    - /url: /login
  - link "Đăng ký":
    - /url: /register
- main:
  - paragraph: Kết nối đúng người, đúng cơ hội
  - heading "Bước tiếp theo cho sự nghiệp của bạn." [level=1]
  - paragraph: Khám phá việc làm với thông tin rõ ràng. Xây dựng hồ sơ để nhà tuyển dụng hiểu đúng năng lực của bạn.
  - link "Khám phá việc làm":
    - /url: /jobs
  - link "Hoàn thiện hồ sơ":
    - /url: /candidate/profile
  - text: Thông tin tuyển dụng rõ ràng Quy trình rõ ràng Bạn kiểm soát hồ sơ
  - img "Minh họa hồ sơ ứng viên và cơ hội việc làm được kết nối": HỒ SƠ MẪU Backend Developer Cơ hội phù hợp Thông tin rõ ràng
  - paragraph: Cơ hội nổi bật
  - heading "Tìm một công việc đáng để bắt đầu" [level=2]
  - paragraph: Xem trước mức lương, địa điểm và yêu cầu trước khi mở tin tuyển dụng.
  - link "Xem tất cả việc làm":
    - /url: /jobs
  - article:
    - heading "E2E Job sẽ duyệt 1792434616315" [level=3]:
      - link "E2E Job sẽ duyệt 1792434616315":
        - /url: /jobs/101
    - paragraph: VinAI Research Lab 1792434616315
    - text: Hà Nội Toàn thời gian Senior Java Spring Boot Thỏa thuận
    - link "Xem chi tiết":
      - /url: /jobs/101
  - article:
    - heading "MÔ PHỎNG · Product Designer UX/UI" [level=3]:
      - link "MÔ PHỎNG · Product Designer UX/UI":
        - /url: /jobs/70
    - text: Tin mô phỏng · Không nhận hồ sơ
    - paragraph: DEMO Sóng Xanh IoT
    - text: Đà Nẵng Làm việc kết hợp Middle Figma UX Research 15 - 30 triệu VNĐ
    - link "Xem chi tiết":
      - /url: /jobs/70
- contentinfo: TalentBridge · Kết nối nghề nghiệp có trách nhiệm Thông tin tuyển dụng do doanh nghiệp cung cấp
- button "Mở nhanh Trợ lý TalentBridge"
```

# Test source

```ts
  111 |     await page.getByLabel('Mật khẩu').fill(password)
  112 |     await page.getByRole('checkbox').check()
  113 |     await page.getByRole('button', { name: 'Tạo tài khoản' }).click()
  114 |
  115 |     await expect(page.getByRole('status')).toContainText('Đăng ký thành công')
  116 |     await expect(page).toHaveURL(/\/$/, { timeout: 10_000 })
  117 |     await expect(page.getByRole('button', { name: 'Đăng xuất' })).toBeVisible()
  118 |
  119 |     await page.getByRole('button', { name: 'Đăng xuất' }).click()
  120 |     await expect(page).toHaveURL(/\/login$/)
  121 |     expect(await page.evaluate(() => localStorage.getItem('talentbridge_access_token'))).toBeNull()
  122 |
  123 |     await page.getByLabel('Email').fill(email)
  124 |     await page.getByLabel('Mật khẩu').fill(password)
  125 |     await page.getByRole('button', { name: 'Đăng nhập' }).click()
  126 |     await expect(page).toHaveURL(/\/$/, { timeout: 10_000 })
  127 |     await expect(page.getByRole('button', { name: 'Đăng xuất' })).toBeVisible()
  128 | })
  129 |
  130 | test('a stalled server logout still clears the local session and returns to login', async ({ page }) => {
  131 |     test.setTimeout(10_000)
  132 |     await page.addInitScript(() => {
  133 |         localStorage.setItem('talentbridge_access_token', 'qa-candidate-access-token')
  134 |         localStorage.setItem('talentbridge_auth_user', JSON.stringify({
  135 |             id: 903,
  136 |             email: 'qa.candidate@example.invalid',
  137 |             fullName: 'QA Candidate',
  138 |             status: 'ACTIVE',
  139 |             roles: ['ROLE_CANDIDATE'],
  140 |         }))
  141 |     })
  142 |
  143 |     let releaseRequest: (() => void) | undefined
  144 |     await page.route('**/api/v1/auth/logout', async (route) => {
  145 |         await new Promise<void>((resolve) => { releaseRequest = resolve })
  146 |         try {
  147 |             await route.fulfill({ status: 200, contentType: 'application/json', body: JSON.stringify({ statusCode: 200 }) })
  148 |         } catch {
  149 |             // A timed-out client request may already be closed.
  150 |         }
  151 |     })
  152 |
  153 |     await page.route('**/api/v1/jobs?*', async (route) => {
  154 |         await route.fulfill({
  155 |             status: 200,
  156 |             contentType: 'application/json',
  157 |             body: JSON.stringify({ statusCode: 200, message: 'ok', data: { content: [], totalElements: 0, totalPages: 0, pageNumber: 1, pageSize: 6 } }),
  158 |         })
  159 |     })
  160 |
  161 |     await page.goto('/')
  162 |     await page.getByRole('button', { name: 'Đăng xuất' }).click()
  163 |     await expect(page).toHaveURL(/\/login$/, { timeout: 5_000 })
  164 |     expect(await page.evaluate(() => localStorage.getItem('talentbridge_access_token'))).toBeNull()
  165 |     releaseRequest?.()
  166 | })
  167 |
  168 | test('expired-token logout returns to login without waiting for a stalled refresh', async ({ page }) => {
  169 |     test.setTimeout(10_000)
  170 |     await page.addInitScript(() => {
  171 |         localStorage.setItem('talentbridge_access_token', 'qa-expired-access-token')
  172 |         localStorage.setItem('talentbridge_refresh_token', 'qa-refresh-token')
  173 |         localStorage.setItem('talentbridge_auth_user', JSON.stringify({
  174 |             id: 904,
  175 |             email: 'qa.expired@example.invalid',
  176 |             fullName: 'QA Expired Candidate',
  177 |             status: 'ACTIVE',
  178 |             roles: ['ROLE_CANDIDATE'],
  179 |         }))
  180 |     })
  181 |
  182 |     let releaseRefresh: (() => void) | undefined
  183 |     let logoutCalls = 0
  184 |     await page.route('**/api/v1/auth/logout', async (route) => {
  185 |         logoutCalls += 1
  186 |         await route.fulfill(logoutCalls === 1
  187 |             ? { status: 401, contentType: 'application/json', body: '{"message":"expired"}' }
  188 |             : { status: 204 })
  189 |     })
  190 |     await page.route('**/api/v1/auth/refresh-token', async (route) => {
  191 |         await new Promise<void>((resolve) => { releaseRefresh = resolve })
  192 |         await route.fulfill({
  193 |             status: 200,
  194 |             contentType: 'application/json',
  195 |             body: JSON.stringify({
  196 |                 statusCode: 200,
  197 |                 message: 'ok',
  198 |                 data: {
  199 |                     accessToken: 'qa-fresh-access-token',
  200 |                     refreshToken: 'qa-rotated-refresh-token',
  201 |                     tokenType: 'Bearer',
  202 |                     expiresInMs: 60_000,
  203 |                     user: { id: 904, email: 'qa.expired@example.invalid', fullName: 'QA Expired Candidate', status: 'ACTIVE', roles: ['ROLE_CANDIDATE'] },
  204 |                 },
  205 |             }),
  206 |         })
  207 |     })
  208 |
  209 |     await page.goto('/')
  210 |     await page.getByRole('button', { name: 'Đăng xuất' }).click()
> 211 |     await expect(page).toHaveURL(/\/login$/, { timeout: 1_500 })
      |                        ^ Error: expect(page).toHaveURL(expected) failed
  212 |     expect(await page.evaluate(() => localStorage.getItem('talentbridge_access_token'))).toBeNull()
  213 |     releaseRefresh?.()
  214 |     await expect.poll(() => logoutCalls).toBe(2)
  215 | })
  216 |
```
