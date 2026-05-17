package dev.nvp.ml.training;

/** Hyper-parameters for Trainer. Pure data, copied to immutable fields. */
public final class TrainingConfig {

    public final double learningRate;
    public final double l2;
    public final int epochs;
    public final int batchSize;
    public final double trainRatio;
    public final long seed;

    public TrainingConfig(double learningRate, double l2, int epochs,
                          int batchSize, double trainRatio, long seed) {
        this.learningRate = learningRate;
        this.l2 = l2;
        this.epochs = epochs;
        this.batchSize = batchSize;
        this.trainRatio = trainRatio;
        this.seed = seed;
    }

    public static TrainingConfig defaults() {
        return new TrainingConfig(0.05, 1e-4, 80, 32, 0.8, 42L);
    }
}
