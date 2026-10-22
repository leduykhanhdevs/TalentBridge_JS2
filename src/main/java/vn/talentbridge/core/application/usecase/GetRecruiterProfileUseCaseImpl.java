package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.RecruiterResult;
import vn.talentbridge.core.application.port.in.GetRecruiterProfileUseCase;
import vn.talentbridge.core.application.port.out.CompanyRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.model.Company;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;

public class GetRecruiterProfileUseCaseImpl implements GetRecruiterProfileUseCase {

    private final RecruiterRepositoryPort recruiterRepository;
    private final CompanyRepositoryPort companyRepository;

    public GetRecruiterProfileUseCaseImpl(
            RecruiterRepositoryPort recruiterRepository,
            CompanyRepositoryPort companyRepository) {
        this.recruiterRepository = recruiterRepository;
        this.companyRepository = companyRepository;
    }

    @Override
    public RecruiterResult getProfile(Long userId) {

        Recruiter recruiter = recruiterRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy hồ sơ nhà tuyển dụng"));

        // Reconcile legacy requests created before the pending company was linked
        // to its creator. The transactional wrapper makes this a one-time repair.
        if (recruiter.getCompany() == null) {
            Company pendingCompany = companyRepository
                    .findPendingByCreatedByUserId(userId)
                    .orElse(null);
            if (pendingCompany != null) {
                recruiter.setCompany(pendingCompany);
                recruiterRepository.save(recruiter);
            }
        }

        return RecruiterResult.from(recruiter);
    }
}
