package vn.talentbridge.core.application.port.in;

/** Provisions or explicitly promotes a production administrator from operator-controlled configuration. */
public interface BootstrapAdminAccountUseCase {
    /**
     * Creates an administrator for a new email, or promotes an existing active account only when
     * {@code promoteExistingAccount} is explicitly enabled. Existing credentials are preserved.
     */
    AdminBootstrapResult execute(String email, String rawPassword, String fullName, boolean promoteExistingAccount);
}
