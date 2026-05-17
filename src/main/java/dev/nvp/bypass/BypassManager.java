package dev.nvp.bypass;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/** Aggregates BypassRules; if any rule says bypass, the AC ignores the event. */
public class BypassManager {

    private final List<BypassRule> rules = new ArrayList<>();

    public void register(BypassRule r) { rules.add(r); }

    public boolean shouldBypass(Player p) {
        if (p == null) return true;
        if (p.getGameMode() == GameMode.CREATIVE || p.getGameMode() == GameMode.SPECTATOR) return true;
        for (BypassRule r : rules) if (r.bypass(p)) return true;
        return false;
    }
}
