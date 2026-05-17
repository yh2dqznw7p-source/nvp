package dev.nvp.ml.dataset;

import dev.nvp.ml.feature.FeatureSchema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/** Append-only dataset of labelled samples with a fixed schema. */
public class LabelledDataset {

    private final FeatureSchema schema;
    private final List<LabelledSample> samples = new ArrayList<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public LabelledDataset(FeatureSchema schema) { this.schema = schema; }

    public FeatureSchema schema() { return schema; }
    public int featureSize() { return schema.size(); }

    public void add(LabelledSample s) {
        if (!s.schema().equalsLayout(schema))
            throw new IllegalArgumentException("Sample schema mismatch");
        lock.writeLock().lock();
        try { samples.add(s); } finally { lock.writeLock().unlock(); }
    }

    public void addAll(Collection<LabelledSample> in) {
        lock.writeLock().lock();
        try { for (LabelledSample s : in) {
            if (!s.schema().equalsLayout(schema))
                throw new IllegalArgumentException("Sample schema mismatch");
            samples.add(s);
        } } finally { lock.writeLock().unlock(); }
    }

    public int size() {
        lock.readLock().lock();
        try { return samples.size(); } finally { lock.readLock().unlock(); }
    }

    public List<LabelledSample> samples() {
        lock.readLock().lock();
        try { return Collections.unmodifiableList(new ArrayList<>(samples)); }
        finally { lock.readLock().unlock(); }
    }

    public int[] classCounts() {
        int[] c = new int[2];
        lock.readLock().lock();
        try { for (LabelledSample s : samples) {
            if (s.label() >= 0 && s.label() < 2) c[s.label()]++;
        } } finally { lock.readLock().unlock(); }
        return c;
    }

    public LabelledDataset shuffled(long seed) {
        List<LabelledSample> copy;
        lock.readLock().lock();
        try { copy = new ArrayList<>(samples); } finally { lock.readLock().unlock(); }
        Collections.shuffle(copy, new Random(seed));
        LabelledDataset out = new LabelledDataset(schema);
        out.addAll(copy);
        return out;
    }

    public void clear() {
        lock.writeLock().lock();
        try { samples.clear(); } finally { lock.writeLock().unlock(); }
    }
}
