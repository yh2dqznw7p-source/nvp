package dev.nvp.ml.dataset;

import dev.nvp.ml.feature.FeatureSchema;
import dev.nvp.ml.feature.FeatureVector;

/** A feature vector with a class label (0 = clean, 1 = cheat). */
public final class LabelledSample {

    private final FeatureVector features;
    private final int label;
    private final long timestampMs;
    private final String source;

    public LabelledSample(FeatureVector features, int label, long timestampMs, String source) {
        this.features = features;
        this.label = label;
        this.timestampMs = timestampMs;
        this.source = source == null ? "" : source;
    }

    public LabelledSample(FeatureVector features, int label) {
        this(features, label, System.currentTimeMillis(), "");
    }

    public double[] features() { return features.values(); }
    public FeatureVector vector() { return features; }
    public FeatureSchema schema() { return features.schema(); }
    public int label() { return label; }
    public long timestampMs() { return timestampMs; }
    public String source() { return source; }
}
