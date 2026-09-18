package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.WorkExperience;

import java.util.List;
import java.util.Optional;

public interface WorkExperienceRepositoryPort {
    List<WorkExperience> findByCandidateId(Long candidateId);
    Optional<WorkExperience> findById(Long id);
    WorkExperience save(WorkExperience workExperience);
    void deleteById(Long id);
}
