package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.dto.CandidateResult;
import vn.talentbridge.core.application.dto.UpdateCandidateProfileCommand;
import vn.talentbridge.core.application.port.in.UpdateCandidateProfileUseCase;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UpdateCandidateProfileUseCaseImpl implements UpdateCandidateProfileUseCase {

    private final CandidateRepositoryPort candidateRepository;
    private final UserRepositoryPort userRepository;

    public UpdateCandidateProfileUseCaseImpl(CandidateRepositoryPort candidateRepository,
                                             UserRepositoryPort userRepository) {
        this.candidateRepository = candidateRepository;
        this.userRepository = userRepository;
    }

    @Override
    public CandidateResult updateProfile(Long userId, UpdateCandidateProfileCommand command) {
        Candidate candidate = candidateRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng", userId));
                    Candidate newCandidate = new Candidate();
                    newCandidate.setUser(user);
                    newCandidate.setCreatedAt(LocalDateTime.now());
                    newCandidate.setUpdatedAt(LocalDateTime.now());
                    return candidateRepository.save(newCandidate);
                });

        User user = candidate.getUser();
        if (user == null) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng", userId));
            candidate.setUser(user);
        }

        // Validate numerical fields
        if (command.experienceYears() != null && command.experienceYears() < 0) {
            throw new DomainException(40001, "Số năm kinh nghiệm không được nhỏ hơn 0");
        }
        if (command.currentSalary() != null && command.currentSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(40001, "Mức lương hiện tại không được nhỏ hơn 0");
        }
        if (command.expectedSalary() != null && command.expectedSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(40001, "Mức lương mong muốn không được nhỏ hơn 0");
        }

        // Update User info
        if (command.fullName() != null && !command.fullName().isBlank()) {
            user.setFullName(command.fullName().trim());
        }
        if (command.phone() != null) {
            user.setPhoneNumber(command.phone().trim());
        }
        if (command.avatarUrl() != null) {
            user.setAvatarUrl(command.avatarUrl().trim());
        }
        user.setUpdatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        candidate.setUser(savedUser);

        // Update Candidate professional info
        if (command.title() != null) {
            candidate.setTitle(command.title().trim());
        }
        if (command.dob() != null) {
            candidate.setDob(command.dob());
        }
        if (command.gender() != null && !command.gender().isBlank()) {
            candidate.setGender(command.gender().trim());
        }
        if (command.summary() != null) {
            candidate.setSummary(command.summary().trim());
        }
        if (command.experienceYears() != null) {
            candidate.setExperienceYears(command.experienceYears());
        }
        if (command.currentSalary() != null) {
            candidate.setCurrentSalary(command.currentSalary());
        }
        if (command.expectedSalary() != null) {
            candidate.setExpectedSalary(command.expectedSalary());
        }
        if (command.city() != null) {
            candidate.setCity(command.city().trim());
        }
        if (command.address() != null) {
            candidate.setAddress(command.address().trim());
        }
        if (command.personalWebsite() != null) {
            candidate.setPersonalWebsite(command.personalWebsite().trim());
        }
        if (command.linkedinUrl() != null) {
            candidate.setLinkedinUrl(command.linkedinUrl().trim());
        }
        if (command.githubUrl() != null) {
            candidate.setGithubUrl(command.githubUrl().trim());
        }
        candidate.setUpdatedAt(LocalDateTime.now());

        Candidate savedCandidate = candidateRepository.save(candidate);
        return CandidateResult.from(savedCandidate);
    }
}
