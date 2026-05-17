package dev.nvp.state.history;

import dev.nvp.util.RingBuffer;

/** Tracks pending knockback velocities for Velocity check. */
public class VelocityHistory {

    public static final class Pending {
        public final long sentAt;
        public final double dx, dy, dz;
        public double observedDx, observedDy, observedDz;
        public boolean responded;

        public Pending(long sentAt, double dx, double dy, double dz) {
            this.sentAt = sentAt; this.dx = dx; this.dy = dy; this.dz = dz;
        }

        public double horizontalApplied() {
            double expected = Math.sqrt(dx * dx + dz * dz);
            double actual   = Math.sqrt(observedDx * observedDx + observedDz * observedDz);
            return expected < 1e-9 ? 1.0 : Math.min(2.0, actual / expected);
        }
    }

    private final RingBuffer<Pending> buf;

    public VelocityHistory(int capacity) { this.buf = new RingBuffer<>(capacity); }

    public void issue(long t, double dx, double dy, double dz) { buf.push(new Pending(t, dx, dy, dz)); }
    public Pending latest() { return buf.newest(); }
    public RingBuffer<Pending> raw() { return buf; }
    public void clear() { buf.clear(); }
}
