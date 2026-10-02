package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.CvTemplate;

import java.time.LocalDateTime;

public record CvTemplateResult(
        Integer id,
        String name,
        String templateCode,
        String thumbnailUrl,
        String description,
        String defaultConfig,
        Boolean isActive,
        LocalDateTime createdAt
) {
    public static CvTemplateResult from(CvTemplate entity) {
        if (entity == null) return null;
        return new CvTemplateResult(
                entity.id(),
                entity.name(),
                entity.templateCode(),
                entity.thumbnailUrl(),
                entity.description(),
                entity.defaultConfig(),
                entity.isActive(),
                entity.createdAt()
        );
    }
}
