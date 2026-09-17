package vn.talentbridge.adapter.out.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryLoginAttemptTrackerAdapterTest {

    @Test
    void blocksAtThresholdAndAutomaticallyExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-09-17T00:00:00Z"));
        InMemoryLoginAttemptTrackerAdapter tracker =
                new InMemoryLoginAttemptTrackerAdapter(3, Duration.ofMinutes(1), clock);

        assertFalse(tracker.recordFailure("user@example.com"));
        assertFalse(tracker.recordFailure("user@example.com"));
        assertTrue(tracker.recordFailure("user@example.com"));
        assertTrue(tracker.isBlocked("user@example.com"));

        clock.advance(Duration.ofMinutes(1));

        assertFalse(tracker.isBlocked("user@example.com"));
        assertFalse(tracker.recordFailure("user@example.com"));
    }

    @Test
    void successfulLoginResetClearsPreviousFailures() {
        InMemoryLoginAttemptTrackerAdapter tracker =
                new InMemoryLoginAttemptTrackerAdapter(
                        2,
                        Duration.ofMinutes(1),
                        Clock.fixed(Instant.parse("2026-09-17T00:00:00Z"), ZoneId.of("UTC")));

        assertFalse(tracker.recordFailure("user@example.com"));
        tracker.reset("user@example.com");
        assertFalse(tracker.recordFailure("user@example.com"));
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
