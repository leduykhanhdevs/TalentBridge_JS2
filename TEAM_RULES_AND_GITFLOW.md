# Bộ Quy Tắc Làm Việc Nhóm & Quy Trình Git Flow: TalentBridge

> **Dự án**: TalentBridge – Nền tảng tuyển dụng trực tuyến (Java Spring 2)  
> **Trưởng nhóm (Team Leader / Reviewer)**: **Lê Duy Khánh**  
> **Các thành viên**: Trần Đình Tình, Nguyễn Phan Minh Hiếu, Đặng Trường Thịnh, Phan Thị Ánh Tuyền  
> **GitHub Repository**: [https://github.com/leduykhanhdevs/TalentBridge_JS2.git](https://github.com/leduykhanhdevs/TalentBridge_JS2.git)

---

## 1. Nguyên Tắc Cốt Lõi Về Phân Nhánh Git (Strict Git Branching Rules)

### ⚠️ QUY ĐỊNH BẮT BUỘC 100% THÀNH VIÊN PHẢI TUÂN THỦ:
1. **TUYỆT ĐỐI KHÔNG PUSH / COMMIT TRỰC TIẾP LÊN NHÁNH `main`**.
2. **KHÔNG TỰ Ý MERGE BẤT KỲ PULL REQUEST NÀO VÀO `main` HOẶC `dev`**.
3. **MỌI CODE MỚI PHẢI ĐƯỢC TẠO TRÊN NHÁNH RIÊNG (`feat/...`, `fix/...`) VÀ PHẢI TẠO PULL REQUEST (PR)**.
4. **DUY NHẤT TRƯỞNG NHÓM (LÊ DUY KHÁNH) ĐƯỢC QUYỀN DUYỆT (APPROVE) VÀ MERGE VÀO `main` / `dev`**.

---

## 2. Cấu Trúc Nhánh Trên GitHub

- `main`: Nhánh ổn định cao nhất (Production-ready). Chỉ chứa code đã được Trưởng nhóm kiểm thử và xác nhận chạy không lỗi. Dùng để nộp bài và demo với giảng viên.
- `dev`: Nhánh tích hợp (Development branch). Nơi hội tụ các tính năng từ các thành viên sau khi Trưởng nhóm review và merge.
- **Nhánh tính năng cá nhân (Feature branches)**: Đặt tên theo chuẩn:
  - `feat/<ten-thanh-vien>/<tinh-nang>`  
    *Ví dụ*:  
    - `feat/tinh/job-crud` (Trần Đình Tình làm CRUD Job)
    - `feat/hieu/company-ats-pipeline` (Nguyễn Phan Minh Hiếu làm ATS Pipeline)
    - `feat/thinh/candidate-profile-apply` (Đặng Trường Thịnh làm Profile & Apply)
    - `feat/tuyen/interview-notification` (Phan Thị Ánh Tuyền làm Lịch PV & Email)
    - `feat/khanh/auth-jwt-admin` (Lê Duy Khánh làm Security & Admin)
  - `fix/<ten-thanh-vien>/<ten-loi>` (Sửa lỗi phát sinh)
  - `docs/<ten-thanh-vien>/<tai-lieu>` (Cập nhật tài liệu)

---

## 3. Hướng Dẫn Thao Tác Git Từng Bước Cho Thành Viên

### Bước 1: Clone repo về máy (làm lần đầu)
```bash
git clone https://github.com/leduykhanhdevs/TalentBridge_JS2.git
cd TalentBridge_JS2
```

### Bước 2: Cập nhật code mới nhất từ nhánh `dev` trước khi làm việc
Mỗi ngày trước khi bắt đầu code, luôn kéo code mới nhất về:
```bash
git checkout dev
git pull origin dev
```

### Bước 3: Tạo nhánh mới cho tính năng được giao
```bash
# Ví dụ: Thành viên Tình tạo nhánh làm chức năng tìm kiếm việc làm
git checkout -b feat/tinh/job-search
```

### Bước 4: Viết code và commit đúng quy chuẩn (Conventional Commits)
Cú pháp commit:
`<type>(<scope>): <mô tả việc đã làm>`

- Ví dụ chuẩn:
  - `feat(job): add advanced search filter by salary and city`
  - `fix(auth): fix password encoding issue with bcrypt`
  - `docs(db): add relationship details in table schema`
  - `test(candidate): add unit test for apply job service`

```bash
git add .
git commit -m "feat(job): implement filter jobs by category and salary range"
```

### Bước 5: Chạy thử local và kiểm tra trước khi đẩy code
- Đảm bảo ứng dụng chạy thành công, không có lỗi biên dịch (build error).
- Không commit các file rác như `.idea/`, `target/`, `.vscode/`, `.env` chứa mật khẩu cá nhân.

### Bước 6: Đẩy nhánh cá nhân lên GitHub
```bash
git push -u origin feat/tinh/job-search
```

### Bước 7: Mở Pull Request (PR) & Báo Trưởng nhóm Review
1. Vào đường link GitHub repo: [https://github.com/leduykhanhdevs/TalentBridge_JS2.git](https://github.com/leduykhanhdevs/TalentBridge_JS2.git)
2. Bấm **Compare & pull request**.
3. **Base branch**: Chọn `dev` (hoặc `main` theo chỉ đạo của Leader).
4. **Compare branch**: Nhánh cá nhân của bạn (ví dụ `feat/tinh/job-search`).
5. Đặt tiêu đề PR rõ ràng, mô tả tóm tắt những gì đã làm, ảnh chụp màn hình chạy thử nghiệm (nếu có).
6. **Reviewer**: Tag chọn **@leduykhanhdevs** (Lê Duy Khánh).
7. Nhắn tin vào nhóm chat: *"Khánh ơi, mình vừa tạo PR cho module [Tên module], nhờ Khánh review giúp nhé!"*

---

## 4. Quy Trình Kiểm Duyệt Của Trưởng Nhóm (Lê Duy Khánh)

Khi nhận được Pull Request từ thành viên, Trưởng nhóm thực hiện theo checklist:

1. **Kiểm tra xung đột (Conflicts)**: Nếu có conflict, yêu cầu thành viên pull nhánh `dev` về rebase/merge và giải quyết conflict trên máy cá nhân trước.
2. **Kiểm tra chất lượng code (Code Review)**:
   - Code có viết theo cấu trúc chuẩn (Controller $\to$ Service $\to$ Repository $\to$ DTO)?
   - Có tuân thủ quy tắc clean code (không hardcode dữ liệu nhạy cảm, không thừa comment rác)?
   - Các API có validation dữ liệu đầu vào (`@Valid`, `@NotNull`)?
3. **Hành động của Leader**:
   - **Nếu cần chỉnh sửa**: Bấm **Request Changes** kèm comment giải thích lý do cụ thể vào từng dòng code.
   - **Nếu đạt yêu cầu**: Bấm **Approve** và thực hiện **Squash and Merge** hoặc **Merge Pull Request**.

---

## 5. Quy Tắc Giao Tiếp & Báo Cáo Tiến Độ (Standup & Deadline)

1. **Cập nhật tiến độ mỗi tối (Daily Update)**: Mỗi thành viên gửi 3 dòng ngắn gọn trước 22:00:
   - *Hôm nay đã hoàn thành:* ...
   - *Ngày mai sẽ làm:* ...
   - *Vướng mắc (Blocker) cần hỗ trợ:* ...
2. **Khi gặp khó khăn**: Không được im lặng quá 12 tiếng. Hãy chủ động tag Trưởng nhóm hoặc bạn cùng nhóm để hỗ trợ gỡ rối (pair programming).
3. **Tôn trọng Deadline**: Mốc thời gian thiết kế CSDL là Thứ Hai (14/09/2026). Các mốc chức năng tiếp theo sẽ được chốt trong bảng phân công.
