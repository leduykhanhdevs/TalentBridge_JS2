package vn.talentbridge.core.application.port.in;

/** Provisions the initial production administrator from operator-supplied credentials. */
public interface BootstrapAdminAccountUseCase {
    /**
     * Creates the administrator when the email is unused. Returns {@code false} when an
     * administrator with that email already exists. Existing non-admin accounts are rejected.
     */
    boolean execute(String email, String rawPassword, String fullName);
}
