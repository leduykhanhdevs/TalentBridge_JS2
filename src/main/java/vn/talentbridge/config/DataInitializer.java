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
    }

    private RoleJpaEntity getOrCreateRole(String name, String description) {
        return roleJpaRepository.findByName(name)
                .orElseGet(() -> roleJpaRepository.save(RoleJpaEntity.builder()
                        .name(name)
                        .description(description)
                        .build()));
    }
}
