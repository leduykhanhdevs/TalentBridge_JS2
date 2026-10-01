package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CandidateApplicationResult;
import vn.talentbridge.core.application.port.in.GetCandidateApplicationsUseCase;
import vn.talentbridge.core.application.port.out.ApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;

import java.util.List;

public class GetCandidateApplicationsUseCaseImpl implements GetCandidateApplicationsUseCase {

    private final ApplicationRepositoryPort applicationRepository;
    private final CandidateRepositoryPort candidateRepository;

    public GetCandidateApplicationsUseCaseImpl(ApplicationRepositoryPort applicationRepository,
                                               CandidateRepositoryPort candidateRepository) {
        this.applicationRepository = applicationRepository;
        this.candidateRepository = candidateRepository;
    }

    @Override
    public List<CandidateApplicationResult> getMyApplications(Long candidateUserId) {
        if (candidateUserId == null || candidateUserId <= 0) {
            throw new IllegalArgumentException("Mã tài khoản ứng viên không hợp lệ");
        }
        candidateRepository.findByUserId(candidateUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ ứng viên", candidateUserId));

        return applicationRepository.findMyApplications(candidateUserId);
    }
}
