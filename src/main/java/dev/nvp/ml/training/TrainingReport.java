package dev.nvp.ml.training;

public final class TrainingReport {

    public final String modelDescribe;
    public final int totalSamples;
    public final int trainSamples;
    public final int testSamples;
    public final ConfusionMatrix train;
    public final ConfusionMatrix test;
    public final long trainTimeMs;
    public final boolean successful;

    public TrainingReport(String modelDescribe, int total, int train, int test,
                          ConfusionMatrix cmTrain, ConfusionMatrix cmTest, long ms) {
        this.modelDescribe = modelDescribe;
        this.totalSamples = total;
        this.trainSamples = train;
        this.testSamples = test;
        this.train = cmTrain;
        this.test = cmTest;
        this.trainTimeMs = ms;
        this.successful = total >= 10;
    }

    public static TrainingReport notEnoughData(int n, String desc) {
        return new TrainingReport(desc, n, 0, 0,
            new ConfusionMatrix(0, 0, 0, 0),
            new ConfusionMatrix(0, 0, 0, 0), 0L);
    }

    public String summary() {
        if (!successful) return "Not enough data: " + totalSamples + " samples";
        return String.format("%s | n=%d (train=%d, test=%d) | acc=%.3f f1=%.3f fpr=%.3f | %dms",
            modelDescribe, totalSamples, trainSamples, testSamples,
            test.accuracy(), test.f1(), test.fpr(), trainTimeMs);
    }

    @Override public String toString() { return summary(); }
}
