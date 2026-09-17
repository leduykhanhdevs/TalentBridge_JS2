package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CandidateResult;

public interface GetCandidateProfileUseCase {
    CandidateResult getProfile(Long userId);
}
