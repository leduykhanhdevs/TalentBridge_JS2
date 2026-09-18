package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.Skill;

public record SkillResult(
        Integer id,
        String name
) {
    public static SkillResult from(Skill skill) {
        return new SkillResult(skill.getId(), skill.getName());
    }
}
