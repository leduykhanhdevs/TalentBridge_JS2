package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.ApplicationResult;
import vn.talentbridge.core.application.port.in.ApplyJobUseCase;
import vn.talentbridge.core.application.port.out.ApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.JobRepositoryPort;
import vn.talentbridge.core.application.port.out.ResumeRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Application;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.Job;
import vn.talentbridge.core.domain.model.Resume;
import vn.talentbridge.core.domain.vo.JobStatus;

import java.time.LocalDate;
import java.util.Objects;

public class ApplyJobUseCaseImpl implements ApplyJobUseCase {

    private final ApplicationRepositoryPort applicationRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final JobRepositoryPort jobRepository;
    private final ResumeRepositoryPort resumeRepository;

    public ApplyJobUseCaseImpl(
            ApplicationRepositoryPort applicationRepository,
            CandidateRepositoryPort candidateRepository,
            JobRepositoryPort jobRepository,
            ResumeRepositoryPort resumeRepository) {
        this.applicationRepository = applicationRepository;
        this.candidateRepository = candidateRepository;
        this.jobRepository = jobRepository;
        this.resumeRepository = resumeRepository;
    }

    @Override
    public ApplicationResult apply(Long userId, Long jobId, Long resumeId, String coverLetter) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Người dùng không hợp lệ.");
        }
        if (jobId == null || jobId <= 0) {
            throw new IllegalArgumentException("Tin tuyển dụng không hợp lệ.");
        }
        if (resumeId == null || resumeId <= 0) {
            throw new IllegalArgumentException("CV không hợp lệ.");
        }

        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ ứng viên", userId));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Tin tuyển dụng", jobId));

        if (job.getStatus() != JobStatus.ACTIVE
                || (job.getDeadline() != null && job.getDeadline().isBefore(LocalDate.now()))) {
            throw new IllegalArgumentException("Tin tuyển dụng không còn nhận hồ sơ.");
        }

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("CV", resumeId));

        if (!Objects.equals(resume.getCandidateId(), candidate.getId())) {
            throw new IllegalArgumentException("Bạn chỉ có thể ứng tuyển bằng CV của mình.");
        }

        Application application = new Application(
                null, jobId, candidate.getId(), resumeId,
                coverLetter, "APPLIED", "SUBMITTED", null, null
        );
        Application saved = applicationRepository.save(application);

        return new ApplicationResult(
                saved.getId(),
                saved.getJobId(),
                saved.getCandidateId(),
                saved.getResumeId(),
                saved.getCoverLetter(),
                saved.getCurrentStage(),
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }
}