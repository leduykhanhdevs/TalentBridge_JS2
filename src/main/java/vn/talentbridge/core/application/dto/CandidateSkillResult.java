package vn.talentbridge.core.application.dto;

import vn.talentbridge.core.domain.model.CandidateSkill;

public record CandidateSkillResult(
        Long id,
        Long candidateId,
        Integer skillId,
        String skillName,
        String proficiencyLevel,
        Integer rating,
        Double yearsOfExperience
) {
    public static CandidateSkillResult from(CandidateSkill cs) {
        return new CandidateSkillResult(
                cs.getId(),
                cs.getCandidateId(),
                cs.getSkillId(),
                cs.getSkillName(),
                cs.getProficiencyLevel(),
                cs.getRating(),
                cs.getYearsOfExperience()
        );
    }
}
