package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.Recruiter;

import java.time.LocalDateTime;

public record RecruiterResult(
    Long id,
    Long userId,
    String fullName,
    String email,
    String phone,
    String avatarUrl,
    String status,
    String position,
    Long companyId,
    String companyName,
    String companyLogoUrl,
    String companyStatus,
    LocalDateTime createdAt
) {
    public static RecruiterResult from(Recruiter recruiter) {
        Long userId = recruiter.getUser() != null ? recruiter.getUser().getId() : null;
        String fullName = recruiter.getUser() != null ? recruiter.getUser().getFullName() : null;
        String email = recruiter.getUser() != null ? recruiter.getUser().getEmail() : null;
        String phone = recruiter.getUser() != null ? recruiter.getUser().getPhoneNumber() : null;
        String avatarUrl = recruiter.getUser() != null ? recruiter.getUser().getAvatarUrl() : null;
        String status = recruiter.getUser() != null && recruiter.getUser().getStatus() != null 
                ? recruiter.getUser().getStatus().name() : null;

        Long companyId = recruiter.getCompany() != null ? recruiter.getCompany().getId() : null;
        String companyName = recruiter.getCompany() != null ? recruiter.getCompany().getName() : null;
        String companyLogoUrl = recruiter.getCompany() != null ? recruiter.getCompany().getLogoUrl() : null;
        String companyStatus = recruiter.getCompany() != null && recruiter.getCompany().getStatus() != null 
                ? recruiter.getCompany().getStatus().name() : null;

        return new RecruiterResult(
            recruiter.getId(),
            userId,
            fullName,
            email,
            phone,
            avatarUrl,
            status,
            recruiter.getPosition(),
            companyId,
            companyName,
            companyLogoUrl,
            companyStatus,
            recruiter.getCreatedAt()
        );
    }
}