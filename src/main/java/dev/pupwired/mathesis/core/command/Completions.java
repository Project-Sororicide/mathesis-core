package dev.pupwired.mathesis.core.command;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public final class Completions {

    private Completions() {}

    /** Options that start with what the player has typed, case-insensitive. */
    public static List<String> filter(Collection<String> options, String typed) {
        String prefix = typed.toLowerCase(Locale.ROOT);
        return options.stream().filter(o -> o.toLowerCase(Locale.ROOT).startsWith(prefix)).toList();
    }
}