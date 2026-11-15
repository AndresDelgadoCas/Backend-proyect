package fairteam_backend.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/** Blocks an email after too many failed sign-ins in a short window (in memory, per instance). */
public class LoginThrottle {
    private final int maxFailures;
    private final Duration window;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

    public LoginThrottle() { this(5, Duration.ofMinutes(15), Clock.systemUTC()); }

    public LoginThrottle(int maxFailures, Duration window, Clock clock) {
        this.maxFailures = maxFailures;
        this.window = window;
        this.clock = clock;
    }

    public boolean isBlocked(String key) {
        Deque<Instant> recent = failures.get(key);
        if (recent == null) return false;
        synchronized (recent) {
            prune(recent);
            return recent.size() >= maxFailures;
        }
    }

    public void recordFailure(String key) {
        Deque<Instant> recent = failures.computeIfAbsent(key, k -> new ArrayDeque<>());
        synchronized (recent) {
            prune(recent);
            recent.addLast(clock.instant());
        }
    }

    public void reset(String key) { failures.remove(key); }

    private void prune(Deque<Instant> recent) {
        Instant limit = clock.instant().minus(window);
        while (!recent.isEmpty() && recent.peekFirst().isBefore(limit)) recent.removeFirst();
    }
}
