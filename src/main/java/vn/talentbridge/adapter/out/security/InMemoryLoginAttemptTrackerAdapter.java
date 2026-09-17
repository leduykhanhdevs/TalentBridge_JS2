package vn.talentbridge.adapter.out.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import vn.talentbridge.core.application.port.out.LoginAttemptTrackerPort;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class InMemoryLoginAttemptTrackerAdapter implements LoginAttemptTrackerPort {
    private final ConcurrentMap<String, AttemptState> attempts = new ConcurrentHashMap<>();
    private final int maxFailedAttempts;
    private final Duration blockDuration;
    private final Clock clock;

    @Autowired
    public InMemoryLoginAttemptTrackerAdapter(
            @Value("${talentbridge.security.login.max-failed-attempts:5}") int maxFailedAttempts,
            @Value("${talentbridge.security.login.block-duration-seconds:60}") long blockDurationSeconds) {
        this(maxFailedAttempts, Duration.ofSeconds(blockDurationSeconds), Clock.systemUTC());
    }

    InMemoryLoginAttemptTrackerAdapter(int maxFailedAttempts, Duration blockDuration, Clock clock) {
        if (maxFailedAttempts < 1) {
            throw new IllegalArgumentException("maxFailedAttempts must be greater than zero");
        }
        if (blockDuration.isZero() || blockDuration.isNegative()) {
            throw new IllegalArgumentException("blockDuration must be greater than zero");
        }

        this.maxFailedAttempts = maxFailedAttempts;
        this.blockDuration = blockDuration;
        this.clock = clock;
    }

    @Override
    public boolean isBlocked(String identifier) {
        Instant now = clock.instant();
        AttemptState state = attempts.computeIfPresent(identifier, (key, currentState) ->
                currentState.hasExpiredAt(now, blockDuration) ? null : currentState);
        return state != null && state.isBlockedAt(now);
    }

    @Override
    public boolean recordFailure(String identifier) {
        Instant now = clock.instant();
        AttemptState state = attempts.compute(identifier, (key, currentState) -> {
            if (currentState != null && currentState.isBlockedAt(now)) {
                return currentState;
            }

            int failureCount = currentState == null
                    || currentState.hasExpiredAt(now, blockDuration)
                    || currentState.blockedUntil() != null
                    ? 1
                    : currentState.failureCount() + 1;
            Instant blockedUntil = failureCount >= maxFailedAttempts
                    ? now.plus(blockDuration)
                    : null;
            return new AttemptState(failureCount, blockedUntil, now);
        });

        return state.isBlockedAt(now);
    }

    @Override
    public void reset(String identifier) {
        attempts.remove(identifier);
    }

    private record AttemptState(int failureCount, Instant blockedUntil, Instant lastFailureAt) {
        private boolean isBlockedAt(Instant instant) {
            return blockedUntil != null && blockedUntil.isAfter(instant);
        }

        private boolean hasExpiredAt(Instant instant, Duration attemptWindow) {
            Instant expiresAt = blockedUntil != null
                    ? blockedUntil
                    : lastFailureAt.plus(attemptWindow);
            return !expiresAt.isAfter(instant);
        }
    }
}
