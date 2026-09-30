package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.ApplicantFilterCriteria;
import vn.talentbridge.core.application.dto.JobApplicantResult;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.GetJobApplicantsUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.JobApplicant;
import vn.talentbridge.core.domain.model.Recruiter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetJobApplicantsUseCaseImplTest {

    @Mock
    private RecruiterRepositoryPort recruiterRepository;

    @Mock
    private JobRepositoryPort jobRepository;

    @Mock
    private JobApplicationRepositoryPort jobApplicationRepository;

    private GetJobApplicantsUseCaseImpl useCase;

    private static final Long RECRUITER_USER_ID = 100L;
    private static final Long COMPANY_ID = 10L;
    private static final Long OTHER_COMPANY_ID = 20L;
    private static final Long JOB_ID = 50L;

    @BeforeEach
    void setUp() {
        useCase = new GetJobApplicantsUseCaseImpl(
                recruiterRepository,
                jobRepository,
                jobApplicationRepository
        );
    }

    @Test
    @DisplayName("HRPM-45: Ném ResourceNotFoundException khi hồ sơ Recruiter không tồn tại")
    void shouldThrowExceptionWhenRecruiterNotFound() {
        when(recruiterRepository.findByUserId(RECRUITER_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getJobApplicants(RECRUITER_USER_ID, JOB_ID, new ApplicantFilterCriteria()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Không tìm thấy hồ sơ nhà tuyển dụng");
    }

    @Test
    @DisplayName("HRPM-45: Ném DomainException khi Recruiter chưa gắn với công ty nào")
    void shouldThrowExceptionWhenRecruiterHasNoCompany() {
        Recruiter recruiterWithoutCompany = new Recruiter();
        recruiterWithoutCompany.setId(1L);
        recruiterWithoutCompany.setCompany(null);

        when(recruiterRepository.findByUserId(RECRUITER_USER_ID)).thenReturn(Optional.of(recruiterWithoutCompany));

        assertThatThrownBy(() -> useCase.getJobApplicants(RECRUITER_USER_ID, JOB_ID, new ApplicantFilterCriteria()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Nhà tuyển dụng chưa thuộc công ty nào");
    }

    @Test
    @DisplayName("HRPM-45: Ném ResourceNotFoundException khi Job không tồn tại")
    void shouldThrowExceptionWhenJobNotFound() {
        Recruiter recruiter = createRecruiter(COMPANY_ID);
        when(recruiterRepository.findByUserId(RECRUITER_USER_ID)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(JOB_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.getJobApplicants(RECRUITER_USER_ID, JOB_ID, new ApplicantFilterCriteria()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tin tuyển dụng");
    }

    @Test
    @DisplayName("HRPM-45: Ném DomainException 40301 khi Job thuộc công ty khác")
    void shouldThrowExceptionWhenJobBelongsToAnotherCompany() {
        Recruiter recruiter = createRecruiter(COMPANY_ID);
        Job jobFromAnotherCompany = createJob(JOB_ID, OTHER_COMPANY_ID);

        when(recruiterRepository.findByUserId(RECRUITER_USER_ID)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(jobFromAnotherCompany));

        assertThatThrownBy(() -> useCase.getJobApplicants(RECRUITER_USER_ID, JOB_ID, new ApplicantFilterCriteria()))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Bạn không có quyền xem danh sách ứng viên");
    }

    @Test
    @DisplayName("HRPM-45, 46, 47: Lấy danh sách ứng viên thành công với bộ lọc và sắp xếp")
    void shouldReturnApplicantsSuccessfullyWithFilterAndSort() {
        Recruiter recruiter = createRecruiter(COMPANY_ID);
        Job job = createJob(JOB_ID, COMPANY_ID);
        ApplicantFilterCriteria criteria = new ApplicantFilterCriteria("Nguyen", "APPLIED", 2, "score", "DESC");

        JobApplicant applicant = new JobApplicant();
        applicant.setId(1L);
        applicant.setJobId(JOB_ID);
        applicant.setCandidateId(200L);
        applicant.setCandidateFullName("Nguyen Van A");
        applicant.setCandidateEmail("vana@example.com");
        applicant.setCurrentStage("APPLIED");
        applicant.setStatus("SUBMITTED");
        applicant.setCandidateExperienceYears(3);
        applicant.setAiMatchScore(new BigDecimal("92.50"));
        applicant.setAppliedAt(LocalDateTime.now().minusDays(1));
        applicant.setAverageRating(4.5);
        applicant.setNotesCount(2);

        when(recruiterRepository.findByUserId(RECRUITER_USER_ID)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(jobApplicationRepository.findApplicants(eq(JOB_ID), any(ApplicantFilterCriteria.class)))
                .thenReturn(List.of(applicant));

        List<JobApplicantResult> results = useCase.getJobApplicants(RECRUITER_USER_ID, JOB_ID, criteria);

        assertThat(results).hasSize(1);
        JobApplicantResult result = results.get(0);
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCandidateFullName()).isEqualTo("Nguyen Van A");
        assertThat(result.getCandidateEmail()).isEqualTo("vana@example.com");
        assertThat(result.getCurrentStage()).isEqualTo("APPLIED");
        assertThat(result.getAiMatchScore()).isEqualTo(new BigDecimal("92.50"));
        assertThat(result.getAverageRating()).isEqualTo(4.5);
        assertThat(result.getNotesCount()).isEqualTo(2);

        verify(jobApplicationRepository).findApplicants(eq(JOB_ID), eq(criteria));
    }

    private Recruiter createRecruiter(Long companyId) {
        Company company = new Company();
        company.setId(companyId);
        company.setName("Công ty Thử nghiệm");

        Recruiter recruiter = new Recruiter();
        recruiter.setId(1L);
        recruiter.setCompany(company);
        return recruiter;
    }

    private Job createJob(Long jobId, Long companyId) {
        Job job = new Job();
        job.setId(jobId);
        job.setCompanyId(companyId);
        job.setTitle("Senior Java Developer");
        return job;
    }
}
