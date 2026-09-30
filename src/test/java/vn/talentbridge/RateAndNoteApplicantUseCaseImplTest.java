package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.ApplicationNoteResult;
import vn.talentbridge.core.application.dto.ApplicationStageResult;
import vn.talentbridge.core.application.dto.RateAndNoteApplicantCommand;
import vn.talentbridge.core.application.port.out.ApplicationNoteRepositoryPort;
import vn.talentbridge.core.application.port.out.ApplicationStageRepositoryPort;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.RateAndNoteApplicantUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RateAndNoteApplicantUseCaseImplTest {

    @Mock
    private RecruiterRepositoryPort recruiterRepository;

    @Mock
    private JobRepositoryPort jobRepository;

    @Mock
    private JobApplicationRepositoryPort jobApplicationRepository;

    @Mock
    private ApplicationNoteRepositoryPort applicationNoteRepository;

    @Mock
    private ApplicationStageRepositoryPort applicationStageRepository;

    private RateAndNoteApplicantUseCaseImpl useCase;

    private static final Long RECRUITER_USER_ID = 100L;
    private static final Long RECRUITER_ID = 5L;
    private static final Long COMPANY_ID = 10L;
    private static final Long JOB_ID = 50L;
    private static final Long APP_ID = 99L;

    @BeforeEach
    void setUp() {
        useCase = new RateAndNoteApplicantUseCaseImpl(
                recruiterRepository,
                jobRepository,
                jobApplicationRepository,
                applicationNoteRepository,
                applicationStageRepository
        );
    }

    @Test
    @DisplayName("HRPM-49: Ném DomainException khi comment để trống")
    void shouldThrowExceptionWhenCommentIsBlank() {
        RateAndNoteApplicantCommand command = new RateAndNoteApplicantCommand(5, "Java", "   ");

        assertThatThrownBy(() -> useCase.addNote(RECRUITER_USER_ID, JOB_ID, APP_ID, command))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Nội dung ghi chú không được để trống");
    }

    @Test
    @DisplayName("HRPM-49: Ném DomainException khi rating ngoài khoảng 1-5")
    void shouldThrowExceptionWhenRatingOutOfRange() {
        RateAndNoteApplicantCommand commandTooHigh = new RateAndNoteApplicantCommand(6, "Java", "Phù hợp");
        RateAndNoteApplicantCommand commandTooLow = new RateAndNoteApplicantCommand(0, "Java", "Phù hợp");

        assertThatThrownBy(() -> useCase.addNote(RECRUITER_USER_ID, JOB_ID, APP_ID, commandTooHigh))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Điểm đánh giá phải từ 1 đến 5 sao");

        assertThatThrownBy(() -> useCase.addNote(RECRUITER_USER_ID, JOB_ID, APP_ID, commandTooLow))
                .isInstanceOf(DomainException.class)
                .hasMessageContaining("Điểm đánh giá phải từ 1 đến 5 sao");
    }

    @Test
    @DisplayName("HRPM-49: Thêm đánh giá 5 sao và ghi chú nội bộ thành công")
    void shouldAddNoteSuccessfully() {
        Recruiter recruiter = createRecruiter();
        Job job = createJob();
        JobApplicant applicant = createApplicant();

        when(recruiterRepository.findByUserId(RECRUITER_USER_ID)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(jobApplicationRepository.findApplicantById(APP_ID)).thenReturn(Optional.of(applicant));

        ApplicationNote savedNote = new ApplicationNote(
                1L,
                APP_ID,
                RECRUITER_ID,
                "Trần Đình Tình",
                5,
                "Senior",
                "Ứng viên xuất sắc",
                LocalDateTime.now()
        );
        when(applicationNoteRepository.save(any(ApplicationNote.class))).thenReturn(savedNote);

        RateAndNoteApplicantCommand command = new RateAndNoteApplicantCommand(5, "Senior", "Ứng viên xuất sắc");
        ApplicationNoteResult result = useCase.addNote(RECRUITER_USER_ID, JOB_ID, APP_ID, command);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getTag()).isEqualTo("Senior");
        assertThat(result.getComment()).isEqualTo("Ứng viên xuất sắc");

        ArgumentCaptor<ApplicationNote> noteCaptor = ArgumentCaptor.forClass(ApplicationNote.class);
        verify(applicationNoteRepository).save(noteCaptor.capture());
        ApplicationNote captured = noteCaptor.getValue();
        assertThat(captured.getApplicationId()).isEqualTo(APP_ID);
        assertThat(captured.getRecruiterId()).isEqualTo(RECRUITER_ID);
        assertThat(captured.getRating()).isEqualTo(5);
        assertThat(captured.getTag()).isEqualTo("Senior");
        assertThat(captured.getComment()).isEqualTo("Ứng viên xuất sắc");
    }

    @Test
    @DisplayName("HRPM-49: Lấy danh sách ghi chú và lịch sử stages thành công")
    void shouldGetNotesAndStageHistorySuccessfully() {
        Recruiter recruiter = createRecruiter();
        Job job = createJob();
        JobApplicant applicant = createApplicant();

        when(recruiterRepository.findByUserId(RECRUITER_USER_ID)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));
        when(jobApplicationRepository.findApplicantById(APP_ID)).thenReturn(Optional.of(applicant));

        ApplicationNote note = new ApplicationNote(1L, APP_ID, RECRUITER_ID, "HR User", 4, "Good", "Note 1", LocalDateTime.now());
        when(applicationNoteRepository.findByApplicationId(APP_ID)).thenReturn(List.of(note));

        ApplicationStage stage = new ApplicationStage(1L, APP_ID, "REVIEWING", "Checking resume", 100L, "HR User", LocalDateTime.now());
        when(applicationStageRepository.findByApplicationId(APP_ID)).thenReturn(List.of(stage));

        List<ApplicationNoteResult> notes = useCase.getNotes(RECRUITER_USER_ID, JOB_ID, APP_ID);
        assertThat(notes).hasSize(1);
        assertThat(notes.get(0).getComment()).isEqualTo("Note 1");

        List<ApplicationStageResult> stages = useCase.getStageHistory(RECRUITER_USER_ID, JOB_ID, APP_ID);
        assertThat(stages).hasSize(1);
        assertThat(stages.get(0).getStage()).isEqualTo("REVIEWING");
    }

    private Recruiter createRecruiter() {
        Company company = new Company();
        company.setId(COMPANY_ID);

        User user = new User();
        user.setId(RECRUITER_USER_ID);
        user.setFullName("Trần Đình Tình");

        Recruiter recruiter = new Recruiter();
        recruiter.setId(RECRUITER_ID);
        recruiter.setUser(user);
        recruiter.setCompany(company);
        return recruiter;
    }

    private Job createJob() {
        Job job = new Job();
        job.setId(JOB_ID);
        job.setCompanyId(COMPANY_ID);
        return job;
    }

    private JobApplicant createApplicant() {
        JobApplicant applicant = new JobApplicant();
        applicant.setId(APP_ID);
        applicant.setJobId(JOB_ID);
        return applicant;
    }
}
