package dev.nvp.state;

import java.util.EnumMap;
import java.util.Map;

/**
 * Lightweight admin/runtime flags for a player: who has triggers on, who is
 * being recorded into the dataset, per-check flag toggles. Histories live in
 * PlayerSession.
 */
public class PlayerState {

    public enum CheckType {
        KILLAURA, REACH, AIM, AUTOCLICKER, VELOCITY,
        SPEED, FLY, NOFALL, JESUS,
        SCAFFOLD, TOWER, FASTPLACE, NUKER
    }

    public enum Recording { OFF, CLEAN, CHEAT }

    private Recording recording = Recording.OFF;
    private final Map<CheckType, Boolean> watching = new EnumMap<>(CheckType.class);
    private final Map<CheckType, Boolean> flagging = new EnumMap<>(CheckType.class);
    private boolean exempt;

    public PlayerState() {
        for (CheckType c : CheckType.values()) {
            watching.put(c, false);
            flagging.put(c, true);
        }
    }

    public Recording recording() { return recording; }
    public void recording(Recording r) { this.recording = r; }

    public boolean watching(CheckType c) { return watching.get(c); }
    public void watching(CheckType c, boolean v) { watching.put(c, v); }

    public boolean flagging(CheckType c) { return flagging.get(c); }
    public void flagging(CheckType c, boolean v) { flagging.put(c, v); }

    /** Any watch trigger active? Used to decide whether to spawn a hologram. */
    public boolean anyWatching() {
        for (Boolean v : watching.values()) if (v) return true;
        return false;
    }

    public boolean isExempt() { return exempt; }
    public void setExempt(boolean v) { this.exempt = v; }
}
