package dev.nvp.ml.dataset;

import dev.nvp.util.Stats;

/** Cheap descriptive stats for an admin /nvp info command. */
public final class DatasetStats {

    public final int total;
    public final int clean;
    public final int cheat;
    public final double[] mean;
    public final double[] std;

    public DatasetStats(int total, int clean, int cheat, double[] mean, double[] std) {
        this.total = total; this.clean = clean; this.cheat = cheat;
        this.mean = mean;   this.std = std;
    }

    public static DatasetStats of(LabelledDataset ds) {
        int n = ds.size();
        int d = ds.featureSize();
        double[] mean = new double[d];
        if (n == 0) return new DatasetStats(0, 0, 0, mean, new double[d]);

        for (LabelledSample s : ds.samples()) {
            double[] x = s.features();
            for (int i = 0; i < d; i++) mean[i] += x[i];
        }
        for (int i = 0; i < d; i++) mean[i] /= n;

        double[][] cols = new double[d][n];
        int row = 0;
        for (LabelledSample s : ds.samples()) {
            double[] x = s.features();
            for (int i = 0; i < d; i++) cols[i][row] = x[i];
            row++;
        }
        double[] std = new double[d];
        for (int i = 0; i < d; i++) std[i] = Stats.std(cols[i]);

        int[] cc = ds.classCounts();
        return new DatasetStats(n, cc[0], cc[1], mean, std);
    }
}
