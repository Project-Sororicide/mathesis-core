package dev.pupwired.mathesis.core.items;


import dev.pupwired.mathesis.core.stats.StatModifier;

/** Reads a stat value from items.yml: a number is flat, a string ending in % is a percent ('10%', '-5%'). */
public final class StatValueParser {

    private StatValueParser() {}

    public static StatModifier parse(String stat, Object raw) {
        if (raw instanceof Number number) return StatModifier.flat(stat, number.doubleValue());
        if (raw instanceof String text) {
            String t = text.trim();
            boolean percent = t.endsWith("%");
            if (percent) t = t.substring(0, t.length() - 1).trim();
            if (t.startsWith("+")) t = t.substring(1);
            try {
                double value = Double.parseDouble(t);
                return percent ? StatModifier.percent(stat, value) : StatModifier.flat(stat, value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("stat '" + stat + "': cannot read value '" + text + "'");
            }
        }
        throw new IllegalArgumentException("stat '" + stat + "': expected a number or a percent like '10%', got " + raw);
    }
}
