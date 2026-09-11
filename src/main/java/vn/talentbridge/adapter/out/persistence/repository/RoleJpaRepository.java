package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.RoleJpaEntity;

import java.util.Optional;

@Repository
public interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, Long> {
    Optional<RoleJpaEntity> findByName(String name);
}