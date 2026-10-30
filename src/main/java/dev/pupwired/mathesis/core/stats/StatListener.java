package dev.pupwired.mathesis.core.stats;

/** Notified when a stat's final value changes (or on {@link StatMap#refresh()}). */
@FunctionalInterface
public interface StatListener {
    void onChange(StatMap map, String stat, double value);
}
