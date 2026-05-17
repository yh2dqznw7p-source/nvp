package dev.nvp.bypass;

import dev.nvp.state.PlayerSession;
import dev.nvp.state.StateManager;
import org.bukkit.entity.Player;

/** Skip checks for the first N seconds after join — avoids false flags during
 *  client-init and chunk-load lag. */
public class NewPlayerBypass implements BypassRule {

    private final StateManager states;
    private final long graceMs;

    public NewPlayerBypass(StateManager states, long graceMs) {
        this.states = states;
        this.graceMs = graceMs;
    }

    @Override
    public boolean bypass(Player p) {
        PlayerSession s = states.session(p);
        return System.currentTimeMillis() - s.joinedMs() < graceMs;
    }
}
