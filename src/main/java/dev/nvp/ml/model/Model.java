package dev.nvp.ml.model;

import dev.nvp.ml.dataset.LabelledDataset;

/**
 * Common contract for all classifiers.
 * Output is calibrated probability of class 1 (cheat) in [0,1].
 */
public interface Model {

    /** Learn parameters from labelled data. May be expensive — call off-thread. */
    void fit(LabelledDataset dataset);

    /** Probability the sample belongs to class 1 (cheat). */
    double predictProba(double[] features);

    /** Hard label (0/1) — implementation may threshold at 0.5 by default. */
    default int predict(double[] features) { return predictProba(features) >= 0.5 ? 1 : 0; }

    boolean isTrained();
    ModelType type();
    String describe();
}
