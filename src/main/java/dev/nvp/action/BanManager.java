package dev.nvp.action;

import dev.nvp.NvpPlugin;
import org.bukkit.Bukkit;

public class BanManager {

    private final NvpPlugin plugin;

    public BanManager(NvpPlugin plugin) { this.plugin = plugin; }

    public double threshold() {
        return plugin.getConfig().getDouble("autoban.threshold", 0.85);
    }

    public void maybeBan(String playerName, double score) {
        if (!plugin.states().autobanEnabled()) return;
        if (score < threshold()) return;
        String cmd = plugin.getConfig().getString("autoban.command",
                "ban %player% [NVP] Cheating detected (%score%)")
                .replace("%player%", playerName)
                .replace("%score%", String.format("%.2f", score));
        Bukkit.getScheduler().runTask(plugin,
                () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
    }
}
