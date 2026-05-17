package dev.nvp.state;

import java.util.EnumMap;
import java.util.Map;

/**
 * Per-player violation level (VL) per check. VL is decayed over time and used
 * by the punishment ladder. Score → VL conversion is calibrated by check.
 */
public class ViolationManager {

    public static final class CheckVl {
        public double level;
        public long lastUpdate;
        public long lastDecay;
    }

    private final Map<PlayerState.CheckType, CheckVl> vls = new EnumMap<>(PlayerState.CheckType.class);
    private final double decayPerSecond;

    public ViolationManager(double decayPerSecond) {
        this.decayPerSecond = decayPerSecond;
        for (PlayerState.CheckType t : PlayerState.CheckType.values()) vls.put(t, new CheckVl());
    }

    /** Add VL based on probability score. score < 0.5 → no addition. */
    public double add(PlayerState.CheckType type, double score) {
        decay(type);
        if (score < 0.5) return vls.get(type).level;
        // Quadratic mapping so confident detections add disproportionately more.
        double added = Math.pow(Math.max(0, score - 0.4) * 2.0, 2.0);
        CheckVl v = vls.get(type);
        v.level += added;
        v.lastUpdate = System.currentTimeMillis();
        return v.level;
    }

    public double level(PlayerState.CheckType type) {
        decay(type);
        return vls.get(type).level;
    }

    public void clear(PlayerState.CheckType type) {
        CheckVl v = vls.get(type);
        v.level = 0; v.lastDecay = System.currentTimeMillis();
    }

    public void clearAll() { for (PlayerState.CheckType t : PlayerState.CheckType.values()) clear(t); }

    private void decay(PlayerState.CheckType type) {
        CheckVl v = vls.get(type);
        long now = System.currentTimeMillis();
        if (v.lastDecay == 0) { v.lastDecay = now; return; }
        double seconds = (now - v.lastDecay) / 1000.0;
        v.level = Math.max(0, v.level - decayPerSecond * seconds);
        v.lastDecay = now;
    }
}
