package dev.nvp.state.history;

import dev.nvp.util.RingBuffer;
import org.bukkit.block.BlockFace;

/** Records block placements — for Scaffold/Tower/FastPlace checks. */
public class BlockPlaceHistory {

    public static final class Placement {
        public final long t;
        public final int x, y, z;
        public final BlockFace against;
        public final float yaw, pitch;
        public final boolean sneaking;
        public Placement(long t, int x, int y, int z, BlockFace against, float yaw, float pitch, boolean sneaking) {
            this.t = t; this.x = x; this.y = y; this.z = z;
            this.against = against; this.yaw = yaw; this.pitch = pitch; this.sneaking = sneaking;
        }
    }

    private final RingBuffer<Placement> buf;

    public BlockPlaceHistory(int capacity) { this.buf = new RingBuffer<>(capacity); }

    public void push(Placement p) { buf.push(p); }
    public int size() { return buf.size(); }
    public Placement latest() { return buf.newest(); }
    public Placement previous() { return buf.size() < 2 ? null : buf.get(buf.size() - 2); }
    public RingBuffer<Placement> raw() { return buf; }

    public long lastIntervalMs() {
        Placement a = previous(), b = latest();
        return (a == null || b == null) ? Long.MAX_VALUE : (b.t - a.t);
    }

    public void clear() { buf.clear(); }
}
