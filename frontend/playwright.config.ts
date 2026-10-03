import { defineConfig } from '@playwright/test'

const screenshotDirectory = process.env.TALENTBRIDGE_E2E_SCREENSHOT_DIR

if (process.env.TALENTBRIDGE_E2E_DATA_ISOLATED !== 'true' || !screenshotDirectory) {
    throw new Error('UI E2E cần chạy qua scripts/run-e2e.ps1 để dùng backend H2 cô lập và lưu ảnh QA.')
}

export default defineConfig({
    testDir: './e2e',
    testMatch: '**/*.spec.ts',
    fullyParallel: false,
    workers: 1,
    reporter: 'list',
    timeout: 90_000,
    expect: { timeout: 10_000 },
    use: {
        baseURL: 'http://127.0.0.1:5174',
        browserName: 'chromium',
        headless: true,
        viewport: { width: 1280, height: 900 },
        trace: 'retain-on-failure',
    },
    webServer: {
        command: 'npm run dev -- --host 127.0.0.1 --port 5174 --strictPort',
        url: 'http://127.0.0.1:5174/login',
        reuseExistingServer: false,
        timeout: 30_000,
        env: {
            TALENTBRIDGE_API_PROXY_TARGET: 'http://127.0.0.1:18080',
        },
    },
})
