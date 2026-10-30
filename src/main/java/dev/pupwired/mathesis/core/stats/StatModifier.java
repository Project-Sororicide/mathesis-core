package dev.pupwired.mathesis.core.stats;

import java.util.Locale;
import java.util.Objects;

/**
 * One adjustment to one stat. The source that owns it (class, level, gear...) is not stored here:
 * modifiers are handed to a {@link StatMap} grouped under a source key.
 *
 * <p>Final value = clamp((base + sum of FLAT) * max(0, 1 + sum of PERCENT / 100)).
 */
public record StatModifier(String stat, Operation operation, double value) {

    public enum Operation { FLAT, PERCENT }

    public StatModifier {
        Objects.requireNonNull(stat, "stat");
        Objects.requireNonNull(operation, "operation");
        stat = stat.toLowerCase(Locale.ROOT);
        if (!Double.isFinite(value))
            throw new IllegalArgumentException("Modifier value for '" + stat + "' must be finite, got " + value);
    }

    public static StatModifier flat(String stat, double value) {
        return new StatModifier(stat, Operation.FLAT, value);
    }

    public static StatModifier percent(String stat, double value) {
        return new StatModifier(stat, Operation.PERCENT, value);
    }
}
