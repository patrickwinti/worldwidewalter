package ch.zhaw.www;

import ch.zhaw.www.utils.InstantWrapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class TimeHelper {
    
    private static final Instant INSTANT = Instant.parse("2022-12-22T10:00:00Z");
    private static final Clock FIXED_CLOCK = Clock.fixed(INSTANT, ZoneId.of("UTC"));
    
    public static Instant getFixedClockInstant() {
        return INSTANT;
    }
    
    public static void enableFixedClocked() {
        InstantWrapper.clock = FIXED_CLOCK;
    }
    
    public static void disableFixedClocked() {
        InstantWrapper.clock = Clock.systemUTC();
    }
    
    public static void offsetFixedClockBy(Duration offset) {
        InstantWrapper.clock = Clock.offset(FIXED_CLOCK, offset);
    }
}
