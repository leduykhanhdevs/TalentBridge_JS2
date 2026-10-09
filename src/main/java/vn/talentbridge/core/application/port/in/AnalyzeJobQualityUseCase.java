package vn.talentbridge.core.application.port.in;

import vn.talentbridge.core.application.dto.JobQualityReportResult;

public interface AnalyzeJobQualityUseCase {
    JobQualityReportResult analyze(Long recruiterUserId, Long jobId);
}
