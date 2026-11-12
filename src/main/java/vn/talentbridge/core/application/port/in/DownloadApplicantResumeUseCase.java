package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.ApplicantResumeDownload;

public interface DownloadApplicantResumeUseCase {
    ApplicantResumeDownload downloadForRecruiter(Long recruiterUserId, Long jobId, Long resumeId);
}
