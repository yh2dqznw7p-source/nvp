package dev.nvp.util;

public final class TimingUtil {
    private TimingUtil() {}

    public static long ticksToMs(int ticks) { return ticks * 50L; }
    public static int  msToTicks(long ms)   { return (int) (ms / 50L); }

    public static long nowMs() { return System.currentTimeMillis(); }

    public static boolean elapsed(long sinceMs, long durationMs) {
        return nowMs() - sinceMs >= durationMs;
    }
}
