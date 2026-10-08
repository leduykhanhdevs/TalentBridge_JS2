package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CandidateJobMatchResult;
import vn.talentbridge.core.application.port.in.MatchApplicantUseCase;
import vn.talentbridge.core.application.port.out.CandidateJobMatchingPort;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.Recruiter;

public class MatchApplicantUseCaseImpl implements MatchApplicantUseCase {

    private final RecruiterRepositoryPort recruiterRepository;
    private final JobRepositoryPort jobRepository;
    private final JobApplicationRepositoryPort applicationRepository;
    private final CandidateJobMatchingPort matchingPort;

    public MatchApplicantUseCaseImpl(
            RecruiterRepositoryPort recruiterRepository,
            JobRepositoryPort jobRepository,
            JobApplicationRepositoryPort applicationRepository,
            CandidateJobMatchingPort matchingPort
    ) {
        this.recruiterRepository = recruiterRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.matchingPort = matchingPort;
    }

    @Override
    public CandidateJobMatchResult matchApplicant(Long recruiterUserId, Long jobId, Long candidateId) {
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));
        if (recruiter.getCompany() == null || recruiter.getCompany().getId() == null) {
            throw new DomainException(40001, "Nhà tuyển dụng chưa thuộc công ty nào");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng", jobId));
        if (!recruiter.getCompany().getId().equals(job.getCompanyId())) {
            throw new DomainException(40301, "Bạn không có quyền đánh giá ứng viên của công ty khác");
        }

        applicationRepository.findApplicantByJobIdAndCandidateId(jobId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn ứng tuyển của ứng viên", candidateId));

        return matchingPort.match(jobId, candidateId);
    }
}
