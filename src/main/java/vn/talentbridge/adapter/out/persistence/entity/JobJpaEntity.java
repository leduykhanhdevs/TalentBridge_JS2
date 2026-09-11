package vn.talentbridge.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyJpaEntity company;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "requirements", columnDefinition = "TEXT")
    private String requirements;

    @Column(name = "benefits", columnDefinition = "TEXT")
    private String benefits;

    @Column(name = "location", length = 150)
    private String location;

    @Column(name = "job_type", length = 50)
    private String jobType;

    @Column(name = "experience_level", length = 50)
    private String experienceLevel;

    @Column(name = "min_salary", precision = 15, scale = 2)
    private BigDecimal minSalary;

    @Column(name = "max_salary", precision = 15, scale = 2)
    private BigDecimal maxSalary;

    @Column(name = "deadline")
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private JobStatus status = JobStatus.PENDING;

    @Column(name = "recruiter_user_id")
    private Long recruiterUserId;
}