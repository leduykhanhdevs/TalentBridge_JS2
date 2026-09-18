package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.AddCandidateSkillCommand;
import vn.talentbridge.core.application.dto.CandidateSkillResult;
import vn.talentbridge.core.application.dto.SkillResult;

import java.util.List;

public interface CandidateSkillUseCase {
    List<CandidateSkillResult> getCandidateSkills(Long userId);
    CandidateSkillResult addCandidateSkill(Long userId, AddCandidateSkillCommand command);
    void deleteCandidateSkill(Long userId, Long candidateSkillId);
    List<SkillResult> getAllAvailableSkills();
}
