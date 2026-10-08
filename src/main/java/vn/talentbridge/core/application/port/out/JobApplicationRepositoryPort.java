package vn.talentbridge.core.application.port.out;

import vn.talentbridge.core.application.dto.ApplicantFilterCriteria;
import vn.talentbridge.core.domain.model.JobApplicant;

import java.util.List;
import java.util.Optional;

public interface JobApplicationRepositoryPort {

    List<JobApplicant> findApplicants(Long jobId, ApplicantFilterCriteria criteria);

    Optional<JobApplicant> findApplicantById(Long applicationId);

    Optional<JobApplicant> findApplicantByJobIdAndCandidateId(Long jobId, Long candidateId);

    void updateStageAndStatus(Long applicationId, String stage, String status);
}
