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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Profile({"local", "test", "dev", "e2e"})
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
            log.info(">>> [DataInitializer] Đã khởi tạo tài khoản Admin mẫu cho môi trường không production.");
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
            log.info(">>> [DataInitializer] Đã khởi tạo tài khoản nhà tuyển dụng mẫu cho môi trường không production.");
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
            log.info(">>> [DataInitializer] Đã khởi tạo tài khoản ứng viên mẫu cho môi trường không production.");
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

        // 7.1. Seed 100 Sample Jobs for Top Tech Companies
        List<JobJpaEntity> createdJobs = new java.util.ArrayList<>();
        if (jobJpaRepository.count() < 100) {
            seed100Jobs(createdJobs, hrUserId);
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

    private void seed100Jobs(List<JobJpaEntity> createdJobs, Long defaultHrUserId) {
        Map<String, CompanyJpaEntity> companyMap = new HashMap<>();
        for (CompanyJpaEntity c : companyJpaRepository.findAll()) {
            companyMap.put(c.getName(), c);
        }

        CompanyJpaEntity fpt = companyMap.get("FPT Software");
        CompanyJpaEntity vng = getOrCreateApprovedCompany(companyMap, "VNG Corporation", "0303888999", "https://vng.com.vn", "1000-5000 nhân viên", "TP. Hồ Chí Minh", "Z06 KCX Tân Thuận, Quận 7", "Kỳ lân công nghệ đầu tiên tại Việt Nam, phát triển Zalo, Zing, VNG Games.");
        CompanyJpaEntity viettel = getOrCreateApprovedCompany(companyMap, "Viettel Digital", "0100109106", "https://viettel.vn", "10000+ nhân viên", "Hà Nội", "Số 1 Giang Văn Minh, Ba Đình", "Tổng công ty Dịch vụ số Viettel, phát triển Viettel Money, Cloud, Big Data.");
        CompanyJpaEntity momo = getOrCreateApprovedCompany(companyMap, "MoMo (M-Service)", "0309489240", "https://momo.vn", "1000-5000 nhân viên", "TP. Hồ Chí Minh", "Tòa nhà Phú Mỹ Hưng, Quận 7", "Siêu ứng dụng thanh toán và tài chính số hàng đầu Việt Nam.");
        CompanyJpaEntity shopee = getOrCreateApprovedCompany(companyMap, "Shopee Vietnam", "0313576722", "https://shopee.vn", "5000+ nhân viên", "TP. Hồ Chí Minh", "Saigon Centre, Quận 1", "Sàn thương mại điện tử hàng đầu Đông Nam Á.");
        CompanyJpaEntity oneMount = getOrCreateApprovedCompany(companyMap, "One Mount Group", "0109012345", "https://onemount.com", "1000-5000 nhân viên", "Hà Nội", "Times City, Hai Bà Trưng", "Tập đoàn công nghệ phát triển hệ sinh thái VinID, OneHousing.");
        CompanyJpaEntity cmc = getOrCreateApprovedCompany(companyMap, "CMC Global", "0107891234", "https://cmcglobal.vn", "1000-5000 nhân viên", "Hà Nội", "CMC Tower, Cầu Giấy", "Doanh nghiệp cung cấp giải pháp và dịch vụ xuất khẩu phần mềm quốc tế.");
        CompanyJpaEntity nashtech = getOrCreateApprovedCompany(companyMap, "NashTech Vietnam", "0302345678", "https://nashtechglobal.com", "1000-5000 nhân viên", "TP. Hồ Chí Minh", "E-Town, Tân Bình", "Tập đoàn tư vấn giải pháp phần mềm và chuyển đổi số toàn cầu.");
        CompanyJpaEntity kms = getOrCreateApprovedCompany(companyMap, "KMS Technology", "0309876543", "https://kms-technology.com", "1000-5000 nhân viên", "TP. Hồ Chí Minh", "Tòa nhà Tản Viên, Tân Bình", "Công ty dịch vụ phát triển phần mềm chuyên nghiệp cho thị trường Mỹ.");
        CompanyJpaEntity tma = getOrCreateApprovedCompany(companyMap, "TMA Solutions", "0301112233", "https://tmasolutions.vn", "1000-5000 nhân viên", "TP. Hồ Chí Minh", "Công viên phần mềm Quang Trung, Q.12", "Công ty công nghệ phần mềm hàng đầu Việt Nam với hơn 4.000 kỹ sư.");
        CompanyJpaEntity rikkei = getOrCreateApprovedCompany(companyMap, "RikkeiSoft", "0105556677", "https://rikkeisoft.com", "1000-5000 nhân viên", "Đà Nẵng", "Tòa nhà Ricco, Hải Châu", "Doanh nghiệp công nghệ thông tin xuất khẩu phần mềm hàng đầu thị trường Nhật Bản.");
        CompanyJpaEntity nexttech = getOrCreateApprovedCompany(companyMap, "NextTech Group", "0109999888", "https://nexttech.asia", "500-1000 nhân viên", "Hà Nội", "Tòa nhà VTC Online, Hai Bà Trưng", "Tập đoàn công nghệ tiên phong chuyển đổi số toàn diện.");
        CompanyJpaEntity vnpt = getOrCreateApprovedCompany(companyMap, "VNPT IT", "0100684378", "https://vnpt.vn", "5000+ nhân viên", "Hà Nội", "Tòa nhà VNPT, Cầu Giấy", "Công ty Công nghệ thông tin VNPT, chuyên trách các giải pháp chính phủ điện tử.");

        List<JobSeedData.SeedJobDef> defs = JobSeedData.get100JobDefinitions();
        Set<String> existingTitles = new HashSet<>();
        for (JobJpaEntity j : jobJpaRepository.findAll()) {
            existingTitles.add(j.getTitle());
            createdJobs.add(j);
        }

        int index = 0;
        for (JobSeedData.SeedJobDef def : defs) {
            if (existingTitles.contains(def.title())) {
                continue;
            }
            CompanyJpaEntity targetCompany = companyMap.getOrDefault(def.companyName(), fpt != null ? fpt : vng);
            if (targetCompany == null) continue;

            LocalDate deadline = LocalDate.now().plusDays(20 + (index % 45));
            JobJpaEntity job = JobJpaEntity.builder()
                    .company(targetCompany)
                    .recruiterUserId(defaultHrUserId)
                    .title(def.title())
                    .description(def.description())
                    .requirements(def.requirements())
                    .benefits(def.benefits())
                    .location(def.location())
                    .city(def.city())
                    .address(def.location())
                    .jobType(def.jobType())
                    .experienceLevel(def.experienceLevel())
                    .minSalary(new BigDecimal(def.minSalaryMln() * 1000000L))
                    .maxSalary(new BigDecimal(def.maxSalaryMln() * 1000000L))
                    .isNegotiable(def.isNegotiable())
                    .deadline(deadline)
                    .status(vn.talentbridge.core.domain.vo.JobStatus.ACTIVE)
                    .skills(getSkillsByNames(def.skills().toArray(new String[0])))
                    .build();

            createdJobs.add(jobJpaRepository.save(job));
            existingTitles.add(def.title());
            index++;
        }
        log.info(">>> [DataInitializer] Đã khởi tạo hoàn tất {} tin tuyển dụng mẫu trên hệ thống.", jobJpaRepository.count());
    }

    private CompanyJpaEntity getOrCreateApprovedCompany(
            Map<String, CompanyJpaEntity> companyMap,
            String name, String taxCode, String website, String size, String city, String address, String desc) {
        CompanyJpaEntity comp = companyMap.get(name);
        if (comp == null) {
            comp = CompanyJpaEntity.builder()
                    .name(name)
                    .taxCode(taxCode)
                    .website(website)
                    .companySize(size)
                    .city(city)
                    .address(address)
                    .description(desc)
                    .status(CompanyStatus.APPROVED)
                    .build();
            comp = companyJpaRepository.save(comp);
            companyMap.put(name, comp);
        } else if (comp.getStatus() != CompanyStatus.APPROVED) {
            comp.setStatus(CompanyStatus.APPROVED);
            comp = companyJpaRepository.save(comp);
        }
        return comp;
    }

    private Set<SkillJpaEntity> getSkillsByNames(String... names) {
        Set<SkillJpaEntity> set = new HashSet<>();
        for (String name : names) {
            SkillJpaEntity skill = skillJpaRepository.findByNameIgnoreCase(name)
                    .orElseGet(() -> skillJpaRepository.save(SkillJpaEntity.builder().name(name).build()));
            set.add(skill);
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
