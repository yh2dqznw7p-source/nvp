package dev.nvp.ml.feature;

import dev.nvp.ml.dataset.LabelledDataset;
import dev.nvp.ml.dataset.LabelledSample;

/** Z-score normalization: (x - μ) / σ per feature. */
public class StandardScaler implements Normalizer {

    private double[] mean;
    private double[] std;
    private boolean fitted;

    @Override
    public void fit(LabelledDataset dataset) {
        int n = dataset.size();
        if (n == 0) throw new IllegalStateException("Cannot fit on empty dataset");
        int d = dataset.featureSize();
        mean = new double[d];
        std  = new double[d];

        for (LabelledSample s : dataset.samples()) {
            double[] x = s.features();
            for (int i = 0; i < d; i++) mean[i] += x[i];
        }
        for (int i = 0; i < d; i++) mean[i] /= n;

        for (LabelledSample s : dataset.samples()) {
            double[] x = s.features();
            for (int i = 0; i < d; i++) {
                double dv = x[i] - mean[i];
                std[i] += dv * dv;
            }
        }
        for (int i = 0; i < d; i++) {
            std[i] = Math.sqrt(std[i] / Math.max(1, n - 1));
            if (std[i] < 1e-9) std[i] = 1.0; // constant feature → no rescaling
        }
        fitted = true;
    }

    @Override
    public double[] transform(double[] x) {
        if (!fitted) return x.clone();
        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) out[i] = (x[i] - mean[i]) / std[i];
        return out;
    }

    @Override public boolean isFitted() { return fitted; }
    @Override public String describe() { return "StandardScaler(" + (fitted ? mean.length + "d" : "unfit") + ")"; }

    public double[] mean() { return mean; }
    public double[] std()  { return std; }

    public void load(double[] mean, double[] std) {
        if (mean.length != std.length) throw new IllegalArgumentException();
        this.mean = mean.clone();
        this.std  = std.clone();
        this.fitted = true;
    }
}
