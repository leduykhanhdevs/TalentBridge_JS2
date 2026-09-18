package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.SkillJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.SkillJpaRepository;
import vn.talentbridge.core.application.port.out.SkillRepositoryPort;
import vn.talentbridge.core.domain.model.Skill;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class SkillRepositoryAdapter implements SkillRepositoryPort {

    private final SkillJpaRepository skillJpaRepository;

    public SkillRepositoryAdapter(SkillJpaRepository skillJpaRepository) {
        this.skillJpaRepository = skillJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Skill> findAll() {
        return skillJpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Skill> findById(Integer id) {
        return skillJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Skill> findByName(String name) {
        return skillJpaRepository.findByNameIgnoreCase(name).map(this::toDomain);
    }

    @Override
    public Skill save(Skill domain) {
        SkillJpaEntity entity = new SkillJpaEntity();
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }
        entity.setName(domain.getName());
        SkillJpaEntity saved = skillJpaRepository.save(entity);
        return toDomain(saved);
    }

    private Skill toDomain(SkillJpaEntity entity) {
        return new Skill(entity.getId(), entity.getName());
    }
}
