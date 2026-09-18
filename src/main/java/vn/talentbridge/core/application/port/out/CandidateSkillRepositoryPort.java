package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.CandidateSkill;

import java.util.List;
import java.util.Optional;

public interface CandidateSkillRepositoryPort {
    List<CandidateSkill> findByCandidateId(Long candidateId);
    Optional<CandidateSkill> findById(Long id);
    Optional<CandidateSkill> findByCandidateIdAndSkillId(Long candidateId, Integer skillId);
    CandidateSkill save(CandidateSkill candidateSkill);
    void deleteById(Long id);
}
