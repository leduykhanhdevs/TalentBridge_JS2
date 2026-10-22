import { readFileSync, writeFileSync } from 'node:fs'
import { join, dirname } from 'node:path'
import { fileURLToPath } from 'node:url'
import {
    AlignmentType,
    BorderStyle,
    Document,
    HeadingLevel,
    ImageRun,
    Packer,
    Paragraph,
    Table,
    TableCell,
    TableRow,
    TextRun,
    WidthType,
} from 'docx'

const root = join(dirname(fileURLToPath(import.meta.url)), '..', '..')
const shots = join(root, 'docs', 'qa', 'mobile-qa-2026-10-22-fixes')
const out = join(root, 'docs', 'Bao_Cao_Sua_Loi_QA_Mobile_TalentBridge_2026-10-22.docx')

const p = (text, opts = {}) => new Paragraph({ spacing: { after: 120 }, children: [new TextRun({ text, ...opts })] })
const h1 = (text) => new Paragraph({ heading: HeadingLevel.HEADING_1, spacing: { before: 240, after: 120 }, children: [new TextRun(text)] })
const h2 = (text) => new Paragraph({ heading: HeadingLevel.HEADING_2, spacing: { before: 200, after: 100 }, children: [new TextRun(text)] })
const bullet = (text) => new Paragraph({ bullet: { level: 0 }, children: [new TextRun(text)] })
const labeled = (label, text) => new Paragraph({ spacing: { after: 80 }, children: [new TextRun({ text: `${label}: `, bold: true }), new TextRun(text)] })

const image = (file, caption) => [
    new Paragraph({
        alignment: AlignmentType.CENTER,
        children: [new ImageRun({ type: 'png', data: readFileSync(join(shots, file)), transformation: { width: 260, height: 563 } })],
    }),
    new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 200 }, children: [new TextRun({ text: caption, italics: true, size: 18 })] }),
]

const cell = (text, bold = false) => new TableCell({ children: [new Paragraph({ children: [new TextRun({ text, bold, size: 20 })] })] })
const border = { style: BorderStyle.SINGLE, size: 4, color: '999999' }
const table = (rows) => new Table({
    width: { size: 100, type: WidthType.PERCENTAGE },
    borders: { top: border, bottom: border, left: border, right: border, insideHorizontal: border, insideVertical: border },
    rows: rows.map((r, i) => new TableRow({ children: r.map((c) => cell(c, i === 0)) })),
})

const bugs = [
    {
        id: 'TB-01 (P1) — CV Builder không lưu CV vào hồ sơ',
        cause: [
            'Bảng cv_templates không có Flyway migration seed; DataInitializer không chạy ở profile prod → API ném "Không tìm thấy mẫu CV với mã: MODERN_IT_01".',
            'ResumeUseCaseImpl ném lỗi ngay khi thiếu template thay vì fallback.',
            'CvBuilderModal đặt thông báo kết quả trong panel "Chỉnh sửa", bị ẩn khi đang ở tab "Xem trước" trên mobile.',
        ],
        fix: [
            'Thêm V5__seed_cv_templates.sql seed 3 mẫu (MODERN_IT_01, CLASSIC_01, MINIMALIST_01), idempotent với WHERE NOT EXISTS.',
            'ResumeUseCaseImpl: fallback template tự phục hồi; làm sạch ký tự xuống dòng/không in được trước khi render PDFBox.',
            'CvBuilderModal: banner trạng thái (role="alert") hiển thị chung cho cả hai tab.',
        ],
        shot: ['tb01-cv-builder-saved-mobile.png', 'Hình 1 — TB-01: Tab "Xem trước" hiển thị "Tạo CV và lưu vào hồ sơ thành công!" (390×844, localhost)'],
    },
    {
        id: 'TB-02 (P2) — Giá trị mặc định số năm kinh nghiệm kỹ năng không hợp lệ',
        cause: ['Input có min="0.1", step="0.5" → giá trị mặc định 1.0 không thuộc dãy bước hợp lệ (0.1, 0.6, 1.1…) nên trình duyệt chặn submit.'],
        fix: [
            'CandidateSkillModal: step="0.1", min="0.1", max="50"; kiểm tra client 0.1 ≤ năm ≤ 50.',
            'CandidateSkillRequest: thêm @DecimalMin("0.1") và @DecimalMax("50.0").',
        ],
        shot: ['tb02-skill-default-valid-mobile.png', 'Hình 2 — TB-02: Giá trị mặc định 1.0 hợp lệ (checkValidity() = true) (390×844, localhost)'],
    },
    {
        id: 'TB-03 (P2) — Cho phép lưu ngày sinh trong tương lai',
        cause: ['Cả frontend và backend đều không kiểm tra dob > ngày hiện tại.'],
        fix: [
            'Backend: @PastOrPresent trên UpdateCandidateProfileRequest.dob + kiểm tra theo múi giờ Asia/Ho_Chi_Minh trong UpdateCandidateProfileUseCaseImpl.',
            'Frontend: getVietnamTodayString(), thuộc tính max cho input ngày, validate khi thay đổi và khi submit, hiển thị lỗi ngay dưới ô nhập.',
        ],
        shot: ['tb03-future-birthdate-blocked-mobile.png', 'Hình 3 — TB-03: Nhập 01/02/2030 bị chặn với thông báo "Ngày sinh không được ở trong tương lai" (390×844, localhost)'],
    },
    {
        id: 'TB-04 (P2) — Hàng chờ duyệt doanh nghiệp tính cả hồ sơ demo',
        cause: ['Các query searchCompanies/countSearchCompanies/findByStatus/countByStatus lọc theo status nhưng không loại is_demo → 20 hồ sơ demo PENDING làm sai hàng chờ.'],
        fix: ['CompanyJpaRepository: khi status = PENDING bắt buộc c.isDemo = false; tab "Tất cả hồ sơ" vẫn tra cứu được hồ sơ demo.'],
        shot: ['tb04-admin-pending-no-demo-mobile.png', 'Hình 4 — TB-04: Tab "Chờ phê duyệt" = 0, tìm "DEMO" trả 0 kết quả (390×844, localhost)'],
    },
]

const children = [
    new Paragraph({ heading: HeadingLevel.TITLE, alignment: AlignmentType.CENTER, children: [new TextRun('BÁO CÁO SỬA LỖI QA MOBILE — TALENTBRIDGE')] }),
    new Paragraph({ alignment: AlignmentType.CENTER, spacing: { after: 240 }, children: [new TextRun({ text: 'Ngày 22/10/2026 · Nhánh fix/tinh/mobile-qa-20261022 (từ origin/dev)', italics: true })] }),
    h1('1. Mục tiêu'),
    p('Khắc phục dứt điểm 4 lỗi trong báo cáo QA mobile ngày 22/10/2026, kiểm thử tự động và chụp ảnh minh chứng thực tế (không có con trỏ/nhãn công cụ).'),
    h1('2. Tổng hợp'),
    table([
        ['Mã', 'Mức', 'Mô tả', 'Trạng thái'],
        ['TB-01', 'P1', 'CV Builder không lưu CV vào hồ sơ', 'Đã sửa · PASS'],
        ['TB-02', 'P2', 'Mặc định số năm kinh nghiệm kỹ năng không hợp lệ', 'Đã sửa · PASS'],
        ['TB-03', 'P2', 'Cho phép ngày sinh tương lai', 'Đã sửa · PASS'],
        ['TB-04', 'P2', 'Hàng chờ duyệt tính hồ sơ demo', 'Đã sửa · PASS'],
    ]),
    h1('3. Chi tiết từng lỗi'),
]

for (const b of bugs) {
    children.push(h2(b.id))
    children.push(labeled('Nguyên nhân', ''))
    b.cause.forEach((c) => children.push(bullet(c)))
    children.push(labeled('Thay đổi mã nguồn', ''))
    b.fix.forEach((f) => children.push(bullet(f)))
    children.push(labeled('Kết quả', 'PASS — đã xác minh bằng test tự động và Playwright trên viewport mobile.'))
    children.push(...image(b.shot[0], b.shot[1]))
}

children.push(
    h1('4. Kết quả kiểm thử'),
    table([
        ['Hạng mục', 'Lệnh', 'Kết quả'],
        ['Backend (JUnit)', 'mvnw test -Dtest=AdminControllerTest,CandidateProfileValidationTest,ResumeUploadIntegrationTest', '26/26 PASS'],
        ['Frontend unit (Vitest)', 'pnpm test', '21 files / 131 tests PASS'],
        ['Frontend lint', 'pnpm lint', 'PASS'],
        ['Frontend build', 'pnpm build', 'PASS'],
        ['E2E mobile (Playwright, Chrome)', 'scripts/run-mobile-qa-verification.ps1', '2/2 PASS'],
    ]),
    h1('5. Môi trường kiểm thử'),
    bullet('Localhost — backend Spring Boot 3.3.4, profile e2e, H2 in-memory cô lập (port 18080), Flyway V1–V5.'),
    bullet('Frontend Vite dev server (port 5174), Google Chrome headless, viewport 390×844.'),
    bullet('Không kiểm thử trên production; migration V5 sẽ tự seed cv_templates khi deploy lên Render.'),
    h1('6. Kết luận'),
    p('Cả 4 lỗi đã được sửa ở tầng gốc (dữ liệu, backend, frontend) và xác minh tự động. Trong quá trình kiểm thử phát hiện thêm migration V5 tham chiếu cột updated_at không tồn tại — đã sửa trước khi bàn giao.'),
)

const doc = new Document({ creator: 'TalentBridge QA', title: 'Báo cáo sửa lỗi QA Mobile 22/10/2026', sections: [{ children }] })
writeFileSync(out, await Packer.toBuffer(doc))
console.log(`Wrote ${out}`)
