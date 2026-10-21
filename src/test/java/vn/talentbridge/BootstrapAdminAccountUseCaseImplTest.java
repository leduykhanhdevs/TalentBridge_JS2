package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vn.talentbridge.core.application.port.in.AdminBootstrapResult;
import vn.talentbridge.core.application.port.out.PasswordEncoderPort;
import vn.talentbridge.core.application.port.out.UserRepositoryPort;
import vn.talentbridge.core.application.usecase.BootstrapAdminAccountUseCaseImpl;
import vn.talentbridge.core.domain.model.Role;
import vn.talentbridge.core.domain.model.User;
import vn.talentbridge.core.domain.vo.RoleName;
import vn.talentbridge.core.domain.vo.UserStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BootstrapAdminAccountUseCaseImplTest {

    private static final String EMAIL = "admin@example.com";
    private static final String PASSWORD = "A-strong-admin-passphrase-2026";

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private PasswordEncoderPort passwordEncoder;

    private BootstrapAdminAccountUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        useCase = new BootstrapAdminAccountUseCaseImpl(userRepository, passwordEncoder);
    }

    @Test
    void createsAdminWithNormalizedEmailAndEncodedPassword() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(PASSWORD)).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(
                AdminBootstrapResult.CREATED,
                useCase.execute(" Admin@Example.com ", PASSWORD, "  Platform Admin  ", false, false)
        );

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals(EMAIL, saved.getEmail());
        assertEquals("$2a$encoded", saved.getPasswordHash());
        assertEquals("Platform Admin", saved.getFullName());
        assertEquals(UserStatus.ACTIVE, saved.getStatus());
        assertTrue(saved.hasRole(RoleName.ROLE_ADMIN));
        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    void existingAdminIsLeftUnchanged() {
        User existingAdmin = userWithRole(RoleName.ROLE_ADMIN);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingAdmin));

        assertEquals(AdminBootstrapResult.ALREADY_ADMIN, useCase.execute(EMAIL, "", "", false, false));

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void refusesToPromoteAnExistingNonAdminAccount() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userWithRole(RoleName.ROLE_CANDIDATE)));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(EMAIL, "", "Admin", false, false)
        );

        assertTrue(error.getMessage().contains("non-admin"));
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void promotesActiveExistingAccountOnlyWithExplicitOptInAndPreservesCredentials() {
        User existingCandidate = userWithRole(RoleName.ROLE_CANDIDATE);
        String originalPasswordHash = existingCandidate.getPasswordHash();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingCandidate));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(
                AdminBootstrapResult.PROMOTED_EXISTING,
                useCase.execute(EMAIL, "", "Ignored name", true, false)
        );

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertTrue(saved.hasRole(RoleName.ROLE_CANDIDATE));
        assertTrue(saved.hasRole(RoleName.ROLE_ADMIN));
        assertEquals(originalPasswordHash, saved.getPasswordHash());
        assertEquals("Existing user", saved.getFullName());
        assertEquals(UserStatus.ACTIVE, saved.getStatus());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void refusesPromotionWhenExistingAccountIsNotActive() {
        User lockedUser = userWithRole(RoleName.ROLE_CANDIDATE);
        lockedUser.setStatus(UserStatus.LOCKED);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(lockedUser));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(EMAIL, "", "Admin", true, false)
        );

        assertTrue(error.getMessage().contains("not active"));
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void refusesPromotionWhenConfiguredEmailDoesNotExist() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(EMAIL, "", "Admin", true, false)
        );

        assertTrue(error.getMessage().contains("does not match an existing account"));
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void rejectsWeakOrMissingPasswordsWhenCreatingNewAccount() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(EMAIL, "short", "Admin", false, false));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(EMAIL, null, "Admin", false, false));

        verify(userRepository, times(2)).findByEmail(EMAIL);
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void rejectsPasswordLongerThanBcryptByteLimit() {
        String oversizedUtf8Password = "密".repeat(25);

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(EMAIL, oversizedUtf8Password, "Admin", false, false)
        );

        verify(userRepository).findByEmail(EMAIL);
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void reactivatesOnlyLockedExistingAdminWhenExplicitlyEnabledAndPreservesCredentials() {
        User existingAdmin = userWithRole(RoleName.ROLE_ADMIN);
        existingAdmin.addRole(new Role(2L, RoleName.ROLE_CANDIDATE, "Ứng viên"));
        existingAdmin.setStatus(UserStatus.LOCKED);
        String originalPasswordHash = existingAdmin.getPasswordHash();
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingAdmin));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertEquals(
                AdminBootstrapResult.REACTIVATED_EXISTING_ADMIN,
                useCase.execute(EMAIL, "", "Ignored name", false, true)
        );

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals(UserStatus.ACTIVE, saved.getStatus());
        assertEquals(originalPasswordHash, saved.getPasswordHash());
        assertEquals("Existing user", saved.getFullName());
        assertTrue(saved.hasRole(RoleName.ROLE_ADMIN));
        assertTrue(saved.hasRole(RoleName.ROLE_CANDIDATE));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void doesNotReactivateAnExistingAdminWhenExplicitOptInIsDisabled() {
        User lockedAdmin = userWithRole(RoleName.ROLE_ADMIN);
        lockedAdmin.setStatus(UserStatus.LOCKED);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(lockedAdmin));

        assertEquals(AdminBootstrapResult.ALREADY_ADMIN, useCase.execute(EMAIL, "", "", false, false));

        verify(userRepository, never()).save(any());
    }

    @Test
    void refusesToReactivateExistingAdminWithStatusOtherThanLockedOrActive() {
        User bannedAdmin = userWithRole(RoleName.ROLE_ADMIN);
        bannedAdmin.setStatus(UserStatus.BANNED);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(bannedAdmin));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(EMAIL, "", "", false, true)
        );

        assertTrue(error.getMessage().contains("not locked"));
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    private User userWithRole(RoleName roleName) {
        User user = new User();
        user.setEmail(EMAIL);
        user.setPasswordHash("already-hashed");
        user.setFullName("Existing user");
        user.setStatus(UserStatus.ACTIVE);
        user.addRole(new Role(1L, roleName, roleName.name()));
        return user;
    }
}
