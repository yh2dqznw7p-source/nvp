package dev.nvp.state;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.Map;

public class PlayerState {

    public enum CheckType { KILLAURA, REACH, SPEED, SCAFFOLD }

    /** Recording mode: feed labelled samples to the dataset. */
    public enum Recording { OFF, CLEAN /* /nvp dataset */, CHEAT /* future */ }

    private Recording recording = Recording.OFF;
    /** Active "watch" triggers — the AC analyses these checks for this player. */
    private final Map<CheckType, Boolean> checks = new EnumMap<>(CheckType.class);
    /** Per-check flagging enable/disable (admin override). */
    private final Map<CheckType, Boolean> flags  = new EnumMap<>(CheckType.class);

    /** Rolling buffer of the last N hit-scores (for the hologram). */
    private final Deque<Double> recentScores = new ArrayDeque<>();

    public PlayerState() {
        for (CheckType c : CheckType.values()) {
            checks.put(c, false);
            flags.put(c, true); // by default flagging is on
        }
    }

    public Recording recording() { return recording; }
    public void recording(Recording r) { this.recording = r; }

    public boolean checking(CheckType c) { return checks.get(c); }
    public void checking(CheckType c, boolean v) { checks.put(c, v); }

    public boolean flagging(CheckType c) { return flags.get(c); }
    public void flagging(CheckType c, boolean v) { flags.put(c, v); }

    public Deque<Double> recentScores() { return recentScores; }
    public void pushScore(double s, int max) {
        recentScores.addLast(s);
        while (recentScores.size() > max) recentScores.removeFirst();
    }
}
