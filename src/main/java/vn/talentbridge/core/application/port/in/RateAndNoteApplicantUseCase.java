package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.ApplicationNoteResult;
import vn.talentbridge.core.application.dto.ApplicationStageResult;
import vn.talentbridge.core.application.dto.RateAndNoteApplicantCommand;

import java.util.List;

public interface RateAndNoteApplicantUseCase {

    ApplicationNoteResult addNote(Long recruiterUserId, Long jobId, Long applicationId, RateAndNoteApplicantCommand command);

    List<ApplicationNoteResult> getNotes(Long recruiterUserId, Long jobId, Long applicationId);

    List<ApplicationStageResult> getStageHistory(Long recruiterUserId, Long jobId, Long applicationId);
}
