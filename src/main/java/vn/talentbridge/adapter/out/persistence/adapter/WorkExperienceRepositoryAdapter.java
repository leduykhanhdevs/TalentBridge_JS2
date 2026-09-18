package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.CandidateJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.WorkExperienceJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.WorkExperienceJpaRepository;
import vn.talentbridge.core.application.port.out.WorkExperienceRepositoryPort;
import vn.talentbridge.core.domain.model.WorkExperience;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class WorkExperienceRepositoryAdapter implements WorkExperienceRepositoryPort {

    private final WorkExperienceJpaRepository workExperienceJpaRepository;
    private final CandidateJpaRepository candidateJpaRepository;

    public WorkExperienceRepositoryAdapter(WorkExperienceJpaRepository workExperienceJpaRepository,
                                           CandidateJpaRepository candidateJpaRepository) {
        this.workExperienceJpaRepository = workExperienceJpaRepository;
        this.candidateJpaRepository = candidateJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WorkExperience> findByCandidateId(Long candidateId) {
        return workExperienceJpaRepository.findByCandidateIdOrderByStartDateDesc(candidateId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WorkExperience> findById(Long id) {
        return workExperienceJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public WorkExperience save(WorkExperience domain) {
        CandidateJpaEntity candidateEntity = candidateJpaRepository.findById(domain.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Candidate id: " + domain.getCandidateId()));

        WorkExperienceJpaEntity entity;
        if (domain.getId() != null) {
            entity = workExperienceJpaRepository.findById(domain.getId())
                    .orElse(new WorkExperienceJpaEntity());
        } else {
            entity = new WorkExperienceJpaEntity();
        }

        entity.setCandidate(candidateEntity);
        entity.setCompanyName(domain.getCompanyName());
        entity.setPosition(domain.getPosition());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setIsCurrent(Boolean.TRUE.equals(domain.getIsCurrent()));
        entity.setDescription(domain.getDescription());
        entity.setAchievements(domain.getAchievements());

        WorkExperienceJpaEntity saved = workExperienceJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        workExperienceJpaRepository.deleteById(id);
    }

    private WorkExperience toDomain(WorkExperienceJpaEntity entity) {
        return new WorkExperience(
                entity.getId(),
                entity.getCandidate() != null ? entity.getCandidate().getId() : null,
                entity.getCompanyName(),
                entity.getPosition(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getIsCurrent(),
                entity.getDescription(),
                entity.getAchievements(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
