package dev.nvp;

import dev.nvp.command.NvpCommand;
import dev.nvp.listener.CombatListener;
import dev.nvp.ml.Dataset;
import dev.nvp.ml.KnnModel;
import dev.nvp.state.StateManager;
import dev.nvp.ui.HologramManager;
import dev.nvp.action.BanManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;

public final class NvpPlugin extends JavaPlugin {

    private StateManager stateManager;
    private Dataset dataset;
    private KnnModel model;
    private HologramManager hologramManager;
    private BanManager banManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Path datasetsDir = getDataFolder().toPath().resolve("datasets");
        this.dataset = new Dataset(datasetsDir, getLogger());
        this.dataset.load();

        int k = getConfig().getInt("model.k", 7);
        this.model = new KnnModel(dataset, k);

        this.stateManager = new StateManager();
        this.hologramManager = new HologramManager(this, getConfig().getInt("model.hologram-window", 6));
        this.banManager = new BanManager(this);

        getServer().getPluginManager().registerEvents(
            new CombatListener(this, stateManager, model, dataset, hologramManager, banManager), this);

        var cmd = getCommand("nvp");
        if (cmd != null) {
            NvpCommand handler = new NvpCommand(this, stateManager, dataset, model);
            cmd.setExecutor(handler);
            cmd.setTabCompleter(handler);
        }

        getLogger().info("NVP enabled. Dataset samples: " + dataset.size());
    }

    @Override
    public void onDisable() {
        if (dataset != null) dataset.save();
        if (hologramManager != null) hologramManager.removeAll();
    }

    public StateManager states() { return stateManager; }
    public Dataset dataset()     { return dataset; }
    public KnnModel model()      { return model; }
    public HologramManager holograms() { return hologramManager; }
    public BanManager bans()     { return banManager; }
}
