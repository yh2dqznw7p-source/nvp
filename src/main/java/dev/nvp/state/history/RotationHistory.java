package dev.nvp.state.history;

import dev.nvp.util.RingBuffer;
import dev.nvp.util.RotationUtil;

/** Recent yaw/pitch samples + derived deltas for rotation-based checks. */
public class RotationHistory {

    public static final class Sample {
        public final long t;
        public final float yaw;
        public final float pitch;
        public Sample(long t, float yaw, float pitch) { this.t = t; this.yaw = yaw; this.pitch = pitch; }
    }

    private final RingBuffer<Sample> buf;

    public RotationHistory(int capacity) {
        this.buf = new RingBuffer<>(capacity);
    }

    public void push(long timeMs, float yaw, float pitch) { buf.push(new Sample(timeMs, yaw, pitch)); }

    public Sample latest() { return buf.newest(); }
    public int size() { return buf.size(); }
    public RingBuffer<Sample> raw() { return buf; }

    public double[] yawDeltas() {
        if (buf.size() < 2) return new double[0];
        double[] out = new double[buf.size() - 1];
        for (int i = 1; i < buf.size(); i++)
            out[i - 1] = RotationUtil.yawDelta(buf.get(i).yaw, buf.get(i - 1).yaw);
        return out;
    }

    public double[] pitchDeltas() {
        if (buf.size() < 2) return new double[0];
        double[] out = new double[buf.size() - 1];
        for (int i = 1; i < buf.size(); i++)
            out[i - 1] = buf.get(i).pitch - buf.get(i - 1).pitch;
        return out;
    }

    public double yawAcceleration() {
        double[] d = yawDeltas();
        if (d.length < 2) return 0;
        return d[d.length - 1] - d[d.length - 2];
    }

    public void clear() { buf.clear(); }
}
