package vn.talentbridge.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "candidate_skills", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cand_skill", columnNames = {"candidate_id", "skill_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateSkillJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private CandidateJpaEntity candidate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private SkillJpaEntity skill;

    @Column(name = "proficiency_level", length = 30)
    @Builder.Default
    private String proficiencyLevel = "INTERMEDIATE";

    @Column(name = "rating")
    @Builder.Default
    private Integer rating = 3;

    @Column(name = "years_of_experience")
    @Builder.Default
    private Double yearsOfExperience = 1.0;
}
