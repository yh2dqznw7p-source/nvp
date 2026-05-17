package dev.nvp.bypass;

import org.bukkit.entity.Player;

/** Skip checks when ping is too high — packet jitter inflates false positives. */
public class LaggingPlayerBypass implements BypassRule {

    private final int maxPingMs;

    public LaggingPlayerBypass(int maxPingMs) { this.maxPingMs = maxPingMs; }

    @Override
    public boolean bypass(Player p) {
        try { return p.getPing() > maxPingMs; }
        catch (Throwable ignored) { return false; }
    }
}
