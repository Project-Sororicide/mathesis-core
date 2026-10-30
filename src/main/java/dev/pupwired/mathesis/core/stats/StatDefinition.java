package dev.pupwired.mathesis.core.stats;

import java.util.Locale;
import java.util.Objects;

/** Static description of a stat: its id, starting value and allowed range. */
public record StatDefinition(String id, double base, double min, double max) {

    public StatDefinition {
        Objects.requireNonNull(id, "id");
        id = id.toLowerCase(Locale.ROOT);
        if (id.isBlank()) throw new IllegalArgumentException("Stat id cannot be blank");
        if (Double.isNaN(base) || Double.isNaN(min) || Double.isNaN(max))
            throw new IllegalArgumentException("Stat '" + id + "' has a NaN value or bound");
        if (min > max)
            throw new IllegalArgumentException("Stat '" + id + "' has min (" + min + ") above max (" + max + ")");
    }

    public double clamp(double value) {
        return Math.max(min, Math.min(max, value));
    }
}
