package vn.talentbridge.adapter.out.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.talentbridge.adapter.out.persistence.entity.BlockedAccountIdentifierJpaEntity;

@Repository
public interface BlockedAccountIdentifierJpaRepository extends JpaRepository<BlockedAccountIdentifierJpaEntity, Long> {
    boolean existsByIdentifierTypeAndIdentifierHash(String identifierType, String identifierHash);
}
