package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.CreateJobCommand;
import vn.talentbridge.core.application.dto.JobDetailResult;
import vn.talentbridge.core.application.dto.UpdateJobCommand;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.JobUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.CompanyStatus;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobUseCaseImplTest {

    @Mock
    private JobRepositoryPort jobRepository;

    @Mock
    private RecruiterRepositoryPort recruiterRepository;

    private JobUseCaseImpl jobUseCase;

    private Recruiter validRecruiter;
    private Company approvedCompany;

    @BeforeEach
    void setUp() {
        jobUseCase = new JobUseCaseImpl(jobRepository, recruiterRepository);

        approvedCompany = new Company();
        approvedCompany.setId(10L);
        approvedCompany.setName("Tech Corp");
        approvedCompany.setStatus(CompanyStatus.APPROVED);

        User user = new User();
        user.setId(100L);
        user.setEmail("hr@techcorp.com");
        user.setFullName("HR Manager");

        validRecruiter = new Recruiter();
        validRecruiter.setId(1L);
        validRecruiter.setUser(user);
        validRecruiter.setCompany(approvedCompany);
    }

    @Test
    @DisplayName("HR tạo tin tuyển dụng thành công khi thuộc công ty đã duyệt")
    void createJob_Success() {
        when(recruiterRepository.findByUserId(100L)).thenReturn(Optional.of(validRecruiter));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> {
            Job job = invocation.getArgument(0);
            job.setId(1L);
            return job;
        });

        CreateJobCommand command = new CreateJobCommand(
                "Java Backend Engineer",
                "Phát triển hệ thống microservices",
                "3+ năm kinh nghiệm Java",
                "Lương thưởng hấp dẫn",
                "Tại văn phòng",
                "Hồ Chí Minh",
                "Quận 1",
                "FULL_TIME",
                "SENIOR",
                new BigDecimal("20000000"),
                new BigDecimal("40000000"),
                false,
                LocalDate.now().plusMonths(1),
                List.of("Java", "Spring Boot", "MySQL")
        );

        JobDetailResult result = jobUseCase.createJob(100L, command);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals("Java Backend Engineer", result.title());
        assertEquals("Hồ Chí Minh", result.city());
        assertEquals(10L, result.companyId());
        assertEquals("Tech Corp", result.companyName());
        assertEquals(3, result.skills().size());
        assertEquals(JobStatus.ACTIVE.name(), result.status());

        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    @DisplayName("Ném lỗi DomainException 40301 khi HR chưa gắn với công ty")
    void createJob_ThrowsException_WhenNoCompany() {
        validRecruiter.setCompany(null);
        when(recruiterRepository.findByUserId(100L)).thenReturn(Optional.of(validRecruiter));

        CreateJobCommand command = new CreateJobCommand(
                "Java Dev", "Mô tả", null, null, null, "Hà Nội", null,
                "FULL_TIME", "JUNIOR", null, null, true,
                LocalDate.now().plusDays(10), List.of()
        );

        DomainException ex = assertThrows(DomainException.class, () -> jobUseCase.createJob(100L, command));
        assertEquals(40301, ex.getCode());
        assertTrue(ex.getMessage().contains("phải thuộc về một doanh nghiệp"));
    }

    @Test
    @DisplayName("Ném lỗi DomainException 40301 khi công ty của HR chưa được APPROVED")
    void createJob_ThrowsException_WhenCompanyNotApproved() {
        approvedCompany.setStatus(CompanyStatus.PENDING);
        when(recruiterRepository.findByUserId(100L)).thenReturn(Optional.of(validRecruiter));

        CreateJobCommand command = new CreateJobCommand(
                "Java Dev", "Mô tả", null, null, null, "Hà Nội", null,
                "FULL_TIME", "JUNIOR", null, null, true,
                LocalDate.now().plusDays(10), List.of()
        );

        DomainException ex = assertThrows(DomainException.class, () -> jobUseCase.createJob(100L, command));
        assertEquals(40301, ex.getCode());
        assertTrue(ex.getMessage().contains("chưa được phê duyệt"));
    }

    @Test
    @DisplayName("Ném lỗi DomainException 40001 khi hạn nộp trong quá khứ")
    void createJob_ThrowsException_WhenDeadlineInPast() {
        when(recruiterRepository.findByUserId(100L)).thenReturn(Optional.of(validRecruiter));

        CreateJobCommand command = new CreateJobCommand(
                "Java Dev", "Mô tả", null, null, null, "Hà Nội", null,
                "FULL_TIME", "JUNIOR", null, null, true,
                LocalDate.now().minusDays(1), List.of()
        );

        DomainException ex = assertThrows(DomainException.class, () -> jobUseCase.createJob(100L, command));
        assertEquals(40001, ex.getCode());
        assertTrue(ex.getMessage().contains("sau ngày hiện tại"));
    }

    @Test
    @DisplayName("Ném lỗi DomainException 40001 khi minSalary > maxSalary")
    void createJob_ThrowsException_WhenMinSalaryGreaterThanMaxSalary() {
        when(recruiterRepository.findByUserId(100L)).thenReturn(Optional.of(validRecruiter));

        CreateJobCommand command = new CreateJobCommand(
                "Java Dev", "Mô tả", null, null, null, "Hà Nội", null,
                "FULL_TIME", "JUNIOR",
                new BigDecimal("50000000"),
                new BigDecimal("30000000"),
                false,
                LocalDate.now().plusDays(15), List.of()
        );

        DomainException ex = assertThrows(DomainException.class, () -> jobUseCase.createJob(100L, command));
        assertEquals(40001, ex.getCode());
        assertTrue(ex.getMessage().contains("Mức lương tối thiểu không được lớn hơn"));
    }

    @Test
    @DisplayName("HR cập nhật tin tuyển dụng thành công")
    void updateJob_Success() {
        Job existingJob = new Job();
        existingJob.setId(5L);
        existingJob.setCompanyId(10L);
        existingJob.setTitle("Old Title");
        existingJob.setStatus(JobStatus.ACTIVE);

        when(jobRepository.findById(5L)).thenReturn(Optional.of(existingJob));
        when(recruiterRepository.findByUserId(100L)).thenReturn(Optional.of(validRecruiter));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateJobCommand updateCommand = new UpdateJobCommand(
                "New Title", "New Desc", "New Req", "New Benefits",
                "Remote", "Đà Nẵng", "Hải Châu", "PART_TIME", "MIDDLE",
                new BigDecimal("15000000"), new BigDecimal("25000000"),
                false, LocalDate.now().plusMonths(2), List.of("React", "TypeScript")
        );

        JobDetailResult result = jobUseCase.updateJob(100L, 5L, updateCommand);

        assertNotNull(result);
        assertEquals("New Title", result.title());
        assertEquals("Đà Nẵng", result.city());
        assertEquals("Remote", result.location());
        assertEquals(2, result.skills().size());
    }

    @Test
    @DisplayName("HR cập nhật tin tuyển dụng của công ty khác bị từ chối 40301")
    void updateJob_ThrowsException_WhenBelongsToAnotherCompany() {
        Job existingJob = new Job();
        existingJob.setId(5L);
        existingJob.setCompanyId(999L); // Different company ID

        when(jobRepository.findById(5L)).thenReturn(Optional.of(existingJob));
        when(recruiterRepository.findByUserId(100L)).thenReturn(Optional.of(validRecruiter));

        UpdateJobCommand updateCommand = new UpdateJobCommand(
                "Hacked Title", "Desc", null, null, null, "HN", null,
                "FULL_TIME", "SENIOR", null, null, true,
                LocalDate.now().plusMonths(1), List.of()
        );

        DomainException ex = assertThrows(DomainException.class, () -> jobUseCase.updateJob(100L, 5L, updateCommand));
        assertEquals(40301, ex.getCode());
        assertTrue(ex.getMessage().contains("không có quyền chỉnh sửa"));
    }

    @Test
    @DisplayName("HR đóng tin tuyển dụng thành công chuyển sang CLOSED")
    void closeJob_Success() {
        Job existingJob = new Job();
        existingJob.setId(5L);
        existingJob.setCompanyId(10L);
        existingJob.setStatus(JobStatus.ACTIVE);

        when(jobRepository.findById(5L)).thenReturn(Optional.of(existingJob));
        when(recruiterRepository.findByUserId(100L)).thenReturn(Optional.of(validRecruiter));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        JobDetailResult result = jobUseCase.closeJob(100L, 5L);

        assertNotNull(result);
        assertEquals(JobStatus.CLOSED.name(), result.status());
    }

    @Test
    @DisplayName("Xem chi tiết việc làm theo ID thành công")
    void getJobById_Success() {
        Job job = new Job();
        job.setId(12L);
        job.setTitle("Frontend Architect");
        job.setStatus(JobStatus.ACTIVE);

        when(jobRepository.findById(12L)).thenReturn(Optional.of(job));

        JobDetailResult result = jobUseCase.getJobById(12L);
        assertEquals(12L, result.id());
        assertEquals("Frontend Architect", result.title());
    }

    @Test
    @DisplayName("Ném ResourceNotFoundException khi không tìm thấy tin tuyển dụng")
    void getJobById_NotFound() {
        when(jobRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> jobUseCase.getJobById(999L));
    }
}
