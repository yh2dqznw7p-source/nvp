package dev.nvp.ml.training;

import dev.nvp.ml.dataset.DatasetSplit;
import dev.nvp.ml.dataset.LabelledDataset;
import dev.nvp.ml.model.Model;

/**
 * Glue: stratified split → fit → evaluate → produce TrainingReport.
 * Caller decides which Model implementation to pass in.
 */
public final class Trainer {

    private Trainer() {}

    public static TrainingReport train(Model model, LabelledDataset full, TrainingConfig cfg) {
        if (full.size() < 10)
            return TrainingReport.notEnoughData(full.size(), model.describe());

        DatasetSplit split = DatasetSplit.stratified(full, cfg.trainRatio, cfg.seed);
        long t0 = System.currentTimeMillis();
        model.fit(split.train);
        long ms = System.currentTimeMillis() - t0;

        ConfusionMatrix cmTrain = Evaluator.evaluate(model, split.train, 0.5);
        ConfusionMatrix cmTest  = Evaluator.evaluate(model, split.test , 0.5);

        return new TrainingReport(model.describe(),
                full.size(), split.train.size(), split.test.size(),
                cmTrain, cmTest, ms);
    }
}
