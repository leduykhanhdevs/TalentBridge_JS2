package vn.talentbridge.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import vn.talentbridge.core.domain.vo.CompanyStatus;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyJpaEntity extends BaseJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "company_size", length = 50)
    private String companySize;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "tax_code", length = 50)
    private String taxCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CompanyStatus status = CompanyStatus.PENDING;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;
}