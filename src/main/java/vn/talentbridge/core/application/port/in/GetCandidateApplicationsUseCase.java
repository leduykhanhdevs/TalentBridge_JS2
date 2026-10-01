package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CandidateApplicationResult;

import java.util.List;

public interface GetCandidateApplicationsUseCase {
    List<CandidateApplicationResult> getMyApplications(Long candidateUserId);
}
