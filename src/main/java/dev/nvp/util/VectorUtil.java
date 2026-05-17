package dev.nvp.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class VectorUtil {
    private VectorUtil() {}

    public static Vector lookVector(Location loc) {
        double y = Math.toRadians(loc.getYaw());
        double p = Math.toRadians(loc.getPitch());
        double cp = Math.cos(p);
        return new Vector(-Math.sin(y) * cp, -Math.sin(p), Math.cos(y) * cp);
    }

    /** Angle (degrees) between two vectors, robust to fp error. */
    public static double angleBetween(Vector a, Vector b) {
        double la = a.length(), lb = b.length();
        if (la < 1e-9 || lb < 1e-9) return 0;
        double cos = a.dot(b) / (la * lb);
        if (cos > 1) cos = 1; else if (cos < -1) cos = -1;
        return Math.toDegrees(Math.acos(cos));
    }

    /** Horizontal (XZ) distance between two locations. */
    public static double horizontalDistance(Location a, Location b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return Math.sqrt(dx * dx + dz * dz);
    }

    /** Closest distance from attacker eye to target hitbox bottom-center
     *  (cheap reach approximation; precise hitbox check is in ReachCheck). */
    public static double approxReach(Location eye, Location target) {
        return eye.distance(target.add(0, 0.9, 0));
    }
}
