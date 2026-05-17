package dev.nvp.listener;

import dev.nvp.NvpPlugin;
import dev.nvp.action.BanManager;
import dev.nvp.check.KillAuraCheck;
import dev.nvp.ml.Dataset;
import dev.nvp.ml.HitSample;
import dev.nvp.ml.KnnModel;
import dev.nvp.state.PlayerState;
import dev.nvp.state.PlayerState.CheckType;
import dev.nvp.state.StateManager;
import dev.nvp.ui.HologramManager;
import dev.nvp.ui.MessageFormatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatListener implements Listener {

    private final NvpPlugin plugin;
    private final StateManager states;
    private final Dataset dataset;
    private final HologramManager holos;
    private final BanManager bans;
    private final KillAuraCheck killAura;

    public CombatListener(NvpPlugin plugin, StateManager states, KnnModel model,
                          Dataset dataset, HologramManager holos, BanManager bans) {
        this.plugin = plugin;
        this.states = states;
        this.dataset = dataset;
        this.holos = holos;
        this.bans = bans;
        this.killAura = new KillAuraCheck(model);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity target)) return;

        PlayerState st = states.get(attacker);

        // 1) Recording mode: store labelled sample, no scoring.
        if (st.recording() == PlayerState.Recording.CLEAN) {
            dataset.add(killAura.buildSample(attacker, target, 0));
            return;
        }

        // 2) Triggered AC: only score if KILLAURA check is on for attacker.
        if (!st.checking(CheckType.KILLAURA)) return;

        HitSample sample = killAura.buildSample(attacker, target, 0);
        double score = killAura.predict(sample);
        st.pushScore(score, holos.window());
        holos.update(attacker, st.recentScores());

        if (st.flagging(CheckType.KILLAURA)) {
            broadcastAlert(attacker.getName(), score);
            bans.maybeBan(attacker.getName(), score);
        }
    }

    private void broadcastAlert(String name, double score) {
        if (!states.messagesEnabled()) return;
        var msg = MessageFormatter.alert(name, score);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.hasPermission("nvp.notify")) p.sendMessage(msg);
        }
        Bukkit.getConsoleSender().sendMessage(msg);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        holos.hide(e.getPlayer());
    }
}
