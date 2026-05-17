package dev.nvp.ml.training;

import dev.nvp.ml.dataset.LabelledDataset;
import dev.nvp.ml.dataset.LabelledSample;
import dev.nvp.ml.model.Model;

public final class Evaluator {

    private Evaluator() {}

    public static ConfusionMatrix evaluate(Model model, LabelledDataset test, double threshold) {
        int tp = 0, fp = 0, tn = 0, fn = 0;
        for (LabelledSample s : test.samples()) {
            double p = model.predictProba(s.features());
            int pred = p >= threshold ? 1 : 0;
            int y = s.label();
            if (pred == 1 && y == 1) tp++;
            else if (pred == 1 && y == 0) fp++;
            else if (pred == 0 && y == 0) tn++;
            else fn++;
        }
        return new ConfusionMatrix(tp, fp, tn, fn);
    }

    /** Sweeps thresholds and picks the one minimising FPR while keeping recall >= minRecall. */
    public static double bestThresholdForLowFpr(Model model, LabelledDataset test, double minRecall) {
        double bestT = 0.5;
        double bestFpr = Double.POSITIVE_INFINITY;
        for (double t = 0.05; t < 1.0; t += 0.05) {
            ConfusionMatrix cm = evaluate(model, test, t);
            if (cm.recall() < minRecall) continue;
            if (cm.fpr() < bestFpr) { bestFpr = cm.fpr(); bestT = t; }
        }
        return bestT;
    }
}
