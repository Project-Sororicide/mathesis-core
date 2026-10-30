package dev.pupwired.mathesis.core.damage;

import dev.pupwired.mathesis.core.stats.DamageTypeStats;
import net.kyori.adventure.text.format.TextColor;

/** A damage type from damage_types.yml. Stat ids are derived from the type id. */
public record DamageType(String id, String displayName, TextColor color) {

    /** Stat holding the attacker's % bonus for this type. */
    public String offenseStat() {
        return DamageTypeStats.offense(id);
    }

    /** Stat holding the defender's % resistance to this type. */
    public String resistStat() {
        return DamageTypeStats.resist(id);
    }
}