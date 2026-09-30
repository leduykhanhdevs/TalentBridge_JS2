package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.ApplicationNoteResult;
import vn.talentbridge.core.application.dto.ApplicationStageResult;
import vn.talentbridge.core.application.dto.RateAndNoteApplicantCommand;
import vn.talentbridge.core.application.port.in.RateAndNoteApplicantUseCase;
import vn.talentbridge.core.application.port.out.ApplicationNoteRepositoryPort;
import vn.talentbridge.core.application.port.out.ApplicationStageRepositoryPort;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.ApplicationNote;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.JobApplicant;
import vn.talentbridge.core.domain.model.Recruiter;

import java.time.LocalDateTime;
import java.util.List;

public class RateAndNoteApplicantUseCaseImpl implements RateAndNoteApplicantUseCase {

    private final RecruiterRepositoryPort recruiterRepository;
    private final JobRepositoryPort jobRepository;
    private final JobApplicationRepositoryPort jobApplicationRepository;
    private final ApplicationNoteRepositoryPort applicationNoteRepository;
    private final ApplicationStageRepositoryPort applicationStageRepository;

    public RateAndNoteApplicantUseCaseImpl(RecruiterRepositoryPort recruiterRepository,
                                           JobRepositoryPort jobRepository,
                                           JobApplicationRepositoryPort jobApplicationRepository,
                                           ApplicationNoteRepositoryPort applicationNoteRepository,
                                           ApplicationStageRepositoryPort applicationStageRepository) {
        this.recruiterRepository = recruiterRepository;
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.applicationNoteRepository = applicationNoteRepository;
        this.applicationStageRepository = applicationStageRepository;
    }

    @Override
    public ApplicationNoteResult addNote(Long recruiterUserId, Long jobId, Long applicationId, RateAndNoteApplicantCommand command) {
        if (command == null || command.getComment() == null || command.getComment().trim().isBlank()) {
            throw new DomainException(40001, "Nội dung ghi chú không được để trống");
        }

        if (command.getRating() != null && (command.getRating() < 1 || command.getRating() > 5)) {
            throw new DomainException(40001, "Điểm đánh giá phải từ 1 đến 5 sao");
        }

        Recruiter recruiter = validateRecruiterAndJobAccess(recruiterUserId, jobId, applicationId);

        ApplicationNote note = new ApplicationNote(
                null,
                applicationId,
                recruiter.getId(),
                recruiter.getUser() != null ? recruiter.getUser().getFullName() : null,
                command.getRating(),
                command.getTag() != null ? command.getTag().trim() : null,
                command.getComment().trim(),
                LocalDateTime.now()
        );

        ApplicationNote saved = applicationNoteRepository.save(note);
        return ApplicationNoteResult.from(saved);
    }

    @Override
    public List<ApplicationNoteResult> getNotes(Long recruiterUserId, Long jobId, Long applicationId) {
        validateRecruiterAndJobAccess(recruiterUserId, jobId, applicationId);
        return applicationNoteRepository.findByApplicationId(applicationId).stream()
                .map(ApplicationNoteResult::from)
                .toList();
    }

    @Override
    public List<ApplicationStageResult> getStageHistory(Long recruiterUserId, Long jobId, Long applicationId) {
        validateRecruiterAndJobAccess(recruiterUserId, jobId, applicationId);
        return applicationStageRepository.findByApplicationId(applicationId).stream()
                .map(ApplicationStageResult::from)
                .toList();
    }

    private Recruiter validateRecruiterAndJobAccess(Long recruiterUserId, Long jobId, Long applicationId) {
        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));

        if (recruiter.getCompany() == null || recruiter.getCompany().getId() == null) {
            throw new DomainException(40001, "Nhà tuyển dụng chưa thuộc công ty nào");
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng", jobId));

        Long recruiterCompanyId = recruiter.getCompany().getId();
        if (!recruiterCompanyId.equals(job.getCompanyId())) {
            throw new DomainException(40301, "Bạn không có quyền thao tác trên ứng viên của tin tuyển dụng thuộc công ty khác");
        }

        JobApplicant applicant = jobApplicationRepository.findApplicantById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn ứng tuyển", applicationId));

        if (!jobId.equals(applicant.getJobId())) {
            throw new DomainException(40001, "Đơn ứng tuyển không thuộc tin tuyển dụng này");
        }

        return recruiter;
    }
}
