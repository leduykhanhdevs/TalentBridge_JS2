package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.port.out.ApplicationRepositoryPort;
import vn.talentbridge.core.application.port.out.CandidateRepositoryPort;
import vn.talentbridge.core.application.usecase.WithdrawApplicationUseCaseImpl;
import vn.talentbridge.core.domain.exception.DomainException;
import vn.talentbridge.core.domain.exception.ResourceNotFoundException;
import vn.talentbridge.core.domain.model.Application;
import vn.talentbridge.core.domain.model.Candidate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WithdrawApplicationUseCaseImplTest {
    @Mock private ApplicationRepositoryPort applicationRepository;
    @Mock private CandidateRepositoryPort candidateRepository;

    private WithdrawApplicationUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new WithdrawApplicationUseCaseImpl(applicationRepository, candidateRepository);
    }

    @Test
    void ownerCanWithdrawApplication() {
        stubCandidate();
        Application application = application(20L);
        when(applicationRepository.findByIdForUpdate(40L)).thenReturn(Optional.of(application));
        when(applicationRepository.save(application)).thenReturn(application);

        assertEquals("WITHDRAWN", useCase.withdraw(10L, 40L).status());
        verify(applicationRepository).save(application);
    }

    @Test
    void otherCandidateCannotWithdrawApplication() {
        stubCandidate();
        when(applicationRepository.findByIdForUpdate(40L)).thenReturn(Optional.of(application(21L)));

        assertThrows(ResourceNotFoundException.class, () -> useCase.withdraw(10L, 40L));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void withdrawnOrCompletedApplicationCannotBeWithdrawn() {
        stubCandidate();
        Application withdrawn = application(20L);
        withdrawn.setStatus("WITHDRAWN");
        when(applicationRepository.findByIdForUpdate(40L)).thenReturn(Optional.of(withdrawn));

        assertThrows(DomainException.class, () -> useCase.withdraw(10L, 40L));

        Application hired = application(20L);
        hired.setCurrentStage("HIRED");
        when(applicationRepository.findByIdForUpdate(40L)).thenReturn(Optional.of(hired));
        assertThrows(DomainException.class, () -> useCase.withdraw(10L, 40L));
        verify(applicationRepository, never()).save(any());
    }

    @Test
    void invalidIdsAreRejectedWithoutRepositoryAccess() {
        assertThrows(IllegalArgumentException.class, () -> useCase.withdraw(null, 40L));
        assertThrows(IllegalArgumentException.class, () -> useCase.withdraw(10L, -1L));
        verifyNoInteractions(candidateRepository, applicationRepository);
    }

    private void stubCandidate() {
        Candidate candidate = new Candidate();
        candidate.setId(20L);
        when(candidateRepository.findByUserId(10L)).thenReturn(Optional.of(candidate));
    }

    private Application application(Long candidateId) {
        return new Application(40L, 5L, candidateId, 30L,
                null, "APPLIED", "SUBMITTED", null, null);
    }
}
