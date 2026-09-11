package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.Company;

import java.time.LocalDateTime;

public record CompanyResult(
    Long id,
    String name,
    String logoUrl,
    String website,
    String description,
    String companySize,
    String address,
    String taxCode,
    String status,
    Long createdByUserId,
    LocalDateTime createdAt
) {
    public static CompanyResult from(Company company) {
        return new CompanyResult(
            company.getId(),
            company.getName(),
            company.getLogoUrl(),
            company.getWebsite(),
            company.getDescription(),
            company.getCompanySize(),
            company.getAddress(),
            company.getTaxCode(),
            company.getStatus() != null ? company.getStatus().name() : null,
            company.getCreatedByUserId(),
            company.getCreatedAt()
        );
    }
}