package dev.pupwired.mathesis.core.config;

import org.bukkit.configuration.ConfigurationSection;

import java.util.*;
import java.util.logging.Logger;

/**
 * Base for anything defined as "one top-level key per entry" in a yml file
 * (damage types now... items, classes and skills are to-do).
 * A broken entry is logged and skipped; it never prevents the others from loading.
 */
public abstract class ConfigRegistry<T> {

    private final Logger logger;
    private final String kind;
    private volatile Map<String, T> entries = Map.of();

    protected ConfigRegistry(Logger logger, String kind) {
        this.logger = logger;
        this.kind = kind;
    }

    public void load(ConfigurationSection root) {
        Map<String, T> loaded = new LinkedHashMap<>();
        if (root != null) {
            for (String key : root.getKeys(false)) {
                String id = key.toLowerCase(Locale.ROOT);
                ConfigurationSection section = root.getConfigurationSection(key);
                if (section == null) {
                    logger.warning("Skipping " + kind + " '" + key + "': not a section");
                    continue;
                }
                try {
                    loaded.put(id, parse(id, section));
                } catch (RuntimeException e) {
                    logger.warning("Skipping " + kind + " '" + key + "': " + e.getMessage());
                }
            }
        }
        entries = Collections.unmodifiableMap(loaded); // swap only after a full load
    }

    protected abstract T parse(String id, ConfigurationSection section);

    public Optional<T> get(String id) {
        return Optional.ofNullable(entries.get(id.toLowerCase(Locale.ROOT)));
    }

    public Collection<T> all() {
        return entries.values();
    }
}
