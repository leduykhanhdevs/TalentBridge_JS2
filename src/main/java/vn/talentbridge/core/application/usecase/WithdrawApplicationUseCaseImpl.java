package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.ApplicationResult;
import vn.talentbridge.core.application.port.in.WithdrawApplicationUseCase;
import vn.talentbridge.core.application.port.out.ApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Application;
import vn.talentbridge.core.domain.model.Candidate;

import java.util.Objects;

public class WithdrawApplicationUseCaseImpl implements WithdrawApplicationUseCase {
    private final ApplicationRepositoryPort applicationRepository;
    private final CandidateRepositoryPort candidateRepository;

    public WithdrawApplicationUseCaseImpl(ApplicationRepositoryPort applicationRepository,
                                          CandidateRepositoryPort candidateRepository) {
        this.applicationRepository = applicationRepository;
        this.candidateRepository = candidateRepository;
    }

    @Override
    public ApplicationResult withdraw(Long userId, Long applicationId) {
        if (userId == null || userId <= 0 || applicationId == null || applicationId <= 0) {
            throw new IllegalArgumentException("Người dùng hoặc đơn ứng tuyển không hợp lệ.");
        }

        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ ứng viên", userId));
        Application application = applicationRepository.findByIdForUpdate(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Đơn ứng tuyển", applicationId));

        // Do not reveal another candidate's application through this endpoint.
        if (!Objects.equals(application.getCandidateId(), candidate.getId())) {
            throw new ResourceNotFoundException("Đơn ứng tuyển", applicationId);
        }

        application.withdraw();
        Application saved = applicationRepository.save(application);
        return new ApplicationResult(saved.getId(), saved.getJobId(), saved.getCandidateId(),
                saved.getResumeId(), saved.getCoverLetter(), saved.getCurrentStage(),
                saved.getStatus(), saved.getCreatedAt());
    }
}
