package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.RecruiterResult;

public interface GetRecruiterProfileUseCase {
    RecruiterResult getProfile(Long userId);
}
