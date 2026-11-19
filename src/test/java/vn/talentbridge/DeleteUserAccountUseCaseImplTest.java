package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.port.out.AccountIdentifierHasherPort;
import vn.talentbridge.core.application.port.out.ApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.BlockedAccountIdentifierRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.port.out.FileStoragePort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.application.port.out.ResumeRepositoryPort;
import vn.talentbridge.core.application.port.out.RecruiterRepositoryPort;
import vn.talentbridge.core.application.usecase.DeleteUserAccountUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.model.Candidate;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.Recruiter;
import vn.talentbridge.core.domain.model.Resume;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.RoleName;

import java.util.Optional;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteUserAccountUseCaseImplTest {
    @Mock private UserRepositoryPort userRepository;
    @Mock private RecruiterRepositoryPort recruiterRepository;
    @Mock private CandidateRepositoryPort candidateRepository;
    @Mock private ApplicationRepositoryPort applicationRepository;
    @Mock private BlockedAccountIdentifierRepositoryPort blockedIdentifiers;
    @Mock private AccountIdentifierHasherPort identifierHasher;
    @Mock private ResumeRepositoryPort resumeRepository;
    @Mock private FileStoragePort fileStorage;

    private DeleteUserAccountUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeleteUserAccountUseCaseImpl(
                userRepository,
                recruiterRepository,
                candidateRepository,
                applicationRepository,
                blockedIdentifiers,
                identifierHasher,
                resumeRepository,
                fileStorage
        );
    }

    @Test
    @DisplayName("Deleting a candidate removes application records before deleting the account")
    void deleteCandidate_removesApplicationsThenUserWithoutBlockingIdentifiers() {
        User candidateUser = user(20L, RoleName.ROLE_CANDIDATE);
        Candidate candidate = new Candidate();
        candidate.setId(200L);
        when(userRepository.findById(20L)).thenReturn(Optional.of(candidateUser));
        when(candidateRepository.findByUserId(20L)).thenReturn(Optional.of(candidate));

        useCase.deleteAccount(20L, 1L, false);

        InOrder order = inOrder(applicationRepository, candidateRepository, userRepository);
        order.verify(applicationRepository).deleteByCandidateId(200L);
        order.verify(candidateRepository).deleteById(200L);
        order.verify(userRepository).deleteById(20L);
        verify(blockedIdentifiers, never()).save(anyString(), anyString());
    }

    @Test
    @DisplayName("Deleting and blocking stores normalized identifier hashes before deletion")
    void deleteAndBlockCandidate_savesEmailAndPhoneHashes() {
        User candidateUser = user(21L, RoleName.ROLE_CANDIDATE);
        candidateUser.setEmail(" Candidate@Example.COM ");
        candidateUser.setPhoneNumber("0912 345-678");
        Candidate candidate = new Candidate();
        candidate.setId(201L);
        Resume resume = mock(Resume.class);
        when(resume.getFileUrl()).thenReturn("resumes/201/cv.pdf");
        when(userRepository.findById(21L)).thenReturn(Optional.of(candidateUser));
        when(candidateRepository.findByUserId(21L)).thenReturn(Optional.of(candidate));
        when(resumeRepository.findByCandidateId(201L)).thenReturn(List.of(resume));
        when(identifierHasher.hash("EMAIL", "candidate@example.com")).thenReturn("email-hash");
        when(identifierHasher.hash("PHONE", "+84912345678")).thenReturn("phone-hash");

        useCase.deleteAccount(21L, 1L, true);

        verify(blockedIdentifiers).save("EMAIL", "email-hash");
        verify(blockedIdentifiers).save("PHONE", "phone-hash");
        verify(fileStorage).deleteFileAfterCommit("resumes/201/cv.pdf");
        verify(userRepository).deleteById(21L);
    }

    @Test
    @DisplayName("Deleting a recruiter removes the personal recruiter profile but keeps company data outside this use case")
    void deleteRecruiter_removesRecruiterProfileBeforeUser() {
        User recruiterUser = user(22L, RoleName.ROLE_RECRUITER);
        Recruiter recruiter = new Recruiter();
        recruiter.setId(202L);
        when(userRepository.findById(22L)).thenReturn(Optional.of(recruiterUser));
        when(recruiterRepository.findByUserId(22L)).thenReturn(Optional.of(recruiter));

        useCase.deleteAccount(22L, 1L, false);

        InOrder order = inOrder(recruiterRepository, userRepository);
        order.verify(recruiterRepository).deleteById(202L);
        order.verify(userRepository).deleteById(22L);
        verify(candidateRepository, never()).findByUserId(22L);
    }

    @Test
    @DisplayName("An admin account or the currently authenticated admin cannot be deleted")
    void deleteAdminOrSelf_rejected() {
        assertEquals(40301, assertThrows(DomainException.class,
                () -> useCase.deleteAccount(1L, 1L, true)).getCode());

        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, RoleName.ROLE_ADMIN)));
        assertEquals(40301, assertThrows(DomainException.class,
                () -> useCase.deleteAccount(2L, 1L, false)).getCode());
        verify(userRepository, never()).deleteById(anyLong());
    }

    private User user(Long id, RoleName roleName) {
        User user = new User();
        user.setId(id);
        user.setEmail("candidate@example.com");
        user.setPhoneNumber("0912345678");
        user.setRoles(Set.of(new Role(1L, roleName, roleName.name())));
        return user;
    }
}
