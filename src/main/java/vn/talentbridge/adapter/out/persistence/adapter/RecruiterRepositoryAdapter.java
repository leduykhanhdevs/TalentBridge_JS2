package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import vn.talentbridge.adapter.out.persistence.entity.CompanyJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RecruiterJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CompanyJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RecruiterJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.RoleName;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RecruiterRepositoryAdapter implements RecruiterRepositoryPort {

    private final RecruiterJpaRepository recruiterJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final CompanyJpaRepository companyJpaRepository;

    public RecruiterRepositoryAdapter(RecruiterJpaRepository recruiterJpaRepository,
                                     UserJpaRepository userJpaRepository,
                                     CompanyJpaRepository companyJpaRepository) {
        this.recruiterJpaRepository = recruiterJpaRepository;
        this.userJpaRepository = userJpaRepository;
        this.companyJpaRepository = companyJpaRepository;
    }

    @Override
    public Optional<Recruiter> findById(Long id) {
        return recruiterJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Recruiter> findByUserId(Long userId) {
        return recruiterJpaRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    public List<Recruiter> findAll(int page, int size, String keyword) {
        Page<RecruiterJpaEntity> resultPage = recruiterJpaRepository.searchRecruiters(keyword, PageRequest.of(page, size));
        return resultPage.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public long count(String keyword) {
        return recruiterJpaRepository.countSearchRecruiters(keyword);
    }

    @Override
    public Recruiter save(Recruiter recruiter) {
        RecruiterJpaEntity entity;
        if (recruiter.getId() != null) {
            entity = recruiterJpaRepository.findById(recruiter.getId())
                    .orElse(new RecruiterJpaEntity());
        } else {
            entity = new RecruiterJpaEntity();
        }

        if (recruiter.getUser() != null && recruiter.getUser().getId() != null) {
            UserJpaEntity userEntity = userJpaRepository.findById(recruiter.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + recruiter.getUser().getId()));
            entity.setUser(userEntity);
        }

        if (recruiter.getCompany() != null && recruiter.getCompany().getId() != null) {
            CompanyJpaEntity companyEntity = companyJpaRepository.findById(recruiter.getCompany().getId())
                    .orElse(null);
            entity.setCompany(companyEntity);
        } else {
            entity.setCompany(null);
        }

        entity.setPosition(recruiter.getPosition());

        RecruiterJpaEntity saved = recruiterJpaRepository.save(entity);
        return toDomain(saved);
    }

    private Recruiter toDomain(RecruiterJpaEntity entity) {
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
                    c.getTaxCode(),
                    c.getStatus(),
                    c.getCreatedByUserId(),
                    c.getCreatedAt(),
                    c.getUpdatedAt()
            );
        }

        return new Recruiter(
                entity.getId(),
                user,
                company,
                entity.getPosition(),
                entity.getCreatedAt()
        );
    }
}