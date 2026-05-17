package dev.nvp.ml.training;

import dev.nvp.ml.dataset.LabelledDataset;
import dev.nvp.ml.dataset.LabelledSample;
import dev.nvp.ml.model.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/** Stratified K-fold cross-validation, averaging metrics across folds. */
public final class CrossValidator {

    private CrossValidator() {}

    public static ConfusionMatrix kFold(Supplier<Model> factory, LabelledDataset full, int k, long seed) {
        if (k < 2) throw new IllegalArgumentException("k >= 2");
        LabelledDataset shuffled = full.shuffled(seed);
        List<LabelledSample> all = shuffled.samples();
        int n = all.size();
        if (n < k) return new ConfusionMatrix(0, 0, 0, 0);

        int tp = 0, fp = 0, tn = 0, fn = 0;
        int foldSize = n / k;

        for (int f = 0; f < k; f++) {
            int testStart = f * foldSize;
            int testEnd   = (f == k - 1) ? n : testStart + foldSize;

            LabelledDataset train = new LabelledDataset(full.schema());
            LabelledDataset test  = new LabelledDataset(full.schema());
            List<LabelledSample> trainList = new ArrayList<>();
            List<LabelledSample> testList  = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                if (i >= testStart && i < testEnd) testList.add(all.get(i));
                else                                trainList.add(all.get(i));
            }
            train.addAll(trainList);
            test.addAll(testList);

            Model m = factory.get();
            m.fit(train);
            ConfusionMatrix cm = Evaluator.evaluate(m, test, 0.5);
            tp += cm.tp; fp += cm.fp; tn += cm.tn; fn += cm.fn;
        }
        return new ConfusionMatrix(tp, fp, tn, fn);
    }
}
