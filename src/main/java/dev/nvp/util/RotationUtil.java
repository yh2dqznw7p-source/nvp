package dev.nvp.util;

public final class RotationUtil {
    private RotationUtil() {}

    /** Wraps yaw to [-180, 180]. */
    public static double wrapDegrees(double a) {
        a = a % 360;
        if (a >= 180)  a -= 360;
        if (a < -180) a += 360;
        return a;
    }

    /** Smallest signed yaw delta between two angles, accounting for wrap-around. */
    public static double yawDelta(double now, double prev) {
        return wrapDegrees(now - prev);
    }

    public static double pitchDelta(double now, double prev) {
        return now - prev;
    }

    /** Sensitivity → GCD multiplier (Minecraft client formula). */
    public static double gcdFor(double sensitivity) {
        double f = sensitivity * 0.6 + 0.2;
        return f * f * f * 8.0;
    }

    /** Greatest common divisor approximation in floating point — used to detect
     *  client-side rotation snapping (modulo of yaw delta over a window). */
    public static double approxGcd(double[] deltas) {
        double g = 0;
        for (double d : deltas) {
            d = Math.abs(d);
            if (d < 1e-6) continue;
            g = g == 0 ? d : gcd(g, d);
        }
        return g;
    }
    private static double gcd(double a, double b) {
        while (b > 1e-6) { double t = a % b; a = b; b = t; }
        return a;
    }
}
