package vn.talentbridge.core.domain.model;

import vn.talentbridge.core.domain.vo.CompanyStatus;

import java.time.LocalDateTime;

public class Company {
    private Long id;
    private String name;
    private String logoUrl;
    private String website;
    private String description;
    private String companySize;
    private String address;
    private String taxCode;
    private CompanyStatus status;
    private Long createdByUserId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Company() {
        this.status = CompanyStatus.PENDING;
    }

    public Company(Long id, String name, String logoUrl, String website, String description,
                   String companySize, String address, String taxCode, CompanyStatus status,
                   Long createdByUserId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.logoUrl = logoUrl;
        this.website = website;
        this.description = description;
        this.companySize = companySize;
        this.address = address;
        this.taxCode = taxCode;
        this.status = status != null ? status : CompanyStatus.PENDING;
        this.createdByUserId = createdByUserId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void approve() { this.status = CompanyStatus.APPROVED; }
    public void reject() { this.status = CompanyStatus.REJECTED; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCompanySize() { return companySize; }
    public void setCompanySize(String companySize) { this.companySize = companySize; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getTaxCode() { return taxCode; }
    public void setTaxCode(String taxCode) { this.taxCode = taxCode; }

    public CompanyStatus getStatus() { return status; }
    public void setStatus(CompanyStatus status) { this.status = status; }

    public Long getCreatedByUserId() { return createdByUserId; }
    public void setCreatedByUserId(Long createdByUserId) { this.createdByUserId = createdByUserId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}