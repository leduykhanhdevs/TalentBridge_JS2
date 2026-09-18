package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.CandidateJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.CandidateSkillJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.SkillJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.CandidateSkillJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.SkillJpaRepository;
import vn.talentbridge.core.application.port.out.CandidateSkillRepositoryPort;
import vn.talentbridge.core.domain.model.CandidateSkill;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class CandidateSkillRepositoryAdapter implements CandidateSkillRepositoryPort {

    private final CandidateSkillJpaRepository candidateSkillJpaRepository;
    private final CandidateJpaRepository candidateJpaRepository;
    private final SkillJpaRepository skillJpaRepository;

    public CandidateSkillRepositoryAdapter(CandidateSkillJpaRepository candidateSkillJpaRepository,
                                           CandidateJpaRepository candidateJpaRepository,
                                           SkillJpaRepository skillJpaRepository) {
        this.candidateSkillJpaRepository = candidateSkillJpaRepository;
        this.candidateJpaRepository = candidateJpaRepository;
        this.skillJpaRepository = skillJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CandidateSkill> findByCandidateId(Long candidateId) {
        return candidateSkillJpaRepository.findByCandidateIdOrderByIdAsc(candidateId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CandidateSkill> findById(Long id) {
        return candidateSkillJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CandidateSkill> findByCandidateIdAndSkillId(Long candidateId, Integer skillId) {
        return candidateSkillJpaRepository.findByCandidateIdAndSkillId(candidateId, skillId).map(this::toDomain);
    }

    @Override
    public CandidateSkill save(CandidateSkill domain) {
        CandidateJpaEntity candidateEntity = candidateJpaRepository.findById(domain.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Candidate id: " + domain.getCandidateId()));

        SkillJpaEntity skillEntity = skillJpaRepository.findById(domain.getSkillId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Skill id: " + domain.getSkillId()));

        CandidateSkillJpaEntity entity;
        if (domain.getId() != null) {
            entity = candidateSkillJpaRepository.findById(domain.getId())
                    .orElse(new CandidateSkillJpaEntity());
        } else {
            entity = new CandidateSkillJpaEntity();
        }

        entity.setCandidate(candidateEntity);
        entity.setSkill(skillEntity);
        entity.setProficiencyLevel(domain.getProficiencyLevel());
        entity.setRating(domain.getRating());
        entity.setYearsOfExperience(domain.getYearsOfExperience());

        CandidateSkillJpaEntity saved = candidateSkillJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        candidateSkillJpaRepository.deleteById(id);
    }

    private CandidateSkill toDomain(CandidateSkillJpaEntity entity) {
        return new CandidateSkill(
                entity.getId(),
                entity.getCandidate() != null ? entity.getCandidate().getId() : null,
                entity.getSkill() != null ? entity.getSkill().getId() : null,
                entity.getSkill() != null ? entity.getSkill().getName() : null,
                entity.getProficiencyLevel(),
                entity.getRating(),
                entity.getYearsOfExperience()
        );
    }
}
