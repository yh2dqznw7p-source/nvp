package dev.nvp.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public final class MessageFormatter {

    private MessageFormatter() {}

    /** [NVP] <player> Результат -> 0.XX  — coloured by score. */
    public static Component alert(String playerName, double score) {
        return Component.text("[NVP] ", NamedTextColor.GRAY)
            .append(Component.text(playerName + " ", NamedTextColor.GRAY))
            .append(Component.text("Результат -> ", NamedTextColor.GRAY))
            .append(Component.text(String.format("%.2f", score), colorFor(score)));
    }

    /** Hologram line for one hit score. */
    public static Component hologramLine(double score) {
        return Component.text(String.format("%.2f", score), colorFor(score));
    }

    public static TextColor colorFor(double score) {
        if (score <= 0.20) return NamedTextColor.GREEN;
        if (score <= 0.40) return NamedTextColor.YELLOW;
        if (score <= 0.60) return NamedTextColor.GOLD;     // orange
        return NamedTextColor.RED;
    }
}
