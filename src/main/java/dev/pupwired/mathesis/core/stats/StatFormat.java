package dev.pupwired.mathesis.core.stats;

import java.util.Locale;

/** Player-facing text for stats and modifiers (item lore, messages). */
public final class StatFormat {

    private StatFormat() {}

    /** "max_health" becomes "Max Health". */
    public static String label(String statId) {
        StringBuilder out = new StringBuilder();
        for (String word : statId.split("_")) {
            if (word.isEmpty()) continue;
            if (out.length() > 0) out.append(' ');
            out.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return out.toString();
    }

    /** "+10", "-3.5", "+15%". */
    public static String value(StatModifier modifier) {
        return (modifier.value() >= 0 ? "+" : "-") + number(Math.abs(modifier.value()))
                + (modifier.operation() == StatModifier.Operation.PERCENT ? "%" : "");
    }

    public static String number(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1e15) return String.valueOf((long) value);
        String text = String.format(Locale.ROOT, "%.2f", value);
        text = text.replaceAll("0+$", "");
        return text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
    }
}
