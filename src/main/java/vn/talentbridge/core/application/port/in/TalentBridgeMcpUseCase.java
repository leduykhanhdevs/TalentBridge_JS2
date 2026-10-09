package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CandidateJobMatchResult;
import vn.talentbridge.core.application.dto.JobApplicantResult;
import vn.talentbridge.core.application.dto.McpCandidateProfileResult;
import vn.talentbridge.core.application.dto.McpJobSummary;

import java.util.List;

public interface TalentBridgeMcpUseCase {
    List<McpJobSummary> searchJobs(Long recruiterUserId, String keyword, String city, int limit);

    McpCandidateProfileResult getCandidateProfile(Long recruiterUserId, Long jobId, Long candidateId);

    CandidateJobMatchResult evaluateCandidate(Long recruiterUserId, Long jobId, Long candidateId);

    JobApplicantResult updateApplicationStage(
            Long recruiterUserId,
            Long jobId,
            Long applicationId,
            String stage,
            String note
    );
}
