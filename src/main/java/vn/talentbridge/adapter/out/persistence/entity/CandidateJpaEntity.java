package vn.talentbridge.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserJpaEntity user;

    @Column(name = "title", length = 150)
    private String title;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "gender", length = 10)
    @Builder.Default
    private String gender = "OTHER";

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "experience_years")
    @Builder.Default
    private Integer experienceYears = 0;

    @Column(name = "current_salary", precision = 12, scale = 2)
    private BigDecimal currentSalary;

    @Column(name = "expected_salary", precision = 12, scale = 2)
    private BigDecimal expectedSalary;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "personal_website", length = 255)
    private String personalWebsite;

    @Column(name = "linkedin_url", length = 255)
    private String linkedinUrl;

    @Column(name = "github_url", length = 255)
    private String githubUrl;
}
