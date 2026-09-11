package vn.talentbridge.adapter.in.web.dto.response;

import lombok.*;
import vn.talentbridge.core.application.dto.CandidateResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateAdminResponse {

    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String phone;
    private String avatarUrl;
    private String status;
    private String title;
    private LocalDate dob;
    private String gender;
    private String summary;
    private Integer experienceYears;
    private BigDecimal currentSalary;
    private BigDecimal expectedSalary;
    private String city;
    private String address;
    private String personalWebsite;
    private String linkedinUrl;
    private String githubUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CandidateAdminResponse from(CandidateResult result) {
        return CandidateAdminResponse.builder()
                .id(result.id())
                .userId(result.userId())
                .fullName(result.fullName())
                .email(result.email())
                .phone(result.phone())
                .avatarUrl(result.avatarUrl())
                .status(result.status())
                .title(result.title())
                .dob(result.dob())
                .gender(result.gender())
                .summary(result.summary())
                .experienceYears(result.experienceYears())
                .currentSalary(result.currentSalary())
                .expectedSalary(result.expectedSalary())
                .city(result.city())
                .address(result.address())
                .personalWebsite(result.personalWebsite())
                .linkedinUrl(result.linkedinUrl())
                .githubUrl(result.githubUrl())
                .createdAt(result.createdAt())
                .updatedAt(result.updatedAt())
                .build();
    }
}
