package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.WorkExperienceCommand;
import vn.talentbridge.core.application.dto.WorkExperienceResult;

import java.util.List;

public interface WorkExperienceUseCase {
    List<WorkExperienceResult> getWorkExperiences(Long userId);
    WorkExperienceResult addWorkExperience(Long userId, WorkExperienceCommand command);
    WorkExperienceResult updateWorkExperience(Long userId, Long experienceId, WorkExperienceCommand command);
    void deleteWorkExperience(Long userId, Long experienceId);
}
