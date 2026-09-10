-- ====================================================================
-- DỰ ÁN: TalentBridge - Nền tảng tuyển dụng trực tuyến (Online ATS)
-- HỌC PHẦN: Java Spring 2
-- MỤC TIÊU: Thiết kế CSDL chuẩn hóa 3NF (Deadline hoàn thành: 14/09/2026)
-- HỆ QUẢN TRỊ CSDL: MySQL 8.0+ / MariaDB
-- ====================================================================

DROP DATABASE IF EXISTS `talentbridge_db`;
CREATE DATABASE `talentbridge_db` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `talentbridge_db`;

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

-- 3. Bảng candidates: Hồ sơ ứng viên chi tiết
CREATE TABLE `candidates` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE,
    `title` VARCHAR(150) NULL, -- Vị trí nghề nghiệp mong muốn
    `summary` TEXT NULL,
    `experience_years` INT DEFAULT 0,
    `current_salary` DECIMAL(12,2) NULL,
    `expected_salary` DECIMAL(12,2) NULL,
    `city` VARCHAR(100) NULL,
    `address` VARCHAR(255) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_candidates_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    INDEX `idx_candidates_city` (`city`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng resumes: Quản lý danh sách CV tải lên
CREATE TABLE `resumes` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `candidate_id` BIGINT NOT NULL,
    `file_name` VARCHAR(255) NOT NULL,
    `file_url` VARCHAR(500) NOT NULL,
    `file_type` VARCHAR(50) DEFAULT 'application/pdf',
    `is_default` BOOLEAN DEFAULT FALSE,
    `parsed_text` LONGTEXT NULL, -- Dùng cho tìm kiếm/AI matching
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_resumes_candidate` FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`id`) ON DELETE CASCADE,
    INDEX `idx_resumes_candidate` (`candidate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng companies: Hồ sơ doanh nghiệp
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

-- 6. Bảng recruiters: Nhà tuyển dụng thuộc công ty
CREATE TABLE `recruiters` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL UNIQUE,
    `company_id` BIGINT NOT NULL,
    `position` VARCHAR(100) NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_recruiters_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_recruiters_company` FOREIGN KEY (`company_id`) REFERENCES `companies` (`id`) ON DELETE CASCADE,
    INDEX `idx_recruiters_company` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. Bảng categories: Danh mục ngành nghề tuyển dụng
CREATE TABLE `categories` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `slug` VARCHAR(120) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Bảng skills: Kỹ năng chuyên môn
CREATE TABLE `skills` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Bảng jobs: Tin tuyển dụng
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

-- 10. Bảng job_skills: Liên kết N-N giữa Jobs và Skills
CREATE TABLE `job_skills` (
    `job_id` BIGINT NOT NULL,
    `skill_id` INT NOT NULL,
    PRIMARY KEY (`job_id`, `skill_id`),
    CONSTRAINT `fk_job_skills_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_job_skills_skill` FOREIGN KEY (`skill_id`) REFERENCES `skills` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Bảng saved_jobs: Ứng viên lưu tin tuyển dụng
CREATE TABLE `saved_jobs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `candidate_id` BIGINT NOT NULL,
    `job_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_saved_jobs_candidate` FOREIGN KEY (`candidate_id`) REFERENCES `candidates` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_saved_jobs_job` FOREIGN KEY (`job_id`) REFERENCES `jobs` (`id`) ON DELETE CASCADE,
    UNIQUE KEY `uk_saved_job` (`candidate_id`, `job_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Bảng applications: Đơn ứng tuyển (ATS Core)
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

-- 13. Bảng application_stages: Lịch sử chuyển vòng tuyển dụng của ứng viên
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

-- 14. Bảng application_notes: Ghi chú nội bộ, tag và đánh giá ứng viên của HR
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

-- 15. Bảng interviews: Lịch phỏng vấn ứng viên
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

-- 16. Bảng notifications: Thông báo người dùng
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

-- Chèn roles
INSERT INTO `roles` (`id`, `name`) VALUES 
(1, 'ROLE_ADMIN'),
(2, 'ROLE_RECRUITER'),
(3, 'ROLE_CANDIDATE');

-- Chèn tài khoản Admin mặc định (Password: admin123 -> BCrypt hash bên dưới)
INSERT INTO `users` (`id`, `email`, `password_hash`, `full_name`, `phone`, `status`) VALUES
(1, 'admin@talentbridge.vn', '$2a$10$eO0V4eL33S5jK79a5lM/kOCgN3w8wWdGqCqfK5yv3d0sYkMlh2D4.', 'Quản Trị Viên', '0901234567', 'ACTIVE');

INSERT INTO `user_roles` (`user_id`, `role_id`) VALUES (1, 1);

-- Chèn danh mục ngành nghề phổ biến
INSERT INTO `categories` (`name`, `slug`, `description`) VALUES
('Công nghệ Thông tin / Phần mềm', 'it-phan-mem', 'Lập trình viên, Kiểm thử, DevOps, AI Engineer'),
('Kinh doanh / Bán hàng', 'kinh-doanh-ban-hang', 'Sales B2B, B2C, Quản lý kinh doanh'),
('Marketing / Truyền thông', 'marketing-truyen-thong', 'Digital Marketing, Content, SEO, Brand Manager'),
('Kế toán / Tài chính', 'ke-toan-tai-chinh', 'Kế toán viên, Kiểm toán viên, Phân tích tài chính'),
('Thiết kế / UI-UX', 'thiet-ke-ui-ux', 'UI/UX Designer, Graphic Designer, 3D Artist'),
('Nhân sự / Tuyển dụng', 'nhan-su-tuyen-dung', 'Chuyên viên tuyển dụng, C&B, HR Generalist');

-- Chèn kỹ năng chuyên môn
INSERT INTO `skills` (`name`) VALUES
('Java'), ('Spring Boot'), ('Spring Security'), ('MySQL'), ('PostgreSQL'),
('RESTful API'), ('Docker'), ('Git'), ('ReactJS'), ('Next.js'),
('TypeScript'), ('HTML5/CSS3'), ('Tailwind CSS'), ('Microservices'),
('Python'), ('Data Analysis'), ('Project Management'), ('Agile/Scrum');
