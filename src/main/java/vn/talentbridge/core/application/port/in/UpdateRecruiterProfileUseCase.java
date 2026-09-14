package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.RecruiterResult;
import vn.talentbridge.core.application.dto.UpdateRecruiterProfileCommand;

public interface UpdateRecruiterProfileUseCase {

    RecruiterResult updateProfile(
            Long userId,
            UpdateRecruiterProfileCommand command);
}