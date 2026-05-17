package dev.nvp.ml.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the active model per check (KILLAURA, REACH, ...). Lookups are by a
 * stable string key so that registry.get("killaura") works the same from
 * commands and listeners.
 */
public class ModelRegistry {

    private final Map<String, Model> models = new LinkedHashMap<>();

    public void register(String key, Model model) { models.put(key.toLowerCase(), model); }
    public Model get(String key)            { return models.get(key.toLowerCase()); }
    public boolean has(String key)          { return models.containsKey(key.toLowerCase()); }
    public Map<String, Model> all()         { return Map.copyOf(models); }
    public void clear()                     { models.clear(); }
}
