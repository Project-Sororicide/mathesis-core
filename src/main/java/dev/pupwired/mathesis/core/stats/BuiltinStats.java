package dev.pupwired.mathesis.core.stats;

import java.util.List;

/**
 * Stats that exist regardless of config. The three attribute-bound stats are expressed in vanilla
 * units (20 = ten hearts, 0.1 = normal walking speed, 4.0 = unarmed attack speed) so the Bukkit
 * bridge can apply them directly. Ranges mirror the vanilla attribute ranges.
 */
public final class BuiltinStats {

    public static final String MAX_HEALTH = "max_health";
    public static final String MOVEMENT_SPEED = "movement_speed";
    public static final String ATTACK_SPEED = "attack_speed";
    public static final String DEFENSE = "defense";

    private BuiltinStats() {}

    public static List<StatDefinition> all() {
        return List.of(
                new StatDefinition(MAX_HEALTH, 20, 1, 1024),
                new StatDefinition(MOVEMENT_SPEED, 0.1, 0, 1024),
                new StatDefinition(ATTACK_SPEED, 4.0, 0, 1024),
                new StatDefinition(DEFENSE, 0, 0, Double.POSITIVE_INFINITY)
        );
    }
}
