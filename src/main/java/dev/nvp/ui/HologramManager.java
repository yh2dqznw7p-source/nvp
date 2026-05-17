package dev.nvp.ui;

import dev.nvp.NvpPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HologramManager {

    private final NvpPlugin plugin;
    private final int window;
    private final Map<UUID, TextDisplay> displays = new HashMap<>();

    public HologramManager(NvpPlugin plugin, int window) {
        this.plugin = plugin;
        this.window = Math.max(1, window);
    }

    public int window() { return window; }

    public void show(Player target) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (displays.containsKey(target.getUniqueId())) return;
            TextDisplay d = target.getWorld().spawn(target.getEyeLocation().add(0, 0.9, 0), TextDisplay.class, td -> {
                td.setBillboard(org.bukkit.entity.Display.Billboard.CENTER);
                td.setSeeThrough(true);
                td.setShadowed(false);
                td.text(Component.text(""));
                td.setTransformation(new Transformation(
                    new Vector3f(0, 0.4f, 0), new AxisAngle4f(),
                    new Vector3f(1, 1, 1),    new AxisAngle4f()));
            });
            target.addPassenger(d);
            displays.put(target.getUniqueId(), d);
        });
    }

    public void hide(Player target) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            TextDisplay d = displays.remove(target.getUniqueId());
            if (d != null && !d.isDead()) d.remove();
        });
    }

    /** Render a list of scores (oldest first, newest last) on the hologram. */
    public void update(Player target, Iterable<Double> scores) {
        TextDisplay d = displays.get(target.getUniqueId());
        if (d == null) return;
        Component line = Component.text("");
        boolean first = true;
        for (Double s : scores) {
            if (s == null) continue;
            if (!first) line = line.append(Component.text("  "));
            line = line.append(MessageFormatter.hologramLine(s));
            first = false;
        }
        Component finalLine = line;
        Bukkit.getScheduler().runTask(plugin, () -> d.text(finalLine));
    }

    public void removeAll() {
        for (TextDisplay d : displays.values()) if (!d.isDead()) d.remove();
        displays.clear();
    }
}
