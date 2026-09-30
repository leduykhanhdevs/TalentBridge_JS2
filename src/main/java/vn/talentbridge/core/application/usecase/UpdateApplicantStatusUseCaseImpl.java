package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.JobApplicantResult;
import vn.talentbridge.core.application.dto.UpdateApplicantStatusCommand;
import vn.talentbridge.core.application.port.in.UpdateApplicantStatusUseCase;
import vn.talentbridge.core.application.port.out.ApplicationStageRepositoryPort;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.ApplicationStage;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.JobApplicant;
import vn.talentbridge.core.domain.model.Recruiter;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Set;

public class UpdateApplicantStatusUseCaseImpl implements UpdateApplicantStatusUseCase {

    private static final Set<String> VALID_STAGES = Set.of(
            "APPLIED", "REVIEWING", "SHORTLISTED", "INTERVIEW", "OFFERED", "HIRED", "REJECTED"
    );

    private final RecruiterRepositoryPort recruiterRepository;
    private final JobRepositoryPort jobRepository;
    private final JobApplicationRepositoryPort jobApplicationRepository;
    private final ApplicationStageRepositoryPort applicationStageRepository;

    public UpdateApplicantStatusUseCaseImpl(RecruiterRepositoryPort recruiterRepository,
                                            JobRepositoryPort jobRepository,
                                            JobApplicationRepositoryPort jobApplicationRepository,
                                            ApplicationStageRepositoryPort applicationStageRepository) {
        this.recruiterRepository = recruiterRepository;
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.applicationStageRepository = applicationStageRepository;
    }

    @Override
    public JobApplicantResult updateStageAndStatus(Long recruiterUserId, Long jobId, Long applicationId, UpdateApplicantStatusCommand command) {
        if (command == null || command.getStage() == null || command.getStage().isBlank()) {
            throw new DomainException(40001, "Trạng thái vòng tuyển dụng không được để trống");
        }

        String normalizedStage = command.getStage().trim().toUpperCase(Locale.ROOT);
        if (!VALID_STAGES.contains(normalizedStage)) {
            throw new DomainException(40001, "Trạng thái vòng tuyển dụng '" + command.getStage() + "' không hợp lệ");
        }

        // 1. Xác định recruiter hiện tại
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));

        if (recruiter.getCompany() == null || recruiter.getCompany().getId() == null) {
            throw new DomainException(40001, "Nhà tuyển dụng chưa thuộc công ty nào");
        }

        // 2. Kiểm tra Job tồn tại và thuộc company của recruiter
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng", jobId));

        Long recruiterCompanyId = recruiter.getCompany().getId();
        if (!recruiterCompanyId.equals(job.getCompanyId())) {
            throw new DomainException(40301, "Bạn không có quyền cập nhật trạng thái ứng viên của tin tuyển dụng thuộc công ty khác");
        }

        // 3. Kiểm tra Application tồn tại và thuộc Job
        JobApplicant applicant = jobApplicationRepository.findApplicantById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn ứng tuyển", applicationId));

        if (!jobId.equals(applicant.getJobId())) {
            throw new DomainException(40001, "Đơn ứng tuyển không thuộc tin tuyển dụng này");
        }

        // 4. Quyết định status
        String targetStatus = command.getStatus();
        if (targetStatus == null || targetStatus.isBlank()) {
            if ("REJECTED".equals(normalizedStage)) {
                targetStatus = "REJECTED";
            } else if ("HIRED".equals(normalizedStage)) {
                targetStatus = "ACCEPTED";
            } else {
                targetStatus = applicant.getStatus() != null ? applicant.getStatus() : "ACTIVE";
            }
        } else {
            targetStatus = targetStatus.trim().toUpperCase(Locale.ROOT);
        }

        // 5. Cập nhật Application
        jobApplicationRepository.updateStageAndStatus(applicationId, normalizedStage, targetStatus);

        // 6. Ghi log chuyển vòng vào application_stages
        ApplicationStage stageHistory = new ApplicationStage(
                null,
                applicationId,
                normalizedStage,
                command.getNote(),
                recruiterUserId,
                null,
                LocalDateTime.now()
        );
        applicationStageRepository.save(stageHistory);

        // 7. Lấy lại applicant sau khi cập nhật
        JobApplicant updatedApplicant = jobApplicationRepository.findApplicantById(applicationId)
                .orElse(applicant);

        return JobApplicantResult.from(updatedApplicant);
    }
}
