package vn.talentbridge;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import vn.talentbridge.adapter.out.persistence.repository.CandidateJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.RecruiterJpaRepository;
import vn.talentbridge.adapter.out.persistence.repository.UserJpaRepository;
import vn.talentbridge.core.application.dto.RegisterCommand;
import vn.talentbridge.core.application.port.in.RegisterUseCase;
import vn.talentbridge.core.application.port.out.AuthSessionRepositoryPort;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@ActiveProfiles("test")
class AtomicRegistrationIntegrationTest {

    @Autowired
    private RegisterUseCase registerUseCase;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private CandidateJpaRepository candidateJpaRepository;

    @Autowired
    private RecruiterJpaRepository recruiterJpaRepository;

    @SpyBean
    private AuthSessionRepositoryPort authSessionRepository;

    @BeforeEach
    @AfterEach
    void cleanup() {
        candidateJpaRepository.deleteAll();
        recruiterJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    @Test
    @DisplayName("When error occurs saving session during registration, user record must be rolled back (no orphan)")
    void register_failsAtSessionSave_rollsBackUser() {
        String testEmail = "orphan-check@talentbridge.vn";
        RegisterCommand command = new RegisterCommand(
                testEmail,
                "Password123!",
                "Atomic Test User",
                "0988776655",
                "ROLE_CANDIDATE",
                null
        );

        // Inject simulated runtime failure at the final step (saving session)
        doThrow(new RuntimeException("Simulated mid-registration failure"))
                .when(authSessionRepository).save(any());

        assertThrows(RuntimeException.class, () -> registerUseCase.register(command));

        // Assert that user record was completely rolled back and no orphan exists
        assertTrue(
                userJpaRepository.findByEmail(testEmail).isEmpty(),
                "User record must not exist when registration fails mid-way"
        );
    }
}
