import { defineConfig } from '@playwright/test'

// This suite intercepts every API call; it never needs a real backend/database.
export default defineConfig({
    testDir: './e2e', testMatch: 'job-search-regression.spec.ts', workers: 1,
    reporter: 'list', timeout: 30000,
    use: { baseURL: 'http://127.0.0.1:5174', headless: true, trace: 'retain-on-failure' },
    webServer: {
        command: 'npm run dev -- --host 127.0.0.1 --port 5174 --strictPort',
        url: 'http://127.0.0.1:5174/jobs', reuseExistingServer: false,
    },
})
