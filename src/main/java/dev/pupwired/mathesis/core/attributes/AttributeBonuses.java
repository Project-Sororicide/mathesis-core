package dev.pupwired.mathesis.core.attributes;

import dev.pupwired.mathesis.core.rpcheck.CharacterAttribute;
import dev.pupwired.mathesis.core.stats.StatModifier;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** What one invested point of each attribute gives, and the maths to turn a player's points into modifiers. */
public final class AttributeBonuses {

    /** The stat source every attribute bonus is written under. */
    public static final String SOURCE = "attributes";

    private final Map<CharacterAttribute, List<StatModifier>> perPoint = new EnumMap<>(CharacterAttribute.class);

    public AttributeBonuses(Map<CharacterAttribute, List<StatModifier>> perPoint) {
        perPoint.forEach((attribute, list) -> this.perPoint.put(attribute, List.copyOf(list)));
    }

    public static AttributeBonuses none() { return new AttributeBonuses(Map.of()); }

    public List<StatModifier> perPoint(CharacterAttribute attribute) {
        return perPoint.getOrDefault(attribute, List.of());
    }

    /** Per-point modifiers times invested points, in a stable order (attribute order) so equal sets compare equal. */
    public List<StatModifier> modifiersFor(AttributeSet set) {
        List<StatModifier> result = new ArrayList<>();
        for (CharacterAttribute attribute : CharacterAttribute.values()) {
            int points = set.get(attribute);
            if (points == 0) continue;
            for (StatModifier modifier : perPoint(attribute)) {
                result.add(new StatModifier(modifier.stat(), modifier.operation(), modifier.value() * points));
            }
        }
        return result;
    }
}