package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.application.dto.CandidateApplicationResult;
import vn.talentbridge.core.domain.model.Application;
import java.util.List;
import java.util.Optional;

public interface ApplicationRepositoryPort {
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);
    Optional<Application> findByIdForUpdate(Long id);
    Application save(Application application);
    List<CandidateApplicationResult> findMyApplications(Long candidateUserId);
}
