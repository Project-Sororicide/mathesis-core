package dev.pupwired.mathesis.core.attributes;

import dev.pupwired.mathesis.core.rpcheck.CharacterAttribute;

import java.util.EnumMap;
import java.util.Map;

/**
 * One character's permanent attribute investments. Points are granted (by level-ups, later) and then spent
 * one attribute at a time. Not thread-safe: main thread only.
 */
public final class AttributeSet {

    private final Map<CharacterAttribute, Integer> invested = new EnumMap<>(CharacterAttribute.class);
    private int granted;

    public AttributeSet() {}

    /** Restores saved data. Bad values are repaired: negatives become 0, and spent points are never taken away. */
    public AttributeSet(int granted, Map<CharacterAttribute, Integer> invested) {
        invested.forEach((attribute, points) -> {
            if (points > 0) this.invested.put(attribute, points);
        });
        this.granted = Math.max(Math.max(0, granted), spent());
    }

    public int get(CharacterAttribute attribute) { return invested.getOrDefault(attribute, 0); }

    public int granted() { return granted; }

    public int spent() { return invested.values().stream().mapToInt(Integer::intValue).sum(); }

    public int unspent() { return granted - spent(); }

    public void grant(int amount) {
        if (amount < 1) throw new IllegalArgumentException("amount must be 1 or more");
        granted += amount;
    }

    /** @return false (and changes nothing) if there are not enough unspent points */
    public boolean invest(CharacterAttribute attribute, int amount) {
        if (amount < 1) throw new IllegalArgumentException("amount must be 1 or more");
        if (amount > unspent()) return false;
        invested.merge(attribute, amount, Integer::sum);
        return true;
    }

    /** Refunds every spent point; the granted total is untouched. */
    public void reset() { invested.clear(); }

    public Map<CharacterAttribute, Integer> snapshot() { return new EnumMap<>(invested); }
}