package vn.talentbridge.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyJoinRequestResponse {

    private Long id;
    private Long userId;
    private String applicantName;
    private String applicantEmail;
    private String applicantPhone;
    private String applicantAvatarUrl;
    private Long companyId;
    private String companyName;
    private String companyLogoUrl;
    private String position;
    private String message;
    private String status;
    private String reason;
    private Long approvedByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CompanyJoinRequestResponse from(CompanyJoinRequestResult result) {
        return CompanyJoinRequestResponse.builder()
                .id(result.id())
                .userId(result.userId())
                .applicantName(result.applicantName())
                .applicantEmail(result.applicantEmail())
                .applicantPhone(result.applicantPhone())
                .applicantAvatarUrl(result.applicantAvatarUrl())
                .companyId(result.companyId())
                .companyName(result.companyName())
                .companyLogoUrl(result.companyLogoUrl())
                .position(result.position())
                .message(result.message())
                .status(result.status())
                .reason(result.reason())
                .approvedByUserId(result.approvedByUserId())
                .createdAt(result.createdAt())
                .updatedAt(result.updatedAt())
                .build();
    }
}
