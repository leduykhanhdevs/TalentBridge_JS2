package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJoinRequestJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJoinRequestJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.port.out.CompanyJoinRequestRepositoryPort;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.CompanyJoinRequest;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.CompanyJoinRequestStatus;
import vn.talentbridge.core.domain.vo.RoleName;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
public class CompanyJoinRequestRepositoryAdapter implements CompanyJoinRequestRepositoryPort {

    private final CompanyJoinRequestJpaRepository companyJoinRequestJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final CompanyJpaRepository companyJpaRepository;

    public CompanyJoinRequestRepositoryAdapter(CompanyJoinRequestJpaRepository companyJoinRequestJpaRepository,
                                               UserJpaRepository userJpaRepository,
                                               CompanyJpaRepository companyJpaRepository) {
        this.companyJoinRequestJpaRepository = companyJoinRequestJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.companyJpaRepository = companyJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CompanyJoinRequest> findById(Long id) {
        return companyJoinRequestJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public CompanyJoinRequest save(CompanyJoinRequest domain) {
        CompanyJoinRequestJpaEntity entity;
        if (domain.getId() != null) {
            entity = companyJoinRequestJpaRepository.findById(domain.getId())
                    .orElse(new CompanyJoinRequestJpaEntity());
        } else {
            entity = new CompanyJoinRequestJpaEntity();
        }

        if (domain.getUser() != null && domain.getUser().getId() != null) {
            UserJpaEntity userEntity = userJpaRepository.findById(domain.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + domain.getUser().getId()));
            entity.setUser(userEntity);
        }

        if (domain.getCompany() != null && domain.getCompany().getId() != null) {
            CompanyJpaEntity companyEntity = companyJpaRepository.findById(domain.getCompany().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + domain.getCompany().getId()));
            entity.setCompany(companyEntity);
        }

        entity.setPosition(domain.getPosition());
        entity.setMessage(domain.getMessage());
        entity.setStatus(domain.getStatus());
        entity.setReason(domain.getReason());
        entity.setApprovedByUserId(domain.getApprovedByUserId());

        CompanyJoinRequestJpaEntity saved = companyJoinRequestJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndStatus(Long userId, CompanyJoinRequestStatus status) {
        return companyJoinRequestJpaRepository.existsByUserIdAndStatus(userId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CompanyJoinRequest> findByCompanyId(Long companyId, CompanyJoinRequestStatus status, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CompanyJoinRequestJpaEntity> entityPage;
        if (status != null) {
            entityPage = companyJoinRequestJpaRepository.findByCompanyIdAndStatus(companyId, status, pageRequest);
        } else {
            entityPage = companyJoinRequestJpaRepository.findByCompanyId(companyId, pageRequest);
        }
        return entityPage.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCompanyId(Long companyId, CompanyJoinRequestStatus status) {
        if (status != null) {
            return companyJoinRequestJpaRepository.countByCompanyIdAndStatus(companyId, status);
        }
        return companyJoinRequestJpaRepository.countByCompanyId(companyId);
    }

    private CompanyJoinRequest toDomain(CompanyJoinRequestJpaEntity entity) {
        User user = null;
        if (entity.getUser() != null) {
            UserJpaEntity u = entity.getUser();
            Set<Role> roles = u.getRoles() == null ? Set.of() : u.getRoles().stream()
                    .map(r -> {
                        RoleName roleName;
                        try {
                            roleName = RoleName.valueOf(r.getName());
                        } catch (Exception e) {
                            roleName = RoleName.ROLE_RECRUITER;
                        }
                        return new Role(r.getId(), roleName, r.getDescription());
                    })
                    .collect(Collectors.toSet());

            user = new User(
                    u.getId(),
                    u.getEmail(),
                    u.getPasswordHash(),
                    u.getFullName(),
                    u.getPhoneNumber(),
                    u.getAvatarUrl(),
                    u.getStatus(),
                    roles,
                    u.getCreatedAt(),
                    u.getUpdatedAt()
            );
        }

        Company company = null;
        if (entity.getCompany() != null) {
            CompanyJpaEntity c = entity.getCompany();
            company = new Company(
                    c.getId(),
                    c.getName(),
                    c.getLogoUrl(),
                    c.getWebsite(),
                    c.getDescription(),
                    c.getCompanySize(),
                    c.getAddress(),
                    c.getCity(),
                    c.getTaxCode(),
                    c.getStatus(),
                    c.getCreatedByUserId(),
                    c.getCreatedAt(),
                    c.getUpdatedAt()
            );
        }

        return new CompanyJoinRequest(
                entity.getId(),
                user,
                company,
                entity.getPosition(),
                entity.getMessage(),
                entity.getStatus(),
                entity.getReason(),
                entity.getApprovedByUserId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
