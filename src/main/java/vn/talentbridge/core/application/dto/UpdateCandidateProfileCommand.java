package vn.talentbridge.core.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateCandidateProfileCommand(
    String fullName,
    String phone,
    String avatarUrl,
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
    String githubUrl
) {}
