package dev.pupwired.mathesis.core.damage;

import dev.pupwired.mathesis.core.stats.BuiltinStats;
import dev.pupwired.mathesis.core.stats.DamageTypeStats;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * Pure damage math. Order of operations:
 * <ol>
 *   <li>per damage type: amount x (1 + attacker's {@code <type>_damage}/100) x (1 - defender's {@code <type>_resist}/100)</li>
 *   <li>sum the types</li>
 *   <li>apply the defense formula once to that sum, using the defender's {@code defense} stat</li>
 * </ol>
 * Stat lookups are plain functions so an entity without stats can pass {@code stat -> 0}.
 */
public final class DamageCalculator {

    /**
     * @param beforeDefense damage after per-type modifiers, before the defense formula
     * @param total         final damage
     * @param perType       each type's share after per-type modifiers (before defense), for future indicators
     */
    public record Result(double beforeDefense, double total, Map<String, Double> perType) {}

    private DamageCalculator() {}

    public static Result calculate(Map<String, Double> packet,
                                   ToDoubleFunction<String> attacker,
                                   ToDoubleFunction<String> defender,
                                   DefenseFormula formula) {
        Map<String, Double> perType = new LinkedHashMap<>();
        double sum = 0;
        for (Map.Entry<String, Double> entry : packet.entrySet()) {
            String type = entry.getKey();
            double amount = entry.getValue();
            if (!(amount > 0)) continue; // also drops NaN

            double offense = Math.max(0, 1 + attacker.applyAsDouble(DamageTypeStats.offense(type)) / 100);
            double resistance = Math.max(0, 1 - defender.applyAsDouble(DamageTypeStats.resist(type)) / 100);
            double value = amount * offense * resistance;
            perType.put(type, value);
            sum += value;
        }
        double total = formula.apply(sum, defender.applyAsDouble(BuiltinStats.DEFENSE));
        return new Result(sum, total, Collections.unmodifiableMap(perType));
    }
}
