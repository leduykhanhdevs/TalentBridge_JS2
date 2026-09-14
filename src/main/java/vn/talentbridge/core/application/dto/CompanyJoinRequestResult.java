package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.CompanyJoinRequest;

import java.time.LocalDateTime;

public record CompanyJoinRequestResult(
        Long id,
        Long userId,
        String applicantName,
        String applicantEmail,
        String applicantPhone,
        String applicantAvatarUrl,
        Long companyId,
        String companyName,
        String companyLogoUrl,
        String position,
        String message,
        String status,
        String reason,
        Long approvedByUserId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CompanyJoinRequestResult from(CompanyJoinRequest req) {
        Long userId = req.getUser() != null ? req.getUser().getId() : null;
        String applicantName = req.getUser() != null ? req.getUser().getFullName() : null;
        String applicantEmail = req.getUser() != null ? req.getUser().getEmail() : null;
        String applicantPhone = req.getUser() != null ? req.getUser().getPhoneNumber() : null;
        String applicantAvatarUrl = req.getUser() != null ? req.getUser().getAvatarUrl() : null;

        Long companyId = req.getCompany() != null ? req.getCompany().getId() : null;
        String companyName = req.getCompany() != null ? req.getCompany().getName() : null;
        String companyLogoUrl = req.getCompany() != null ? req.getCompany().getLogoUrl() : null;

        return new CompanyJoinRequestResult(
                req.getId(),
                userId,
                applicantName,
                applicantEmail,
                applicantPhone,
                applicantAvatarUrl,
                companyId,
                companyName,
                companyLogoUrl,
                req.getPosition(),
                req.getMessage(),
                req.getStatus() != null ? req.getStatus().name() : null,
                req.getReason(),
                req.getApprovedByUserId(),
                req.getCreatedAt(),
                req.getUpdatedAt()
        );
    }
}
