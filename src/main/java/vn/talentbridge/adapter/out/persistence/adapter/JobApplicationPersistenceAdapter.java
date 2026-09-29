package vn.talentbridge.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.talentbridge.adapter.out.persistence.entity.ApplicationJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.ApplicationJpaRepository;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.domain.model.JobApplication;

import java.util.List;

@Component
@RequiredArgsConstructor
public class JobApplicationPersistenceAdapter implements JobApplicationRepositoryPort {

    private final ApplicationJpaRepository applicationJpaRepository;

    @Override
    public List<JobApplication> findByJobId(Long jobId) {
        return applicationJpaRepository.findByJobId(jobId).stream()
                .map(this::toDomain)
                .toList();
    }

    private JobApplication toDomain(ApplicationJpaEntity entity) {
        JobApplication domain = new JobApplication();
        domain.setId(entity.getId());
        domain.setJobId(entity.getJob() != null ? entity.getJob().getId() : null);
        if (entity.getCandidate() != null) {
            domain.setCandidateId(entity.getCandidate().getId());
            domain.setCandidateTitle(entity.getCandidate().getTitle());
            domain.setCandidateExperienceYears(entity.getCandidate().getExperienceYears());
            domain.setCandidateCity(entity.getCandidate().getCity());
            if (entity.getCandidate().getUser() != null) {
                domain.setCandidateFullName(entity.getCandidate().getUser().getFullName());
                domain.setCandidateEmail(entity.getCandidate().getUser().getEmail());
                domain.setCandidatePhone(entity.getCandidate().getUser().getPhoneNumber());
                domain.setCandidateAvatarUrl(entity.getCandidate().getUser().getAvatarUrl());
            }
        }
        domain.setCoverLetter(entity.getCoverLetter());
        domain.setCurrentStage(entity.getCurrentStage());
        domain.setStatus(entity.getStatus());
        domain.setAiMatchScore(entity.getAiMatchScore());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setUpdatedAt(entity.getUpdatedAt());
        return domain;
    }
}
