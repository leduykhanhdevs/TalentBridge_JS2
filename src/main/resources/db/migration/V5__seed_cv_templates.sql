-- V5: Seed standard CV Templates for Resume Builder in production and all environments
INSERT INTO `cv_templates` (`id`, `name`, `template_code`, `thumbnail_url`, `description`, `default_config`, `is_active`, `created_at`)
SELECT 1, 'Modern IT Professional', 'MODERN_IT_01', 'https://images.unsplash.com/photo-1586281380349-632531db7ed4?w=400&auto=format&fit=crop&q=60', 'Mẫu CV hiện đại chuyên biệt cho ngành IT & Phần mềm, tối ưu hiển thị kỹ năng và dự án', '{"primaryColor": "#2563EB", "fontFamily": "Inter", "columns": 2, "layout": "sidebar-left"}', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `cv_templates` WHERE `template_code` = 'MODERN_IT_01');

INSERT INTO `cv_templates` (`id`, `name`, `template_code`, `thumbnail_url`, `description`, `default_config`, `is_active`, `created_at`)
SELECT 2, 'Classic Elegant', 'CLASSIC_01', 'https://images.unsplash.com/photo-1512486130939-2c4f79935e4f?w=400&auto=format&fit=crop&q=60', 'Mẫu CV phong cách cổ điển, trang trọng, phù hợp cho ngành Kinh doanh, Quản lý & Tài chính', '{"primaryColor": "#1F2937", "fontFamily": "Merriweather", "columns": 1, "layout": "single-column"}', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `cv_templates` WHERE `template_code` = 'CLASSIC_01');

INSERT INTO `cv_templates` (`id`, `name`, `template_code`, `thumbnail_url`, `description`, `default_config`, `is_active`, `created_at`)
SELECT 3, 'Creative Minimalist', 'MINIMALIST_01', 'https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=400&auto=format&fit=crop&q=60', 'Mẫu CV tối giản tinh tế, tập trung vào điểm nhấn kinh nghiệm và thành tựu cá nhân', '{"primaryColor": "#059669", "fontFamily": "Roboto", "columns": 2, "layout": "grid"}', TRUE, NOW()
WHERE NOT EXISTS (SELECT 1 FROM `cv_templates` WHERE `template_code` = 'MINIMALIST_01');
