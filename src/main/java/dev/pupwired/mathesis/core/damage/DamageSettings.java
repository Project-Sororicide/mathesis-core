package dev.pupwired.mathesis.core.damage;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * The reloadable damage settings from the {@code damage:} section of config.yml. A bad formula keeps the
 * previous one; bad mapping entries are skipped with a warning.
 */
public final class DamageSettings {

    private volatile DefenseFormula formula = DefenseFormula.defaultFormula();
    private volatile DamageSourceResolver resolver =
            new DamageSourceResolver(null, new NameMap(Map.of()), new NameMap(Map.of()), new NameMap(Map.of()));

    public DefenseFormula formula() { return formula; }

    public DamageSourceResolver resolver() { return resolver; }

    public void reload(ConfigurationSection section, DamageTypeRegistry types, Logger log) {
        if (section == null) {
            log.warning("config.yml has no 'damage' section (copy it from the plugin's default config); using defaults.");
            section = new MemoryConfiguration();
        }

        String source = section.getString("defense-formula", DefenseFormula.DEFAULT_SOURCE);
        try {
            formula = DefenseFormula.compile(source);
        } catch (IllegalArgumentException e) {
            log.severe("damage.defense-formula rejected, keeping the previous formula: " + e.getMessage());
        }

        String defaultType = section.getString("default-type");
        if (defaultType != null && types.get(defaultType).isEmpty()) {
            log.warning("damage.default-type: unknown damage type '" + defaultType + "', ignored.");
            defaultType = null;
        } else if (defaultType != null) {
            defaultType = defaultType.toLowerCase();
        }

        resolver = new DamageSourceResolver(defaultType,
                loadMap(section, "weapons", types, log),
                loadMap(section, "entities", types, log),
                loadMap(section, "causes", types, log));
        log.info("Defense formula: " + formula.source());
    }

    private static NameMap loadMap(ConfigurationSection root, String name, DamageTypeRegistry types, Logger log) {
        Map<String, String> entries = new LinkedHashMap<>();
        ConfigurationSection section = root.getConfigurationSection(name);
        if (section != null) {
            for (String key : section.getKeys(false)) {
                String type = section.getString(key);
                if (type == null || types.get(type).isEmpty()) {
                    log.warning("damage." + name + "." + key + ": unknown damage type '" + type + "', skipped.");
                    continue;
                }
                entries.put(key, type);
            }
        }
        return new NameMap(entries);
    }
}
