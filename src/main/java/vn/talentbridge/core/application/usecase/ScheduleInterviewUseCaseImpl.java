package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.InterviewResult;
import vn.talentbridge.core.application.dto.ScheduleInterviewCommand;
import vn.talentbridge.core.application.port.in.ScheduleInterviewUseCase;
import vn.talentbridge.core.application.port.out.ApplicationStageRepositoryPort;
import vn.talentbridge.core.application.port.out.InterviewRepositoryPort;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.ApplicationStage;
import vn.talentbridge.core.domain.model.Interview;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.JobApplicant;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.vo.ApplicationPipelineStage;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ScheduleInterviewUseCaseImpl implements ScheduleInterviewUseCase {

    private static final DateTimeFormatter ICS_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private final InterviewRepositoryPort interviewRepository;
    private final JobApplicationRepositoryPort jobApplicationRepository;
    private final JobRepositoryPort jobRepository;
    private final RecruiterRepositoryPort recruiterRepository;
    private final ApplicationStageRepositoryPort applicationStageRepository;

    public ScheduleInterviewUseCaseImpl(
            InterviewRepositoryPort interviewRepository,
            JobApplicationRepositoryPort jobApplicationRepository,
            JobRepositoryPort jobRepository,
            RecruiterRepositoryPort recruiterRepository,
            ApplicationStageRepositoryPort applicationStageRepository) {
        this.interviewRepository = interviewRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.jobRepository = jobRepository;
        this.recruiterRepository = recruiterRepository;
        this.applicationStageRepository = applicationStageRepository;
    }

    @Override
    public InterviewResult scheduleInterview(Long recruiterUserId, Long applicationId, ScheduleInterviewCommand command) {
        if (command == null || command.interviewTime() == null) {
            throw new DomainException(40001, "Thời gian phỏng vấn không được để trống");
        }
        if (command.meetingLinkOrAddress() == null || command.meetingLinkOrAddress().isBlank()) {
            throw new DomainException(40001, "Địa điểm hoặc đường dẫn phòng họp (Google Meet) không được để trống");
        }

        Recruiter recruiter = recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));

        if (recruiter.getCompany() == null || recruiter.getCompany().getId() == null) {
            throw new DomainException(40001, "Nhà tuyển dụng chưa thuộc công ty nào");
        }

        JobApplicant applicant = jobApplicationRepository.findApplicantById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn ứng tuyển", applicationId));

        Job job = jobRepository.findById(applicant.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng", applicant.getJobId()));

        if (!recruiter.getCompany().getId().equals(job.getCompanyId())) {
            throw new DomainException(40301, "Bạn không có quyền lên lịch phỏng vấn cho ứng viên của công ty khác");
        }

        if ("WITHDRAWN".equalsIgnoreCase(applicant.getStatus())) {
            throw new DomainException(40001, "Không thể lên lịch cho đơn ứng tuyển đã được rút");
        }
        ApplicationPipelineStage currentStage;
        try {
            currentStage = ApplicationPipelineStage.from(applicant.getCurrentStage());
        } catch (IllegalArgumentException exception) {
            throw new DomainException(40001, exception.getMessage());
        }
        if (currentStage != ApplicationPipelineStage.INTERVIEW
                && !currentStage.canAdvanceTo(ApplicationPipelineStage.INTERVIEW)) {
            throw new DomainException(40001, "Không thể lên lịch phỏng vấn cho hồ sơ đã kết thúc hoặc ở vòng sau");
        }

        String locationType = (command.locationType() != null && !command.locationType().isBlank())
                ? command.locationType().trim().toUpperCase()
                : "ONLINE";

        Interview interview = new Interview(
                null,
                applicationId,
                command.interviewTime(),
                locationType,
                command.meetingLinkOrAddress().trim(),
                command.notes(),
                "SCHEDULED",
                LocalDateTime.now()
        );

        Interview saved = interviewRepository.save(interview);

        // Stage drives the lifecycle status for active pipeline applications.
        jobApplicationRepository.updateStageAndStatus(applicationId, ApplicationPipelineStage.INTERVIEW.name(),
                ApplicationPipelineStage.INTERVIEW.applicationStatus());

        // Record stage transition audit
        ApplicationStage stageHistory = new ApplicationStage(
                null,
                applicationId,
                ApplicationPipelineStage.INTERVIEW.name(),
                "HR lên lịch phỏng vấn: " + (command.notes() != null ? command.notes() : ""),
                recruiterUserId,
                null,
                LocalDateTime.now()
        );
        applicationStageRepository.save(stageHistory);

        return InterviewResult.from(
                saved,
                job.getId(),
                job.getTitle(),
                job.getCompanyName(),
                applicant.getCandidateFullName(),
                applicant.getCandidateEmail()
        );
    }

    @Override
    public List<InterviewResult> getInterviewsByApplication(Long userId, Long applicationId) {
        JobApplicant applicant = jobApplicationRepository.findApplicantById(applicationId).orElse(null);
        Job job = (applicant != null && applicant.getJobId() != null)
                ? jobRepository.findById(applicant.getJobId()).orElse(null) : null;

        Long jobId = job != null ? job.getId() : null;
        String jobTitle = job != null ? job.getTitle() : (applicant != null ? applicant.getJobTitle() : "Phỏng vấn");
        String companyName = job != null ? job.getCompanyName() : "TalentBridge";
        String candidateName = applicant != null ? applicant.getCandidateFullName() : "Ứng viên";
        String candidateEmail = applicant != null ? applicant.getCandidateEmail() : "";

        return interviewRepository.findByApplicationId(applicationId)
                .stream()
                .map(i -> InterviewResult.from(i, jobId, jobTitle, companyName, candidateName, candidateEmail))
                .toList();
    }

    @Override
    public InterviewResult getLatestInterviewForApplication(Long userId, Long applicationId) {
        JobApplicant applicant = jobApplicationRepository.findApplicantById(applicationId).orElse(null);
        Job job = (applicant != null && applicant.getJobId() != null)
                ? jobRepository.findById(applicant.getJobId()).orElse(null) : null;

        Long jobId = job != null ? job.getId() : null;
        String jobTitle = job != null ? job.getTitle() : (applicant != null ? applicant.getJobTitle() : "Phỏng vấn");
        String companyName = job != null ? job.getCompanyName() : "TalentBridge";
        String candidateName = applicant != null ? applicant.getCandidateFullName() : "Ứng viên";
        String candidateEmail = applicant != null ? applicant.getCandidateEmail() : "";

        return interviewRepository.findLatestByApplicationId(applicationId)
                .map(i -> InterviewResult.from(i, jobId, jobTitle, companyName, candidateName, candidateEmail))
                .orElse(null);
    }

    @Override
    public byte[] exportIcsCalendar(Long interviewId) {
        Interview interview = interviewRepository.findById(interviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch phỏng vấn", interviewId));

        JobApplicant applicant = jobApplicationRepository.findApplicantById(interview.getApplicationId()).orElse(null);
        Job job = (applicant != null && applicant.getJobId() != null)
                ? jobRepository.findById(applicant.getJobId()).orElse(null) : null;

        String jobTitle = job != null ? job.getTitle() : (applicant != null ? applicant.getJobTitle() : "Phỏng vấn");
        String companyName = job != null ? job.getCompanyName() : "TalentBridge";
        String candName = applicant != null ? applicant.getCandidateFullName() : "Ứng viên";

        LocalDateTime start = interview.getInterviewTime();
        LocalDateTime end = start.plusHours(1);

        String startUtc = start.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).withZoneSameInstant(ZoneOffset.UTC).format(ICS_DATE_FORMATTER);
        String endUtc = end.atZone(ZoneId.of("Asia/Ho_Chi_Minh")).withZoneSameInstant(ZoneOffset.UTC).format(ICS_DATE_FORMATTER);
        String nowUtc = LocalDateTime.now().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).withZoneSameInstant(ZoneOffset.UTC).format(ICS_DATE_FORMATTER);

        String summary = "Phỏng vấn: " + jobTitle + " - " + companyName;
        String description = "Phỏng vấn tuyển dụng vị trí " + jobTitle + "\\nỨng viên: " + candName +
                (interview.getNotes() != null ? "\\nGhi chú: " + interview.getNotes() : "") +
                "\\nĐịa điểm / Link: " + interview.getMeetingLinkOrAddress();

        StringBuilder ics = new StringBuilder();
        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//TalentBridge//Interview Scheduler//EN\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");
        ics.append("BEGIN:VEVENT\r\n");
        ics.append("UID:tb-interview-").append(interview.getId()).append("@talentbridge.vn\r\n");
        ics.append("DTSTAMP:").append(nowUtc).append("\r\n");
        ics.append("DTSTART:").append(startUtc).append("\r\n");
        ics.append("DTEND:").append(endUtc).append("\r\n");
        ics.append("SUMMARY:").append(summary).append("\r\n");
        ics.append("DESCRIPTION:").append(description).append("\r\n");
        ics.append("LOCATION:").append(interview.getMeetingLinkOrAddress()).append("\r\n");
        ics.append("STATUS:CONFIRMED\r\n");
        ics.append("END:VEVENT\r\n");
        ics.append("END:VCALENDAR\r\n");

        return ics.toString().getBytes(StandardCharsets.UTF_8);
    }
}
