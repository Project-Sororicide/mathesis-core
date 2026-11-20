package dev.pupwired.mathesis.core.rpcheck;

import java.util.Locale;

/** The five character attributes. */
public enum CharacterAttribute {
    MIGHT, REFLEX, WITS, RESOLVE, PRESENCE;

    /** "might", "Might" and "MIGHT" all work. */
    public static CharacterAttribute fromConfig(String name) {
        try {
            return valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("unknown attribute '" + name + "'");
        }
    }

    public String id() { return name().toLowerCase(Locale.ROOT); }

    public String label() { return name().charAt(0) + name().substring(1).toLowerCase(Locale.ROOT); }
}