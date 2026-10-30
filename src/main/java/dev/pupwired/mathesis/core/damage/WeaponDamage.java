package dev.pupwired.mathesis.core.damage;

/**
 * Keeps vanilla's attack-strength scaling and crits when a weapon's own damage replaces vanilla's number.
 * A swing's event damage is (attack_damage attribute) x (cooldown scaling, crit multiplier), so dividing the
 * two gives just that scaling: about 0.2-1.0 for a spam-clicked swing and up to 1.5 for a crit.
 */
public final class WeaponDamage {

    private static final double MAX_SCALE = 2.0;

    private WeaponDamage() {}

    public static double scale(double eventDamage, double attackDamageAttribute) {
        if (!(attackDamageAttribute > 0)) return 1;
        double scale = eventDamage / attackDamageAttribute;
        if (!Double.isFinite(scale)) return 1;
        return Math.max(0, Math.min(MAX_SCALE, scale));
    }
}
