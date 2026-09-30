package vn.talentbridge.adapter.in.web.dto.response;

import lombok.*;
import vn.talentbridge.core.application.dto.ApplicationStageResult;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStageResponse {
    private Long id;
    private Long applicationId;
    private String stage;
    private String note;
    private Long changedByUserId;
    private String changedByUserName;
    private LocalDateTime changedAt;

    public static ApplicationStageResponse from(ApplicationStageResult result) {
        if (result == null) return null;
        return ApplicationStageResponse.builder()
                .id(result.getId())
                .applicationId(result.getApplicationId())
                .stage(result.getStage())
                .note(result.getNote())
                .changedByUserId(result.getChangedByUserId())
                .changedByUserName(result.getChangedByUserName())
                .changedAt(result.getChangedAt())
                .build();
    }
}
