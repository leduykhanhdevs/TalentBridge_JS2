package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.ApplicationResult;

public interface ApplyJobUseCase {
    ApplicationResult apply(Long userId, Long jobId, Long resumeId, String coverLetter);
}