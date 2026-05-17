package dev.nvp.state.history;

import dev.nvp.util.RingBuffer;

/** Rolling buffer of recent model scores per check (for hologram + smoothing). */
public class ScoreHistory {

    private final RingBuffer<Double> buf;

    public ScoreHistory(int capacity) { this.buf = new RingBuffer<>(capacity); }

    public void push(double score) { buf.push(score); }
    public int size() { return buf.size(); }

    /** Exponentially weighted average — newer scores count more. */
    public double ewma(double alpha) {
        if (buf.isEmpty()) return 0;
        double s = buf.get(0);
        for (int i = 1; i < buf.size(); i++) s = alpha * buf.get(i) + (1 - alpha) * s;
        return s;
    }

    public double max() {
        double m = 0;
        for (Double d : buf) if (d > m) m = d;
        return m;
    }

    public RingBuffer<Double> raw() { return buf; }
    public void clear() { buf.clear(); }
}
