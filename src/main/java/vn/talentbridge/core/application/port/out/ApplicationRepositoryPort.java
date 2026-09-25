package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.domain.model.Application;

public interface ApplicationRepositoryPort {
    boolean existsByJobIdAndCandidateId(Long jobId, Long candidateId);
    Application save(Application application);
}
