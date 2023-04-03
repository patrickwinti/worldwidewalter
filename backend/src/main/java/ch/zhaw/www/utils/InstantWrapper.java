package ch.zhaw.www.utils;

import jakarta.validation.constraints.NotNull;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Helper class for tests. Per default, it takes UTC timezone.
 */
public class InstantWrapper {
    public static Clock clock = Clock.systemUTC();
    
    private InstantWrapper() {
    }
    
    /**
     * Offsets now by given minutes.
     *
     * @param duration offset from now
     * @return now with minutes offset
     */
    public static Instant offsetNow(@NotNull Duration duration) {
        return getNow().plus(duration);
    }
    
    /**
     * Checks if given instant is after now
     *
     * @param instant instant to check in timeline
     * @return true if instant is in the future
     */
    public static boolean isAfterNow(@NotNull Instant instant) {
        return instant.isAfter(getNow());
    }
    
    private static Instant getNow() {
        return Instant.now(clock);
    }
}
