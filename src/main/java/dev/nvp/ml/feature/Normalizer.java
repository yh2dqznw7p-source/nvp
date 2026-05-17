package dev.nvp.ml.feature;

import dev.nvp.ml.dataset.LabelledDataset;

/**
 * Per-feature scaler. fit() learns parameters on a dataset; transform() applies
 * them to a single vector. Implementations are stateless once fit.
 */
public interface Normalizer {

    void fit(LabelledDataset dataset);
    double[] transform(double[] x);
    boolean isFitted();
    String describe();
}
