package dev.nvp.ml.model;

import dev.nvp.util.MathUtil;

/** Activation functions for MLP layers, with derivative wrt their output. */
public enum Activation {

    SIGMOID {
        @Override public double f(double z) { return MathUtil.sigmoid(z); }
        @Override public double dfFromOutput(double a) { return a * (1.0 - a); }
    },
    TANH {
        @Override public double f(double z) { return Math.tanh(z); }
        @Override public double dfFromOutput(double a) { return 1.0 - a * a; }
    },
    RELU {
        @Override public double f(double z) { return z > 0 ? z : 0; }
        @Override public double dfFromOutput(double a) { return a > 0 ? 1 : 0; }
    },
    LEAKY_RELU {
        @Override public double f(double z) { return z > 0 ? z : 0.01 * z; }
        @Override public double dfFromOutput(double a) { return a > 0 ? 1 : 0.01; }
    },
    LINEAR {
        @Override public double f(double z) { return z; }
        @Override public double dfFromOutput(double a) { return 1.0; }
    };

    public abstract double f(double z);
    /** dActivation/dz given the post-activation value (faster than recomputing z). */
    public abstract double dfFromOutput(double a);
}
