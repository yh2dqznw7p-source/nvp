package dev.nvp.listener;

import dev.nvp.NvpPlugin;
import dev.nvp.action.BanManager;
import dev.nvp.bypass.BypassManager;
import dev.nvp.check.KillAuraCheck;
import dev.nvp.ml.dataset.CsvCodec;
import dev.nvp.ml.dataset.LabelledDataset;
import dev.nvp.ml.dataset.LabelledSample;
import dev.nvp.ml.feature.FeatureVector;
import dev.nvp.ml.model.Model;
import dev.nvp.ml.model.ModelRegistry;
import dev.nvp.state.PlayerSession;
import dev.nvp.state.PlayerState;
import dev.nvp.state.PlayerState.CheckType;
import dev.nvp.state.StateManager;
import dev.nvp.ui.HologramManager;
import dev.nvp.ui.MessageFormatter;
import dev.nvp.util.TimingUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.IOException;
import java.nio.file.Path;

public class CombatListener implements Listener {

    private final NvpPlugin plugin;
    private final StateManager states;
    private final ModelRegistry models;
    private final LabelledDataset killAuraDs;
    private final HologramManager holos;
    private final BanManager bans;
    private final BypassManager bypasses;
    private final Path datasetPath;

    public CombatListener(NvpPlugin plugin, StateManager states, ModelRegistry models,
                          LabelledDataset killAuraDs, HologramManager holos, BanManager bans,
                          BypassManager bypasses, Path datasetPath) {
        this.plugin = plugin;
        this.states = states;
        this.models = models;
        this.killAuraDs = killAuraDs;
        this.holos = holos;
        this.bans = bans;
        this.bypasses = bypasses;
        this.datasetPath = datasetPath;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if (e.getTo() == null) return;
        PlayerSession sess = states.session(p);
        sess.rotation().push(TimingUtil.nowMs(), e.getTo().getYaw(), e.getTo().getPitch());
        sess.touchMove();
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player attacker)) return;
        if (!(e.getEntity() instanceof LivingEntity target)) return;
        if (bypasses.shouldBypass(attacker)) return;

        PlayerState st = states.get(attacker);
        PlayerSession sess = states.session(attacker);
        sess.clicks().click(TimingUtil.nowMs());
        sess.touchHit();

        Model model = models.get("killaura");
        KillAuraCheck killAura = new KillAuraCheck(model);
        FeatureVector fv = killAura.buildFeatures(attacker, target, sess);

        if (st.recording() == PlayerState.Recording.CLEAN) {
            LabelledSample ls = killAura.asSample(fv, 0);
            killAuraDs.add(ls);
            try { CsvCodec.append(datasetPath, ls); }
            catch (IOException ex) { plugin.getLogger().warning("Dataset append failed: " + ex.getMessage()); }
            return;
        }

        if (!st.watching(CheckType.KILLAURA)) return;

        double score = killAura.predict(fv);
        sess.recordScore(CheckType.KILLAURA, score);
        holos.update(attacker, sess.scores(CheckType.KILLAURA).raw());

        if (st.flagging(CheckType.KILLAURA)) {
            states.violations().add(CheckType.KILLAURA, score);
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
        states.onPlayerQuit(e.getPlayer().getUniqueId());
    }
}
