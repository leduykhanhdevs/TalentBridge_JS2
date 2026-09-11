package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.Candidate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CandidateResult(
    Long id,
    Long userId,
    String fullName,
    String email,
    String phone,
    String avatarUrl,
    String status,
    String title,
    LocalDate dob,
    String gender,
    String summary,
    Integer experienceYears,
    BigDecimal currentSalary,
    BigDecimal expectedSalary,
    String city,
    String address,
    String personalWebsite,
    String linkedinUrl,
    String githubUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static CandidateResult from(Candidate candidate) {
        Long userId = candidate.getUser() != null ? candidate.getUser().getId() : null;
        String fullName = candidate.getUser() != null ? candidate.getUser().getFullName() : null;
        String email = candidate.getUser() != null ? candidate.getUser().getEmail() : null;
        String phone = candidate.getUser() != null ? candidate.getUser().getPhoneNumber() : null;
        String avatarUrl = candidate.getUser() != null ? candidate.getUser().getAvatarUrl() : null;
        String status = candidate.getUser() != null && candidate.getUser().getStatus() != null
                ? candidate.getUser().getStatus().name() : null;

        return new CandidateResult(
            candidate.getId(),
            userId,
            fullName,
            email,
            phone,
            avatarUrl,
            status,
            candidate.getTitle(),
            candidate.getDob(),
            candidate.getGender(),
            candidate.getSummary(),
            candidate.getExperienceYears(),
            candidate.getCurrentSalary(),
            candidate.getExpectedSalary(),
            candidate.getCity(),
            candidate.getAddress(),
            candidate.getPersonalWebsite(),
            candidate.getLinkedinUrl(),
            candidate.getGithubUrl(),
            candidate.getCreatedAt(),
            candidate.getUpdatedAt()
        );
    }
}
