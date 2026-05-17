package dev.nvp.ml.feature;

import java.util.Arrays;

/**
 * Immutable feature vector. Keeps a reference to the schema so model training,
 * persistence and debugging can correlate values to names.
 */
public final class FeatureVector {

    private final double[] values;
    private final FeatureSchema schema;

    public FeatureVector(FeatureSchema schema, double[] values) {
        if (values.length != schema.size())
            throw new IllegalArgumentException("Length mismatch: " + values.length + " vs schema " + schema.size());
        this.schema = schema;
        this.values = values;
    }

    public double[] values() { return values; }
    public int size() { return values.length; }
    public FeatureSchema schema() { return schema; }
    public double get(int i) { return values[i]; }
    public double get(String name) { return values[schema.indexOf(name)]; }

    public FeatureVector copy() { return new FeatureVector(schema, values.clone()); }
    public double[] toArray() { return values.clone(); }

    @Override public String toString() {
        return "FeatureVector" + Arrays.toString(values);
    }
}
