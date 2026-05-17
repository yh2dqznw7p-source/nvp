package dev.nvp.ml;

import java.util.List;
import java.util.PriorityQueue;

/**
 * Simple k-Nearest-Neighbours classifier over HitSample feature vectors.
 * Returns probability of class "cheat" (label = 1) for the query sample.
 *
 * Notes:
 *  - O(N) per prediction. Fine for thousands of samples; replace with a KD-tree
 *    or a real model (MLP / LightGBM via JNI / external service) if N grows.
 *  - With <k samples or no cheat samples yet, returns 0.0 (don't false-flag).
 */
public class KnnModel {

    private final Dataset dataset;
    private final int k;

    public KnnModel(Dataset dataset, int k) {
        this.dataset = dataset;
        this.k = Math.max(1, k);
    }

    public double cheatProbability(HitSample query) {
        List<HitSample> all = dataset.all();
        if (all.size() < k) return 0.0;

        // max-heap by distance, kept at size k
        PriorityQueue<double[]> heap = new PriorityQueue<>(
            (a, b) -> Double.compare(b[0], a[0]));

        double[] q = query.features();
        for (HitSample s : all) {
            double d = euclidean(q, s.features());
            if (heap.size() < k) {
                heap.offer(new double[]{d, s.label()});
            } else if (d < heap.peek()[0]) {
                heap.poll();
                heap.offer(new double[]{d, s.label()});
            }
        }

        // Distance-weighted vote (closer neighbours count more).
        double cheatWeight = 0, totalWeight = 0;
        for (double[] e : heap) {
            double w = 1.0 / (1e-6 + e[0]);
            totalWeight += w;
            if (e[1] == 1.0) cheatWeight += w;
        }
        return totalWeight == 0 ? 0.0 : cheatWeight / totalWeight;
    }

    private static double euclidean(double[] a, double[] b) {
        double sum = 0;
        for (int i = 0; i < a.length; i++) {
            double d = a[i] - b[i];
            sum += d * d;
        }
        return Math.sqrt(sum);
    }
}
