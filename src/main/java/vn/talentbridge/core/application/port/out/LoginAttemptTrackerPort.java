package vn.talentbridge.core.application.port.out;

public interface LoginAttemptTrackerPort {
    int recordFailedAttempt(String email);
    void resetAttempts(String email);
    int getAttempts(String email);
}
