package dev.nvp.state.history;

import dev.nvp.util.RingBuffer;
import dev.nvp.util.Stats;

/** Click timestamps + derived rate features for AutoClicker / KillAura cadence. */
public class ClickHistory {

    private final RingBuffer<Long> buf;

    public ClickHistory(int capacity) { this.buf = new RingBuffer<>(capacity); }

    public void click(long t) { buf.push(t); }
    public int size() { return buf.size(); }

    /** CPS over the last 1 second relative to `now`. */
    public int cps(long now) {
        int c = 0;
        for (long t : buf) if (now - t <= 1000) c++;
        return c;
    }

    /** Standard deviation of click intervals in ms. Low std → too regular → suspicious. */
    public double intervalStdDev() {
        if (buf.size() < 3) return 0;
        double[] gaps = new double[buf.size() - 1];
        for (int i = 1; i < buf.size(); i++) gaps[i - 1] = buf.get(i) - buf.get(i - 1);
        return Stats.std(gaps);
    }

    public double meanInterval() {
        if (buf.size() < 2) return 0;
        double[] gaps = new double[buf.size() - 1];
        for (int i = 1; i < buf.size(); i++) gaps[i - 1] = buf.get(i) - buf.get(i - 1);
        return Stats.mean(gaps);
    }

    public RingBuffer<Long> raw() { return buf; }
    public void clear() { buf.clear(); }
}
