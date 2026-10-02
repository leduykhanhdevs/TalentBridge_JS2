package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.Interview;

import java.util.List;
import java.util.Optional;

public interface InterviewRepositoryPort {
    Interview save(Interview interview);
    Optional<Interview> findById(Long id);
    List<Interview> findByApplicationId(Long applicationId);
    Optional<Interview> findLatestByApplicationId(Long applicationId);
    List<Interview> findByCandidateUserId(Long candidateUserId);
}
