package ch.zhaw.www.utils;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

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
     * @param minutes number of minutes to offset
     * @return now with minutes offset
     */
    public static Instant offsetNowMinutes(int minutes) {
        return getNow().plus(minutes, ChronoUnit.MINUTES);
    }
    
    /**
     * Checks if given instant is after now
     *
     * @param instant         instant to check in timeline
     * @param offsetInMinutes instant can be offset by the passed minutes. To remove offset pass it negative
     * @return true if instant is in the future
     */
    public static boolean isAfterNow(Instant instant, int offsetInMinutes) {
        return instant != null && instant.plus(offsetInMinutes, ChronoUnit.MINUTES).isAfter(getNow());
    }
    
    private static Instant getNow() {
        return Instant.now(clock);
    }
}
