package dev.pupwired.mathesis.core.items;

import dev.pupwired.mathesis.core.config.ConfigRegistry;
import dev.pupwired.mathesis.core.damage.DamageTypeRegistry;
import dev.pupwired.mathesis.core.stats.StatModifier;
import dev.pupwired.mathesis.core.stats.StatRegistry;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/** Loads items.yml. Stats and damage types are validated against the registries current at load time. */
public final class ItemRegistry extends ConfigRegistry<ItemTemplate> {

    private final StatRegistry stats;
    private final DamageTypeRegistry damageTypes;

    public ItemRegistry(Logger logger, StatRegistry stats, DamageTypeRegistry damageTypes) {
        super(logger, "item");
        this.stats = stats;
        this.damageTypes = damageTypes;
    }

    @Override
    protected ItemTemplate parse(String id, ConfigurationSection section) {
        String material = section.getString("material");
        if (material == null || Material.matchMaterial(material) == null)
            throw new IllegalArgumentException("unknown material '" + material + "'");

        GearSlot slot = GearSlot.fromConfig(section.getString("slot", "hand"));

        String damageType = section.getString("damage-type");
        if (damageType != null && damageTypes.get(damageType).isEmpty())
            throw new IllegalArgumentException("unknown damage type '" + damageType + "'");

        List<StatModifier> modifiers = new ArrayList<>();
        ConfigurationSection statSection = section.getConfigurationSection("stats");
        if (statSection != null) {
            for (String stat : statSection.getKeys(false)) {
                if (!stats.contains(stat)) throw new IllegalArgumentException("unknown stat '" + stat + "'");
                StatModifier modifier = StatValueParser.parse(stat.toLowerCase(Locale.ROOT), statSection.get(stat));
                // for stats that are already percentages, 15 and '15%' mean the same; keep the % flavor for the lore
                if (stats.require(stat).percentPoints()) modifier = StatModifier.percent(modifier.stat(), modifier.value());
                modifiers.add(modifier);
            }
        }

        Double attackSpeed = section.isSet("attack-speed") ? section.getDouble("attack-speed") : null;

        return new ItemTemplate(id, material, section.getString("name", id), slot,
                section.getDouble("damage", 0), damageType, attackSpeed, modifiers, section.getStringList("lore"));
    }
}
