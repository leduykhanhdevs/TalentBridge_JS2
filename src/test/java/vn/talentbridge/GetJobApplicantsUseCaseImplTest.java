package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.JobApplicantResult;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.GetJobApplicantsUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.JobApplication;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetJobApplicantsUseCaseImplTest {

    @Mock
    private RecruiterRepositoryPort recruiterRepository;

    @Mock
    private JobRepositoryPort jobRepository;

    @Mock
    private JobApplicationRepositoryPort jobApplicationRepository;

    private GetJobApplicantsUseCaseImpl useCase;

    private final Long recruiterUserId = 1L;
    private final Long companyId = 10L;
    private final Long jobId = 100L;
    private Recruiter recruiter;
    private Job job;

    @BeforeEach
    void setUp() {
        useCase = new GetJobApplicantsUseCaseImpl(recruiterRepository, jobRepository, jobApplicationRepository);

        Company company = new Company();
        company.setId(companyId);
        company.setName("FPT Software");

        User user = new User();
        user.setId(recruiterUserId);
        user.setEmail("recruiter@fpt.com");

        recruiter = new Recruiter();
        recruiter.setId(5L);
        recruiter.setUser(user);
        recruiter.setCompany(company);

        job = new Job();
        job.setId(jobId);
        job.setCompanyId(companyId);
        job.setTitle("Senior Java Backend Engineer");
        job.setStatus(JobStatus.ACTIVE);
    }

    @Test
    @DisplayName("HR xem applicants của Job thuộc company mình thành công")
    void shouldReturnApplicants_whenRecruiterBelongsToJobCompany() {
        when(recruiterRepository.findByUserId(recruiterUserId)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        JobApplication app1 = new JobApplication(
                1L, jobId, 101L, "Nguyễn Văn A", "vana@gmail.com", "0901111222",
                "https://avatar.com/a.jpg", "Java Developer", 3, "Hồ Chí Minh",
                "Tôi rất mong muốn ứng tuyển vị trí này", "APPLIED", "SUBMITTED",
                new BigDecimal("85.50"), LocalDateTime.now().minusDays(1), LocalDateTime.now()
        );

        JobApplication app2 = new JobApplication(
                2L, jobId, 102L, "Trần Thị B", "thib@gmail.com", "0903333444",
                null, "Backend Lead", 5, "Hà Nội",
                "CV của tôi phù hợp với yêu cầu", "SCREENING", "SUBMITTED",
                new BigDecimal("92.00"), LocalDateTime.now().minusDays(2), LocalDateTime.now()
        );

        when(jobApplicationRepository.findByJobId(jobId)).thenReturn(List.of(app1, app2));

        List<JobApplicantResult> results = useCase.getJobApplicants(recruiterUserId, jobId);

        assertNotNull(results);
        assertEquals(2, results.size());

        JobApplicantResult res1 = results.get(0);
        assertEquals(1L, res1.getId());
        assertEquals(jobId, res1.getJobId());
        assertEquals(101L, res1.getCandidateId());
        assertEquals("Nguyễn Văn A", res1.getFullName());
        assertEquals("vana@gmail.com", res1.getEmail());
        assertEquals("0901111222", res1.getPhone());
        assertEquals("Java Developer", res1.getTitle());
        assertEquals(3, res1.getYearsOfExperience());
        assertEquals("Hồ Chí Minh", res1.getCity());
        assertEquals("APPLIED", res1.getCurrentStage());
    }

    @Test
    @DisplayName("Ném ResourceNotFoundException (404) khi Job không tồn tại")
    void shouldThrowResourceNotFound_whenJobDoesNotExist() {
        when(recruiterRepository.findByUserId(recruiterUserId)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> useCase.getJobApplicants(recruiterUserId, jobId)
        );

        assertEquals(40401, exception.getCode());
        verify(jobApplicationRepository, never()).findByJobId(any());
    }

    @Test
    @DisplayName("Ném DomainException (403) khi Job thuộc công ty khác")
    void shouldThrowDomainException40301_whenJobBelongsToDifferentCompany() {
        job.setCompanyId(999L); // Different company!
        when(recruiterRepository.findByUserId(recruiterUserId)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.getJobApplicants(recruiterUserId, jobId)
        );

        assertEquals(40301, exception.getCode());
        assertTrue(exception.getMessage().contains("không có quyền"));
        verify(jobApplicationRepository, never()).findByJobId(any());
    }

    @Test
    @DisplayName("Trả về danh sách rỗng (200 + []) khi Job chưa có ai nộp đơn")
    void shouldReturnEmptyList_whenJobHasNoApplicants() {
        when(recruiterRepository.findByUserId(recruiterUserId)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(jobApplicationRepository.findByJobId(jobId)).thenReturn(Collections.emptyList());

        List<JobApplicantResult> results = useCase.getJobApplicants(recruiterUserId, jobId);

        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Ném DomainException (400) khi Recruiter chưa thuộc công ty nào")
    void shouldThrowDomainException40001_whenRecruiterHasNoCompany() {
        recruiter.setCompany(null);
        when(recruiterRepository.findByUserId(recruiterUserId)).thenReturn(Optional.of(recruiter));

        DomainException exception = assertThrows(
                DomainException.class,
                () -> useCase.getJobApplicants(recruiterUserId, jobId)
        );

        assertEquals(40001, exception.getCode());
        verify(jobRepository, never()).findById(any());
    }
}
