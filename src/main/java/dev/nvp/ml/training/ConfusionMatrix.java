package dev.nvp.ml.training;

/** Standard binary confusion matrix + derived metrics. */
public final class ConfusionMatrix {

    public final int tp;
    public final int fp;
    public final int tn;
    public final int fn;

    public ConfusionMatrix(int tp, int fp, int tn, int fn) {
        this.tp = tp; this.fp = fp; this.tn = tn; this.fn = fn;
    }

    public int total() { return tp + fp + tn + fn; }

    public double accuracy() {
        int t = total();
        return t == 0 ? 0 : (tp + tn) / (double) t;
    }
    public double precision() { return tp + fp == 0 ? 0 : tp / (double) (tp + fp); }
    public double recall()    { return tp + fn == 0 ? 0 : tp / (double) (tp + fn); }
    public double f1() {
        double p = precision(), r = recall();
        return p + r == 0 ? 0 : 2 * p * r / (p + r);
    }
    /** False-positive rate — critical for an anti-cheat (do not flag clean players). */
    public double fpr() { return fp + tn == 0 ? 0 : fp / (double) (fp + tn); }

    @Override public String toString() {
        return String.format("CM(tp=%d fp=%d tn=%d fn=%d acc=%.3f prec=%.3f rec=%.3f f1=%.3f fpr=%.3f)",
                tp, fp, tn, fn, accuracy(), precision(), recall(), f1(), fpr());
    }
}
