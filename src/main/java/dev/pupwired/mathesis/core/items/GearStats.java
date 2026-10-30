package dev.pupwired.mathesis.core.items;

import dev.pupwired.mathesis.core.stats.BuiltinStats;
import dev.pupwired.mathesis.core.stats.StatModifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Combines what a player has equipped into the modifier list for the {@code gear} stat source. */
public final class GearStats {

    private GearStats() {}

    /**
     * @param equipped the template found in each slot (absent = empty or not a plugin item)
     * @return modifiers of every item that sits in the slot its template asks for (a template's attack-speed
     *         becomes a flat attack_speed modifier), in a stable slot order
     *         so two calls with the same gear compare equal
     */
    public static List<StatModifier> compute(Map<GearSlot, ItemTemplate> equipped) {
        List<StatModifier> result = new ArrayList<>();
        for (GearSlot slot : GearSlot.values()) {
            ItemTemplate template = equipped.get(slot);
            if (template == null || template.slot() != slot) continue;
            result.addAll(template.stats());
            if (template.attackSpeed() != null) {
                // the template states the absolute speed; the stat starts at the base, so contribute the difference
                result.add(StatModifier.flat(BuiltinStats.ATTACK_SPEED, template.attackSpeed() - BuiltinStats.ATTACK_SPEED_BASE));
            }
        }
        return result;
    }
}
