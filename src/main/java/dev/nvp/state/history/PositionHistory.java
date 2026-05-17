package dev.nvp.state.history;

import dev.nvp.util.RingBuffer;
import org.bukkit.Location;

/** Recent position snapshots for movement checks. */
public class PositionHistory {

    public static final class Sample {
        public final long t;
        public final double x, y, z;
        public final boolean onGround;
        public final boolean inLiquid;
        public final boolean climbing;
        public Sample(long t, Location loc, boolean onGround, boolean inLiquid, boolean climbing) {
            this.t = t; this.x = loc.getX(); this.y = loc.getY(); this.z = loc.getZ();
            this.onGround = onGround; this.inLiquid = inLiquid; this.climbing = climbing;
        }
        public double distXZ(Sample o) {
            double dx = x - o.x, dz = z - o.z;
            return Math.sqrt(dx * dx + dz * dz);
        }
        public double dy(Sample o) { return y - o.y; }
    }

    private final RingBuffer<Sample> buf;

    public PositionHistory(int capacity) { this.buf = new RingBuffer<>(capacity); }

    public void push(long t, Location loc, boolean onGround, boolean inLiquid, boolean climbing) {
        buf.push(new Sample(t, loc, onGround, inLiquid, climbing));
    }

    public int size() { return buf.size(); }
    public Sample latest() { return buf.newest(); }
    public Sample previous() { return buf.size() < 2 ? null : buf.get(buf.size() - 2); }

    /** Horizontal speed (blocks/tick) between the last two samples. */
    public double lastHorizontalSpeed() {
        Sample a = previous(), b = latest();
        if (a == null || b == null) return 0;
        return b.distXZ(a);
    }

    public double averageHorizontalSpeed(int n) {
        if (buf.size() < 2) return 0;
        int from = Math.max(1, buf.size() - n);
        double s = 0; int count = 0;
        for (int i = from; i < buf.size(); i++) { s += buf.get(i).distXZ(buf.get(i - 1)); count++; }
        return count == 0 ? 0 : s / count;
    }

    public RingBuffer<Sample> raw() { return buf; }
    public void clear() { buf.clear(); }
}
