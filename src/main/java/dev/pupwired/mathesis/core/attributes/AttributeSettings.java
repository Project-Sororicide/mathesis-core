package dev.pupwired.mathesis.core.attributes;

import dev.pupwired.mathesis.core.items.StatValueParser;
import dev.pupwired.mathesis.core.rpcheck.CharacterAttribute;
import dev.pupwired.mathesis.core.stats.StatModifier;
import dev.pupwired.mathesis.core.stats.StatRegistry;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

/** The reloadable per-point bonuses from attributes.yml. Bad entries are skipped with a warning. */
public final class AttributeSettings {

    private volatile AttributeBonuses bonuses = AttributeBonuses.none();

    public AttributeBonuses bonuses() { return bonuses; }

    /** Call after the stat registry is rebuilt, since stat ids are validated against it. */
    public void reload(ConfigurationSection root, StatRegistry stats, Logger log) {
        Map<CharacterAttribute, List<StatModifier>> loaded = new EnumMap<>(CharacterAttribute.class);
        for (String key : root.getKeys(false)) {
            CharacterAttribute attribute;
            try {
                attribute = CharacterAttribute.fromConfig(key);
            } catch (IllegalArgumentException e) {
                log.warning("attributes.yml: unknown attribute '" + key + "', skipped.");
                continue;
            }
            ConfigurationSection section = root.getConfigurationSection(key);
            if (section == null) {
                log.warning("attributes.yml: '" + key + "' is not a section, skipped.");
                continue;
            }
            List<StatModifier> modifiers = new ArrayList<>();
            for (String stat : section.getKeys(false)) {
                if (!stats.contains(stat)) {
                    log.warning("attributes.yml: " + key + ": unknown stat '" + stat + "', skipped.");
                    continue;
                }
                try {
                    modifiers.add(StatValueParser.parse(stat.toLowerCase(Locale.ROOT), section.get(stat)));
                } catch (IllegalArgumentException e) {
                    log.warning("attributes.yml: " + key + ": " + e.getMessage() + ", skipped.");
                }
            }
            loaded.put(attribute, modifiers);
        }
        bonuses = new AttributeBonuses(loaded);
        log.info("Loaded attribute bonuses for " + loaded.size() + " attribute(s).");
    }
}