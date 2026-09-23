package vn.talentbridge.adapter.out.persistence.adapter;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import vn.talentbridge.adapter.out.persistence.entity.ApplicationJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.CandidateJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.JobJpaEntity;
import vn.talentbridge.adapter.out.persistence.entity.ResumeJpaEntity;
import vn.talentbridge.adapter.out.persistence.repository.ApplicationJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.JobJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.ResumeJpaRepository;
import vn.talentbridge.core.application.port.out.ApplicationRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Application;

@Component
@Transactional
public class ApplicationRepositoryAdapter implements ApplicationRepositoryPort {

    private final ApplicationJpaRepository applicationJpaRepository;
    private final JobJpaRepository jobJpaRepository;
    private final CandidateJpaRepository candidateJpaRepository;
    private final ResumeJpaRepository resumeJpaRepository;

    public ApplicationRepositoryAdapter(
            ApplicationJpaRepository applicationJpaRepository,
            JobJpaRepository jobJpaRepository,
            CandidateJpaRepository candidateJpaRepository,
            ResumeJpaRepository resumeJpaRepository) {
        this.applicationJpaRepository = applicationJpaRepository;
        this.jobJpaRepository = jobJpaRepository;
        this.candidateJpaRepository = candidateJpaRepository;
        this.resumeJpaRepository = resumeJpaRepository;
    }

    @Override
    public Application save(Application application) {
        ApplicationJpaEntity entity = application.getId() == null
                ? new ApplicationJpaEntity()
                : applicationJpaRepository.findById(application.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Đơn ứng tuyển", application.getId()));

        JobJpaEntity job = jobJpaRepository.findById(application.getJobId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Tin tuyển dụng", application.getJobId()));
        CandidateJpaEntity candidate = candidateJpaRepository.findById(application.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ứng viên", application.getCandidateId()));
        ResumeJpaEntity resume = resumeJpaRepository.findById(application.getResumeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "CV", application.getResumeId()));

        entity.setJob(job);
        entity.setCandidate(candidate);
        entity.setResume(resume);
        entity.setCoverLetter(application.getCoverLetter());
        entity.setCurrentStage(application.getCurrentStage() != null
                ? application.getCurrentStage() : "APPLIED");
        entity.setStatus(application.getStatus() != null
                ? application.getStatus() : "SUBMITTED");

        return toDomain(applicationJpaRepository.save(entity));
    }

    private Application toDomain(ApplicationJpaEntity entity) {
        return new Application(
                entity.getId(),
                entity.getJob().getId(),
                entity.getCandidate().getId(),
                entity.getResume().getId(),
                entity.getCoverLetter(),
                entity.getCurrentStage(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}