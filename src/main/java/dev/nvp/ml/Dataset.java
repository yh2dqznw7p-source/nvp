package dev.nvp.ml;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Persistent CSV-backed dataset. Files live in <plugin>/datasets/{clean,cheat}.csv.
 * Append-only on add(); rewrites whole file on save() (called on disable).
 */
public class Dataset {

    private final Path dir;
    private final Path cleanCsv;
    private final Path cheatCsv;
    private final Logger log;

    private final List<HitSample> samples = new CopyOnWriteArrayList<>();

    public Dataset(Path dir, Logger log) {
        this.dir = dir;
        this.cleanCsv = dir.resolve("clean.csv");
        this.cheatCsv = dir.resolve("cheat.csv");
        this.log = log;
    }

    public void load() {
        try {
            Files.createDirectories(dir);
            if (Files.exists(cleanCsv)) loadFile(cleanCsv);
            if (Files.exists(cheatCsv)) loadFile(cheatCsv);
        } catch (IOException e) {
            log.warning("[NVP] Dataset load failed: " + e.getMessage());
        }
    }

    private void loadFile(Path p) throws IOException {
        for (String line : Files.readAllLines(p)) {
            if (line.isBlank() || line.startsWith("#")) continue;
            try { samples.add(HitSample.fromCsv(line)); }
            catch (Exception ex) { log.warning("[NVP] Bad CSV line in " + p + ": " + line); }
        }
    }

    public void add(HitSample s) {
        samples.add(s);
        Path target = s.label() == 1 ? cheatCsv : cleanCsv;
        try {
            Files.createDirectories(dir);
            Files.writeString(target, s.toCsv() + System.lineSeparator(),
                    java.nio.file.StandardOpenOption.CREATE,
                    java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
            log.warning("[NVP] Dataset append failed: " + e.getMessage());
        }
    }

    public void save() {
        try {
            Files.createDirectories(dir);
            List<String> clean = new ArrayList<>();
            List<String> cheat = new ArrayList<>();
            for (HitSample s : samples) {
                (s.label() == 1 ? cheat : clean).add(s.toCsv());
            }
            Files.write(cleanCsv, clean);
            Files.write(cheatCsv, cheat);
        } catch (IOException e) {
            log.warning("[NVP] Dataset save failed: " + e.getMessage());
        }
    }

    public List<HitSample> all() { return Collections.unmodifiableList(samples); }
    public int size() { return samples.size(); }
}
