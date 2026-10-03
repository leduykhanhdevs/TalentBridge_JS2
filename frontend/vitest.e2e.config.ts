import { defineConfig, mergeConfig } from 'vitest/config'
import viteConfig from './vite.config.ts'

export default mergeConfig(viteConfig, defineConfig({
    test: {
        include: ['e2e/**/*.test.ts'],
        testTimeout: 30_000,
        hookTimeout: 30_000,
    },
}))
