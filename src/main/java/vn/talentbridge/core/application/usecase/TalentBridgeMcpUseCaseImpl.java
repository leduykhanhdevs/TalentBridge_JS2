package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CandidateJobMatchResult;
import vn.talentbridge.core.application.dto.JobApplicantResult;
import vn.talentbridge.core.application.dto.McpCandidateProfileResult;
import vn.talentbridge.core.application.dto.McpJobSummary;
import vn.talentbridge.core.application.dto.UpdateApplicantStatusCommand;
import vn.talentbridge.core.application.port.in.JobUseCase;
import vn.talentbridge.core.application.port.in.MatchApplicantUseCase;
import vn.talentbridge.core.application.port.in.TalentBridgeMcpUseCase;
import vn.talentbridge.core.application.port.in.UpdateApplicantStatusUseCase;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateSkillRepositoryPort;
import vn.talentbridge.core.application.port.out.JobApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.Recruiter;

import java.util.List;

public class TalentBridgeMcpUseCaseImpl implements TalentBridgeMcpUseCase {
    private static final int MAX_SEARCH_LIMIT = 20;

    private final JobUseCase jobUseCase;
    private final MatchApplicantUseCase matchApplicantUseCase;
    private final UpdateApplicantStatusUseCase updateApplicantStatusUseCase;
    private final RecruiterRepositoryPort recruiterRepository;
    private final JobRepositoryPort jobRepository;
    private final JobApplicationRepositoryPort jobApplicationRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final CandidateSkillRepositoryPort candidateSkillRepository;

    public TalentBridgeMcpUseCaseImpl(
            JobUseCase jobUseCase,
            MatchApplicantUseCase matchApplicantUseCase,
            UpdateApplicantStatusUseCase updateApplicantStatusUseCase,
            RecruiterRepositoryPort recruiterRepository,
            JobRepositoryPort jobRepository,
            JobApplicationRepositoryPort jobApplicationRepository,
            CandidateRepositoryPort candidateRepository,
            CandidateSkillRepositoryPort candidateSkillRepository
    ) {
        this.jobUseCase = jobUseCase;
        this.matchApplicantUseCase = matchApplicantUseCase;
        this.updateApplicantStatusUseCase = updateApplicantStatusUseCase;
        this.recruiterRepository = recruiterRepository;
        this.jobRepository = jobRepository;
        this.jobApplicationRepository = jobApplicationRepository;
        this.candidateRepository = candidateRepository;
        this.candidateSkillRepository = candidateSkillRepository;
    }

    @Override
    public List<McpJobSummary> searchJobs(Long recruiterUserId, String keyword, String city, int limit) {
        requireRecruiter(recruiterUserId);
        int boundedLimit = Math.max(1, Math.min(limit, MAX_SEARCH_LIMIT));
        return jobUseCase.searchJobs(keyword, city, null, null, null, null, 0, boundedLimit)
                .stream()
                .map(job -> new McpJobSummary(
                        job.id(), job.title(), job.companyName(),
                        firstNonBlank(job.city(), job.location(), "Toàn quốc"),
                        firstNonBlank(job.experienceLevel(), "Không yêu cầu")))
                .toList();
    }

    @Override
    public McpCandidateProfileResult getCandidateProfile(Long recruiterUserId, Long jobId, Long candidateId) {
        requireCandidateApplication(recruiterUserId, jobId, candidateId);
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Ứng viên", candidateId));
        String name = candidate.getUser() == null ? "Ứng viên" : candidate.getUser().getFullName();
        List<String> skills = candidateSkillRepository.findByCandidateId(candidateId).stream()
                .map(skill -> skill.getSkillName())
                .filter(skill -> skill != null && !skill.isBlank())
                .distinct()
                .toList();

        return new McpCandidateProfileResult(
                candidate.getId(), name, candidate.getTitle(), candidate.getExperienceYears(),
                candidate.getCity(), candidate.getSummary(), skills);
    }

    @Override
    public CandidateJobMatchResult evaluateCandidate(Long recruiterUserId, Long jobId, Long candidateId) {
        return matchApplicantUseCase.matchApplicant(recruiterUserId, jobId, candidateId);
    }

    @Override
    public JobApplicantResult updateApplicationStage(
            Long recruiterUserId, Long jobId, Long applicationId, String stage, String note) {
        UpdateApplicantStatusCommand command = new UpdateApplicantStatusCommand(stage, null, note);
        return updateApplicantStatusUseCase.updateStageAndStatus(recruiterUserId, jobId, applicationId, command);
    }

    private void requireCandidateApplication(Long recruiterUserId, Long jobId, Long candidateId) {
        Recruiter recruiter = requireRecruiter(recruiterUserId);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng", jobId));
        if (recruiter.getCompany() == null || recruiter.getCompany().getId() == null
                || !recruiter.getCompany().getId().equals(job.getCompanyId())) {
            throw new DomainException(40301, "Bạn không có quyền xem ứng viên của tin tuyển dụng này");
        }
        jobApplicationRepository.findApplicantByJobIdAndCandidateId(jobId, candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn ứng tuyển của ứng viên", candidateId));
    }

    private Recruiter requireRecruiter(Long recruiterUserId) {
        return recruiterRepository.findByUserId(recruiterUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hồ sơ nhà tuyển dụng"));
    }

    private static String firstNonBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private static String firstNonBlank(String first, String second, String fallback) {
        return first != null && !first.isBlank() ? first : firstNonBlank(second, fallback);
    }
}
