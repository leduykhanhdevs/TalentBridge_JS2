-- Demo records are intentionally explicit and non-actionable.
-- Company profiles stay PENDING and carry is_demo=true; demo jobs are clearly
-- labeled and the application use case rejects submissions for them.
ALTER TABLE companies ADD COLUMN is_demo BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE jobs ADD COLUMN is_demo BOOLEAN NOT NULL DEFAULT FALSE;

INSERT INTO companies (
    name, logo_url, website, industry, company_size, address, city, tax_code,
    description, status, created_by_user_id, is_demo
)
SELECT demo.name, NULL, NULL, demo.industry, 'Quy mô mô phỏng',
       'Địa chỉ mô phỏng — không có văn phòng thật', demo.city, NULL,
       'Hồ sơ doanh nghiệp hư cấu phục vụ trình diễn TalentBridge. Không đại diện nhà tuyển dụng thật và không có hoạt động tuyển dụng thực tế.',
       'PENDING', NULL, TRUE
FROM (
    SELECT 'DEMO Sao Bắc Software' AS name, 'Phần mềm mô phỏng' AS industry, 'Hà Nội' AS city UNION ALL
    SELECT 'DEMO Mây Mới Fintech', 'Fintech mô phỏng', 'TP. Hồ Chí Minh' UNION ALL
    SELECT 'DEMO Hạt Nắng Commerce', 'Thương mại điện tử mô phỏng', 'Đà Nẵng' UNION ALL
    SELECT 'DEMO Dòng Sông Data Lab', 'Dữ liệu mô phỏng', 'Cần Thơ' UNION ALL
    SELECT 'DEMO Nếp Gấp Product Studio', 'Sản phẩm số mô phỏng', 'Hà Nội' UNION ALL
    SELECT 'DEMO Rừng Mã AI', 'Trí tuệ nhân tạo mô phỏng', 'TP. Hồ Chí Minh' UNION ALL
    SELECT 'DEMO Vệt Sao Cloud', 'Điện toán đám mây mô phỏng', 'Đà Nẵng' UNION ALL
    SELECT 'DEMO Chồi Xanh AgriTech', 'Nông nghiệp công nghệ mô phỏng', 'Cần Thơ' UNION ALL
    SELECT 'DEMO Cánh Buồm Logistics', 'Logistics mô phỏng', 'Hà Nội' UNION ALL
    SELECT 'DEMO Đom Đóm HealthTech', 'Y tế số mô phỏng', 'TP. Hồ Chí Minh' UNION ALL
    SELECT 'DEMO Đường Chân Trời EdTech', 'Giáo dục số mô phỏng', 'Đà Nẵng' UNION ALL
    SELECT 'DEMO Hạt Cát Cybersecurity', 'An ninh mạng mô phỏng', 'Cần Thơ' UNION ALL
    SELECT 'DEMO Gốm Mộc Design', 'Thiết kế sản phẩm số mô phỏng', 'Hà Nội' UNION ALL
    SELECT 'DEMO Bản Đồ SaaS', 'Phần mềm dịch vụ mô phỏng', 'TP. Hồ Chí Minh' UNION ALL
    SELECT 'DEMO Sóng Xanh IoT', 'Internet vạn vật mô phỏng', 'Đà Nẵng' UNION ALL
    SELECT 'DEMO Vườn Nhỏ Mobile', 'Ứng dụng di động mô phỏng', 'Cần Thơ' UNION ALL
    SELECT 'DEMO Nhịp Cầu QA Labs', 'Kiểm thử phần mềm mô phỏng', 'Hà Nội' UNION ALL
    SELECT 'DEMO Vùng Trời DevOps', 'DevOps mô phỏng', 'TP. Hồ Chí Minh' UNION ALL
    SELECT 'DEMO Phố Mới RetailTech', 'Bán lẻ công nghệ mô phỏng', 'Đà Nẵng' UNION ALL
    SELECT 'DEMO Ánh Dương Analytics', 'Phân tích dữ liệu mô phỏng', 'Cần Thơ'
) demo
WHERE NOT EXISTS (
    SELECT 1 FROM companies existing WHERE existing.name = demo.name
);

INSERT INTO jobs (
    company_id, recruiter_user_id, title, description, requirements, benefits,
    location, city, address, job_type, experience_level, min_salary, max_salary,
    is_negotiable, status, deadline, is_demo
)
SELECT c.id, NULL,
       CONCAT('MÔ PHỎNG · ', role.role_name),
       CONCAT(
           'TIN TUYỂN DỤNG MÔ PHỎNG — ', role.role_name, ' tại ', c.name, '. ',
           'Dữ liệu giả lập để trình diễn tìm kiếm và ATS trên TalentBridge; ',
           'không đại diện vị trí đang tuyển của doanh nghiệp thật và không nhận hồ sơ. ',
           role.description
       ),
       CONCAT('Yêu cầu minh họa: ', role.requirements, ' Đây là nội dung mẫu, không phải yêu cầu tuyển dụng thật.'),
       'Quyền lợi mô phỏng; không phải chế độ của nhà tuyển dụng thật.',
       CONCAT('Địa điểm mô phỏng · ', c.city), c.city,
       'Địa điểm mô phỏng — không có văn phòng thật', role.job_type,
       role.experience_level, role.min_salary, role.max_salary,
       FALSE, 'ACTIVE', NULL, TRUE
FROM companies c
CROSS JOIN (
    SELECT 'Backend Java Developer' AS role_name, 'FULL_TIME' AS job_type,
           'MIDDLE' AS experience_level, 18000000 AS min_salary, 35000000 AS max_salary,
           'Phát triển API và dịch vụ backend mẫu bằng Java, Spring Boot và cơ sở dữ liệu quan hệ.' AS description,
           'Nắm kiến thức Java, REST API, SQL và quy trình Git.' AS requirements
    UNION ALL SELECT 'Frontend React Developer', 'HYBRID', 'JUNIOR', 16000000, 32000000,
           'Xây dựng giao diện web mẫu bằng React, TypeScript và CSS thích ứng.',
           'Biết React, TypeScript, HTML/CSS và cách gọi REST API.'
    UNION ALL SELECT 'QA Automation Engineer', 'FULL_TIME', 'MIDDLE', 14000000, 28000000,
           'Xây dựng kịch bản kiểm thử tự động và kiểm thử API cho sản phẩm mô phỏng.',
           'Hiểu kiểm thử phần mềm, viết test case và có trải nghiệm với Playwright hoặc Selenium.'
    UNION ALL SELECT 'Data Analyst', 'REMOTE', 'JUNIOR', 15000000, 30000000,
           'Phân tích bộ dữ liệu mẫu, viết truy vấn và trình bày kết quả bằng dashboard.',
           'Nắm SQL, xử lý dữ liệu cơ bản và biết trực quan hóa bằng công cụ BI.'
    UNION ALL SELECT 'Product Designer UX/UI', 'HYBRID', 'MIDDLE', 15000000, 30000000,
           'Thiết kế luồng và giao diện cho sản phẩm số minh họa, có prototype và tiêu chí dễ dùng.',
           'Biết Figma, thiết kế responsive, kiểm thử khả năng sử dụng và trình bày quyết định thiết kế.'
) role
WHERE c.is_demo = TRUE
  AND c.name LIKE 'DEMO %'
  AND NOT EXISTS (
      SELECT 1 FROM jobs existing
      WHERE existing.company_id = c.id
        AND existing.title = CONCAT('MÔ PHỎNG · ', role.role_name)
  );

INSERT INTO skills (name)
SELECT demo_skill.name
FROM (
    SELECT 'Java' AS name UNION ALL SELECT 'Spring Boot' UNION ALL SELECT 'REST API' UNION ALL
    SELECT 'React' UNION ALL SELECT 'TypeScript' UNION ALL SELECT 'HTML/CSS' UNION ALL
    SELECT 'Playwright' UNION ALL SELECT 'Selenium' UNION ALL SELECT 'SQL' UNION ALL
    SELECT 'Python' UNION ALL SELECT 'Power BI' UNION ALL SELECT 'Figma' UNION ALL
    SELECT 'UX Research'
) demo_skill
WHERE NOT EXISTS (SELECT 1 FROM skills existing WHERE existing.name = demo_skill.name);

INSERT INTO job_skills (job_id, skill_id)
SELECT j.id, s.id
FROM jobs j
JOIN skills s ON (
    (j.title LIKE '%Backend Java Developer%' AND s.name IN ('Java', 'Spring Boot', 'REST API')) OR
    (j.title LIKE '%Frontend React Developer%' AND s.name IN ('React', 'TypeScript', 'HTML/CSS')) OR
    (j.title LIKE '%QA Automation Engineer%' AND s.name IN ('Playwright', 'Selenium', 'REST API')) OR
    (j.title LIKE '%Data Analyst%' AND s.name IN ('SQL', 'Python', 'Power BI')) OR
    (j.title LIKE '%Product Designer UX/UI%' AND s.name IN ('Figma', 'UX Research'))
)
WHERE j.is_demo = TRUE
  AND NOT EXISTS (
      SELECT 1 FROM job_skills existing
      WHERE existing.job_id = j.id AND existing.skill_id = s.id
  );
