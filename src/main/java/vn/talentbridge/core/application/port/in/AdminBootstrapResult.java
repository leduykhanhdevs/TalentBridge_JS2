package vn.talentbridge.core.application.port.in;

/** Result of a one-time production administrator bootstrap operation. */
public enum AdminBootstrapResult {
    CREATED,
    PROMOTED_EXISTING,
    ALREADY_ADMIN
}
