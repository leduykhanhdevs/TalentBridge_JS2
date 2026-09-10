package vn.talentbridge.modules.admin.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.talentbridge.modules.company.entity.Company;
import vn.talentbridge.modules.company.enums.CompanyStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyAdminResponse {

    private Long id;
    private String name;
    private String logoUrl;
    private String website;
    private String industry;
    private String companySize;
    private String address;
    private String city;
    private String description;
    private CompanyStatus status;
    private LocalDateTime createdAt;

    public static CompanyAdminResponse from(Company company) {
        return CompanyAdminResponse.builder()
                .id(company.getId())
                .name(company.getName())
                .logoUrl(company.getLogoUrl())
                .website(company.getWebsite())
                .industry(company.getIndustry())
                .companySize(company.getCompanySize())
                .address(company.getAddress())
                .city(company.getCity())
                .description(company.getDescription())
                .status(company.getStatus())
                .createdAt(company.getCreatedAt())
                .build();
    }
}