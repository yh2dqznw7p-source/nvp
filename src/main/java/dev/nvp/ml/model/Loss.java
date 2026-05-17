package dev.nvp.ml.model;

public enum Loss {
    /** Binary cross-entropy. y in {0,1}, p in (0,1). */
    BCE {
        @Override public double value(double y, double p) {
            p = Math.max(1e-9, Math.min(1 - 1e-9, p));
            return -(y * Math.log(p) + (1 - y) * Math.log(1 - p));
        }
        @Override public double gradWrtOutput(double y, double p) {
            p = Math.max(1e-9, Math.min(1 - 1e-9, p));
            return (p - y) / (p * (1 - p));
        }
    },
    MSE {
        @Override public double value(double y, double p) { double d = p - y; return 0.5 * d * d; }
        @Override public double gradWrtOutput(double y, double p) { return p - y; }
    };

    public abstract double value(double y, double p);
    public abstract double gradWrtOutput(double y, double p);
}
