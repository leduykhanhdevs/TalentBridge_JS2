package vn.talentbridge.adapter.in.web.dto.response;

import lombok.*;
import vn.talentbridge.core.application.dto.ApplicationNoteResult;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationNoteResponse {
    private Long id;
    private Long applicationId;
    private Long recruiterId;
    private String recruiterName;
    private Integer rating;
    private String tag;
    private String comment;
    private LocalDateTime createdAt;

    public static ApplicationNoteResponse from(ApplicationNoteResult result) {
        if (result == null) return null;
        return ApplicationNoteResponse.builder()
                .id(result.getId())
                .applicationId(result.getApplicationId())
                .recruiterId(result.getRecruiterId())
                .recruiterName(result.getRecruiterName())
                .rating(result.getRating())
                .tag(result.getTag())
                .comment(result.getComment())
                .createdAt(result.getCreatedAt())
                .build();
    }
}
