package dev.nvp.util;

import java.util.Collection;

/** Online statistics helpers. */
public final class Stats {
    private Stats() {}

    public static double mean(double[] xs) {
        if (xs.length == 0) return 0;
        double s = 0;
        for (double x : xs) s += x;
        return s / xs.length;
    }

    public static double mean(Collection<Double> xs) {
        if (xs.isEmpty()) return 0;
        double s = 0;
        for (double x : xs) s += x;
        return s / xs.size();
    }

    /** Sample standard deviation (Bessel's correction). */
    public static double std(double[] xs) {
        if (xs.length < 2) return 0;
        double mu = mean(xs);
        double s = 0;
        for (double x : xs) s += (x - mu) * (x - mu);
        return Math.sqrt(s / (xs.length - 1));
    }

    public static double variance(double[] xs) {
        if (xs.length < 2) return 0;
        double mu = mean(xs);
        double s = 0;
        for (double x : xs) s += (x - mu) * (x - mu);
        return s / (xs.length - 1);
    }

    /** Welford running variance — useful for streaming features. */
    public static final class Running {
        private long n = 0;
        private double mean = 0, m2 = 0;

        public void add(double x) {
            n++;
            double delta = x - mean;
            mean += delta / n;
            double delta2 = x - mean;
            m2 += delta * delta2;
        }
        public long count() { return n; }
        public double mean() { return mean; }
        public double variance() { return n < 2 ? 0 : m2 / (n - 1); }
        public double std() { return Math.sqrt(variance()); }
        public void reset() { n = 0; mean = 0; m2 = 0; }
    }
}
