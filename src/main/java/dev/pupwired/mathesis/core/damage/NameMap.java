package dev.pupwired.mathesis.core.damage;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Case-insensitive lookup from an enum-style name (a material, entity type or damage cause) to a damage type id.
 * Keys are exact names, or {@code *_SUFFIX} wildcards such as {@code *_SWORD}. An exact key always beats a
 * wildcard, and among wildcards the longest suffix wins.
 */
public final class NameMap {

    private final Map<String, String> exact = new HashMap<>();
    private final Map<String, String> suffixes = new HashMap<>();

    public NameMap(Map<String, String> entries) {
        for (Map.Entry<String, String> entry : entries.entrySet()) {
            String key = entry.getKey().trim().toUpperCase(Locale.ROOT);
            String value = entry.getValue().trim().toLowerCase(Locale.ROOT);
            if (key.startsWith("*")) suffixes.put(key.substring(1), value);
            else exact.put(key, value);
        }
    }

    public Optional<String> find(String name) {
        String key = name.toUpperCase(Locale.ROOT);
        String hit = exact.get(key);
        if (hit != null) return Optional.of(hit);

        String bestSuffix = null;
        for (String suffix : suffixes.keySet()) {
            if (key.endsWith(suffix) && (bestSuffix == null || suffix.length() > bestSuffix.length())) bestSuffix = suffix;
        }
        return bestSuffix == null ? Optional.empty() : Optional.of(suffixes.get(bestSuffix));
    }

    public int size() {
        return exact.size() + suffixes.size();
    }
}
