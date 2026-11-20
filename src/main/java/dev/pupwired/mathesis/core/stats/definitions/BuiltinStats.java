package dev.pupwired.mathesis.core.stats.definitions;

import dev.pupwired.mathesis.core.stats.StatDefinition;

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
    public static final String MELEE_DAMAGE = "melee_damage";
    public static final String RANGED_DAMAGE = "ranged_damage";
    public static final String CRIT_CHANCE = "crit_chance";
    public static final String CRIT_DAMAGE = "crit_damage";
    public static final String DEFENSE_PENETRATION = "defense_penetration";
    public static final String KNOCKBACK_RESIST = "knockback_resist";
    public static final String HEALING_POWER = "healing_power";
    public static final String FALL_RESIST = "fall_resist";
    public static final String COOLDOWN_REDUCTION = "cooldown_reduction";
    public static final String STATUS_RESIST = "status_resist";
    public static final String JUMP_STRENGTH = "jump_strength";
    public static final String SAFE_FALL_DISTANCE = "safe_fall_distance";
    public static final String RESOURCE_MAX = "resource_max";
    public static final String RESOURCE_REGEN = "resource_regen";
    public static final String RESOURCE_EFFICIENCY = "resource_efficiency";

    /** Vanilla bare-hand attack speed. With vanilla attributes stripped, every item swings at this rate unless a template sets its own. */
    public static final double ATTACK_SPEED_BASE = 4.0;

    private BuiltinStats() {}

    public static List<StatDefinition> all() {
        return List.of(
                new StatDefinition(MAX_HEALTH, 20, 1, 1024),
                new StatDefinition(MOVEMENT_SPEED, 0.1, 0, 1024),
                new StatDefinition(ATTACK_SPEED, ATTACK_SPEED_BASE, 0, 1024),
                new StatDefinition(DEFENSE, 0, 0, Double.POSITIVE_INFINITY),
                new StatDefinition(MELEE_DAMAGE, 0, -100, Double.POSITIVE_INFINITY, true),
                new StatDefinition(RANGED_DAMAGE, 0, -100, Double.POSITIVE_INFINITY, true),
                new StatDefinition(CRIT_CHANCE, 0, 0, 100, true),
                new StatDefinition(CRIT_DAMAGE, 50, 0, Double.POSITIVE_INFINITY, true),
                new StatDefinition(DEFENSE_PENETRATION, 0, 0, 100, true),
                new StatDefinition(KNOCKBACK_RESIST, 0, 0, 100, true),
                new StatDefinition(HEALING_POWER, 0, -100, Double.POSITIVE_INFINITY, true),
                new StatDefinition(JUMP_STRENGTH, 0.42, 0, 32),
                new StatDefinition(FALL_RESIST, 0, -50, 90, true),
                new StatDefinition(SAFE_FALL_DISTANCE, 3, 0, 1024),
                new StatDefinition(COOLDOWN_REDUCTION, 0, -60, 60, true),
                new StatDefinition(STATUS_RESIST, 0, 0, 50, true),
                new StatDefinition(RESOURCE_MAX, 0, -60, 60),
                new StatDefinition(RESOURCE_REGEN, 0, -60, 60),
                new StatDefinition(RESOURCE_EFFICIENCY, 0, -60, 60)

        );
    }
}
