package vn.talentbridge.adapter.out.persistence.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.ApplicationJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.ApplicationStageJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.ApplicationJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.ApplicationStageJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.port.out.ApplicationStageRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.ApplicationStage;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class ApplicationStagePersistenceAdapter implements ApplicationStageRepositoryPort {

    private final ApplicationStageJpaRepository applicationStageJpaRepository;
    private final ApplicationJpaRepository applicationJpaRepository;
    private final UserJpaRepository userJpaRepository;

    @Override
    public ApplicationStage save(ApplicationStage stage) {
        ApplicationJpaEntity applicationEntity = applicationJpaRepository.findById(stage.getApplicationId())
                .orElseThrow(() -> new ResourceNotFoundException("Đơn ứng tuyển", stage.getApplicationId()));

        UserJpaEntity userEntity = userJpaRepository.findById(stage.getChangedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng", stage.getChangedByUserId()));

        ApplicationStageJpaEntity entity = ApplicationStageJpaEntity.builder()
                .application(applicationEntity)
                .stage(stage.getStage())
                .note(stage.getNote())
                .changedByUser(userEntity)
                .changedAt(stage.getChangedAt() != null ? stage.getChangedAt() : LocalDateTime.now())
                .build();

        ApplicationStageJpaEntity saved = applicationStageJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApplicationStage> findByApplicationId(Long applicationId) {
        return applicationStageJpaRepository.findByApplicationIdOrderByChangedAtDesc(applicationId).stream()
                .map(this::toDomain)
                .toList();
    }

    private ApplicationStage toDomain(ApplicationStageJpaEntity entity) {
        ApplicationStage domain = new ApplicationStage();
        domain.setId(entity.getId());
        if (entity.getApplication() != null) {
            domain.setApplicationId(entity.getApplication().getId());
        }
        domain.setStage(entity.getStage());
        domain.setNote(entity.getNote());
        if (entity.getChangedByUser() != null) {
            domain.setChangedByUserId(entity.getChangedByUser().getId());
            domain.setChangedByUserName(entity.getChangedByUser().getFullName());
        }
        domain.setChangedAt(entity.getChangedAt());
        return domain;
    }
}
