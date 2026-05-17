package dev.nvp.ml.feature;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maps feature names to indices. Pinned per check / per model so that loaded
 * datasets and trained models can validate their feature layout.
 */
public final class FeatureSchema {

    private final List<String> names;
    private final Map<String, Integer> index;

    public FeatureSchema(List<String> names) {
        this.names = List.copyOf(names);
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < this.names.size(); i++) idx.put(this.names.get(i), i);
        this.index = Collections.unmodifiableMap(idx);
    }

    public int size() { return names.size(); }
    public List<String> names() { return names; }
    public int indexOf(String name) {
        Integer i = index.get(name);
        if (i == null) throw new IllegalArgumentException("Unknown feature: " + name);
        return i;
    }
    public boolean equalsLayout(FeatureSchema other) { return names.equals(other.names); }

    public String header() { return String.join(",", names); }
}
