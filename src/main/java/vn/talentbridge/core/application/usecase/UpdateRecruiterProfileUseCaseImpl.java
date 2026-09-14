package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.RecruiterResult;
import vn.talentbridge.core.application.dto.UpdateRecruiterProfileCommand;
import vn.talentbridge.core.application.port.in.UpdateRecruiterProfileUseCase;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.model.User;

import java.time.LocalDateTime;

public class UpdateRecruiterProfileUseCaseImpl
        implements UpdateRecruiterProfileUseCase {

    private final RecruiterRepositoryPort recruiterRepository;
    private final UserRepositoryPort userRepository;

    public UpdateRecruiterProfileUseCaseImpl(
            RecruiterRepositoryPort recruiterRepository,
            UserRepositoryPort userRepository) {
        this.recruiterRepository = recruiterRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RecruiterResult updateProfile(
            Long userId,
            UpdateRecruiterProfileCommand command) {

        Recruiter recruiter = recruiterRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy hồ sơ nhà tuyển dụng"));

        User user = recruiter.getUser();

        if (command.fullName() != null
                && !command.fullName().isBlank()) {
            user.setFullName(command.fullName().trim());
        }

        if (command.phone() != null) {
            user.setPhoneNumber(command.phone().trim());
        }

        if (command.avatarUrl() != null) {
            user.setAvatarUrl(command.avatarUrl().trim());
        }

        if (command.position() != null
                && !command.position().isBlank()) {
            recruiter.setPosition(command.position().trim());
        }

        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        recruiter.setUser(savedUser);

        Recruiter savedRecruiter = recruiterRepository.save(recruiter);

        return RecruiterResult.from(savedRecruiter);
    }
}