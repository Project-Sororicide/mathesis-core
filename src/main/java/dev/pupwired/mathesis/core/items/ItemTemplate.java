package dev.pupwired.mathesis.core.items;

import dev.pupwired.mathesis.core.stats.StatModifier;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * A fixed item definition from items.yml. Items carry only the template id; everything else is looked up here.
 *
 * @param material    Bukkit material name (validated when loading, not here)
 * @param slot        where the item must be for its stats to apply
 * @param damage      base damage per melee hit; 0 for non-weapons
 * @param damageType  damage type id of that hit; required when damage > 0, otherwise null
 * @param attackSpeed attacks per second while held (vanilla: sword 1.6, axe 0.9); null leaves the bare-hand
 *                    speed of 4.0. Only for items with slot hand.
 */
public record ItemTemplate(String id, String material, String name, GearSlot slot,
                           double damage, String damageType, Double attackSpeed,
                           List<StatModifier> stats, List<String> lore) {

    public ItemTemplate {
        Objects.requireNonNull(id, "id");
        id = id.toLowerCase(Locale.ROOT);
        if (id.isBlank()) throw new IllegalArgumentException("item id cannot be blank");
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(slot, "slot");
        if (!Double.isFinite(damage) || damage < 0) throw new IllegalArgumentException("damage must be 0 or more");
        if (damage > 0) {
            if (damageType == null) throw new IllegalArgumentException("'damage' needs a 'damage-type'");
            if (slot != GearSlot.MAIN_HAND) throw new IllegalArgumentException("'damage' only works on items with slot: hand");
        }
        if (attackSpeed != null) {
            if (!Double.isFinite(attackSpeed) || attackSpeed <= 0)
                throw new IllegalArgumentException("'attack-speed' must be above 0 (attacks per second)");
            if (slot != GearSlot.MAIN_HAND) throw new IllegalArgumentException("'attack-speed' only works on items with slot: hand");
        }
        if (damageType != null) damageType = damageType.toLowerCase(Locale.ROOT);
        name = name == null ? id : name;
        stats = List.copyOf(stats);
        lore = List.copyOf(lore);
    }

    public boolean isWeapon() {
        return damage > 0;
    }
}
