-- ====================================================================
-- DỰ ÁN: TalentBridge - Nền tảng tuyển dụng trực tuyến (Online ATS)
-- HỌC PHẦN: Java Spring 2
-- MỤC TIÊU: Thiết kế CSDL chuẩn hóa 3NF tích hợp Hồ sơ TopCV & Bộ sinh CV (CV Builder)
-- HỆ QUẢN TRỊ CSDL: MySQL 8.0+ / MariaDB
-- ====================================================================

DROP DATABASE IF EXISTS `talentbridge_db`;
CREATE DATABASE `talentbridge_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `talentbridge_db`;

-- ====================================================================
-- PHÂN HỆ 1: XÁC THỰC & PHÂN QUYỀN (AUTHENTICATION & RBAC)
-- ====================================================================

-- 1. Bảng users: Tài khoản đăng nhập hệ thống
CREATE TABLE `users` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(150) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20) NULL,
    `avatar_url` VARCHAR(500) NULL,
    `status` VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, BANNED
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_users_email` (`email`),
    INDEX `idx_users_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng roles & user_roles: Phân quyền RBAC
CREATE TABLE `roles` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE -- ROLE_CANDIDATE, ROLE_RECRUITER, ROLE_ADMIN
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_roles` (
    `user_id` BIGINT NOT NULL,
    `role_id` INT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    CONSTRAINT `fk_user_roles_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_roles_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- PHÂN HỆ 2: HỒ SƠ ỨNG VIÊN CHI TIẾT & TOPCV PROFILE
-- ====================================================================

-- 3. Bảng candidates: Hồ sơ ứng viên chi tiết (Mở rộng cho CV Builder)
CREATE TABLE `candidates` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE,
    `title` VARCHAR(150) NULL, -- Vị trí nghề nghiệp mong muốn (vd: "Senior Java Developer")
    `dob` DATE NULL, -- Ngày sinh
    `gender` VARCHAR(10) NULL DEFAULT 'OTHER', -- MALE, FEMALE, OTHER
    `summary` TEXT NULL, -- Tóm tắt bản thân / Mục tiêu nghề nghiệp (Career Objective)
    `experience_years` INT DEFAULT 0,
    `current_salary` DECIMAL(12,2) NULL,
    `expected_salary` DECIMAL(12,2) NULL,
    `city` VARCHAR(100) NULL,
    `address` VARCHAR(255) NULL,
    `personal_website` VARCHAR(255) NULL, -- Portfolio / Website cá nhân
    `linkedin_url` VARCHAR(255) NULL,
    `github_url` VARCHAR(255) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_candidates_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    INDEX `idx_candidates_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng skills: Danh mục kỹ năng chuyên môn dùng chung
CREATE TABLE `skills` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng work_experiences: Lịch sử làm việc (TopCV Work History)
CREATE TABLE `work_experiences` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `candidate_id` BIGINT NOT NULL,
    `company_name` VARCHAR(200) NOT NULL,
    `position` VARCHAR(150) NOT NULL, -- Chức danh công việc (vd: Backend Developer, Tech Lead)
    `start_date` DATE NOT NULL,
    `end_date` DATE NULL, -- NULL nếu là công việc hiện tại
    `is_current` BOOLEAN DEFAULT FALSE,
    `description` TEXT NULL, -- Mô tả trách nhiệm & công việc đảm nhận
    `achievements` TEXT NULL, -- Thành tựu nổi bật / Key Accomplishments
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_work_exp_candidate` FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`id`) ON DELETE CASCADE,
    INDEX `idx_work_exp_candidate` (`candidate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Bảng educations: Lịch sử học vấn & bằng cấp (TopCV Education)
CREATE TABLE `educations` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `candidate_id` BIGINT NOT NULL,
    `institution_name` VARCHAR(200) NOT NULL, -- Tên trường đại học / cao đẳng / viện đào tạo
    `degree` VARCHAR(100) NOT NULL, -- Cử nhân, Kỹ sư, Thạc sĩ, Bằng nghề...
    `field_of_study` VARCHAR(150) NOT NULL, -- Chuyên ngành (vd: Kỹ thuật phần mềm, CNTT)
    `start_date` DATE NOT NULL,
    `end_date` DATE NULL,
    `is_current` BOOLEAN DEFAULT FALSE,
    `gpa` VARCHAR(20) NULL, -- Điểm trung bình (vd: "3.6 / 4.0" hoặc "Xuất sắc")
    `description` TEXT NULL, -- Đề tài khóa luận hoặc hoạt động nổi bật
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_educations_candidate` FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`id`) ON DELETE CASCADE,
    INDEX `idx_educations_candidate` (`candidate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Bảng candidate_skills: Kỹ năng của ứng viên (TopCV Skills Rating)
CREATE TABLE `candidate_skills` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `candidate_id` BIGINT NOT NULL,
    `skill_id` INT NOT NULL,
    `proficiency_level` VARCHAR(30) DEFAULT 'INTERMEDIATE', -- BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    `rating` TINYINT DEFAULT 3, -- Đánh giá sao (1-5 sao như TopCV)
    `years_of_experience` DECIMAL(3,1) DEFAULT 1.0,
    CONSTRAINT `fk_cand_skills_candidate` FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_cand_skills_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_cand_skill` (`candidate_id`, `skill_id`),
    INDEX `idx_cand_skills_candidate` (`candidate_id`),
    INDEX `idx_cand_skills_skill` (`skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Bảng candidate_projects: Dự án cá nhân & thực tế (TopCV Projects)
CREATE TABLE `candidate_projects` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `candidate_id` BIGINT NOT NULL,
    `project_name` VARCHAR(200) NOT NULL,
    `role` VARCHAR(100) NOT NULL, -- Vai trò trong dự án (vd: Backend Lead, Fullstack)
    `team_size` INT NULL, -- Số lượng thành viên nhóm
    `start_date` DATE NOT NULL,
    `end_date` DATE NULL,
    `is_current` BOOLEAN DEFAULT FALSE,
    `technologies` VARCHAR(500) NULL, -- Công nghệ sử dụng (vd: Java 21, Spring Boot, MySQL, Redis, Docker)
    `project_url` VARCHAR(500) NULL, -- Link website dự án / Demo
    `github_url` VARCHAR(500) NULL, -- Link mã nguồn
    `description` TEXT NULL, -- Giới thiệu dự án
    `responsibilities` TEXT NULL, -- Trách nhiệm & đóng góp cụ thể
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_projects_candidate` FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`id`) ON DELETE CASCADE,
    INDEX `idx_projects_candidate` (`candidate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Bảng candidate_certificates: Chứng chỉ chuyên môn (TopCV Certificates)
CREATE TABLE `candidate_certificates` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `candidate_id` BIGINT NOT NULL,
    `certificate_name` VARCHAR(200) NOT NULL, -- vd: "AWS Certified Solutions Architect"
    `issuing_organization` VARCHAR(200) NOT NULL, -- vd: "Amazon Web Services"
    `issue_date` DATE NOT NULL,
    `expiration_date` DATE NULL, -- NULL nếu chứng chỉ vĩnh viễn
    `credential_id` VARCHAR(100) NULL, -- Mã tra cứu chứng chỉ
    `credential_url` VARCHAR(500) NULL, -- Link xác thực trực tuyến
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_certificates_candidate` FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`id`) ON DELETE CASCADE,
    INDEX `idx_certificates_candidate` (`candidate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Bảng candidate_awards: Giải thưởng & Thành tựu (TopCV Awards)
CREATE TABLE `candidate_awards` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `candidate_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL, -- vd: "Giải Nhất Olympic Tin học Sinh viên"
    `organization` VARCHAR(200) NOT NULL, -- vd: "Hội Tin học Việt Nam"
    `issue_date` DATE NOT NULL,
    `description` TEXT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_awards_candidate` FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`id`) ON DELETE CASCADE,
    INDEX `idx_awards_candidate` (`candidate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- PHÂN HỆ 3: MẪU CV & BỘ SINH CV TỰ ĐỘNG (CV BUILDER & GENERATOR)
-- ====================================================================

-- 11. Bảng cv_templates: Kho mẫu CV cho tính năng Generate Resume từ Profile
CREATE TABLE `cv_templates` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL, -- Tên hiển thị (vd: "Modern IT Standard", "Executive Professional")
    `template_code` VARCHAR(50) NOT NULL UNIQUE, -- Mã code (MODERN_IT, CLASSIC_ELEGANT, MINIMALIST_TECH)
    `thumbnail_url` VARCHAR(500) NULL, -- Ảnh xem trước mẫu CV
    `description` VARCHAR(255) NULL,
    `default_config` JSON NULL, -- Cấu hình mặc định: primary_color, font_family, layout_type
    `is_active` BOOLEAN DEFAULT TRUE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Bảng resumes: Quản lý CV tải lên & CV tự động tạo từ Profile
CREATE TABLE `resumes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `candidate_id` BIGINT NOT NULL,
    `template_id` INT NULL, -- FK tới cv_templates nếu là CV sinh tự động
    `resume_type` VARCHAR(20) NOT NULL DEFAULT 'UPLOADED', -- UPLOADED (tải lên PDF), GENERATED (sinh từ profile)
    `title` VARCHAR(200) NOT NULL DEFAULT 'My Resume', -- Tiêu đề hồ sơ (vd: "CV Java Backend - 2026")
    `file_name` VARCHAR(255) NOT NULL,
    `file_url` VARCHAR(500) NOT NULL, -- Đường dẫn file PDF tải lên hoặc PDF do hệ thống render
    `file_type` VARCHAR(50) DEFAULT 'application/pdf',
    `is_default` BOOLEAN DEFAULT FALSE,
    `customization_json` JSON NULL, -- Tùy chọn giao diện: màu sắc, font, thứ tự hiển thị các khối section...
    `parsed_text` LONGTEXT NULL, -- Dùng cho tìm kiếm/AI matching
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_resumes_candidate` FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_resumes_template` FOREIGN KEY (`template_id`) REFERENCES `cv_templates` (`id`) ON DELETE SET NULL,
    INDEX `idx_resumes_candidate` (`candidate_id`),
    INDEX `idx_resumes_type` (`resume_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- PHÂN HỆ 4: DOANH NGHIỆP & NHÀ TUYỂN DỤNG (COMPANIES & RECRUITERS)
-- ====================================================================

-- 13. Bảng companies: Hồ sơ doanh nghiệp
CREATE TABLE `companies` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(200) NOT NULL,
    `logo_url` VARCHAR(500) NULL,
    `banner_url` VARCHAR(500) NULL,
    `website` VARCHAR(255) NULL,
    `industry` VARCHAR(150) NULL,
    `company_size` VARCHAR(50) NULL, -- ví dụ: "50-150 nhân viên"
    `address` VARCHAR(300) NOT NULL,
    `city` VARCHAR(100) NOT NULL,
    `description` TEXT NULL,
    `status` VARCHAR(20) DEFAULT 'PENDING', -- PENDING, APPROVED, REJECTED
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_companies_name` (`name`),
    INDEX `idx_companies_city` (`city`),
    INDEX `idx_companies_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Bảng recruiters: Nhà tuyển dụng thuộc công ty
CREATE TABLE `recruiters` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE,
    `company_id` BIGINT NULL,
    `position` VARCHAR(100) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_recruiters_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_recruiters_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`) ON DELETE SET NULL,
    INDEX `idx_recruiters_company` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- PHÂN HỆ 5: VIỆC LÀM & TÌM KIẾM (JOBS & CATEGORIES)
-- ====================================================================

-- 15. Bảng categories: Danh mục ngành nghề tuyển dụng
CREATE TABLE `categories` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `slug` VARCHAR(120) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. Bảng jobs: Tin tuyển dụng
CREATE TABLE `jobs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `company_id` BIGINT NOT NULL,
    `recruiter_id` BIGINT NOT NULL,
    `category_id` INT NOT NULL,
    `title` VARCHAR(255) NOT NULL,
    `description` LONGTEXT NOT NULL,
    `requirements` LONGTEXT NOT NULL,
    `benefits` TEXT NULL,
    `job_type` VARCHAR(50) NOT NULL, -- FULL_TIME, PART_TIME, REMOTE, HYBRID
    `experience_level` VARCHAR(50) NOT NULL, -- INTERN, FRESHER, JUNIOR, MIDDLE, SENIOR
    `salary_min` DECIMAL(12,2) NULL,
    `salary_max` DECIMAL(12,2) NULL,
    `is_negotiable` BOOLEAN DEFAULT FALSE,
    `city` VARCHAR(100) NOT NULL,
    `address` VARCHAR(300) NULL,
    `status` VARCHAR(20) DEFAULT 'ACTIVE', -- DRAFT, PENDING, ACTIVE, EXPIRED, CLOSED
    `deadline` DATE NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_jobs_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_jobs_recruiter` FOREIGN KEY (`recruiter_id`) REFERENCES `recruiters` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_jobs_category` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE RESTRICT,
    INDEX `idx_jobs_company` (`company_id`),
    INDEX `idx_jobs_category` (`category_id`),
    INDEX `idx_jobs_status` (`status`),
    INDEX `idx_jobs_city` (`city`),
    INDEX `idx_jobs_deadline` (`deadline`),
    INDEX `idx_jobs_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. Bảng job_skills: Liên kết N-N giữa Jobs và Skills yêu cầu
CREATE TABLE `job_skills` (
    `job_id` BIGINT NOT NULL,
    `skill_id` INT NOT NULL,
    PRIMARY KEY (`job_id`, `skill_id`),
    CONSTRAINT `fk_job_skills_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_job_skills_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. Bảng saved_jobs: Ứng viên lưu tin tuyển dụng yêu thích
CREATE TABLE `saved_jobs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `candidate_id` BIGINT NOT NULL,
    `job_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_saved_jobs_candidate` FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_saved_jobs_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_saved_job` (`candidate_id`, `job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- PHÂN HỆ 6: QUẢN LÝ TUYỂN DỤNG ATS & PHỎNG VẤN (ATS PIPELINE)
-- ====================================================================

-- 19. Bảng applications: Đơn ứng tuyển (ATS Core)
CREATE TABLE `applications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `job_id` BIGINT NOT NULL,
    `candidate_id` BIGINT NOT NULL,
    `resume_id` BIGINT NOT NULL,
    `cover_letter` TEXT NULL,
    `current_stage` VARCHAR(30) DEFAULT 'APPLIED', -- APPLIED, SCREENING, INTERVIEW, OFFER, HIRED, REJECTED
    `status` VARCHAR(20) DEFAULT 'SUBMITTED', -- SUBMITTED, IN_REVIEW, ACCEPTED, DECLINED
    `ai_match_score` DECIMAL(5,2) NULL, -- Điểm tương thích AI (0.00 - 100.00%)
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_applications_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_applications_candidate` FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_applications_resume` FOREIGN KEY (`resume_id`) REFERENCES `resumes` (`id`) ON DELETE RESTRICT,
    UNIQUE KEY `uk_job_candidate` (`job_id`, `candidate_id`),
    INDEX `idx_applications_stage` (`current_stage`),
    INDEX `idx_applications_job` (`job_id`),
    INDEX `idx_applications_candidate` (`candidate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. Bảng application_stages: Lịch sử chuyển vòng tuyển dụng của ứng viên
CREATE TABLE `application_stages` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `application_id` BIGINT NOT NULL,
    `stage` VARCHAR(30) NOT NULL, -- APPLIED, SCREENING, INTERVIEW, OFFER, HIRED, REJECTED
    `note` TEXT NULL,
    `changed_by_user_id` BIGINT NOT NULL,
    `changed_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_app_stages_app` FOREIGN KEY (`application_id`) REFERENCES `applications` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_app_stages_user` FOREIGN KEY (`changed_by_user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    INDEX `idx_app_stages_app` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. Bảng application_notes: Ghi chú nội bộ, tag và đánh giá ứng viên của HR
CREATE TABLE `application_notes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `application_id` BIGINT NOT NULL,
    `recruiter_id` BIGINT NOT NULL,
    `rating` TINYINT NULL, -- 1 đến 5 sao
    `tag` VARCHAR(50) NULL, -- vd: "Ưu tiên", "Pass Technical", "Lương thỏa thuận"
    `comment` TEXT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_app_notes_app` FOREIGN KEY (`application_id`) REFERENCES `applications` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_app_notes_recruiter` FOREIGN KEY (`recruiter_id`) REFERENCES `recruiters` (`id`) ON DELETE CASCADE,
    INDEX `idx_app_notes_app` (`application_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 22. Bảng interviews: Lịch phỏng vấn ứng viên
CREATE TABLE `interviews` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `application_id` BIGINT NOT NULL,
    `interview_time` DATETIME NOT NULL,
    `location_type` VARCHAR(20) NOT NULL DEFAULT 'ONLINE', -- ONLINE, OFFLINE
    `meeting_link_or_address` VARCHAR(500) NOT NULL,
    `notes` TEXT NULL,
    `status` VARCHAR(20) DEFAULT 'SCHEDULED', -- SCHEDULED, COMPLETED, CANCELLED, NO_SHOW
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_interviews_app` FOREIGN KEY (`application_id`) REFERENCES `applications` (`id`) ON DELETE CASCADE,
    INDEX `idx_interviews_app` (`application_id`),
    INDEX `idx_interviews_time` (`interview_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 23. Bảng notifications: Thông báo người dùng
CREATE TABLE `notifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `message` TEXT NOT NULL,
    `type` VARCHAR(50) NOT NULL, -- APPLICATION_STATUS, INTERVIEW_INVITE, SYSTEM
    `is_read` BOOLEAN DEFAULT FALSE,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    INDEX `idx_notifications_user` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ====================================================================
-- DỮ LIỆU KHỞI TẠO BAN ĐẦU (SEED DATA)
-- ====================================================================

-- 1. Chèn roles
INSERT INTO `roles` (`id`, `name`) VALUES 
(1, 'ROLE_ADMIN'),
(2, 'ROLE_RECRUITER'),
(3, 'ROLE_CANDIDATE');

-- 2. Chèn tài khoản Admin mặc định (Password: admin123 -> BCrypt hash)
INSERT INTO `users` (`id`, `email`, `password_hash`, `full_name`, `phone`, `status`) VALUES
(1, 'admin@talentbridge.vn', '$2a$10$eO0V4eL33S5jK79a5lM/kOCgN3w8wWdGqCqfK5yv3d0sYkMlh2D4.', 'Quản Trị Viên', '0901234567', 'ACTIVE');

INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES (1, 1);

-- 3. Chèn danh mục ngành nghề phổ biến
INSERT INTO `categories` (`name`, `slug`, `description`) VALUES
('Công nghệ Thông tin / Phần mềm', 'it-phan-mem', 'Lập trình viên, Kiểm thử, DevOps, AI Engineer'),
('Kinh doanh / Bán hàng', 'kinh-doanh-ban-hang', 'Sales B2B, B2C, Quản lý kinh doanh'),
('Marketing / Truyền thông', 'marketing-truyen-thong', 'Digital Marketing, Content, SEO, Brand Manager'),
('Kế toán / Tài chính', 'ke-toan-tai-chinh', 'Kế toán viên, Kiểm toán viên, Phân tích tài chính'),
('Thiết kế / UI-UX', 'thiet-ke-ui-ux', 'UI/UX Designer, Graphic Designer, 3D Artist'),
('Nhân sự / Tuyển dụng', 'nhan-su-tuyen-dung', 'Chuyên viên tuyển dụng, C&B, HR Generalist');

-- 4. Chèn danh mục kỹ năng chuyên môn phong phú
INSERT INTO `skills` (`name`) VALUES
('Java'), ('Spring Boot'), ('Spring Security'), ('MySQL'), ('PostgreSQL'),
('RESTful API'), ('Docker'), ('Git'), ('ReactJS'), ('Next.js'),
('TypeScript'), ('HTML5/CSS3'), ('Tailwind CSS'), ('Microservices'),
('Python'), ('Data Analysis'), ('Project Management'), ('Agile/Scrum'),
('Kubernetes'), ('Redis'), ('RabbitMQ'), ('Kafka'), ('AWS'),
('CI/CD'), ('Unit Testing / JUnit'), ('Figma'), ('Clean Architecture');

-- 5. Chèn danh mục Mẫu CV (CV Templates cho Resume Generator)
INSERT INTO `cv_templates` (`id`, `name`, `template_code`, `thumbnail_url`, `description`, `default_config`, `is_active`) VALUES
(1, 'Modern IT Professional', 'MODERN_IT_01', 'https://talentbridge.vn/templates/modern_it.png', 'Mẫu CV hiện đại chuyên biệt cho ngành IT & Phần mềm, tối ưu hiển thị kỹ năng và dự án', '{"primaryColor": "#1E40AF", "fontFamily": "Inter", "columns": 2, "layout": "sidebar-left"}', TRUE),
(2, 'Classic Elegant', 'CLASSIC_01', 'https://talentbridge.vn/templates/classic.png', 'Mẫu CV phong cách cổ điển, trang trọng, phù hợp cho ngành Kinh doanh, Quản lý & Tài chính', '{"primaryColor": "#1F2937", "fontFamily": "Merriweather", "columns": 1, "layout": "single-column"}', TRUE),
(3, 'Creative Minimalist', 'MINIMALIST_01', 'https://talentbridge.vn/templates/minimalist.png', 'Mẫu CV tối giản tinh tế, tập trung vào điểm nhấn kinh nghiệm và thành tựu cá nhân', '{"primaryColor": "#059669", "fontFamily": "Roboto", "columns": 2, "layout": "grid"}', TRUE);