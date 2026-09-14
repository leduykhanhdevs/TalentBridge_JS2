package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.RecruiterResult;
import vn.talentbridge.core.application.port.in.GetRecruiterProfileUseCase;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;

public class GetRecruiterProfileUseCaseImpl implements GetRecruiterProfileUseCase {

    private final RecruiterRepositoryPort recruiterRepository;

    public GetRecruiterProfileUseCaseImpl(
            RecruiterRepositoryPort recruiterRepository) {
        this.recruiterRepository = recruiterRepository;
    }

    @Override
    public RecruiterResult getProfile(Long userId) {

        Recruiter recruiter = recruiterRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy hồ sơ nhà tuyển dụng"));

        return RecruiterResult.from(recruiter);
    }
}