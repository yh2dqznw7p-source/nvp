package dev.nvp.ml.model;

import dev.nvp.ml.dataset.LabelledDataset;
import dev.nvp.ml.dataset.LabelledSample;
import dev.nvp.ml.feature.Normalizer;

import java.util.PriorityQueue;

/**
 * k-NN classifier with optional Normalizer and inverse-distance vote weighting.
 * Stores training samples in raw form; transforms on query/distance.
 *
 *   p(cheat | x) = Σ w_i · 1[label_i = 1] / Σ w_i ,  w_i = 1 / (1e-6 + d_i)
 *
 * Fast enough for ~50k samples on a single core; for larger sets we'll bolt on
 * a KD-tree or move to MLP/ensemble.
 */
public class WeightedKnnModel implements Model {

    private final int k;
    private final Normalizer normalizer;
    private double[][] xs;       // normalized
    private int[]      ys;
    private boolean trained;

    public WeightedKnnModel(int k, Normalizer normalizer) {
        if (k < 1) throw new IllegalArgumentException("k must be >= 1");
        this.k = k;
        this.normalizer = normalizer;
    }

    @Override
    public void fit(LabelledDataset dataset) {
        int n = dataset.size();
        if (n == 0) { trained = false; return; }
        if (normalizer != null && !normalizer.isFitted()) normalizer.fit(dataset);

        xs = new double[n][];
        ys = new int[n];
        int i = 0;
        for (LabelledSample s : dataset.samples()) {
            xs[i] = normalizer == null ? s.features().clone() : normalizer.transform(s.features());
            ys[i] = s.label();
            i++;
        }
        trained = true;
    }

    @Override
    public double predictProba(double[] features) {
        if (!trained || xs.length < k) return 0.0;
        double[] q = normalizer == null ? features : normalizer.transform(features);

        // max-heap by distance, kept at size k
        PriorityQueue<double[]> heap = new PriorityQueue<>((a, b) -> Double.compare(b[0], a[0]));
        for (int i = 0; i < xs.length; i++) {
            double d = euclidean(q, xs[i]);
            if (heap.size() < k) heap.offer(new double[]{d, ys[i]});
            else if (d < heap.peek()[0]) { heap.poll(); heap.offer(new double[]{d, ys[i]}); }
        }
        double cheatW = 0, totalW = 0;
        for (double[] e : heap) {
            double w = 1.0 / (1e-6 + e[0]);
            totalW += w;
            if (e[1] == 1.0) cheatW += w;
        }
        return totalW == 0 ? 0.0 : cheatW / totalW;
    }

    @Override public boolean isTrained() { return trained; }
    @Override public ModelType type() { return ModelType.KNN; }
    @Override public String describe() {
        return "WeightedKnn(k=" + k + ", n=" + (xs == null ? 0 : xs.length) + ")";
    }

    private static double euclidean(double[] a, double[] b) {
        double s = 0;
        for (int i = 0; i < a.length; i++) { double d = a[i] - b[i]; s += d * d; }
        return Math.sqrt(s);
    }
}
