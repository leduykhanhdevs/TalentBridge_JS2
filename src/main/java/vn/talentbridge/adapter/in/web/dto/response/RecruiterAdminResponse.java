package vn.talentbridge.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.talentbridge.core.application.dto.RecruiterResult;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruiterAdminResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String status;
    private String position;
    private Long companyId;
    private String companyName;
    private String companyLogoUrl;
    private String companyStatus;
    private LocalDateTime createdAt;

    public static RecruiterAdminResponse from(RecruiterResult result) {
        return RecruiterAdminResponse.builder()
                .id(result.id())
                .userId(result.userId())
                .fullName(result.fullName())
                .email(result.email())
                .phone(result.phone())
                .avatarUrl(result.avatarUrl())
                .status(result.status())
                .position(result.position())
                .companyId(result.companyId())
                .companyName(result.companyName())
                .companyLogoUrl(result.companyLogoUrl())
                .companyStatus(result.companyStatus())
                .createdAt(result.createdAt())
                .build();
    }
}