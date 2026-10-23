package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.port.out.ApplicationStageRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.InterviewRepositoryPort;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.ScheduleInterviewUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Interview;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.JobApplicant;
import vn.talentbridge.core.domain.model.Recruiter;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleInterviewCalendarAuthorizationTest {
    private static final long INTERVIEW_ID = 9L;
    private static final long APPLICATION_ID = 18L;
    private static final long JOB_ID = 27L;
    private static final long COMPANY_ID = 36L;
    private static final long CANDIDATE_ID = 45L;

    @Mock private InterviewRepositoryPort interviewRepository;
    @Mock private JobApplicationRepositoryPort applicationRepository;
    @Mock private JobRepositoryPort jobRepository;
    @Mock private RecruiterRepositoryPort recruiterRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @Mock private ApplicationStageRepositoryPort stageRepository;

    private ScheduleInterviewUseCaseImpl useCase;
    private JobApplicant applicant;
    private Job job;
    private Interview interview;

    @BeforeEach
    void setUp() {
        useCase = new ScheduleInterviewUseCaseImpl(interviewRepository, applicationRepository, jobRepository,
                recruiterRepository, candidateRepository, stageRepository);
        applicant = new JobApplicant();
        applicant.setId(APPLICATION_ID);
        applicant.setJobId(JOB_ID);
        applicant.setCandidateId(CANDIDATE_ID);
        applicant.setCandidateFullName("Ứng viên, A");
        applicant.setJobTitle("Developer; Senior");
        job = new Job();
        job.setId(JOB_ID);
        job.setCompanyId(COMPANY_ID);
        job.setCompanyName("TalentBridge");
        job.setTitle("Developer; Senior");
        interview = new Interview(INTERVIEW_ID, APPLICATION_ID,
                LocalDateTime.of(2026, 10, 30, 14, 0), "OFFLINE",
                "Tầng 2, phòng; A", "Mang CV, bản in", "SCHEDULED", LocalDateTime.now());

        when(interviewRepository.findById(INTERVIEW_ID)).thenReturn(Optional.of(interview));
        when(applicationRepository.findApplicantById(APPLICATION_ID)).thenReturn(Optional.of(applicant));
    }

    @Test
    void candidateCanDownloadOwnCalendarWithVietnamTimeAndEscapedText() {
        Candidate candidate = new Candidate();
        candidate.setId(CANDIDATE_ID);
        when(candidateRepository.findByUserId(100L)).thenReturn(Optional.of(candidate));
        when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));

        String calendar = new String(useCase.exportIcsCalendar(100L, "ROLE_CANDIDATE", INTERVIEW_ID),
                java.nio.charset.StandardCharsets.UTF_8);

        assertThat(calendar).contains("DTSTART:20261030T070000Z")
                .contains("SUMMARY:Phỏng vấn: Developer\\; Senior - TalentBridge")
                .contains("LOCATION:Tầng 2\\, phòng\\; A")
                .contains("DESCRIPTION:");
        String unfoldedCalendar = calendar.replaceAll("\\r?\\n[ \\t]", "");
        assertThat(unfoldedCalendar).contains("\\nỨng viên: Ứng viên\\, A\\nGhi chú: Mang CV\\, bản in");
    }

    @Test
    void candidateCannotDownloadAnotherCandidatesCalendar() {
        Candidate otherCandidate = new Candidate();
        otherCandidate.setId(CANDIDATE_ID + 1);
        when(candidateRepository.findByUserId(101L)).thenReturn(Optional.of(otherCandidate));

        assertThatThrownBy(() -> useCase.exportIcsCalendar(101L, "ROLE_CANDIDATE", INTERVIEW_ID))
                .isInstanceOf(DomainException.class)
                .extracting(error -> ((DomainException) error).getCode()).isEqualTo(40301);
    }

    @Test
    void recruiterCannotDownloadCalendarForAnotherCompany() {
        Company otherCompany = new Company();
        otherCompany.setId(COMPANY_ID + 1);
        Recruiter recruiter = new Recruiter();
        recruiter.setCompany(otherCompany);
        when(recruiterRepository.findByUserId(102L)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));

        assertThatThrownBy(() -> useCase.exportIcsCalendar(102L, "ROLE_RECRUITER", INTERVIEW_ID))
                .isInstanceOf(DomainException.class)
                .extracting(error -> ((DomainException) error).getCode()).isEqualTo(40301);
    }

    @Test
    void recruiterCanDownloadCalendarForTheirCompany() {
        Company company = new Company();
        company.setId(COMPANY_ID);
        Recruiter recruiter = new Recruiter();
        recruiter.setCompany(company);
        when(recruiterRepository.findByUserId(103L)).thenReturn(Optional.of(recruiter));
        when(jobRepository.findById(JOB_ID)).thenReturn(Optional.of(job));

        String calendar = new String(useCase.exportIcsCalendar(103L, "ROLE_RECRUITER", INTERVIEW_ID),
                java.nio.charset.StandardCharsets.UTF_8);

        assertThat(calendar).contains("BEGIN:VCALENDAR", "DTSTART:20261030T070000Z", "END:VCALENDAR");
    }
}
