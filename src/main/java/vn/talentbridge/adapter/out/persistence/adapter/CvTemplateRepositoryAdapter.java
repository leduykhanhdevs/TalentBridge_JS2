package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.CvTemplateJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CvTemplateJpaRepository;
import vn.talentbridge.core.application.port.out.CvTemplateRepositoryPort;
import vn.talentbridge.core.domain.model.CvTemplate;

import java.util.List;
import java.util.Optional;

@Component
@Transactional(readOnly = true)
public class CvTemplateRepositoryAdapter implements CvTemplateRepositoryPort {

    private final CvTemplateJpaRepository cvTemplateJpaRepository;

    public CvTemplateRepositoryAdapter(CvTemplateJpaRepository cvTemplateJpaRepository) {
        this.cvTemplateJpaRepository = cvTemplateJpaRepository;
    }

    @Override
    public List<CvTemplate> findAllActive() {
        return cvTemplateJpaRepository.findByIsActiveTrue()
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<CvTemplate> findByTemplateCode(String templateCode) {
        return cvTemplateJpaRepository.findByTemplateCode(templateCode).map(this::toDomain);
    }

    private CvTemplate toDomain(CvTemplateJpaEntity entity) {
        if (entity == null) return null;
        return new CvTemplate(
                entity.getId(),
                entity.getName(),
                entity.getTemplateCode(),
                entity.getThumbnailUrl(),
                entity.getDescription(),
                entity.getDefaultConfig(),
                entity.getIsActive(),
                entity.getCreatedAt()
        );
    }
}
