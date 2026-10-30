package dev.pupwired.mathesis.core.stats;

import java.util.Locale;
import java.util.Objects;

/**
 * Static description of a stat: its id, starting value and allowed range.
 *
 * @param percentPoints true when the stat's value is itself a percentage (a resist of 15 means 15%). Percent
 *                      modifiers cannot "scale" such a stat, since its base is 0, so they add points instead:
 *                      '15%' and 15 mean the same thing for these stats.
 */
public record StatDefinition(String id, double base, double min, double max, boolean percentPoints) {

    public StatDefinition {
        Objects.requireNonNull(id, "id");
        id = id.toLowerCase(Locale.ROOT);
        if (id.isBlank()) throw new IllegalArgumentException("Stat id cannot be blank");
        if (Double.isNaN(base) || Double.isNaN(min) || Double.isNaN(max))
            throw new IllegalArgumentException("Stat '" + id + "' has a NaN value or bound");
        if (min > max)
            throw new IllegalArgumentException("Stat '" + id + "' has min (" + min + ") above max (" + max + ")");
    }

    /** An ordinary stat whose percent modifiers scale (base + flat bonuses). */
    public StatDefinition(String id, double base, double min, double max) {
        this(id, base, min, max, false);
    }

    public double clamp(double value) {
        return Math.max(min, Math.min(max, value));
    }
}
