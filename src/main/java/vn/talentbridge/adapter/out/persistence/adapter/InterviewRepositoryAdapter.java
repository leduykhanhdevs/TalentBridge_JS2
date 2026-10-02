package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.ApplicationJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.InterviewJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.ApplicationJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.InterviewJpaRepository;
import vn.talentbridge.core.application.port.out.InterviewRepositoryPort;
import vn.talentbridge.core.domain.model.Interview;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class InterviewRepositoryAdapter implements InterviewRepositoryPort {

    private final InterviewJpaRepository interviewJpaRepository;
    private final ApplicationJpaRepository applicationJpaRepository;

    public InterviewRepositoryAdapter(
            InterviewJpaRepository interviewJpaRepository,
            ApplicationJpaRepository applicationJpaRepository) {
        this.interviewJpaRepository = interviewJpaRepository;
        this.applicationJpaRepository = applicationJpaRepository;
    }

    @Override
    public Interview save(Interview domain) {
        ApplicationJpaEntity appEntity = applicationJpaRepository.findById(domain.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Application id: " + domain.getApplicationId()));

        InterviewJpaEntity entity;
        if (domain.getId() != null) {
            entity = interviewJpaRepository.findById(domain.getId())
                    .orElse(new InterviewJpaEntity());
        } else {
            entity = new InterviewJpaEntity();
        }

        entity.setApplication(appEntity);
        entity.setInterviewTime(domain.getInterviewTime());
        entity.setLocationType(domain.getLocationType());
        entity.setMeetingLinkOrAddress(domain.getMeetingLinkOrAddress());
        entity.setNotes(domain.getNotes());
        entity.setStatus(domain.getStatus());

        InterviewJpaEntity saved = interviewJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Interview> findById(Long id) {
        return interviewJpaRepository.findDetailedById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Interview> findByApplicationId(Long applicationId) {
        return interviewJpaRepository.findByApplicationIdOrderByInterviewTimeDesc(applicationId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Interview> findLatestByApplicationId(Long applicationId) {
        return interviewJpaRepository.findFirstByApplicationIdOrderByInterviewTimeDesc(applicationId)
                .map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Interview> findByCandidateUserId(Long candidateUserId) {
        return interviewJpaRepository.findByCandidateUserId(candidateUserId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private Interview toDomain(InterviewJpaEntity entity) {
        if (entity == null) return null;
        return new Interview(
                entity.getId(),
                entity.getApplication() != null ? entity.getApplication().getId() : null,
                entity.getInterviewTime(),
                entity.getLocationType(),
                entity.getMeetingLinkOrAddress(),
                entity.getNotes(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
