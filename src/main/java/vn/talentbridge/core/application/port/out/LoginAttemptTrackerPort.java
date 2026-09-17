package vn.talentbridge.core.application.port.out;

public interface LoginAttemptTrackerPort {
    boolean isBlocked(String identifier);
    boolean recordFailure(String identifier);
    void reset(String identifier);
}
