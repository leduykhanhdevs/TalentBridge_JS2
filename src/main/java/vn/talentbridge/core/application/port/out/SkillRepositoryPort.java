package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.Skill;

import java.util.List;
import java.util.Optional;

public interface SkillRepositoryPort {
    List<Skill> findAll();
    Optional<Skill> findById(Integer id);
    Optional<Skill> findByName(String name);
    Skill save(Skill skill);
}
