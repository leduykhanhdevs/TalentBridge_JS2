package vn.talentbridge.adapter.in.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.talentbridge.core.application.dto.CompanyResult;
import vn.talentbridge.core.domain.vo.CompanyStatus;

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
    private String companySize;
    private String address;
    private String taxCode;
    private String description;
    private CompanyStatus status;
    private Long createdByUserId;
    private LocalDateTime createdAt;

    public static CompanyAdminResponse from(CompanyResult result) {
        return CompanyAdminResponse.builder()
                .id(result.id())
                .name(result.name())
                .logoUrl(result.logoUrl())
                .website(result.website())
                .companySize(result.companySize())
                .address(result.address())
                .taxCode(result.taxCode())
                .description(result.description())
                .status(result.status() != null ? CompanyStatus.valueOf(result.status()) : null)
                .createdByUserId(result.createdByUserId())
                .createdAt(result.createdAt())
                .build();
    }
}