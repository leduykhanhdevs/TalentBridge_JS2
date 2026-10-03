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
import vn.talentbridge.core.domain.vo.ApplicationPipelineStage;

import java.time.LocalDateTime;

public class UpdateApplicantStatusUseCaseImpl implements UpdateApplicantStatusUseCase {
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
    public JobApplicantResult updateStageAndStatus(Long recruiterUserId, Long jobId, Long applicationId,
                                                   UpdateApplicantStatusCommand command) {
        if (command == null || command.getStage() == null || command.getStage().isBlank()) {
            throw new DomainException(40001, "Trạng thái vòng tuyển dụng không được để trống");
        }
        ApplicationPipelineStage target = parseStage(command.getStage());
        JobApplicant applicant = findAuthorizedApplicant(recruiterUserId, jobId, applicationId);
        if ("WITHDRAWN".equalsIgnoreCase(applicant.getStatus())) {
            throw new DomainException(40001, "Đơn ứng tuyển đã được ứng viên rút và không thể cập nhật");
        }

        ApplicationPipelineStage current = parseStage(applicant.getCurrentStage());
        if (!current.canAdvanceTo(target)) {
            throw new DomainException(40001, "Chỉ được chuyển tiếp vòng tuyển dụng; dùng chức năng mở lại cho hồ sơ đã kết thúc");
        }

        jobApplicationRepository.updateStageAndStatus(applicationId, target.name(), target.applicationStatus());
        saveStageHistory(applicationId, target, command.getNote(), recruiterUserId);
        JobApplicant updated = jobApplicationRepository.findApplicantById(applicationId).orElse(applicant);
        return JobApplicantResult.from(updated);
    }

    @Override
    public JobApplicantResult reopenApplication(Long recruiterUserId, Long jobId, Long applicationId, String reason) {
        if (reason == null || reason.isBlank()) {
            throw new DomainException(40001, "Vui lòng nhập lý do mở lại hồ sơ");
        }
        JobApplicant applicant = findAuthorizedApplicant(recruiterUserId, jobId, applicationId);
        if ("WITHDRAWN".equalsIgnoreCase(applicant.getStatus())) {
            throw new DomainException(40001, "Không thể mở lại đơn ứng tuyển mà ứng viên đã rút");
        }
        ApplicationPipelineStage current = parseStage(applicant.getCurrentStage());
        if (current != ApplicationPipelineStage.REJECTED && current != ApplicationPipelineStage.HIRED) {
            throw new DomainException(40001, "Chỉ có thể mở lại hồ sơ đã bị từ chối hoặc đã tuyển");
        }

        jobApplicationRepository.updateStageAndStatus(applicationId,
                ApplicationPipelineStage.REVIEWING.name(), ApplicationPipelineStage.REVIEWING.applicationStatus());
        saveStageHistory(applicationId, ApplicationPipelineStage.REVIEWING,
                "Mở lại hồ sơ: " + reason.trim(), recruiterUserId);
        JobApplicant updated = jobApplicationRepository.findApplicantById(applicationId).orElse(applicant);
        return JobApplicantResult.from(updated);
    }

    private JobApplicant findAuthorizedApplicant(Long recruiterUserId, Long jobId, Long applicationId) {
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));
        if (recruiter.getCompany() == null || recruiter.getCompany().getId() == null) {
            throw new DomainException(40001, "Nhà tuyển dụng chưa thuộc công ty nào");
        }
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng", jobId));
        if (!recruiter.getCompany().getId().equals(job.getCompanyId())) {
            throw new DomainException(40301, "Bạn không có quyền cập nhật ứng viên của công ty khác");
        }
        JobApplicant applicant = jobApplicationRepository.findApplicantById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn ứng tuyển", applicationId));
        if (!jobId.equals(applicant.getJobId())) {
            throw new DomainException(40001, "Đơn ứng tuyển không thuộc tin tuyển dụng này");
        }
        return applicant;
    }

    private ApplicationPipelineStage parseStage(String value) {
        try {
            return ApplicationPipelineStage.from(value);
        } catch (IllegalArgumentException exception) {
            throw new DomainException(40001, exception.getMessage());
        }
    }

    private void saveStageHistory(Long applicationId, ApplicationPipelineStage target, String note, Long recruiterUserId) {
        applicationStageRepository.save(new ApplicationStage(null, applicationId, target.name(), note,
                recruiterUserId, null, LocalDateTime.now()));
    }
}
