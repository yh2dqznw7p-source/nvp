package dev.nvp.bypass;

import org.bukkit.entity.Player;

@FunctionalInterface
public interface BypassRule {
    boolean bypass(Player player);
}
