package vn.talentbridge.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private JobJpaEntity job;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private CandidateJpaEntity candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private ResumeJpaEntity resume;

    @Column(name = "cover_letter", columnDefinition = "TEXT")
    private String coverLetter;

    @Builder.Default
    @Column(name = "current_stage", length = 30)
    private String currentStage = "APPLIED";

    @Builder.Default
    @Column(name = "status", length = 20)
    private String status = "SUBMITTED";

    @Column(name = "ai_match_score", precision = 5, scale = 2)
    private BigDecimal aiMatchScore;
}