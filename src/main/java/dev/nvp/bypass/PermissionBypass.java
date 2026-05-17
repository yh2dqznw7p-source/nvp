package dev.nvp.bypass;

import org.bukkit.entity.Player;

/** Players with `nvp.bypass` skip all checks (admins, staff). */
public class PermissionBypass implements BypassRule {
    @Override public boolean bypass(Player player) { return player.hasPermission("nvp.bypass"); }
}
