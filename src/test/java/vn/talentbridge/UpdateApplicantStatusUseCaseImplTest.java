package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.JobApplicantResult;
import vn.talentbridge.core.application.dto.UpdateApplicantStatusCommand;
import vn.talentbridge.core.application.port.out.ApplicationStageRepositoryPort;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.UpdateApplicantStatusUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.*;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpdateApplicantStatusUseCaseImplTest {

    @Mock
    private RecruiterRepositoryPort recruiterRepository;

    @Mock
    private JobRepositoryPort jobRepository;

    @Mock
    private JobApplicationRepositoryPort jobApplicationRepository;

    @Mock
    private ApplicationStageRepositoryPort applicationStageRepository;

    private UpdateApplicantStatusUseCaseImpl useCase;

    private static final Long RECRUITER_USER_ID = 100L;
    private static final Long COMPANY_ID = 10L;
    private static final Long JOB_ID = 50L;
    private static final Long APP_ID = 99L;

    @BeforeEach
    void setUp() {
        useCase = new UpdateApplicantStatusUseCaseImpl(
                recruiterRepository,
                jobRepository,
                jobApplicationRepository,
                applicationStageRepository
        );
    }

    @Test
    @DisplayName("HRPM-48: Ném DomainException khi stage để trống")
    void shouldThrowExceptionWhenStageIsBlank() {
        UpdateApplicantStatusCommand command = new UpdateApplicantStatusCommand("", "ACTIVE", "Note");

        assertThatThrownBy(() -> useCase.updateStageAndStatus(RECRUITER_USER_ID, JOB_ID, APP_ID, command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Trạng thái vòng tuyển dụng không được để trống");
    }

    @Test
    @DisplayName("HRPM-48: Ném DomainException khi stage không nằm trong danh sách hợp lệ")
    void shouldThrowExceptionWhenStageIsInvalid() {
        UpdateApplicantStatusCommand command = new UpdateApplicantStatusCommand("UNKNOWN_STAGE", "ACTIVE", "Note");

        assertThatThrownBy(() -> useCase.updateStageAndStatus(RECRUITER_USER_ID, JOB_ID, APP_ID, command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("không hợp lệ");
    }

    @Test
    @DisplayName("HRPM-48: Ném DomainException khi Job thuộc công ty khác")
    void shouldThrowExceptionWhenJobBelongsToAnotherCompany() {
        Recruiter recruiter = createRecruiter(COMPANY_ID);
        Job job = createJob(JOB_ID, 999L); // different company

        when(recruiterRepository.findByUserId(RECRUITER_USER_ID)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));

        UpdateApplicantStatusCommand command = new UpdateApplicantStatusCommand("SHORTLISTED", "ACTIVE", "Note");

        assertThatThrownBy(() -> useCase.updateStageAndStatus(RECRUITER_USER_ID, JOB_ID, APP_ID, command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Bạn không có quyền cập nhật");
    }

    @Test
    @DisplayName("HRPM-48: Ném DomainException khi Application không thuộc JobId yêu cầu")
    void shouldThrowExceptionWhenApplicationDoesNotMatchJob() {
        Recruiter recruiter = createRecruiter(COMPANY_ID);
        Job job = createJob(JOB_ID, COMPANY_ID);

        JobApplicant applicant = new JobApplicant();
        applicant.setId(APP_ID);
        applicant.setJobId(777L); // different job!

        when(recruiterRepository.findByUserId(RECRUITER_USER_ID)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(jobApplicationRepository.findApplicantById(APP_ID)).thenReturn(Optional.of(applicant));

        UpdateApplicantStatusCommand command = new UpdateApplicantStatusCommand("SHORTLISTED", "ACTIVE", "Note");

        assertThatThrownBy(() -> useCase.updateStageAndStatus(RECRUITER_USER_ID, JOB_ID, APP_ID, command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Đơn ứng tuyển không thuộc tin tuyển dụng này");
    }

    @Test
    @DisplayName("HRPM-48: Cập nhật vòng tuyển dụng SHORTLISTED thành công và ghi lịch sử")
    void shouldUpdateStageAndRecordAuditLogSuccessfully() {
        Recruiter recruiter = createRecruiter(COMPANY_ID);
        Job job = createJob(JOB_ID, COMPANY_ID);

        JobApplicant applicant = new JobApplicant();
        applicant.setId(APP_ID);
        applicant.setJobId(JOB_ID);
        applicant.setCurrentStage("APPLIED");
        applicant.setStatus("SUBMITTED");

        JobApplicant updatedApplicant = new JobApplicant();
        updatedApplicant.setId(APP_ID);
        updatedApplicant.setJobId(JOB_ID);
        updatedApplicant.setCurrentStage("SHORTLISTED");
        updatedApplicant.setStatus("ACTIVE");

        when(recruiterRepository.findByUserId(RECRUITER_USER_ID)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(jobApplicationRepository.findApplicantById(APP_ID))
                .thenReturn(Optional.of(applicant))
                .thenReturn(Optional.of(updatedApplicant));

        UpdateApplicantStatusCommand command = new UpdateApplicantStatusCommand("SHORTLISTED", "ACTIVE", "Đã duyệt CV");
        JobApplicantResult result = useCase.updateStageAndStatus(RECRUITER_USER_ID, JOB_ID, APP_ID, command);

        assertThat(result).isNotNull();
        assertThat(result.getCurrentStage()).isEqualTo("SHORTLISTED");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");

        verify(jobApplicationRepository).updateStageAndStatus(APP_ID, "SHORTLISTED", "ACTIVE");

        ArgumentCaptor<ApplicationStage> stageCaptor = ArgumentCaptor.forClass(ApplicationStage.class);
        verify(applicationStageRepository).save(stageCaptor.capture());
        ApplicationStage savedStage = stageCaptor.getValue();
        assertThat(savedStage.getApplicationId()).isEqualTo(APP_ID);
        assertThat(savedStage.getStage()).isEqualTo("SHORTLISTED");
        assertThat(savedStage.getNote()).isEqualTo("Đã duyệt CV");
        assertThat(savedStage.getChangedByUserId()).isEqualTo(RECRUITER_USER_ID);
    }

    @Test
    @DisplayName("HRPM-48: Tự động đổi status thành REJECTED khi stage là REJECTED")
    void shouldAutoSetStatusToRejectedWhenStageIsRejected() {
        Recruiter recruiter = createRecruiter(COMPANY_ID);
        Job job = createJob(JOB_ID, COMPANY_ID);

        JobApplicant applicant = new JobApplicant();
        applicant.setId(APP_ID);
        applicant.setJobId(JOB_ID);
        applicant.setCurrentStage("APPLIED");
        applicant.setStatus("SUBMITTED");

        when(recruiterRepository.findByUserId(RECRUITER_USER_ID)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(jobApplicationRepository.findApplicantById(APP_ID)).thenReturn(Optional.of(applicant));

        UpdateApplicantStatusCommand command = new UpdateApplicantStatusCommand("REJECTED", null, "Không phù hợp kinh nghiệm");
        useCase.updateStageAndStatus(RECRUITER_USER_ID, JOB_ID, APP_ID, command);

        verify(jobApplicationRepository).updateStageAndStatus(APP_ID, "REJECTED", "REJECTED");
    }

    private Recruiter createRecruiter(Long companyId) {
        Company company = new Company();
        company.setId(companyId);

        Recruiter recruiter = new Recruiter();
        recruiter.setId(1L);
        recruiter.setCompany(company);
        return recruiter;
    }

    private Job createJob(Long jobId, Long companyId) {
        Job job = new Job();
        job.setId(jobId);
        job.setCompanyId(companyId);
        return job;
    }
}
