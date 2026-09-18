package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.CandidateJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.ResumeJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.ResumeJpaRepository;
import vn.talentbridge.core.application.port.out.ResumeRepositoryPort;
import vn.talentbridge.core.domain.model.Resume;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class ResumeRepositoryAdapter implements ResumeRepositoryPort {

    private final ResumeJpaRepository resumeJpaRepository;
    private final CandidateJpaRepository candidateJpaRepository;

    public ResumeRepositoryAdapter(ResumeJpaRepository resumeJpaRepository,
                                  CandidateJpaRepository candidateJpaRepository) {
        this.resumeJpaRepository = resumeJpaRepository;
        this.candidateJpaRepository = candidateJpaRepository;
    }

    @Override
    public Resume save(Resume domain) {
        CandidateJpaEntity candidateEntity = candidateJpaRepository.findById(domain.getCandidateId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Candidate id: " + domain.getCandidateId()));

        ResumeJpaEntity entity;
        if (domain.getId() != null) {
            entity = resumeJpaRepository.findById(domain.getId())
                    .orElse(new ResumeJpaEntity());
        } else {
            entity = new ResumeJpaEntity();
        }

        entity.setCandidate(candidateEntity);
        entity.setTemplateId(domain.getTemplateId());
        entity.setResumeType(domain.getResumeType());
        entity.setTitle(domain.getTitle());
        entity.setFileName(domain.getFileName());
        entity.setFileUrl(domain.getFileUrl());
        entity.setFileType(domain.getFileType());
        entity.setIsDefault(domain.isDefault());

        ResumeJpaEntity saved = resumeJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Resume> findById(Long id) {
        return resumeJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Resume> findByCandidateId(Long candidateId) {
        return resumeJpaRepository.findByCandidateIdOrderByIsDefaultDescCreatedAtDesc(candidateId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public void delete(Long id) {
        resumeJpaRepository.deleteById(id);
    }

    @Override
    public void clearDefault(Long candidateId) {
        resumeJpaRepository.clearDefaultByCandidateId(candidateId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Resume> findDefaultByCandidateId(Long candidateId) {
        return resumeJpaRepository.findByCandidateIdAndIsDefaultTrue(candidateId).map(this::toDomain);
    }

    private Resume toDomain(ResumeJpaEntity entity) {
        return new Resume(
                entity.getId(),
                entity.getCandidate() != null ? entity.getCandidate().getId() : null,
                entity.getTemplateId(),
                entity.getResumeType(),
                entity.getTitle(),
                entity.getFileName(),
                entity.getFileUrl(),
                entity.getFileType(),
                Boolean.TRUE.equals(entity.getIsDefault()),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
