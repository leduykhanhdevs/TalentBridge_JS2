package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.CandidateJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.RoleName;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
public class CandidateRepositoryAdapter implements CandidateRepositoryPort {

    private final CandidateJpaRepository candidateJpaRepository;
    private final UserJpaRepository userJpaRepository;

    public CandidateRepositoryAdapter(CandidateJpaRepository candidateJpaRepository,
                                      UserJpaRepository userJpaRepository) {
        this.candidateJpaRepository = candidateJpaRepository;
        this.userJpaRepository = userJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Candidate> findById(Long id) {
        return candidateJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Candidate> findByUserId(Long userId) {
        return candidateJpaRepository.findByUserId(userId).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Candidate> findAll(int page, int size, String keyword, UserStatus status) {
        Page<CandidateJpaEntity> resultPage = candidateJpaRepository.searchCandidates(keyword, status, PageRequest.of(page, size));
        return resultPage.stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long count(String keyword, UserStatus status) {
        return candidateJpaRepository.countSearchCandidates(keyword, status);
    }

    @Override
    public Candidate save(Candidate candidate) {
        CandidateJpaEntity entity;
        if (candidate.getId() != null) {
            entity = candidateJpaRepository.findById(candidate.getId())
                    .orElse(new CandidateJpaEntity());
        } else {
            entity = new CandidateJpaEntity();
        }

        if (candidate.getUser() != null && candidate.getUser().getId() != null) {
            UserJpaEntity userEntity = userJpaRepository.findById(candidate.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + candidate.getUser().getId()));
            entity.setUser(userEntity);
        }

        entity.setTitle(candidate.getTitle());
        entity.setDob(candidate.getDob());
        entity.setGender(candidate.getGender() != null ? candidate.getGender() : "OTHER");
        entity.setSummary(candidate.getSummary());
        entity.setExperienceYears(candidate.getExperienceYears() != null ? candidate.getExperienceYears() : 0);
        entity.setCurrentSalary(candidate.getCurrentSalary());
        entity.setExpectedSalary(candidate.getExpectedSalary());
        entity.setCity(candidate.getCity());
        entity.setAddress(candidate.getAddress());
        entity.setPersonalWebsite(candidate.getPersonalWebsite());
        entity.setLinkedinUrl(candidate.getLinkedinUrl());
        entity.setGithubUrl(candidate.getGithubUrl());

        CandidateJpaEntity saved = candidateJpaRepository.save(entity);
        return toDomain(saved);
    }

    private Candidate toDomain(CandidateJpaEntity entity) {
        User user = null;
        if (entity.getUser() != null) {
            UserJpaEntity u = entity.getUser();
            Set<Role> roles = u.getRoles() == null ? Set.of() : u.getRoles().stream()
                    .map(r -> {
                        RoleName roleName;
                        try {
                            roleName = RoleName.valueOf(r.getName());
                        } catch (Exception e) {
                            roleName = RoleName.ROLE_CANDIDATE;
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

        return new Candidate(
                entity.getId(),
                user,
                entity.getTitle(),
                entity.getDob(),
                entity.getGender(),
                entity.getSummary(),
                entity.getExperienceYears(),
                entity.getCurrentSalary(),
                entity.getExpectedSalary(),
                entity.getCity(),
                entity.getAddress(),
                entity.getPersonalWebsite(),
                entity.getLinkedinUrl(),
                entity.getGithubUrl(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
