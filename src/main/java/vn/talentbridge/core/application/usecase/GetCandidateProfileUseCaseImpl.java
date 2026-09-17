package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CandidateResult;
import vn.talentbridge.core.application.port.in.GetCandidateProfileUseCase;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.User;

import java.time.LocalDateTime;

public class GetCandidateProfileUseCaseImpl implements GetCandidateProfileUseCase {

    private final CandidateRepositoryPort candidateRepository;
    private final UserRepositoryPort userRepository;

    public GetCandidateProfileUseCaseImpl(CandidateRepositoryPort candidateRepository,
                                          UserRepositoryPort userRepository) {
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CandidateResult getProfile(Long userId) {
        return candidateRepository.findByUserId(userId)
                .map(CandidateResult::from)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng", userId));

                    Candidate newCandidate = new Candidate();
                    newCandidate.setUser(user);
                    newCandidate.setCreatedAt(LocalDateTime.now());
                    newCandidate.setUpdatedAt(LocalDateTime.now());
                    Candidate saved = candidateRepository.save(newCandidate);
                    return CandidateResult.from(saved);
                });
    }
}
