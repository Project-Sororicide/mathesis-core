package dev.pupwired.mathesis.core.stats;

import java.util.*;

/** All known stats. Replaced wholesale on reload; a failed replacement leaves the old contents untouched. */
public final class StatRegistry {

    private volatile Map<String, StatDefinition> definitions = Map.of();

    public void replaceAll(Collection<StatDefinition> newDefinitions) {
        Map<String, StatDefinition> loaded = new LinkedHashMap<>();
        for (StatDefinition definition : newDefinitions) {
            if (loaded.putIfAbsent(definition.id(), definition) != null)
                throw new IllegalArgumentException("Duplicate stat id: " + definition.id());
        }
        definitions = Collections.unmodifiableMap(loaded);
    }

    public Optional<StatDefinition> find(String id) {
        return Optional.ofNullable(definitions.get(id.toLowerCase(Locale.ROOT)));
    }

    public StatDefinition require(String id) {
        StatDefinition definition = definitions.get(id.toLowerCase(Locale.ROOT));
        if (definition == null) throw new IllegalArgumentException("Unknown stat '" + id + "'");
        return definition;
    }

    public boolean contains(String id) {
        return definitions.containsKey(id.toLowerCase(Locale.ROOT));
    }

    public Collection<StatDefinition> all() {
        return definitions.values();
    }
}
