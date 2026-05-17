package dev.nvp.ml;

/**
 * Feature vector for a single melee hit. Keep small & fast — k-NN scans linearly.
 * Add features here freely; KnnModel computes Euclidean distance over all of them.
 */
public record HitSample(
        double yawDelta,        // |Δyaw| since previous tick
        double pitchDelta,      // |Δpitch| since previous tick
        double aimError,        // angle (deg) between look vector and target direction
        double reach,           // horizontal distance attacker→target
        double cps,             // recent clicks-per-second
        double sprintFactor,    // 0/1 sprinting
        double sneakFactor,     // 0/1 sneaking
        double yDiff,           // attacker.y − target.y
        int    label            // 0 = clean, 1 = cheat
) {
    public double[] features() {
        return new double[] { yawDelta, pitchDelta, aimError, reach, cps, sprintFactor, sneakFactor, yDiff };
    }

    public String toCsv() {
        return yawDelta + "," + pitchDelta + "," + aimError + "," + reach + "," +
               cps + "," + sprintFactor + "," + sneakFactor + "," + yDiff + "," + label;
    }

    public static HitSample fromCsv(String line) {
        String[] p = line.split(",");
        return new HitSample(
            Double.parseDouble(p[0]), Double.parseDouble(p[1]), Double.parseDouble(p[2]),
            Double.parseDouble(p[3]), Double.parseDouble(p[4]), Double.parseDouble(p[5]),
            Double.parseDouble(p[6]), Double.parseDouble(p[7]),
            Integer.parseInt(p[8].trim())
        );
    }
}
