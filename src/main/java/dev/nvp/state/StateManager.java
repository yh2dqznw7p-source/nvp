package dev.nvp.state;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StateManager {

    private final Map<UUID, PlayerState>   states   = new ConcurrentHashMap<>();
    private final Map<UUID, PlayerSession> sessions = new ConcurrentHashMap<>();
    private final ViolationManager violations;

    private volatile boolean messagesEnabled = true;
    private volatile boolean autobanEnabled  = false;

    public StateManager(double decayPerSecond) {
        this.violations = new ViolationManager(decayPerSecond);
    }

    public PlayerState get(Player p) { return get(p.getUniqueId()); }
    public PlayerState get(UUID id)  { return states.computeIfAbsent(id, x -> new PlayerState()); }

    public PlayerSession session(Player p) { return session(p.getUniqueId()); }
    public PlayerSession session(UUID id)  { return sessions.computeIfAbsent(id, PlayerSession::new); }

    public ViolationManager violations() { return violations; }

    public void onPlayerQuit(UUID id) {
        // We keep PlayerState (admin flags) so reconnect remembers triggers,
        // but drop the heavy session data.
        sessions.remove(id);
    }

    public boolean messagesEnabled() { return messagesEnabled; }
    public void messagesEnabled(boolean v) { messagesEnabled = v; }

    public boolean autobanEnabled() { return autobanEnabled; }
    public void autobanEnabled(boolean v) { autobanEnabled = v; }
}
