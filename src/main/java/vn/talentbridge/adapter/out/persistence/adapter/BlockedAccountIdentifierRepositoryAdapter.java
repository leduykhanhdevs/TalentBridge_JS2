package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.BlockedAccountIdentifierJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.BlockedAccountIdentifierJpaRepository;
import vn.talentbridge.core.application.port.out.BlockedAccountIdentifierRepositoryPort;

import java.time.LocalDateTime;

@Component
@Transactional
public class BlockedAccountIdentifierRepositoryAdapter implements BlockedAccountIdentifierRepositoryPort {
    private final BlockedAccountIdentifierJpaRepository repository;

    public BlockedAccountIdentifierRepositoryAdapter(BlockedAccountIdentifierJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean exists(String identifierType, String identifierHash) {
        return repository.existsByIdentifierTypeAndIdentifierHash(identifierType, identifierHash);
    }

    @Override
    public void save(String identifierType, String identifierHash) {
        if (repository.existsByIdentifierTypeAndIdentifierHash(identifierType, identifierHash)) {
            return;
        }
        BlockedAccountIdentifierJpaEntity entity = new BlockedAccountIdentifierJpaEntity();
        entity.setIdentifierType(identifierType);
        entity.setIdentifierHash(identifierHash);
        entity.setCreatedAt(LocalDateTime.now());
        repository.save(entity);
    }
}
