package vn.talentbridge.core.application.dto;

public record AddCandidateSkillCommand(
        Integer skillId,
        String skillName,
        String proficiencyLevel,
        Integer rating,
        Double yearsOfExperience
) {
}
