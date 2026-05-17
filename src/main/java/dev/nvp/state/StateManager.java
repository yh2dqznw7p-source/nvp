package dev.nvp.state;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StateManager {

    private final Map<UUID, PlayerState> players = new ConcurrentHashMap<>();

    /** Global toggles. */
    private volatile boolean messagesEnabled = true;
    private volatile boolean autobanEnabled  = false;

    public PlayerState get(Player p) {
        return players.computeIfAbsent(p.getUniqueId(), id -> new PlayerState());
    }

    public PlayerState get(UUID id) {
        return players.computeIfAbsent(id, x -> new PlayerState());
    }

    public boolean messagesEnabled() { return messagesEnabled; }
    public void messagesEnabled(boolean v) { messagesEnabled = v; }

    public boolean autobanEnabled() { return autobanEnabled; }
    public void autobanEnabled(boolean v) { autobanEnabled = v; }
}
