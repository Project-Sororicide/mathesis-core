package dev.pupwired.mathesis.core.stats;

import java.util.*;

/**
 * Per-entity stat values. Every system that affects stats (class, level, attributes, gear...) owns one
 * <em>source</em> key and always replaces its whole modifier list at once with {@link #setSource}, so
 * "recompute from scratch" is the only update model and stale modifiers cannot accumulate.
 *
 * <p>Not thread-safe: use from the main server thread only.
 */
public final class StatMap {

    private final StatRegistry registry;
    private final Map<String, List<StatModifier>> sources = new LinkedHashMap<>();
    private final Map<String, Double> cache = new HashMap<>();
    private final List<StatListener> listeners = new ArrayList<>();

    public StatMap(StatRegistry registry) {
        this.registry = registry;
    }

    public void addListener(StatListener listener) {
        listeners.add(listener);
    }

    public void removeListener(StatListener listener) {
        listeners.remove(listener);
    }

    /** Final value of a stat. */
    public double get(String stat) {
        StatDefinition definition = registry.require(stat);
        return cache.computeIfAbsent(definition.id(), id -> compute(definition));
    }

    /** Final value of a stat, or {@code fallback} if no such stat is registered (e.g. a damage type removed by a reload). */
    public double getOrDefault(String stat, double fallback) {
        return registry.contains(stat) ? get(stat) : fallback;
    }


    /**
     * Replaces everything {@code source} contributes. Validates first: if any modifier targets an unknown
     * stat, nothing changes. Listeners fire once per stat whose final value actually changed.
     */
    public void setSource(String source, Collection<StatModifier> modifiers) {
        List<StatModifier> incoming = List.copyOf(modifiers);
        for (StatModifier modifier : incoming) registry.require(modifier.stat());

        Set<String> affected = new LinkedHashSet<>();
        List<StatModifier> previous = sources.get(source);
        if (previous != null) for (StatModifier modifier : previous) affected.add(modifier.stat());
        for (StatModifier modifier : incoming) affected.add(modifier.stat());

        Map<String, Double> before = new LinkedHashMap<>();
        for (String stat : affected) before.put(stat, get(stat));

        if (incoming.isEmpty()) sources.remove(source);
        else sources.put(source, incoming);
        cache.keySet().removeAll(affected);

        for (String stat : affected) {
            double after = get(stat);
            if (Double.compare(before.get(stat), after) != 0) fire(stat, after);
        }
    }

    public void clearSource(String source) {
        setSource(source, List.of());
    }

    /** The modifiers a source currently contributes (empty if none). */
    public List<StatModifier> modifiersOf(String source) {
        return sources.getOrDefault(source, List.of());
    }

    /**
     * Drops cached values and notifies listeners about every registered stat. Use after the registry changed
     * (reload) and for the initial push when a player joins. Modifiers aimed at stats that no longer exist stay
     * inert instead of being deleted.
     */
    public void refresh() {
        cache.clear();
        for (StatDefinition definition : registry.all()) fire(definition.id(), get(definition.id()));
    }

    /** Snapshot of every registered stat's final value, in registry order. */
    public Map<String, Double> all() {
        Map<String, Double> result = new LinkedHashMap<>();
        for (StatDefinition definition : registry.all()) result.put(definition.id(), get(definition.id()));
        return result;
    }

    private double compute(StatDefinition definition) {
        double flat = 0, percent = 0;
        for (List<StatModifier> list : sources.values()) {
            for (StatModifier modifier : list) {
                if (!modifier.stat().equals(definition.id())) continue;
                switch (modifier.operation()) {
                    case FLAT -> flat += modifier.value();
                    case PERCENT -> percent += modifier.value();
                }
            }
        }
        double multiplier = Math.max(0, 1 + percent / 100.0);
        return definition.clamp((definition.base() + flat) * multiplier);
    }

    private void fire(String stat, double value) {
        for (StatListener listener : List.copyOf(listeners)) listener.onChange(this, stat, value);
    }
}
