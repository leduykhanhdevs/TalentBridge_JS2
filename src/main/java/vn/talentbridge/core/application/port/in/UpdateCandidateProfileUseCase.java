package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.CandidateResult;
import vn.talentbridge.core.application.dto.UpdateCandidateProfileCommand;

public interface UpdateCandidateProfileUseCase {
    CandidateResult updateProfile(Long userId, UpdateCandidateProfileCommand command);
}
