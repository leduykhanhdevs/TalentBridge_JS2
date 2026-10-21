package vn.talentbridge.core.application.port.in;

/** Provisions or explicitly promotes a production administrator from operator-controlled configuration. */
public interface BootstrapAdminAccountUseCase {
    /**
     * Creates an administrator for a new email, promotes an existing active account only when
     * {@code promoteExistingAccount} is explicitly enabled, or reactivates a locked existing admin
     * only when {@code reactivateLockedAdmin} is explicitly enabled. Existing credentials are preserved.
     */
    AdminBootstrapResult execute(
            String email,
            String rawPassword,
            String fullName,
            boolean promoteExistingAccount,
            boolean reactivateLockedAdmin
    );
}
