package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CompanyJoinRequestResult;
import vn.talentbridge.core.application.port.in.GetMyPendingJoinRequestUseCase;
import vn.talentbridge.core.application.port.out.CompanyJoinRequestRepositoryPort;

import java.util.Optional;

public class GetMyPendingJoinRequestUseCaseImpl implements GetMyPendingJoinRequestUseCase {

    private final CompanyJoinRequestRepositoryPort companyJoinRequestRepository;

    public GetMyPendingJoinRequestUseCaseImpl(CompanyJoinRequestRepositoryPort companyJoinRequestRepository) {
        this.companyJoinRequestRepository = companyJoinRequestRepository;
    }

    @Override
    public Optional<CompanyJoinRequestResult> getMyPendingJoinRequest(Long userId) {
        if (userId == null) {
            return Optional.empty();
        }
        return companyJoinRequestRepository.findPendingByUserId(userId)
                .map(CompanyJoinRequestResult::from);
    }
}
