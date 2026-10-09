package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.JobQualityReportResult;
import vn.talentbridge.core.application.port.in.AnalyzeJobQualityUseCase;
import vn.talentbridge.core.application.port.out.CompanyRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.service.JobQualityScorer;

import java.time.LocalDate;

public class AnalyzeJobQualityUseCaseImpl implements AnalyzeJobQualityUseCase {
    private final RecruiterRepositoryPort recruiterRepository;
    private final JobRepositoryPort jobRepository;
    private final CompanyRepositoryPort companyRepository;
    private final JobQualityScorer scorer;

    public AnalyzeJobQualityUseCaseImpl(RecruiterRepositoryPort recruiterRepository,
                                        JobRepositoryPort jobRepository,
                                        CompanyRepositoryPort companyRepository,
                                        JobQualityScorer scorer) {
        this.recruiterRepository = recruiterRepository;
        this.jobRepository = jobRepository;
        this.companyRepository = companyRepository;
        this.scorer = scorer;
    }

    @Override
    public JobQualityReportResult analyze(Long recruiterUserId, Long jobId) {
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng", jobId));
        if (recruiter.getCompany() == null || recruiter.getCompany().getId() == null
                || !recruiter.getCompany().getId().equals(job.getCompanyId())) {
            throw new DomainException(40301, "Bạn không có quyền phân tích tin tuyển dụng này");
        }
        Company company = job.getCompanyId() == null
                ? null
                : companyRepository.findById(job.getCompanyId()).orElse(null);
        return JobQualityReportResult.from(job.getId(), job.getTitle(),
                job.getStatus() == null ? null : job.getStatus().name(),
                scorer.assess(job, company, LocalDate.now()));
    }
}
