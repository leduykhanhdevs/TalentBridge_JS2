package vn.talentbridge.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.*;
import vn.talentbridge.adapter.out.persistence.repository.*;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.RoleName;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Profile({"local", "test", "dev"})
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;
    private final CandidateJpaRepository candidateJpaRepository;
    private final CompanyJpaRepository companyJpaRepository;
    private final RecruiterJpaRepository recruiterJpaRepository;
    private final SkillJpaRepository skillJpaRepository;
    private final JobJpaRepository jobJpaRepository;
    private final ResumeJpaRepository resumeJpaRepository;
    private final ApplicationJpaRepository applicationJpaRepository;
    private final CvTemplateJpaRepository cvTemplateJpaRepository;
    private final org.springframework.core.env.Environment environment;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Initialize Roles
        RoleJpaEntity adminRole = getOrCreateRole(RoleName.ROLE_ADMIN.name(), "Quản trị viên hệ thống");
        RoleJpaEntity recruiterRole = getOrCreateRole(RoleName.ROLE_RECRUITER.name(), "Nhà tuyển dụng / Chuyên viên nhân sự");
        RoleJpaEntity candidateRole = getOrCreateRole(RoleName.ROLE_CANDIDATE.name(), "Người tìm việc / Ứng viên");

        // 2. Initialize Default Admin Account
        if (userJpaRepository.findByEmail("admin@talentbridge.vn").isEmpty()) {
            UserJpaEntity admin = UserJpaEntity.builder()
                    .email("admin@talentbridge.vn")
                    .fullName("Lê Duy Khánh (Admin)")
                    .passwordHash(passwordEncoder.encode("AdminPassword123!"))
                    .phoneNumber("0901234567")
                    .status(UserStatus.ACTIVE)
                    .roles(new HashSet<>(Set.of(adminRole)))
                    .build();
            userJpaRepository.save(admin);
            log.info(">>> [DataInitializer] Tạo tài khoản Admin mặc định: admin@talentbridge.vn / AdminPassword123!");
        }

        // 3. Initialize Sample Companies
        CompanyJpaEntity fptCompany = null;
        if (companyJpaRepository.findAll().isEmpty()) {
            fptCompany = CompanyJpaEntity.builder()
                    .name("FPT Software")
                    .taxCode("0101234567")
                    .website("https://fptsoftware.com")
                    .companySize("10000+ nhân viên")
                    .city("TP. Hồ Chí Minh")
                    .address("Khu Công Nghệ Cao, TP. Thủ Đức")
                    .description("Công ty dịch vụ công nghệ thông tin hàng đầu khu vực Châu Á - Thái Bình Dương.")
                    .status(CompanyStatus.APPROVED)
                    .build();
            fptCompany = companyJpaRepository.save(fptCompany);

            CompanyJpaEntity vngCompany = CompanyJpaEntity.builder()
                    .name("VNG Corporation")
                    .taxCode("0303888999")
                    .website("https://vng.com.vn")
                    .companySize("1000-5000 nhân viên")
                    .city("TP. Hồ Chí Minh")
                    .address("Z06 Đường số 13, KCX Tân Thuận, Quận 7")
                    .description("Kỳ lân công nghệ đầu tiên tại Việt Nam, phát triển Zalo, Zing, VNG Games.")
                    .status(CompanyStatus.PENDING) // Pending approval
                    .build();
            companyJpaRepository.save(vngCompany);

            CompanyJpaEntity techCorp = CompanyJpaEntity.builder()
                    .name("NextTech Group")
                    .taxCode("0109999888")
                    .website("https://nexttech.asia")
                    .companySize("500-1000 nhân viên")
                    .city("Hà Nội")
                    .address("Tầng 3, Tòa nhà VTC Online, Hai Bà Trưng")
                    .description("Tập đoàn công nghệ tiên phong chuyển đổi số toàn diện.")
                    .status(CompanyStatus.PENDING) // Pending approval
                    .build();
            companyJpaRepository.save(techCorp);

            log.info(">>> [DataInitializer] Đã khởi tạo danh sách doanh nghiệp mẫu (FPT Software, VNG Corporation, NextTech).");
        }

        // 4. Initialize Sample Recruiter
        if (userJpaRepository.findByEmail("recruiter@fpt.com").isEmpty()) {
            if (fptCompany == null) {
                fptCompany = companyJpaRepository.findAll().stream()
                        .filter(c -> c.getStatus() == CompanyStatus.APPROVED)
                        .findFirst()
                        .orElse(null);
            }

            UserJpaEntity hrUser = UserJpaEntity.builder()
                    .email("recruiter@fpt.com")
                    .fullName("Nguyễn Văn Hoàng")
                    .passwordHash(passwordEncoder.encode("Password123!"))
                    .phoneNumber("0912345678")
                    .status(UserStatus.ACTIVE)
                    .roles(new HashSet<>(Set.of(recruiterRole)))
                    .build();
            hrUser = userJpaRepository.save(hrUser);

            RecruiterJpaEntity recruiter = RecruiterJpaEntity.builder()
                    .user(hrUser)
                    .company(fptCompany)
                    .position("Talent Acquisition Manager")
                    .build();
            recruiterJpaRepository.save(recruiter);
            log.info(">>> [DataInitializer] Tạo tài khoản HR mẫu: recruiter@fpt.com / Password123!");
        }

        // 5. Initialize Sample Candidate
        if (userJpaRepository.findByEmail("candidate@talentbridge.vn").isEmpty()) {
            UserJpaEntity candidateUser = UserJpaEntity.builder()
                    .email("candidate@talentbridge.vn")
                    .fullName("Trần Minh Anh")
                    .passwordHash(passwordEncoder.encode("Password123!"))
                    .phoneNumber("0987654321")
                    .status(UserStatus.ACTIVE)
                    .roles(new HashSet<>(Set.of(candidateRole)))
                    .build();
            candidateUser = userJpaRepository.save(candidateUser);

            CandidateJpaEntity candidate = CandidateJpaEntity.builder()
                    .user(candidateUser)
                    .title("Fullstack Developer")
                    .dob(LocalDate.of(2000, 5, 15))
                    .gender("NAM")
                    .experienceYears(3)
                    .currentSalary(new BigDecimal("22000000"))
                    .expectedSalary(new BigDecimal("30000000"))
                    .city("TP. Hồ Chí Minh")
                    .address("227 Nguyễn Văn Cừ, Quận 5")
                    .summary("Lập trình viên nhiệt huyết với 3 năm kinh nghiệm phát triển hệ thống Spring Boot & React.")
                    .personalWebsite("https://minhanh-dev.io")
                    .githubUrl("https://github.com/minhanh-dev")
                    .linkedinUrl("https://linkedin.com/in/minhanh-dev")
                    .build();
            candidateJpaRepository.save(candidate);
            log.info(">>> [DataInitializer] Tạo tài khoản Ứng viên mẫu: candidate@talentbridge.vn / Password123!");
        }

        // 6. Initialize Standard Skills Catalog
        if (skillJpaRepository.count() == 0) {
            List<String> defaultSkills = List.of(
                    "Java", "Spring Boot", "React", "TypeScript", "JavaScript",
                    "Node.js", "Python", "SQL / MySQL", "PostgreSQL", "Docker",
                    "AWS", "Git / GitHub", "RESTful API", "Microservices",
                    "Figma / UI-UX", "HTML5 & CSS3", "TailwindCSS", "Agile / Scrum"
            );
            for (String skillName : defaultSkills) {
                skillJpaRepository.save(SkillJpaEntity.builder().name(skillName).build());
            }
            log.info(">>> [DataInitializer] Đã khởi tạo danh mục {} kỹ năng tiêu chuẩn.", defaultSkills.size());
        }

        // 6.1. Seed CV Templates
        if (cvTemplateJpaRepository.count() == 0) {
            cvTemplateJpaRepository.save(CvTemplateJpaEntity.builder()
                    .name("Modern IT Professional")
                    .templateCode("MODERN_IT_01")
                    .thumbnailUrl("https://images.unsplash.com/photo-1586281380349-632531db7ed4?w=400&auto=format&fit=crop&q=60")
                    .description("Mẫu CV hiện đại chuyên biệt cho ngành IT & Phần mềm, tối ưu hiển thị kỹ năng và dự án")
                    .defaultConfig("{\"primaryColor\": \"#2563EB\", \"fontFamily\": \"Inter\", \"columns\": 2, \"layout\": \"sidebar-left\"}")
                    .isActive(true)
                    .build());
            cvTemplateJpaRepository.save(CvTemplateJpaEntity.builder()
                    .name("Classic Elegant")
                    .templateCode("CLASSIC_01")
                    .thumbnailUrl("https://images.unsplash.com/photo-1512486130939-2c4f79935e4f?w=400&auto=format&fit=crop&q=60")
                    .description("Mẫu CV phong cách cổ điển, trang trọng, phù hợp cho ngành Kinh doanh, Quản lý & Tài chính")
                    .defaultConfig("{\"primaryColor\": \"#1F2937\", \"fontFamily\": \"Merriweather\", \"columns\": 1, \"layout\": \"single-column\"}")
                    .isActive(true)
                    .build());
            cvTemplateJpaRepository.save(CvTemplateJpaEntity.builder()
                    .name("Creative Minimalist")
                    .templateCode("MINIMALIST_01")
                    .thumbnailUrl("https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=400&auto=format&fit=crop&q=60")
                    .description("Mẫu CV tối giản tinh tế, tập trung vào điểm nhấn kinh nghiệm và thành tựu cá nhân")
                    .defaultConfig("{\"primaryColor\": \"#059669\", \"fontFamily\": \"Roboto\", \"columns\": 2, \"layout\": \"grid\"}")
                    .isActive(true)
                    .build());
            log.info(">>> [DataInitializer] Đã khởi tạo 3 mẫu CV chuẩn cho Resume Generator.");
        }

        // 7. Seed Sample Jobs, Resumes and Candidates for Local/Dev environment
        boolean isTestProfile = List.of(environment.getActiveProfiles()).contains("test");
        if (!isTestProfile) {
            seedLocalDevData(candidateRole, fptCompany);
        }
    }

    private void seedLocalDevData(RoleJpaEntity candidateRole, CompanyJpaEntity fptCompany) {
        if (fptCompany == null) {
            fptCompany = companyJpaRepository.findAll().stream()
                    .filter(c -> "FPT Software".equals(c.getName()))
                    .findFirst()
                    .orElse(null);
        }
        if (fptCompany == null) return;

        UserJpaEntity hrUser = userJpaRepository.findByEmail("recruiter@fpt.com").orElse(null);
        Long hrUserId = hrUser != null ? hrUser.getId() : 1L;

        // 7.1. Seed 5 Sample Jobs for FPT Software
        List<JobJpaEntity> createdJobs = new java.util.ArrayList<>();
        if (jobJpaRepository.count() == 0) {
            JobJpaEntity job1 = JobJpaEntity.builder()
                    .company(fptCompany)
                    .recruiterUserId(hrUserId)
                    .title("Senior Java Spring Boot Engineer")
                    .description("Tham gia phát triển kiến trúc backend microservices cho các dự án FinTech và E-commerce quy mô lớn. Tối ưu hóa hiệu năng cơ sở dữ liệu MySQL và cache Redis.")
                    .requirements("Tối thiểu 4 năm kinh nghiệm với Java và Spring Boot. Thành thạo Spring Data JPA, Spring Security, Hibernate, MySQL, Docker, RESTful API.")
                    .benefits("Thu nhập 35 - 50 triệu/tháng + tháng lương 13 và thưởng dự án. Bảo hiểm FPT Care cho bản thân và gia đình. Môi trường quốc tế, hỗ trợ thi chứng chỉ AWS.")
                    .location("Khu Công Nghệ Cao, TP. Thủ Đức")
                    .city("TP. Hồ Chí Minh")
                    .jobType("FULL_TIME")
                    .experienceLevel("SENIOR")
                    .minSalary(new BigDecimal("35000000"))
                    .maxSalary(new BigDecimal("50000000"))
                    .isNegotiable(false)
                    .deadline(LocalDate.now().plusDays(30))
                    .status(vn.talentbridge.core.domain.vo.JobStatus.ACTIVE)
                    .skills(getSkillsByNames("Java", "Spring Boot", "SQL / MySQL", "Docker", "Microservices"))
                    .build();
            createdJobs.add(jobJpaRepository.save(job1));

            JobJpaEntity job2 = JobJpaEntity.builder()
                    .company(fptCompany)
                    .recruiterUserId(hrUserId)
                    .title("Frontend React / TypeScript Developer")
                    .description("Phát triển giao diện web portal responsive sử dụng React 18/19, TypeScript, TailwindCSS và TanStack Query. Đảm bảo trải nghiệm mượt mà 60fps và đạt chuẩn Core Web Vitals.")
                    .requirements("Có từ 2-3 năm kinh nghiệm lập trình React & TypeScript. Sử dụng thành thạo HTML5/CSS3, TailwindCSS, REST API, Git.")
                    .benefits("Lương cạnh tranh 20 - 32 triệu. Xét tăng lương định kỳ 2 lần/năm. Được cấp MacBook Pro làm việc. Tham gia các khóa đào tạo công nghệ mới.")
                    .location("FPT Tower, Cầu Giấy")
                    .city("Hà Nội")
                    .jobType("FULL_TIME")
                    .experienceLevel("MIDDLE")
                    .minSalary(new BigDecimal("20000000"))
                    .maxSalary(new BigDecimal("32000000"))
                    .isNegotiable(false)
                    .deadline(LocalDate.now().plusDays(25))
                    .status(vn.talentbridge.core.domain.vo.JobStatus.ACTIVE)
                    .skills(getSkillsByNames("React", "TypeScript", "TailwindCSS", "HTML5 & CSS3", "Git / GitHub"))
                    .build();
            createdJobs.add(jobJpaRepository.save(job2));

            JobJpaEntity job3 = JobJpaEntity.builder()
                    .company(fptCompany)
                    .recruiterUserId(hrUserId)
                    .title("Cloud & DevOps Engineer (AWS / Docker)")
                    .description("Xây dựng và tự động hóa hạ tầng đám mây AWS, triển khai hệ thống CI/CD pipeline với GitHub Actions và Docker. Giám sát hệ thống và đảm bảo độ sẵn sàng 99.99%.")
                    .requirements("Từ 3 năm kinh nghiệm với AWS, Docker, Linux, CI/CD. Có kinh nghiệm triển khai Microservices.")
                    .benefits("Làm việc từ xa linh hoạt (Remote 100%). Lương 30 - 45 triệu. Gói bảo hiểm quốc tế cao cấp.")
                    .location("FPT Complex, Ngũ Hành Sơn")
                    .city("Đà Nẵng")
                    .jobType("REMOTE")
                    .experienceLevel("SENIOR")
                    .minSalary(new BigDecimal("30000000"))
                    .maxSalary(new BigDecimal("45000000"))
                    .isNegotiable(false)
                    .deadline(LocalDate.now().plusDays(40))
                    .status(vn.talentbridge.core.domain.vo.JobStatus.ACTIVE)
                    .skills(getSkillsByNames("AWS", "Docker", "Git / GitHub", "Microservices"))
                    .build();
            createdJobs.add(jobJpaRepository.save(job3));

            JobJpaEntity job4 = JobJpaEntity.builder()
                    .company(fptCompany)
                    .recruiterUserId(hrUserId)
                    .title("Fresher / Junior Java Developer")
                    .description("Dành cho các bạn mới tốt nghiệp hoặc dưới 1 năm kinh nghiệm. Tham gia khóa đào tạo chuyên sâu và thực chiến trên các dự án phần mềm doanh nghiệp của FPT.")
                    .requirements("Nắm vững kiến thức Java Core, OOP, CSDL quan hệ SQL. Tinh thần học hỏi cao, đam mê lập trình.")
                    .benefits("Lương đào tạo và khởi điểm hấp dẫn từ 10 - 15 triệu. Lộ trình thăng tiến rõ ràng lên Junior/Middle sau 6 tháng.")
                    .location("Quận 9, TP. Thủ Đức")
                    .city("TP. Hồ Chí Minh")
                    .jobType("FULL_TIME")
                    .experienceLevel("FRESHER")
                    .minSalary(new BigDecimal("10000000"))
                    .maxSalary(new BigDecimal("15000000"))
                    .isNegotiable(false)
                    .deadline(LocalDate.now().plusDays(20))
                    .status(vn.talentbridge.core.domain.vo.JobStatus.ACTIVE)
                    .skills(getSkillsByNames("Java", "SQL / MySQL", "RESTful API"))
                    .build();
            createdJobs.add(jobJpaRepository.save(job4));

            JobJpaEntity job5 = JobJpaEntity.builder()
                    .company(fptCompany)
                    .recruiterUserId(hrUserId)
                    .title("UI/UX Product Designer (Figma)")
                    .description("Thiết kế trải nghiệm người dùng (UX) và giao diện trực quan (UI) cho các sản phẩm web/mobile. Xây dựng Design System chuẩn chỉn trên Figma.")
                    .requirements("Thành thạo Figma, Design Tokens, wireframing, prototyping. Có portfolio dự án thực tế.")
                    .benefits("Lương thỏa thuận không giới hạn theo năng lực. Môi trường làm việc sáng tạo, năng động.")
                    .location("Toàn quốc (Hybrid)")
                    .city("TP. Hồ Chí Minh")
                    .jobType("HYBRID")
                    .experienceLevel("JUNIOR")
                    .isNegotiable(true)
                    .deadline(LocalDate.now().plusDays(35))
                    .status(vn.talentbridge.core.domain.vo.JobStatus.ACTIVE)
                    .skills(getSkillsByNames("Figma / UI-UX"))
                    .build();
            createdJobs.add(jobJpaRepository.save(job5));

            log.info(">>> [DataInitializer] Đã khởi tạo {} tin tuyển dụng mẫu cho FPT Software.", createdJobs.size());
        } else {
            createdJobs.addAll(jobJpaRepository.findAll());
        }

        // 7.2. Seed Default Resume for Candidate 1 (Trần Minh Anh - candidate@talentbridge.vn)
        UserJpaEntity candidateUser1 = userJpaRepository.findByEmail("candidate@talentbridge.vn").orElse(null);
        if (candidateUser1 != null) {
            CandidateJpaEntity candidate1 = candidateJpaRepository.findByUserId(candidateUser1.getId()).orElse(null);
            if (candidate1 != null && resumeJpaRepository.findByCandidateIdOrderByIsDefaultDescCreatedAtDesc(candidate1.getId()).isEmpty()) {
                resumeJpaRepository.save(ResumeJpaEntity.builder()
                        .candidate(candidate1)
                        .title("CV Lập Trình Viên Fullstack (Spring Boot & React)")
                        .fileName("CV_TranMinhAnh_Fullstack.pdf")
                        .fileUrl("/uploads/resumes/CV_TranMinhAnh_Fullstack.pdf")
                        .fileType("application/pdf")
                        .isDefault(true)
                        .resumeType("UPLOADED")
                        .build());
                log.info(">>> [DataInitializer] Đã tạo CV mặc định cho ứng viên Trần Minh Anh.");
            }
        }

        // 7.3. Seed Candidate 2 (Lê Thị Thu Hà)
        if (userJpaRepository.findByEmail("lethithuha@gmail.com").isEmpty()) {
            UserJpaEntity user2 = UserJpaEntity.builder()
                    .email("lethithuha@gmail.com")
                    .fullName("Lê Thị Thu Hà")
                    .passwordHash(passwordEncoder.encode("Password123!"))
                    .phoneNumber("0971234567")
                    .status(UserStatus.ACTIVE)
                    .roles(new HashSet<>(Set.of(candidateRole)))
                    .build();
            user2 = userJpaRepository.save(user2);

            CandidateJpaEntity cand2 = CandidateJpaEntity.builder()
                    .user(user2)
                    .title("Senior Frontend Engineer (React/TypeScript)")
                    .dob(LocalDate.of(1998, 8, 20))
                    .gender("NU")
                    .experienceYears(4)
                    .currentSalary(new BigDecimal("28000000"))
                    .expectedSalary(new BigDecimal("35000000"))
                    .city("Hà Nội")
                    .address("Tòa nhà Keangnam, Cầu Giấy")
                    .summary("Chuyên gia phát triển giao diện người dùng với React, Next.js và TailwindCSS. Đam mê thiết kế UI/UX.")
                    .build();
            cand2 = candidateJpaRepository.save(cand2);

            ResumeJpaEntity resume2 = resumeJpaRepository.save(ResumeJpaEntity.builder()
                    .candidate(cand2)
                    .title("CV Lê Thị Thu Hà - Senior Frontend.pdf")
                    .fileName("CV_LeThiThuHa_Frontend.pdf")
                    .fileUrl("/uploads/resumes/CV_LeThiThuHa_Frontend.pdf")
                    .fileType("application/pdf")
                    .isDefault(true)
                    .resumeType("UPLOADED")
                    .build());

            // Apply to Job 2 (Frontend) if available
            if (!createdJobs.isEmpty()) {
                JobJpaEntity targetJob = createdJobs.stream()
                        .filter(j -> j.getTitle().contains("Frontend"))
                        .findFirst()
                        .orElse(createdJobs.getFirst());
                if (!applicationJpaRepository.existsByJobIdAndCandidateId(targetJob.getId(), cand2.getId())) {
                    applicationJpaRepository.save(ApplicationJpaEntity.builder()
                            .job(targetJob)
                            .candidate(cand2)
                            .resume(resume2)
                            .coverLetter("Kính gửi bộ phận Tuyển dụng FPT Software, tôi có 4 năm kinh nghiệm làm việc chuyên sâu với React và TypeScript. Tôi rất mong muốn được đồng hành cùng dự án.")
                            .currentStage("APPLIED")
                            .status("SUBMITTED")
                            .build());
                }
            }
            log.info(">>> [DataInitializer] Tạo tài khoản Ứng viên 2: lethithuha@gmail.com / Password123!");
        }

        // 7.4. Seed Candidate 3 (Phạm Hoàng Nam)
        if (userJpaRepository.findByEmail("phamhoangnam@gmail.com").isEmpty()) {
            UserJpaEntity user3 = UserJpaEntity.builder()
                    .email("phamhoangnam@gmail.com")
                    .fullName("Phạm Hoàng Nam")
                    .passwordHash(passwordEncoder.encode("Password123!"))
                    .phoneNumber("0934567890")
                    .status(UserStatus.ACTIVE)
                    .roles(new HashSet<>(Set.of(candidateRole)))
                    .build();
            user3 = userJpaRepository.save(user3);

            CandidateJpaEntity cand3 = CandidateJpaEntity.builder()
                    .user(user3)
                    .title("Backend Java & Cloud Architect")
                    .dob(LocalDate.of(1996, 12, 10))
                    .gender("NAM")
                    .experienceYears(6)
                    .currentSalary(new BigDecimal("38000000"))
                    .expectedSalary(new BigDecimal("48000000"))
                    .city("TP. Hồ Chí Minh")
                    .address("123 Lê Lợi, Quận 1")
                    .summary("Chuyên viên phát triển hệ thống phân tán High Concurrency với Spring Boot, Kafka và AWS.")
                    .build();
            cand3 = candidateJpaRepository.save(cand3);

            ResumeJpaEntity resume3 = resumeJpaRepository.save(ResumeJpaEntity.builder()
                    .candidate(cand3)
                    .title("CV Phạm Hoàng Nam - Backend Architect.pdf")
                    .fileName("CV_PhamHoangNam_Backend.pdf")
                    .fileUrl("/uploads/resumes/CV_PhamHoangNam_Backend.pdf")
                    .fileType("application/pdf")
                    .isDefault(true)
                    .resumeType("UPLOADED")
                    .build());

            // Apply to Job 1 (Senior Java) if available
            if (!createdJobs.isEmpty()) {
                JobJpaEntity targetJob = createdJobs.stream()
                        .filter(j -> j.getTitle().contains("Java Spring Boot"))
                        .findFirst()
                        .orElse(createdJobs.getFirst());
                if (!applicationJpaRepository.existsByJobIdAndCandidateId(targetJob.getId(), cand3.getId())) {
                    applicationJpaRepository.save(ApplicationJpaEntity.builder()
                            .job(targetJob)
                            .candidate(cand3)
                            .resume(resume3)
                            .coverLetter("Chào anh/chị tuyển dụng, với hơn 5 năm kinh nghiệm backend microservices và chứng chỉ AWS Certified Solutions Architect, tôi tin mình sẽ đóng góp tốt cho hệ thống của FPT.")
                            .currentStage("SCREENING")
                            .status("IN_REVIEW")
                            .build());
                }
            }
            log.info(">>> [DataInitializer] Tạo tài khoản Ứng viên 3: phamhoangnam@gmail.com / Password123!");
        }
    }

    private Set<SkillJpaEntity> getSkillsByNames(String... names) {
        Set<SkillJpaEntity> set = new HashSet<>();
        for (String name : names) {
            skillJpaRepository.findByNameIgnoreCase(name).ifPresent(set::add);
        }
        return set;
    }

    private RoleJpaEntity getOrCreateRole(String name, String description) {
        return roleJpaRepository.findByName(name)
                .orElseGet(() -> roleJpaRepository.save(RoleJpaEntity.builder()
                        .name(name)
                        .description(description)
                        .build()));
    }
}
