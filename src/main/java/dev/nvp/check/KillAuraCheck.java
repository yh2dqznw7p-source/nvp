package dev.nvp.check;

import dev.nvp.ml.dataset.LabelledSample;
import dev.nvp.ml.feature.FeatureSchema;
import dev.nvp.ml.feature.FeatureVector;
import dev.nvp.ml.model.Model;
import dev.nvp.state.PlayerSession;
import dev.nvp.state.history.AttackHistory;
import dev.nvp.state.history.RotationHistory;
import dev.nvp.util.RotationUtil;
import dev.nvp.util.TimingUtil;
import dev.nvp.util.VectorUtil;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

/**
 * KillAura check: builds a feature vector from melee event geometry/timing
 * and asks the bound Model for cheat probability.
 */
public class KillAuraCheck {

    public static final FeatureSchema SCHEMA = new FeatureSchema(List.of(
        "yawDelta", "pitchDelta", "aimError", "reach", "cps", "sprint", "sneak", "yDiff"
    ));

    private final Model model;

    public KillAuraCheck(Model model) { this.model = model; }

    public FeatureVector buildFeatures(Player attacker, LivingEntity target, PlayerSession sess) {
        Location a = attacker.getLocation();
        Location t = target.getLocation();

        double yawDelta = 0, pitDelta = 0;
        RotationHistory rot = sess.rotation();
        if (rot.size() >= 2) {
            RotationHistory.Sample now  = rot.raw().get(rot.size() - 1);
            RotationHistory.Sample prev = rot.raw().get(rot.size() - 2);
            yawDelta = Math.abs(RotationUtil.yawDelta(now.yaw, prev.yaw));
            pitDelta = Math.abs(now.pitch - prev.pitch);
        }

        Vector look = a.getDirection();
        Vector toTarget = t.toVector().subtract(a.toVector()).normalize();
        double aimError = VectorUtil.angleBetween(look, toTarget);

        double reach  = a.toVector().setY(0).distance(t.toVector().setY(0));
        double cps    = sess.clicks().cps(TimingUtil.nowMs());
        double sprint = attacker.isSprinting() ? 1 : 0;
        double sneak  = attacker.isSneaking()  ? 1 : 0;
        double yDiff  = a.getY() - t.getY();

        sess.attacks().push(new AttackHistory.Hit(
            TimingUtil.nowMs(), target.getUniqueId(), reach, aimError, a.getYaw(), a.getPitch()));

        return new FeatureVector(SCHEMA, new double[]{
            yawDelta, pitDelta, aimError, reach, cps, sprint, sneak, yDiff
        });
    }

    public LabelledSample asSample(FeatureVector fv, int label) {
        return new LabelledSample(fv, label);
    }

    public double predict(FeatureVector fv) {
        if (model == null || !model.isTrained()) return 0.0;
        return model.predictProba(fv.values());
    }

    public Model model() { return model; }
}
