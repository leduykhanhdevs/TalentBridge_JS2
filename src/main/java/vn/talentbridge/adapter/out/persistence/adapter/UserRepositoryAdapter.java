package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.UserJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.RoleJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.RoleName;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Transactional
public class UserRepositoryAdapter implements UserRepositoryPort {
    private final UserJpaRepository userJpaRepository;
    private final RoleJpaRepository roleJpaRepository;

    public UserRepositoryAdapter(UserJpaRepository userJpaRepository, RoleJpaRepository roleJpaRepository) {
        this.userJpaRepository = userJpaRepository;
        this.roleJpaRepository = roleJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public User save(User domainUser) {
        UserJpaEntity entity;
        if (domainUser.getId() != null) {
            entity = userJpaRepository.findById(domainUser.getId())
                    .orElse(new UserJpaEntity());
        } else {
            entity = new UserJpaEntity();
        }

        entity.setEmail(domainUser.getEmail());
        entity.setPasswordHash(domainUser.getPasswordHash());
        entity.setFullName(domainUser.getFullName());
        entity.setPhoneNumber(domainUser.getPhoneNumber());
        entity.setAvatarUrl(domainUser.getAvatarUrl());
        entity.setStatus(domainUser.getStatus());

        if (domainUser.getRoles() != null && !domainUser.getRoles().isEmpty()) {
            Set<RoleJpaEntity> roleEntities = new HashSet<>();
            for (Role role : domainUser.getRoles()) {
                String roleNameStr = role.getName() != null ? role.getName().name() : RoleName.ROLE_CANDIDATE.name();
                RoleJpaEntity roleEntity = roleJpaRepository.findByName(roleNameStr)
                        .orElseGet(() -> roleJpaRepository.save(RoleJpaEntity.builder()
                                .name(roleNameStr)
                                .description(role.getDescription())
                                .build()));
                roleEntities.add(roleEntity);
            }
            entity.setRoles(roleEntities);
        }

        UserJpaEntity saved = userJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll(int page, int size) {
        return userJpaRepository.findAll(PageRequest.of(page, size)).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return userJpaRepository.count();
    }

    private User toDomain(UserJpaEntity entity) {
        Set<Role> domainRoles = entity.getRoles().stream()
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

        return new User(
                entity.getId(),
                entity.getEmail(),
                entity.getPasswordHash(),
                entity.getFullName(),
                entity.getPhoneNumber(),
                entity.getAvatarUrl(),
                entity.getStatus(),
                domainRoles,
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}