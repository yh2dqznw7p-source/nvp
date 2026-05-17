package dev.nvp.ml.dataset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Train/test split, stratified by label so both classes appear in both sides. */
public final class DatasetSplit {

    public final LabelledDataset train;
    public final LabelledDataset test;

    public DatasetSplit(LabelledDataset train, LabelledDataset test) {
        this.train = train;
        this.test  = test;
    }

    public static DatasetSplit stratified(LabelledDataset src, double trainRatio, long seed) {
        if (trainRatio <= 0 || trainRatio >= 1)
            throw new IllegalArgumentException("trainRatio must be in (0,1)");
        Random r = new Random(seed);
        List<LabelledSample> clean = new ArrayList<>();
        List<LabelledSample> cheat = new ArrayList<>();
        for (LabelledSample s : src.samples()) (s.label() == 1 ? cheat : clean).add(s);
        Collections.shuffle(clean, r);
        Collections.shuffle(cheat, r);

        int cleanTrain = (int) Math.round(clean.size() * trainRatio);
        int cheatTrain = (int) Math.round(cheat.size() * trainRatio);

        LabelledDataset train = new LabelledDataset(src.schema());
        LabelledDataset test  = new LabelledDataset(src.schema());
        train.addAll(clean.subList(0, cleanTrain));
        train.addAll(cheat.subList(0, cheatTrain));
        test .addAll(clean.subList(cleanTrain, clean.size()));
        test .addAll(cheat.subList(cheatTrain, cheat.size()));
        return new DatasetSplit(train, test);
    }
}
