package vn.talentbridge.adapter.out.security;

import org.springframework.stereotype.Component;
import vn.talentbridge.core.application.port.out.LoginAttemptTrackerPort;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryLoginAttemptAdapter implements LoginAttemptTrackerPort {

    private static final long LOCKOUT_WINDOW_MINUTES = 15;

    private static class AttemptData {
        int count;
        Instant lastAttemptTime;

        AttemptData(int count, Instant lastAttemptTime) {
            this.count = count;
            this.lastAttemptTime = lastAttemptTime;
        }
    }

    private final ConcurrentHashMap<String, AttemptData> attempts = new ConcurrentHashMap<>();

    @Override
    public int recordFailedAttempt(String email) {
        if (email == null) {
            return 0;
        }
        String key = email.trim().toLowerCase();
        Instant now = Instant.now();

        AttemptData data = attempts.compute(key, (k, current) -> {
            if (current == null) {
                return new AttemptData(1, now);
            }
            if (Duration.between(current.lastAttemptTime, now).toMinutes() >= LOCKOUT_WINDOW_MINUTES) {
                return new AttemptData(1, now);
            }
            return new AttemptData(current.count + 1, now);
        });

        return data.count;
    }

    @Override
    public void resetAttempts(String email) {
        if (email != null) {
            attempts.remove(email.trim().toLowerCase());
        }
    }

    @Override
    public int getAttempts(String email) {
        if (email == null) {
            return 0;
        }
        AttemptData data = attempts.get(email.trim().toLowerCase());
        if (data == null) {
            return 0;
        }
        if (Duration.between(data.lastAttemptTime, Instant.now()).toMinutes() >= LOCKOUT_WINDOW_MINUTES) {
            attempts.remove(email.trim().toLowerCase());
            return 0;
        }
        return data.count;
    }
}
