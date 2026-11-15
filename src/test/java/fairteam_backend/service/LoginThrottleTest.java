package fairteam_backend.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

class LoginThrottleTest {
    private static final class MutableClock extends Clock {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        @Override public java.time.ZoneId getZone() { return ZoneOffset.UTC; }
        @Override public Clock withZone(java.time.ZoneId zone) { return this; }
        @Override public Instant instant() { return now; }
    }

    @Test
    void blocksAfterTooManyFailuresAndReleasesAfterTheWindow() {
        MutableClock clock = new MutableClock();
        LoginThrottle throttle = new LoginThrottle(3, Duration.ofMinutes(10), clock);
        for (int i = 0; i < 3; i++) throttle.recordFailure("a@x.test");
        assertThat(throttle.isBlocked("a@x.test")).isTrue();
        assertThat(throttle.isBlocked("b@x.test")).isFalse();
        clock.now = clock.now.plus(Duration.ofMinutes(11));
        assertThat(throttle.isBlocked("a@x.test")).isFalse();
    }

    @Test
    void aSuccessfulSignInClearsTheFailures() {
        LoginThrottle throttle = new LoginThrottle(2, Duration.ofMinutes(10), new MutableClock());
        throttle.recordFailure("a@x.test");
        throttle.recordFailure("a@x.test");
        throttle.reset("a@x.test");
        assertThat(throttle.isBlocked("a@x.test")).isFalse();
    }
}
