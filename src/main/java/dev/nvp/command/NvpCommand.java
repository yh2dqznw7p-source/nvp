package dev.nvp.command;

import dev.nvp.NvpPlugin;
import dev.nvp.ml.Dataset;
import dev.nvp.ml.KnnModel;
import dev.nvp.state.PlayerState;
import dev.nvp.state.PlayerState.CheckType;
import dev.nvp.state.StateManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * /nvp dataset <player>            — toggle clean-PvP recording trigger
 * /nvp message on|off              — enable/disable AC chat alerts
 * /nvp autoban on|off              — enable/disable autoban above threshold
 * /nvp check <type> <player>       — toggle a check trigger (killaura/reach/speed/scaffold)
 * /nvp flag  <type> on|off <player>— enable/disable flagging for a player+check
 */
public class NvpCommand implements CommandExecutor, TabCompleter {

    private final NvpPlugin plugin;
    private final StateManager states;
    private final Dataset dataset;
    private final KnnModel model;

    public NvpCommand(NvpPlugin plugin, StateManager states, Dataset dataset, KnnModel model) {
        this.plugin = plugin;
        this.states = states;
        this.dataset = dataset;
        this.model = model;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender s, @NotNull Command c, @NotNull String label, @NotNull String[] a) {
        if (a.length == 0) { usage(s); return true; }
        String sub = a[0].toLowerCase(Locale.ROOT);

        switch (sub) {
            case "dataset" -> {
                if (!s.hasPermission("nvp.command.dataset")) { deny(s); return true; }
                if (a.length < 2) { s.sendMessage("§7[NVP] /nvp dataset <player>"); return true; }
                Player target = Bukkit.getPlayerExact(a[1]);
                if (target == null) { s.sendMessage("§cPlayer not found."); return true; }
                PlayerState st = states.get(target);
                boolean on = st.recording() != PlayerState.Recording.CLEAN;
                st.recording(on ? PlayerState.Recording.CLEAN : PlayerState.Recording.OFF);
                s.sendMessage("§7[NVP] dataset(clean) for " + target.getName() + ": " + (on ? "§aON" : "§cOFF"));
            }
            case "message" -> {
                if (!s.hasPermission("nvp.command.message")) { deny(s); return true; }
                if (a.length < 2) { s.sendMessage("§7[NVP] /nvp message on|off"); return true; }
                states.messagesEnabled(parseOn(a[1]));
                s.sendMessage("§7[NVP] messages: " + (states.messagesEnabled() ? "§aON" : "§cOFF"));
            }
            case "autoban" -> {
                if (!s.hasPermission("nvp.command.autoban")) { deny(s); return true; }
                if (a.length < 2) { s.sendMessage("§7[NVP] /nvp autoban on|off"); return true; }
                states.autobanEnabled(parseOn(a[1]));
                s.sendMessage("§7[NVP] autoban: " + (states.autobanEnabled() ? "§aON" : "§cOFF"));
            }
            case "check" -> {
                if (!s.hasPermission("nvp.command.check")) { deny(s); return true; }
                if (a.length < 3) { s.sendMessage("§7[NVP] /nvp check <killaura|reach|speed|scaffold> <player>"); return true; }
                CheckType t = parseCheck(a[1]);
                Player target = Bukkit.getPlayerExact(a[2]);
                if (t == null || target == null) { s.sendMessage("§cBad type or player."); return true; }
                PlayerState st = states.get(target);
                boolean on = !st.checking(t);
                st.checking(t, on);
                if (on) plugin.holograms().show(target); else plugin.holograms().hide(target);
                s.sendMessage("§7[NVP] check " + t.name().toLowerCase() + " on " + target.getName() + ": " + (on ? "§aON" : "§cOFF"));
            }
            case "flag" -> {
                if (!s.hasPermission("nvp.command.flag")) { deny(s); return true; }
                if (a.length < 4) { s.sendMessage("§7[NVP] /nvp flag <killaura|reach|speed|scaffold> on|off <player>"); return true; }
                CheckType t = parseCheck(a[1]);
                Player target = Bukkit.getPlayerExact(a[3]);
                if (t == null || target == null) { s.sendMessage("§cBad type or player."); return true; }
                states.get(target).flagging(t, parseOn(a[2]));
                s.sendMessage("§7[NVP] flag " + t.name().toLowerCase() + " on " + target.getName() + ": " + a[2].toLowerCase());
            }
            default -> usage(s);
        }
        return true;
    }

    private static boolean parseOn(String v) { return v.equalsIgnoreCase("on") || v.equalsIgnoreCase("true"); }

    private static CheckType parseCheck(String s) {
        try { return CheckType.valueOf(s.toUpperCase(Locale.ROOT)); }
        catch (Exception e) { return null; }
    }

    private static void deny(CommandSender s) { s.sendMessage("§cYou don't have permission."); }

    private static void usage(CommandSender s) {
        s.sendMessage("§7[NVP] /nvp dataset|message|autoban|check|flag ...");
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String label, @NotNull String[] a) {
        if (a.length == 1) return List.of("dataset", "message", "autoban", "check", "flag");
        if (a.length == 2) {
            return switch (a[0].toLowerCase(Locale.ROOT)) {
                case "message", "autoban" -> List.of("on", "off");
                case "check", "flag"      -> Arrays.asList("killaura", "reach", "speed", "scaffold");
                case "dataset"            -> playerNames();
                default -> List.of();
            };
        }
        if (a.length == 3) {
            return switch (a[0].toLowerCase(Locale.ROOT)) {
                case "check" -> playerNames();
                case "flag"  -> List.of("on", "off");
                default      -> List.of();
            };
        }
        if (a.length == 4 && a[0].equalsIgnoreCase("flag")) return playerNames();
        return List.of();
    }

    private static List<String> playerNames() {
        List<String> out = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) out.add(p.getName());
        return out;
    }
}
