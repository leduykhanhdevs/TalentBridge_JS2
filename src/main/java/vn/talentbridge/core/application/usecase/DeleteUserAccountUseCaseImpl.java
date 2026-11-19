package vn.talentbridge.core.application.usecase;

import vn.talentbridge.core.application.port.in.DeleteUserAccountUseCase;
import vn.talentbridge.core.application.port.out.AccountIdentifierHasherPort;
import vn.talentbridge.core.application.port.out.ApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.BlockedAccountIdentifierRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.FileStoragePort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.port.out.ResumeRepositoryPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.Resume;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.RoleName;

import java.util.Locale;

public class DeleteUserAccountUseCaseImpl implements DeleteUserAccountUseCase {
    private static final String EMAIL = "EMAIL";
    private static final String PHONE = "PHONE";

    private final UserRepositoryPort userRepository;
    private final RecruiterRepositoryPort recruiterRepository;
    private final CandidateRepositoryPort candidateRepository;
    private final ApplicationRepositoryPort applicationRepository;
    private final BlockedAccountIdentifierRepositoryPort blockedIdentifiers;
    private final AccountIdentifierHasherPort identifierHasher;
    private final ResumeRepositoryPort resumeRepository;
    private final FileStoragePort fileStorage;

    public DeleteUserAccountUseCaseImpl(
            UserRepositoryPort userRepository,
            RecruiterRepositoryPort recruiterRepository,
            CandidateRepositoryPort candidateRepository,
            ApplicationRepositoryPort applicationRepository,
            BlockedAccountIdentifierRepositoryPort blockedIdentifiers,
            AccountIdentifierHasherPort identifierHasher,
            ResumeRepositoryPort resumeRepository,
            FileStoragePort fileStorage) {
        this.userRepository = userRepository;
        this.recruiterRepository = recruiterRepository;
        this.candidateRepository = candidateRepository;
        this.applicationRepository = applicationRepository;
        this.blockedIdentifiers = blockedIdentifiers;
        this.identifierHasher = identifierHasher;
        this.resumeRepository = resumeRepository;
        this.fileStorage = fileStorage;
    }

    @Override
    public void deleteAccount(Long accountUserId, Long adminUserId, boolean blockIdentifiers) {
        if (accountUserId == null || accountUserId.equals(adminUserId)) {
            throw new DomainException(40301, "Không thể xóa tài khoản quản trị đang đăng nhập");
        }

        User user = userRepository.findById(accountUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Tài khoản", accountUserId));
        boolean isCandidate = user.hasRole(RoleName.ROLE_CANDIDATE);
        boolean isRecruiter = user.hasRole(RoleName.ROLE_RECRUITER);
        if (user.hasRole(RoleName.ROLE_ADMIN) || (!isCandidate && !isRecruiter)) {
            throw new DomainException(40301, "Chỉ có thể xóa tài khoản ứng viên hoặc nhà tuyển dụng");
        }

        if (blockIdentifiers) {
            block(EMAIL, normalizeEmail(user.getEmail()));
            block(PHONE, normalizePhone(user.getPhoneNumber()));
        }

        if (isCandidate) {
            Candidate candidate = candidateRepository.findByUserId(accountUserId).orElse(null);
            if (candidate != null) {
                resumeRepository.findByCandidateId(candidate.getId()).stream()
                        .map(Resume::getFileUrl)
                        .filter(fileUrl -> fileUrl != null && !fileUrl.isBlank())
                        .forEach(fileStorage::deleteFileAfterCommit);
                // Applications reference candidate CVs with ON DELETE RESTRICT, so remove the
                // application pipeline first; the remaining candidate-owned data cascades.
                applicationRepository.deleteByCandidateId(candidate.getId());
                candidateRepository.deleteById(candidate.getId());
            }
        }

        if (isRecruiter) {
            recruiterRepository.findByUserId(accountUserId)
                    .ifPresent(recruiter -> recruiterRepository.deleteById(recruiter.getId()));
        }

        userRepository.deleteById(accountUserId);
    }

    private void block(String type, String normalizedIdentifier) {
        if (normalizedIdentifier == null || normalizedIdentifier.isBlank()) {
            return;
        }
        blockedIdentifiers.save(type, identifierHasher.hash(type, normalizedIdentifier));
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }
        String compact = phone.trim().replaceAll("[\\s().-]", "");
        if (compact.startsWith("+")) {
            return compact;
        }
        if (compact.startsWith("0")) {
            return "+84" + compact.substring(1);
        }
        if (compact.startsWith("84")) {
            return "+" + compact;
        }
        return compact;
    }
}
