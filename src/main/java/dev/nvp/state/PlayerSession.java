package dev.nvp.state;

import dev.nvp.state.history.AttackHistory;
import dev.nvp.state.history.BlockPlaceHistory;
import dev.nvp.state.history.ClickHistory;
import dev.nvp.state.history.PositionHistory;
import dev.nvp.state.history.RotationHistory;
import dev.nvp.state.history.ScoreHistory;
import dev.nvp.state.history.VelocityHistory;
import dev.nvp.util.TimingUtil;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/**
 * Heavy-weight per-player runtime state. Holds histories for every check that
 * needs temporal context, score buffers for the hologram, and convenience
 * timestamps. PlayerState (light flags) lives separately.
 */
public class PlayerSession {

    private final UUID playerId;
    private final long joinTimeMs;

    private final RotationHistory rotation = new RotationHistory(40);
    private final PositionHistory position = new PositionHistory(40);
    private final AttackHistory   attacks  = new AttackHistory(40);
    private final ClickHistory    clicks   = new ClickHistory(40);
    private final BlockPlaceHistory blocks = new BlockPlaceHistory(40);
    private final VelocityHistory velocity = new VelocityHistory(8);

    private final Map<PlayerState.CheckType, ScoreHistory> scores =
            new EnumMap<>(PlayerState.CheckType.class);

    /** Last successful prediction per check (cached for hologram refresh). */
    private final Map<PlayerState.CheckType, Double> latestScore =
            new EnumMap<>(PlayerState.CheckType.class);

    private long lastHitMs;
    private long lastMoveMs;
    private long lastBlockPlaceMs;
    private boolean firstSampleSent;

    public PlayerSession(UUID playerId) {
        this.playerId = playerId;
        this.joinTimeMs = TimingUtil.nowMs();
        for (PlayerState.CheckType t : PlayerState.CheckType.values()) {
            scores.put(t, new ScoreHistory(6));
            latestScore.put(t, 0.0);
        }
    }

    public UUID playerId() { return playerId; }
    public long joinedMs() { return joinTimeMs; }

    public RotationHistory rotation() { return rotation; }
    public PositionHistory position() { return position; }
    public AttackHistory   attacks()  { return attacks; }
    public ClickHistory    clicks()   { return clicks; }
    public BlockPlaceHistory blocks() { return blocks; }
    public VelocityHistory velocity() { return velocity; }

    public ScoreHistory scores(PlayerState.CheckType t) { return scores.get(t); }
    public double latestScore(PlayerState.CheckType t) { return latestScore.get(t); }
    public void recordScore(PlayerState.CheckType t, double s) {
        scores.get(t).push(s);
        latestScore.put(t, s);
    }

    public long lastHitMs() { return lastHitMs; }
    public void touchHit() { lastHitMs = TimingUtil.nowMs(); }

    public long lastMoveMs() { return lastMoveMs; }
    public void touchMove() { lastMoveMs = TimingUtil.nowMs(); }

    public long lastBlockPlaceMs() { return lastBlockPlaceMs; }
    public void touchBlockPlace() { lastBlockPlaceMs = TimingUtil.nowMs(); }

    public boolean firstSampleSent() { return firstSampleSent; }
    public void markFirstSampleSent() { firstSampleSent = true; }
}
