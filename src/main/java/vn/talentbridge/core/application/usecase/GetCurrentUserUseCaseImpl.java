package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.UserResult;
import vn.talentbridge.core.application.port.in.GetCurrentUserUseCase;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.User;

public class GetCurrentUserUseCaseImpl implements GetCurrentUserUseCase {
    private final UserRepositoryPort userRepository;

    public GetCurrentUserUseCaseImpl(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResult getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("NgÆ°á»i dÃ¹ng", email));
        return UserResult.from(user);
    }
}