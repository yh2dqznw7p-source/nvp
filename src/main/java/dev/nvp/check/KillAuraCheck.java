package dev.nvp.check;

import dev.nvp.ml.HitSample;
import dev.nvp.ml.KnnModel;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * KillAura check: builds a HitSample from melee event geometry/timing and asks
 * KnnModel for cheat probability. Stateless toward the dataset — recording is
 * handled by CombatListener via PlayerState.Recording.
 */
public class KillAuraCheck {

    private final KnnModel model;

    /** Per-player rolling click timestamps (ms) — for CPS. */
    private final Map<UUID, long[]> clickRing = new HashMap<>();
    /** Last yaw/pitch per player. */
    private final Map<UUID, float[]> lastLook = new HashMap<>();

    public KillAuraCheck(KnnModel model) { this.model = model; }

    public HitSample buildSample(Player attacker, LivingEntity target, int label) {
        Location a = attacker.getLocation();
        Location t = target.getLocation();

        float[] last = lastLook.get(attacker.getUniqueId());
        double yawDelta = last == null ? 0 : Math.abs(angleDelta(a.getYaw(), last[0]));
        double pitDelta = last == null ? 0 : Math.abs(a.getPitch() - last[1]);
        lastLook.put(attacker.getUniqueId(), new float[]{a.getYaw(), a.getPitch()});

        Vector look = a.getDirection();
        Vector toTarget = t.toVector().subtract(a.toVector()).normalize();
        double dot = Math.max(-1, Math.min(1, look.dot(toTarget)));
        double aimError = Math.toDegrees(Math.acos(dot));

        double reach = a.toVector().setY(0).distance(t.toVector().setY(0));
        double cps   = recordClickAndCps(attacker.getUniqueId());

        return new HitSample(
            yawDelta, pitDelta, aimError, reach, cps,
            attacker.isSprinting() ? 1 : 0,
            attacker.isSneaking()  ? 1 : 0,
            a.getY() - t.getY(),
            label
        );
    }

    public double predict(HitSample s) { return model.cheatProbability(s); }

    private static double angleDelta(float a, float b) {
        double d = ((a - b) % 360 + 540) % 360 - 180;
        return d;
    }

    /** Maintains a 20-slot ring of click times; returns CPS over last 1s. */
    private double recordClickAndCps(UUID id) {
        long now = System.currentTimeMillis();
        long[] ring = clickRing.computeIfAbsent(id, x -> new long[20]);
        // shift left, append now
        System.arraycopy(ring, 1, ring, 0, ring.length - 1);
        ring[ring.length - 1] = now;
        int count = 0;
        for (long ts : ring) if (ts > 0 && now - ts <= 1000) count++;
        return count;
    }
}
