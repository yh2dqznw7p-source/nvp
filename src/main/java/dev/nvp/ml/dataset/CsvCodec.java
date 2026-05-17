package dev.nvp.ml.dataset;

import dev.nvp.ml.feature.FeatureSchema;
import dev.nvp.ml.feature.FeatureVector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/** Serialises LabelledDataset as CSV with a header that pins the schema. */
public final class CsvCodec {

    private CsvCodec() {}

    public static void write(Path file, LabelledDataset ds) throws IOException {
        Files.createDirectories(file.getParent());
        List<String> lines = new ArrayList<>(ds.size() + 2);
        lines.add("# nvp-dataset v1");
        lines.add(ds.schema().header() + ",label,timestamp,source");
        for (LabelledSample s : ds.samples()) lines.add(encodeRow(s));
        Files.write(file, lines);
    }

    public static void append(Path file, LabelledSample s) throws IOException {
        Files.createDirectories(file.getParent());
        boolean exists = Files.exists(file) && Files.size(file) > 0;
        if (!exists) {
            List<String> head = List.of("# nvp-dataset v1",
                s.schema().header() + ",label,timestamp,source");
            Files.write(file, head, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        Files.writeString(file, encodeRow(s) + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    public static LabelledDataset read(Path file, FeatureSchema schema) throws IOException {
        LabelledDataset out = new LabelledDataset(schema);
        if (!Files.exists(file)) return out;
        boolean headerSeen = false;
        for (String line : Files.readAllLines(file)) {
            if (line.isBlank() || line.startsWith("#")) continue;
            if (!headerSeen) { headerSeen = true; continue; }
            String[] parts = line.split(",");
            int d = schema.size();
            if (parts.length < d + 1) continue;
            double[] vals = new double[d];
            for (int i = 0; i < d; i++) vals[i] = Double.parseDouble(parts[i]);
            int label = Integer.parseInt(parts[d].trim());
            long ts   = parts.length > d + 1 ? Long.parseLong(parts[d + 1].trim()) : 0L;
            String src = parts.length > d + 2 ? parts[d + 2] : "";
            out.add(new LabelledSample(new FeatureVector(schema, vals), label, ts, src));
        }
        return out;
    }

    private static String encodeRow(LabelledSample s) {
        StringBuilder sb = new StringBuilder();
        double[] x = s.features();
        for (int i = 0; i < x.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(x[i]);
        }
        sb.append(',').append(s.label())
          .append(',').append(s.timestampMs())
          .append(',').append(s.source().replace(',', ';'));
        return sb.toString();
    }
}
