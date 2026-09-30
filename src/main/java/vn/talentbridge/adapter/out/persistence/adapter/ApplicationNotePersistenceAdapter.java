package vn.talentbridge.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.ApplicationJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.ApplicationNoteJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RecruiterJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.ApplicationJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.ApplicationNoteJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RecruiterJpaRepository;
import vn.talentbridge.core.application.port.out.ApplicationNoteRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.ApplicationNote;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class ApplicationNotePersistenceAdapter implements ApplicationNoteRepositoryPort {

    private final ApplicationNoteJpaRepository applicationNoteJpaRepository;
    private final ApplicationJpaRepository applicationJpaRepository;
    private final RecruiterJpaRepository recruiterJpaRepository;

    @Override
    public ApplicationNote save(ApplicationNote note) {
        ApplicationJpaEntity applicationEntity = applicationJpaRepository.findById(note.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Đơn ứng tuyển", note.getApplicationId()));

        RecruiterJpaEntity recruiterEntity = recruiterJpaRepository.findById(note.getRecruiterId())
                .orElseThrow(() -> new ResourceNotFoundException("Nhà tuyển dụng", note.getRecruiterId()));

        ApplicationNoteJpaEntity entity = ApplicationNoteJpaEntity.builder()
                .application(applicationEntity)
                .recruiter(recruiterEntity)
                .rating(note.getRating())
                .tag(note.getTag())
                .comment(note.getComment())
                .createdAt(note.getCreatedAt() != null ? note.getCreatedAt() : LocalDateTime.now())
                .build();

        ApplicationNoteJpaEntity saved = applicationNoteJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationNote> findByApplicationId(Long applicationId) {
        return applicationNoteJpaRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRatingByApplicationId(Long applicationId) {
        return applicationNoteJpaRepository.findAverageRatingByApplicationId(applicationId);
    }

    @Override
    @Transactional(readOnly = true)
    public int countByApplicationId(Long applicationId) {
        return applicationNoteJpaRepository.countByApplicationId(applicationId);
    }

    private ApplicationNote toDomain(ApplicationNoteJpaEntity entity) {
        ApplicationNote domain = new ApplicationNote();
        domain.setId(entity.getId());
        if (entity.getApplication() != null) {
            domain.setApplicationId(entity.getApplication().getId());
        }
        if (entity.getRecruiter() != null) {
            domain.setRecruiterId(entity.getRecruiter().getId());
            if (entity.getRecruiter().getUser() != null) {
                domain.setRecruiterName(entity.getRecruiter().getUser().getFullName());
            }
        }
        domain.setRating(entity.getRating());
        domain.setTag(entity.getTag());
        domain.setComment(entity.getComment());
        domain.setCreatedAt(entity.getCreatedAt());
        return domain;
    }
}
