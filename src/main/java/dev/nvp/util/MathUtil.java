package dev.nvp.util;

public final class MathUtil {
    private MathUtil() {}

    public static double clamp(double v, double lo, double hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
    public static int clamp(int v, int lo, int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
    public static double sigmoid(double x) {
        if (x >= 0) { double z = Math.exp(-x); return 1.0 / (1.0 + z); }
        double z = Math.exp(x); return z / (1.0 + z);
    }
    public static double tanh(double x) { return Math.tanh(x); }
    public static double relu(double x) { return x > 0 ? x : 0; }
    public static double lerp(double a, double b, double t) { return a + (b - a) * t; }
    public static double sq(double x) { return x * x; }
    public static double safeDiv(double a, double b, double fallback) {
        return Math.abs(b) < 1e-12 ? fallback : a / b;
    }
}
