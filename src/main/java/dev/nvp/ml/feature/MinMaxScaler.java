package dev.nvp.ml.feature;

import dev.nvp.ml.dataset.LabelledDataset;
import dev.nvp.ml.dataset.LabelledSample;

/** Maps each feature into [0,1] linearly. */
public class MinMaxScaler implements Normalizer {

    private double[] min, max;
    private boolean fitted;

    @Override
    public void fit(LabelledDataset dataset) {
        int d = dataset.featureSize();
        min = new double[d];
        max = new double[d];
        for (int i = 0; i < d; i++) { min[i] = Double.POSITIVE_INFINITY; max[i] = Double.NEGATIVE_INFINITY; }
        for (LabelledSample s : dataset.samples()) {
            double[] x = s.features();
            for (int i = 0; i < d; i++) {
                if (x[i] < min[i]) min[i] = x[i];
                if (x[i] > max[i]) max[i] = x[i];
            }
        }
        for (int i = 0; i < d; i++) {
            if (!Double.isFinite(min[i]) || !Double.isFinite(max[i]) || max[i] - min[i] < 1e-9) {
                min[i] = 0; max[i] = 1;
            }
        }
        fitted = true;
    }

    @Override
    public double[] transform(double[] x) {
        if (!fitted) return x.clone();
        double[] out = new double[x.length];
        for (int i = 0; i < x.length; i++) out[i] = (x[i] - min[i]) / (max[i] - min[i]);
        return out;
    }

    @Override public boolean isFitted() { return fitted; }
    @Override public String describe() { return "MinMaxScaler"; }
}
