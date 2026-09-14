package vn.talentbridge.core.application.dto;

public record RequestCreateCompanyCommand(
        String name,
        String taxCode,
        String website,
        String companySize,
        String address,
        String city,
        String description,
        String logoUrl
) {
}
