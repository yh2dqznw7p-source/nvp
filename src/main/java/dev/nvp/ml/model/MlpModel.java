package dev.nvp.ml.model;

import dev.nvp.ml.dataset.LabelledDataset;
import dev.nvp.ml.dataset.LabelledSample;
import dev.nvp.ml.feature.Normalizer;
import dev.nvp.util.MathUtil;

import java.util.Random;

/**
 * Multi-layer perceptron, trained by mini-batch SGD with backprop on BCE loss.
 * Hidden activations configurable; output is a single sigmoid neuron giving
 * P(cheat). Glorot/Xavier-uniform weight initialisation.
 *
 * This is a from-scratch implementation, no external libraries.
 */
public class MlpModel implements Model {

    private final int[] layerSizes;        // [in, hidden..., 1]
    private final Activation hiddenAct;
    private final Activation outputAct;
    private final Normalizer normalizer;
    private final double learningRate;
    private final double l2;
    private final int    epochs;
    private final int    batchSize;
    private final long   seed;

    private double[][][] W;                // W[layer][out][in]
    private double[][]   b;                // b[layer][out]
    private boolean trained;

    public MlpModel(int[] layerSizes, Activation hiddenAct, Normalizer normalizer,
                    double learningRate, double l2, int epochs, int batchSize, long seed) {
        if (layerSizes.length < 2) throw new IllegalArgumentException("Need >=2 layers");
        if (layerSizes[layerSizes.length - 1] != 1) throw new IllegalArgumentException("Output layer must be 1");
        this.layerSizes = layerSizes.clone();
        this.hiddenAct = hiddenAct;
        this.outputAct = Activation.SIGMOID;
        this.normalizer = normalizer;
        this.learningRate = learningRate;
        this.l2 = l2;
        this.epochs = epochs;
        this.batchSize = Math.max(1, batchSize);
        this.seed = seed;
    }

    @Override
    public void fit(LabelledDataset dataset) {
        if (dataset.size() == 0) { trained = false; return; }
        if (normalizer != null && !normalizer.isFitted()) normalizer.fit(dataset);
        Random r = new Random(seed);
        initWeights(r);

        int n = dataset.size();
        int[] perm = new int[n];
        for (int i = 0; i < n; i++) perm[i] = i;
        LabelledSample[] all = dataset.samples().toArray(new LabelledSample[0]);

        for (int epoch = 0; epoch < epochs; epoch++) {
            shuffle(perm, r);
            for (int start = 0; start < n; start += batchSize) {
                int end = Math.min(n, start + batchSize);
                trainBatch(all, perm, start, end);
            }
        }
        trained = true;
    }

    private void initWeights(Random r) {
        int L = layerSizes.length - 1;
        W = new double[L][][];
        b = new double[L][];
        for (int l = 0; l < L; l++) {
            int in = layerSizes[l], out = layerSizes[l + 1];
            double limit = Math.sqrt(6.0 / (in + out));
            W[l] = new double[out][in];
            b[l] = new double[out];
            for (int o = 0; o < out; o++)
                for (int i = 0; i < in; i++)
                    W[l][o][i] = (r.nextDouble() * 2 - 1) * limit;
        }
    }

    private void shuffle(int[] a, Random r) {
        for (int i = a.length - 1; i > 0; i--) {
            int j = r.nextInt(i + 1);
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }

    private void trainBatch(LabelledSample[] all, int[] perm, int start, int end) {
        int L = W.length;
        double[][][] dW = new double[L][][];
        double[][]   dB = new double[L][];
        for (int l = 0; l < L; l++) { dW[l] = new double[W[l].length][W[l][0].length]; dB[l] = new double[b[l].length]; }

        int batch = end - start;
        for (int idx = start; idx < end; idx++) {
            LabelledSample s = all[perm[idx]];
            double[] x = normalizer == null ? s.features() : normalizer.transform(s.features());

            // forward
            double[][] activations = new double[L + 1][];
            activations[0] = x;
            for (int l = 0; l < L; l++) {
                double[] aIn = activations[l];
                double[] aOut = new double[W[l].length];
                Activation act = (l == L - 1) ? outputAct : hiddenAct;
                for (int o = 0; o < aOut.length; o++) {
                    double z = b[l][o];
                    double[] row = W[l][o];
                    for (int i = 0; i < aIn.length; i++) z += row[i] * aIn[i];
                    aOut[o] = act.f(z);
                }
                activations[l + 1] = aOut;
            }

            // backward
            double y  = s.label();
            double p  = activations[L][0];
            // dL/dz_out for sigmoid + BCE simplifies to (p - y)
            double[] delta = new double[]{ p - y };

            for (int l = L - 1; l >= 0; l--) {
                double[] aIn  = activations[l];
                double[] aOut = activations[l + 1];
                int outDim = W[l].length;
                int inDim  = W[l][0].length;

                for (int o = 0; o < outDim; o++) {
                    double d = delta[o];
                    dB[l][o] += d;
                    for (int i = 0; i < inDim; i++) dW[l][o][i] += d * aIn[i];
                }

                if (l > 0) {
                    double[] prevDelta = new double[inDim];
                    Activation prevAct = (l - 1 == L - 1) ? outputAct : hiddenAct;
                    for (int i = 0; i < inDim; i++) {
                        double acc = 0;
                        for (int o = 0; o < outDim; o++) acc += W[l][o][i] * delta[o];
                        prevDelta[i] = acc * prevAct.dfFromOutput(aIn[i]);
                    }
                    delta = prevDelta;
                }
            }
        }

        // apply averaged gradient + L2
        double scale = learningRate / batch;
        for (int l = 0; l < L; l++) {
            for (int o = 0; o < W[l].length; o++) {
                b[l][o] -= scale * dB[l][o];
                for (int i = 0; i < W[l][0].length; i++) {
                    double g = dW[l][o][i] + l2 * W[l][o][i];
                    W[l][o][i] -= scale * g;
                }
            }
        }
    }

    @Override
    public double predictProba(double[] features) {
        if (!trained) return 0.0;
        double[] a = normalizer == null ? features : normalizer.transform(features);
        for (int l = 0; l < W.length; l++) {
            double[] out = new double[W[l].length];
            Activation act = (l == W.length - 1) ? outputAct : hiddenAct;
            for (int o = 0; o < out.length; o++) {
                double z = b[l][o];
                double[] row = W[l][o];
                for (int i = 0; i < a.length; i++) z += row[i] * a[i];
                out[o] = act.f(z);
            }
            a = out;
        }
        return MathUtil.clamp(a[0], 0, 1);
    }

    @Override public boolean isTrained() { return trained; }
    @Override public ModelType type() { return ModelType.MLP; }
    @Override public String describe() {
        StringBuilder sb = new StringBuilder("MLP[");
        for (int i = 0; i < layerSizes.length; i++) { if (i > 0) sb.append("-"); sb.append(layerSizes[i]); }
        return sb.append(", ").append(hiddenAct).append("]").toString();
    }

    public double[][][] weights() { return W; }
    public double[][]   biases()  { return b; }
}
