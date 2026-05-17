package dev.nvp;

import dev.nvp.action.BanManager;
import dev.nvp.bypass.BypassManager;
import dev.nvp.bypass.LaggingPlayerBypass;
import dev.nvp.bypass.NewPlayerBypass;
import dev.nvp.bypass.PermissionBypass;
import dev.nvp.check.KillAuraCheck;
import dev.nvp.command.NvpCommand;
import dev.nvp.listener.CombatListener;
import dev.nvp.ml.dataset.CsvCodec;
import dev.nvp.ml.dataset.LabelledDataset;
import dev.nvp.ml.feature.StandardScaler;
import dev.nvp.ml.model.Model;
import dev.nvp.ml.model.ModelRegistry;
import dev.nvp.ml.model.WeightedKnnModel;
import dev.nvp.state.StateManager;
import dev.nvp.ui.HologramManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.nio.file.Path;

public final class NvpPlugin extends JavaPlugin {

    private StateManager stateManager;
    private LabelledDataset killAuraDataset;
    private ModelRegistry modelRegistry;
    private HologramManager hologramManager;
    private BanManager banManager;
    private BypassManager bypassManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Path dsPath = getDataFolder().toPath().resolve("datasets").resolve("killaura.csv");
        killAuraDataset = new LabelledDataset(KillAuraCheck.SCHEMA);
        try {
            LabelledDataset loaded = CsvCodec.read(dsPath, KillAuraCheck.SCHEMA);
            killAuraDataset.addAll(loaded.samples());
            getLogger().info("Loaded " + killAuraDataset.size() + " killaura samples.");
        } catch (IOException e) {
            getLogger().warning("Failed to load dataset: " + e.getMessage());
        }

        int k = getConfig().getInt("model.k", 7);
        WeightedKnnModel knn = new WeightedKnnModel(k, new StandardScaler());
        if (killAuraDataset.size() >= k) {
            knn.fit(killAuraDataset);
            getLogger().info("Trained " + knn.describe());
        } else {
            getLogger().info("Not enough samples to train (have " + killAuraDataset.size() + ", need " + k + ").");
        }
        modelRegistry = new ModelRegistry();
        modelRegistry.register("killaura", knn);

        double decay = getConfig().getDouble("violations.decay-per-second", 0.05);
        stateManager = new StateManager(decay);

        bypassManager = new BypassManager();
        bypassManager.register(new PermissionBypass());
        long graceMs = getConfig().getLong("bypass.new-player-ms", 5000);
        bypassManager.register(new NewPlayerBypass(stateManager, graceMs));
        int maxPing = getConfig().getInt("bypass.max-ping-ms", 350);
        bypassManager.register(new LaggingPlayerBypass(maxPing));

        hologramManager = new HologramManager(this, getConfig().getInt("model.hologram-window", 6));
        banManager = new BanManager(this);

        getServer().getPluginManager().registerEvents(
            new CombatListener(this, stateManager, modelRegistry, killAuraDataset,
                hologramManager, banManager, bypassManager, dsPath), this);

        var cmd = getCommand("nvp");
        if (cmd != null) {
            NvpCommand handler = new NvpCommand(this, stateManager, killAuraDataset, modelRegistry);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        getLogger().info("NVP enabled.");
    }

    @Override
    public void onDisable() {
        if (killAuraDataset != null) {
            try {
                Path dsPath = getDataFolder().toPath().resolve("datasets").resolve("killaura.csv");
                CsvCodec.write(dsPath, killAuraDataset);
            } catch (IOException e) {
                getLogger().warning("Failed to save dataset: " + e.getMessage());
            }
        }
        if (hologramManager != null) hologramManager.removeAll();
    }

    public StateManager states()         { return stateManager; }
    public LabelledDataset killAuraDs()  { return killAuraDataset; }
    public ModelRegistry models()        { return modelRegistry; }
    public Model killAuraModel()         { return modelRegistry.get("killaura"); }
    public HologramManager holograms()   { return hologramManager; }
    public BanManager bans()             { return banManager; }
    public BypassManager bypasses()      { return bypassManager; }
}
