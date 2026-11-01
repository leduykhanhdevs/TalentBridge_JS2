# Job search QA fixes — 2026-11-01

Base: origin/dev (252549c). Member branch: codex/fix-job-search-qa-dev-20261101.
Scope excludes TB-001. No production requests, database migrations, deploys, or shared-branch merges are part of this change.

## Changes

- TB-002: deduplicate city segments while preserving the simulation label and street address.
- TB-003: expose NO_EXPERIENCE and UNDER_1_YEAR already supported by recruiter data and backend search.
- TB-004: numeric salary conditions exclude negotiable/undisclosed pay; search and count use the same predicate. Retain the existing overlapping-range semantics for disclosed pay.
- TB-005: append unique job id to public search ordering to make tied sort values deterministic. Concurrent insertion/deletion can still shift offset pagination.
- TB-006: persist page, size, applied filters and sort in the list URL; initialize the form on returning from details. Browser Back and reload restore the selected page. Scroll position restoration is not included.

## Acceptance

- Frontend: 135 unit tests passed; production TypeScript/Vite build passed; full ESLint passed.
- Backend: JobSearchRegressionTest, JobUseCaseImplTest and JobManagementIntegrationTest — 28 tests passed, H2 only.
- Salary regression covers lower-only, upper-only, bounded, no-match, unfiltered and count consistency.
- Pagination regression uses 22 jobs sharing all sort values, traverses all pages for createdAt/title/minSalary/maxSalary in both directions, and asserts exact unique ids.
- UI: 4/4 tests passed (deduplicated city, both experience options, Back/reload page 3 with job 82, Back preserving all search settings). Playwright uses a local Vite server and intercepts all APIs; external requests are blocked. This verifies frontend behavior, not a live backend.

Commands:

    pnpm --dir frontend install --frozen-lockfile --ignore-scripts
    pnpm --dir frontend test
    pnpm --dir frontend build
    pnpm --dir frontend lint
    pnpm --dir frontend exec playwright test --config playwright.job-search.config.ts
    ./mvnw.cmd -Dtest=JobSearchRegressionTest,JobUseCaseImplTest,JobManagementIntegrationTest test

The previous API test explicitly required negotiable jobs in salary-filtered results. It was updated to the requirement confirmed by the user; it now asserts only disclosed matching pay and the matching count.

The original checkout has an existing user modification in docs/PROGRESS_REPORT.md. Work was isolated in a separate worktree; that modification is not part of this branch.
