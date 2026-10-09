package vn.talentbridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

        assertTrue(useCase.execute(" Admin@Example.com ", PASSWORD, "  Platform Admin  "));

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

        assertFalse(useCase.execute(EMAIL, PASSWORD, "Admin"));

        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void refusesToPromoteAnExistingNonAdminAccount() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(userWithRole(RoleName.ROLE_CANDIDATE)));

        IllegalStateException error = assertThrows(
                IllegalStateException.class,
                () -> useCase.execute(EMAIL, PASSWORD, "Admin")
        );

        assertTrue(error.getMessage().contains("non-admin"));
        verify(userRepository, never()).save(any());
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void rejectsWeakOrMissingPasswordsBeforeRepositoryAccess() {
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(EMAIL, "short", "Admin"));
        assertThrows(IllegalArgumentException.class, () -> useCase.execute(EMAIL, null, "Admin"));

        verifyNoInteractions(userRepository, passwordEncoder);
    }

    @Test
    void rejectsPasswordLongerThanBcryptByteLimit() {
        String oversizedUtf8Password = "密".repeat(25);

        assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(EMAIL, oversizedUtf8Password, "Admin")
        );

        verifyNoInteractions(userRepository, passwordEncoder);
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
