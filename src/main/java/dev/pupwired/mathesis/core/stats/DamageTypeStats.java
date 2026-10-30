package dev.pupwired.mathesis.core.stats;

import java.util.List;

/** Naming and definitions for the two stats every damage type gets. */
public final class DamageTypeStats {

    private DamageTypeStats() {}

    /** Attacker's % bonus with this type. */
    public static String offense(String typeId) {
        return typeId + "_damage";
    }

    /** Defender's % resistance to this type. */
    public static String resist(String typeId) {
        return typeId + "_resist";
    }

    /**
     * @param resistCap highest allowed resistance in percent (below 100 so nothing becomes fully immune by stacking)
     */
    public static List<StatDefinition> definitionsFor(String typeId, double resistCap) {
        return List.of(
                new StatDefinition(offense(typeId), 0, -100, Double.POSITIVE_INFINITY),
                new StatDefinition(resist(typeId), 0, -100, resistCap)
        );
    }
}
