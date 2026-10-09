package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.PipelineStageStatistic;
import vn.talentbridge.core.application.dto.RecruiterPipelineAnalyticsResult;
import vn.talentbridge.core.application.port.in.GetRecruiterPipelineAnalyticsUseCase;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.vo.ApplicationPipelineStage;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetRecruiterPipelineAnalyticsUseCaseImpl implements GetRecruiterPipelineAnalyticsUseCase {
    private final RecruiterRepositoryPort recruiterRepository;
    private final JobRepositoryPort jobRepository;
    private final JobApplicationRepositoryPort applicationRepository;

    public GetRecruiterPipelineAnalyticsUseCaseImpl(RecruiterRepositoryPort recruiterRepository,
                                                     JobRepositoryPort jobRepository,
                                                     JobApplicationRepositoryPort applicationRepository) {
        this.recruiterRepository = recruiterRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    public RecruiterPipelineAnalyticsResult getForJob(Long recruiterUserId, Long jobId) {
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng", jobId));
        if (recruiter.getCompany() == null || recruiter.getCompany().getId() == null
                || !recruiter.getCompany().getId().equals(job.getCompanyId())) {
            throw new DomainException(40301, "Bạn không có quyền xem thống kê tin tuyển dụng này");
        }

        Map<String, Long> rawCounts = applicationRepository.countApplicationsByStage(jobId);
        Map<String, Long> counts = new LinkedHashMap<>();
        for (ApplicationPipelineStage stage : ApplicationPipelineStage.values()) {
            counts.put(stage.name(), rawCounts.getOrDefault(stage.name(), 0L));
        }
        rawCounts.forEach((stage, count) -> counts.putIfAbsent(stage, count));
        long total = counts.values().stream().mapToLong(Long::longValue).sum();
        List<PipelineStageStatistic> stages = new ArrayList<>();
        counts.forEach((stage, count) -> stages.add(new PipelineStageStatistic(
                stage, count, total == 0 ? 0.0 : Math.round(count * 10_000.0 / total) / 100.0)));

        return new RecruiterPipelineAnalyticsResult(job.getId(), job.getTitle(), total, stages,
                "Tỷ lệ là phân bố hồ sơ theo vòng hiện tại; không biểu thị xác suất tuyển dụng hoặc tỷ lệ chuyển đổi lịch sử.");
    }
}
