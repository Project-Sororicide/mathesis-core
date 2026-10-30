package dev.pupwired.mathesis.core.damage;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NameMapTest {

    private final NameMap map = new NameMap(Map.of(
            "IRON_SWORD", "Piercing",
            "*_SWORD", "edged",
            "*_PICKAXE", "piercing",
            "*_GOLDEN_PICKAXE", "blunt",
            "AIR", "blunt"));

    @Test void exactMatch() { assertEquals(Optional.of("blunt"), map.find("AIR")); }
    @Test void exactBeatsWildcardAndValuesAreLowercased() { assertEquals(Optional.of("piercing"), map.find("IRON_SWORD")); }
    @Test void wildcardSuffix() { assertEquals(Optional.of("edged"), map.find("DIAMOND_SWORD")); }
    @Test void lookupIsCaseInsensitive() { assertEquals(Optional.of("edged"), map.find("netherite_sword")); }
    @Test void longestSuffixWins() { assertEquals(Optional.of("blunt"), map.find("X_GOLDEN_PICKAXE")); }
    @Test void noMatch() { assertEquals(Optional.empty(), map.find("STICK")); }
    @Test void size() { assertEquals(5, map.size()); }
}
