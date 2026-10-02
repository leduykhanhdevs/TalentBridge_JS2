package vn.talentbridge.core.domain.model;

import java.time.LocalDateTime;

public record CvTemplate(
        Integer id,
        String name,
        String templateCode,
        String thumbnailUrl,
        String description,
        String defaultConfig,
        Boolean isActive,
        LocalDateTime createdAt
) {}
