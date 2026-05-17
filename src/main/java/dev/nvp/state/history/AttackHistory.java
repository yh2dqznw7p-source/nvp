package dev.nvp.state.history;

import dev.nvp.util.RingBuffer;

import java.util.UUID;

/** Last melee hits — used by KillAura/Reach/MultiAura to spot patterns. */
public class AttackHistory {

    public static final class Hit {
        public final long t;
        public final UUID targetId;
        public final double reach;
        public final double aimError;
        public final float yaw, pitch;
        public Hit(long t, UUID targetId, double reach, double aimError, float yaw, float pitch) {
            this.t = t; this.targetId = targetId;
            this.reach = reach; this.aimError = aimError;
            this.yaw = yaw; this.pitch = pitch;
        }
    }

    private final RingBuffer<Hit> buf;

    public AttackHistory(int capacity) { this.buf = new RingBuffer<>(capacity); }

    public void push(Hit h) { buf.push(h); }
    public int size() { return buf.size(); }
    public Hit latest() { return buf.newest(); }
    public RingBuffer<Hit> raw() { return buf; }

    public int distinctTargets(long withinMs, long now) {
        int count = 0;
        java.util.HashSet<UUID> seen = new java.util.HashSet<>();
        for (Hit h : buf) if (now - h.t <= withinMs && seen.add(h.targetId)) count++;
        return count;
    }

    public double averageReach() {
        if (buf.isEmpty()) return 0;
        double s = 0;
        for (Hit h : buf) s += h.reach;
        return s / buf.size();
    }

    public void clear() { buf.clear(); }
}
