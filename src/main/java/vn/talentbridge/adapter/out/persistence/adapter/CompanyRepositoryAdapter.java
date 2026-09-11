package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.core.application.port.out.CompanyRepositoryPort;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.vo.CompanyStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CompanyRepositoryAdapter implements CompanyRepositoryPort {
    private final CompanyJpaRepository companyJpaRepository;

    public CompanyRepositoryAdapter(CompanyJpaRepository companyJpaRepository) {
        this.companyJpaRepository = companyJpaRepository;
    }

    @Override
    public Optional<Company> findById(Long id) {
        return companyJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Company save(Company domainCompany) {
        CompanyJpaEntity entity;
        if (domainCompany.getId() != null) {
            entity = companyJpaRepository.findById(domainCompany.getId())
                    .orElse(new CompanyJpaEntity());
        } else {
            entity = new CompanyJpaEntity();
        }

        entity.setName(domainCompany.getName());
        entity.setLogoUrl(domainCompany.getLogoUrl());
        entity.setWebsite(domainCompany.getWebsite());
        entity.setDescription(domainCompany.getDescription());
        entity.setCompanySize(domainCompany.getCompanySize());
        entity.setAddress(domainCompany.getAddress());
        entity.setTaxCode(domainCompany.getTaxCode());
        entity.setStatus(domainCompany.getStatus());
        entity.setCreatedByUserId(domainCompany.getCreatedByUserId());

        CompanyJpaEntity saved = companyJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<Company> findAll(int page, int size, CompanyStatus status) {
        Page<CompanyJpaEntity> resultPage;
        if (status != null) {
            resultPage = companyJpaRepository.findByStatus(status, PageRequest.of(page, size));
        } else {
            resultPage = companyJpaRepository.findAll(PageRequest.of(page, size));
        }
        return resultPage.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count() {
        return companyJpaRepository.count();
    }

    @Override
    public long countByStatus(CompanyStatus status) {
        return companyJpaRepository.countByStatus(status);
    }

    private Company toDomain(CompanyJpaEntity entity) {
        return new Company(
                entity.getId(),
                entity.getName(),
                entity.getLogoUrl(),
                entity.getWebsite(),
                entity.getDescription(),
                entity.getCompanySize(),
                entity.getAddress(),
                entity.getTaxCode(),
                entity.getStatus(),
                entity.getCreatedByUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}