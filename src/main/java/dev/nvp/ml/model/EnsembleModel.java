package dev.nvp.ml.model;

import dev.nvp.ml.dataset.LabelledDataset;

import java.util.ArrayList;
import java.util.List;

/**
 * Weighted-average ensemble. Each member is fit on the full dataset (caller can
 * also pass already-fit models with weight 0 fit-skip). Output is the weighted
 * mean of member probabilities, normalised to [0,1].
 */
public class EnsembleModel implements Model {

    public static final class Member {
        public final Model model;
        public final double weight;
        public final boolean refit;
        public Member(Model model, double weight, boolean refit) {
            this.model = model; this.weight = weight; this.refit = refit;
        }
    }

    private final List<Member> members;
    private boolean trained;

    public EnsembleModel(List<Member> members) {
        if (members.isEmpty()) throw new IllegalArgumentException("Ensemble needs at least one member");
        this.members = new ArrayList<>(members);
    }

    @Override
    public void fit(LabelledDataset dataset) {
        for (Member m : members) if (m.refit) m.model.fit(dataset);
        trained = members.stream().allMatch(m -> m.model.isTrained());
    }

    @Override
    public double predictProba(double[] features) {
        double sumW = 0, acc = 0;
        for (Member m : members) {
            if (!m.model.isTrained()) continue;
            acc  += m.weight * m.model.predictProba(features);
            sumW += m.weight;
        }
        return sumW == 0 ? 0 : acc / sumW;
    }

    @Override public boolean isTrained() { return trained; }
    @Override public ModelType type() { return ModelType.ENSEMBLE; }
    @Override public String describe() {
        StringBuilder sb = new StringBuilder("Ensemble{");
        for (int i = 0; i < members.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(members.get(i).model.describe()).append("@").append(members.get(i).weight);
        }
        return sb.append("}").toString();
    }
}
