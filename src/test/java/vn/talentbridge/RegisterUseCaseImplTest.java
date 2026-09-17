package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.dto.AuthResult;
import vn.talentbridge.core.application.dto.RegisterCommand;
import vn.talentbridge.core.application.port.out.*;
import vn.talentbridge.core.application.usecase.RegisterUseCaseImpl;
import vn.talentbridge.core.domain.exception.InvalidRoleException;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.RoleName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterUseCaseImplTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private RecruiterRepositoryPort recruiterRepository;

    @Mock
    private CandidateRepositoryPort candidateRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private TokenProviderPort tokenProvider;

    @Mock
    private AuthSessionRepositoryPort authSessionRepository;

    private RegisterUseCaseImpl registerUseCase;

    @BeforeEach
    void setUp() {
        registerUseCase = new RegisterUseCaseImpl(
                userRepository,
                recruiterRepository,
                candidateRepository,
                passwordEncoder,
                tokenProvider,
                authSessionRepository,
                900000L,
                604800000L
        );
    }

    @Test
    @DisplayName("Should throw InvalidRoleException when role is invalid string")
    void register_withInvalidRoleString_throwsInvalidRoleException() {
        RegisterCommand command = new RegisterCommand(
                "user@test.com",
                "Password123!",
                "Nguyen Van A",
                "0912345678",
                "INVALID_ROLE",
                null
        );

        when(userRepository.existsByEmail("user@test.com")).thenReturn(false);

        InvalidRoleException exception = assertThrows(
                InvalidRoleException.class,
                () -> registerUseCase.register(command)
        );

        assertEquals(40005, exception.getCode());
        assertTrue(exception.getMessage().contains("INVALID_ROLE"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidRoleException when role is ROLE_ADMIN")
    void register_withRoleAdmin_throwsInvalidRoleException() {
        RegisterCommand command = new RegisterCommand(
                "admin@test.com",
                "Password123!",
                "Admin Wannabe",
                "0912345678",
                "ROLE_ADMIN",
                null
        );

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(false);

        InvalidRoleException exception = assertThrows(
                InvalidRoleException.class,
                () -> registerUseCase.register(command)
        );

        assertEquals(40005, exception.getCode());
        assertTrue(exception.getMessage().contains("ROLE_ADMIN"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InvalidRoleException when role is null or blank")
    void register_withNullOrBlankRole_throwsInvalidRoleException() {
        RegisterCommand command = new RegisterCommand(
                "nullrole@test.com",
                "Password123!",
                "Null Role",
                "0912345678",
                null,
                null
        );

        when(userRepository.existsByEmail("nullrole@test.com")).thenReturn(false);

        assertThrows(
                InvalidRoleException.class,
                () -> registerUseCase.register(command)
        );
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should register successfully when role is ROLE_CANDIDATE")
    void register_withRoleCandidate_success() {
        RegisterCommand command = new RegisterCommand(
                "candidate@test.com",
                "Password123!",
                "Candidate A",
                "0912345678",
                "ROLE_CANDIDATE",
                null
        );

        when(userRepository.existsByEmail("candidate@test.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(tokenProvider.generateAccessToken(any(), any(), any(), any(), any())).thenReturn("access-token");
        when(tokenProvider.generateRefreshToken(any(), any(), any(), any())).thenReturn("refresh-token");
        when(tokenProvider.hashRefreshToken(anyString())).thenReturn("hashed-token");

        AuthResult result = registerUseCase.register(command);

        assertNotNull(result);
        assertEquals("access-token", result.accessToken());
        assertEquals("candidate@test.com", result.user().email());
        verify(candidateRepository, times(1)).save(any());
        verify(recruiterRepository, never()).save(any());
    }
}
