package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.JobResult;
import vn.talentbridge.core.application.port.in.GetCompanyJobsUseCase;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Recruiter;

import java.util.Collections;
import java.util.List;

public class GetCompanyJobsUseCaseImpl implements GetCompanyJobsUseCase {

    private final RecruiterRepositoryPort recruiterRepository;
    private final JobRepositoryPort jobRepository;

    public GetCompanyJobsUseCaseImpl(RecruiterRepositoryPort recruiterRepository, JobRepositoryPort jobRepository) {
        this.recruiterRepository = recruiterRepository;
        this.jobRepository = jobRepository;
    }

    @Override
    public List<JobResult> getCompanyJobs(Long recruiterUserId) {
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));

        if (recruiter.getCompany() == null || recruiter.getCompany().getId() == null) {
            return Collections.emptyList();
        }

        return jobRepository.findByCompanyId(recruiter.getCompany().getId()).stream()
                .map(JobResult::from)
                .toList();
    }
}
