package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.JobApplicantResult;
import vn.talentbridge.core.application.port.in.GetJobApplicantsUseCase;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.Recruiter;

import java.util.List;

public class GetJobApplicantsUseCaseImpl implements GetJobApplicantsUseCase {

    private final RecruiterRepositoryPort recruiterRepository;
    private final JobRepositoryPort jobRepository;
    private final JobApplicationRepositoryPort jobApplicationRepository;

    public GetJobApplicantsUseCaseImpl(RecruiterRepositoryPort recruiterRepository,
                                      JobRepositoryPort jobRepository,
                                      JobApplicationRepositoryPort jobApplicationRepository) {
        this.recruiterRepository = recruiterRepository;
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    @Override
    public List<JobApplicantResult> getJobApplicants(Long recruiterUserId, Long jobId) {
        // 1. Xác định recruiter hiện tại
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));

        // 2. Xác định company của recruiter
        if (recruiter.getCompany() == null || recruiter.getCompany().getId() == null) {
            throw new DomainException(40001, "Nhà tuyển dụng chưa thuộc công ty nào");
        }

        // 3. Kiểm tra Job tồn tại
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng", jobId));

        // 4. Kiểm tra Job thuộc company của recruiter
        Long recruiterCompanyId = recruiter.getCompany().getId();
        if (!recruiterCompanyId.equals(job.getCompanyId())) {
            throw new DomainException(40301, "Bạn không có quyền xem danh sách ứng viên của tin tuyển dụng thuộc công ty khác");
        }

        // 5. Lấy applications của Job & 6. Map sang applicant DTO an toàn
        return jobApplicationRepository.findByJobId(jobId).stream()
                .map(JobApplicantResult::from)
                .toList();
    }
}
